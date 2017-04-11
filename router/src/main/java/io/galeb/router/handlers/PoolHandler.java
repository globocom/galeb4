package io.galeb.router.handlers;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient;
import io.galeb.router.client.hostselectors.HostSelector;
import io.galeb.router.client.hostselectors.HostSelectorAlgorithm;
import io.galeb.router.client.hostselectors.HostSelectorInitializer;
import io.galeb.router.services.ExternalData;
import io.undertow.client.UndertowClient;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.zalando.boot.etcd.EtcdNode;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.galeb.router.services.ExternalData.POOLS_KEY;

public class PoolHandler implements HttpHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HttpHandler defaultHandler;
    private final ExternalData data;
    private final ApplicationContext context;
    private final ExtendedLoadBalancingProxyClient proxyClient;

    private ExtendedProxyHandler proxyHandler = null;
    private String poolname = null;
    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private final HostSelectorInitializer hostSelectorInicializer = new HostSelectorInitializer();

    public PoolHandler(final ApplicationContext context, final ExternalData externalData) {
        this.context = context;
        this.data = externalData;
        this.defaultHandler = buildPoolHandler();
        this.proxyClient =
                new ExtendedLoadBalancingProxyClient(UndertowClient.getInstance(),
                        exchange -> exchange.getRequestHeaders().contains(Headers.UPGRADE), hostSelectorInicializer)
                    .setConnectionsPerThread(2000);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (proxyHandler != null) {
            proxyHandler.handleRequest(exchange);
        } else {
            defaultHandler.handleRequest(exchange);
        }
    }

    PoolHandler setPoolName(String aPoolname) {
        poolname = aPoolname;
        return this;
    }

    private HttpHandler buildPoolHandler() {
        return exchange -> {
            synchronized (loaded) {
                loaded.set(true);
                if (poolname != null) {
                    logger.info("creating pool " + poolname);
                    addTargets();
                    defineHostSelector();
                    proxyHandler = context.getBean(ExtendedProxyHandler.class);
                    proxyHandler.setProxyClientAndDefaultHandler(proxyClient, badGatewayExchange -> badGatewayExchange.setStatusCode(502));
                    proxyHandler.handleRequest(exchange);
                    return;
                }
                ResponseCodeHandler.HANDLE_500.handleRequest(exchange);
            }
        };
    }

    private void defineHostSelector() {
        if (poolname != null) {
            final String hostSelectorKeyName = POOLS_KEY + "/" + poolname + "/loadbalance";
            final EtcdNode hostSelectorNode = data.node(hostSelectorKeyName);
            final HostSelector hostSelector;
            if (hostSelectorNode.getKey() != null) {
                String hostSelectorName = hostSelectorNode.getValue();
                hostSelector = HostSelectorAlgorithm.valueOf(hostSelectorName).getHostSelector();
                hostSelectorInicializer.setHostSelector(hostSelector);
            } else {
                hostSelector = hostSelectorInicializer.getHostSelector();
            }
            logger.info("[Pool " + poolname + "] HostSelector: " + hostSelector.getClass().getSimpleName());
        }
    }

    private void addTargets() {
        if (poolname != null) {
            final String poolNameKey = POOLS_KEY + "/" + poolname + "/targets";
            for (EtcdNode etcdNode : data.listFrom(poolNameKey)) {
                String value = etcdNode.getValue();
                URI uri = URI.create(value);
                proxyClient.addHost(uri);
                logger.info("added target " + value);
            }
        }
    }
}
