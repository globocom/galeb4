package io.galeb.core.configuration;

import com.lambdaworks.redis.RedisURI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;
import java.util.stream.Collectors;

import static com.lambdaworks.redis.RedisURI.URI_SCHEME_REDIS_SENTINEL;

@Configuration
public class RedisConfiguration {

    private static final Logger LOGGER = LogManager.getLogger(RedisConfiguration.class);

    @Bean
    @Primary
    public RedisProperties redisProperties() {
        return new RedisProperties();
    }

    @Bean
    public RedisConnectionFactory lettuceConnectionFactory(RedisProperties redisProperties) {
        RedisURI redisURI = RedisURI.create(redisProperties().getUrl());
        final LettuceConnectionFactory connectionFactory;
        if (redisProperties.getUrl().startsWith(URI_SCHEME_REDIS_SENTINEL)) {
            final Set<String> nodesSentinel = redisURI.getSentinels().stream().map(r -> r.getHost() + ":" + r.getPort()).collect(Collectors.toSet());
            RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration(redisURI.getSentinelMasterId(), nodesSentinel);
            connectionFactory = new LettuceConnectionFactory(redisSentinelConfiguration);
            LOGGER.info("Using Redis Sentinel");
        } else {
            connectionFactory = new LettuceConnectionFactory(redisURI.getHost(), redisURI.getPort());
            LOGGER.info("Using Redis Standalone");
        }
        if (redisURI.getPassword() != null) connectionFactory.setPassword(new String(redisURI.getPassword()));
        if (redisURI.getDatabase() != 0) connectionFactory.setDatabase(redisURI.getDatabase());
        return connectionFactory;
    }

    @Bean(name = "redisTemplate")
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

}

