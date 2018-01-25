/*
 * Copyright (c) 2014-2018 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.oldapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.galeb.core.entity.WithStatus;
import io.galeb.oldapi.entities.v1.AbstractEntity;
import io.galeb.oldapi.entities.v1.Environment;
import io.galeb.oldapi.services.http.HttpClientService;
import io.galeb.oldapi.services.utils.LinkProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class EnvironmentService extends AbstractConverterService<Environment> {

    private static final Logger LOGGER = LogManager.getLogger(EnvironmentService.class);

    private final String resourceName = Environment.class.getSimpleName().toLowerCase();
    private final String resourceUrlBase = System.getenv("GALEB_API_URL") + "/" + resourceName;
    private final HttpClientService httpClientService;
    private final LinkProcessor linkProcessor;

    @Autowired
    public EnvironmentService(HttpClientService httpClientService, LinkProcessor linkProcessor) {
        super();
        this.httpClientService = httpClientService;
        this.linkProcessor = linkProcessor;
    }

    @Override
    protected String getResourceName() {
        return resourceName;
    }

    @Override
    AbstractEntity.EntityStatus extractStatus(io.galeb.core.entity.AbstractEntity entity) {
        io.galeb.core.entity.Environment v2Environment = (io.galeb.core.entity.Environment) entity;
        WithStatus.Status status = v2Environment.getStatus().entrySet().stream().map(Map.Entry::getValue).findAny().orElse(WithStatus.Status.UNKNOWN);
        return convertStatus(status);
    }

    @Override
    protected Set<Resource<Environment>> convertResources(ArrayList<LinkedHashMap> v2s) {
        return v2s.stream().
                map(resource -> {
                    try {
                        Environment environment = convertResource(resource);
                        Set<Link> links = linkProcessor.extractLinks(resource, resourceName);
                        Long id = linkProcessor.extractId(links);
                        linkProcessor.add(links,"/" + resourceName + "/" + id + "/farms", "farms")
                                     .add(links,"/" + resourceName + "/" + id + "/targets", "targets")
                                     .remove(links, "rulesordered");
                        environment.setId(id);
                        return new Resource<>(environment, links);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    protected Environment convertResource(LinkedHashMap resource) throws IOException {
        io.galeb.core.entity.Environment v2Environment = (io.galeb.core.entity.Environment) mapToV2AbstractEntity(resource, io.galeb.core.entity.Environment.class);
        Environment environment = new Environment(v2Environment.getName()) {
            @Override
            public Date getCreatedAt() {
                return v2Environment.getCreatedAt();
            }
            @Override
            public String getCreatedBy() {
                return v2Environment.getCreatedBy();
            }

            @Override
            public Date getLastModifiedAt() {
                return v2Environment.getLastModifiedAt();
            }

            @Override
            public String getLastModifiedBy() {
                return v2Environment.getLastModifiedBy();
            }
        };
        environment.setStatus(extractStatus(v2Environment));
        return environment;
    }

    @Override
    public ResponseEntity<PagedResources<Resource<Environment>>> getSearch(String findType, Map<String, String> queryMap) {
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PagedResources<Resource<Environment>>> get(Integer size, Integer page) {
        String url = resourceUrlBase +
                (size != null || page != null ? "?" : "") +
                (size != null ? "size=" + size : "") +
                (size != null && page != null ? "&" : "") +
                (page != null ? "page=" + page : "");
        try {
            final Set<Resource<Environment>> v1Environments = convertResources(httpClientService.getResponseListOfMap(url, resourceName));
            int totalElements = v1Environments.size();
            size = size != null ? size : 9999;
            page = page != null ? page : 0;
            final PagedResources.PageMetadata metadata =
                    new PagedResources.PageMetadata(size, page, totalElements, Math.max(1, totalElements / size));
            final PagedResources<Resource<Environment>> pagedResources = new PagedResources<>(v1Environments, metadata, linkProcessor.pagedLinks(resourceName, size, page));
            return ResponseEntity.ok(pagedResources);
        } catch (InterruptedException | ExecutionException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<Resource<Environment>> getWithId(String id) {
        String url = resourceUrlBase + "/" + id;
        try {
            LinkedHashMap resource = httpClientService.getResponseMap(url);
            Set<Link> links = linkProcessor.extractLinks(resource, resourceName);
            linkProcessor.add(links,"/" + resourceName + "/" + id + "/farms", "farms")
                         .add(links,"/" + resourceName + "/" + id + "/targets", "targets")
                         .remove(links, "rulesordered");
            Environment environment = convertResource(resource);
            environment.setId(Long.parseLong(id));
            return ResponseEntity.ok(new Resource<>(environment, links));
        } catch (InterruptedException | ExecutionException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<String> post(String body) {
        try {
            return ResponseEntity.ok(getEmptyMap(body));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    @Override
    public ResponseEntity<String> postWithId(String id, String body) {
        try {
            return ResponseEntity.ok(getEmptyMap(Long.parseLong(id), body));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    @Override
    public ResponseEntity<String> put(String body) {
        try {
            return ResponseEntity.ok(getEmptyMap(body));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    @Override
    public ResponseEntity<String> putWithId(String id, String body) {
        try {
            return ResponseEntity.ok(getEmptyMap(Long.parseLong(id), body));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    @Override
    public ResponseEntity<String> delete() {
        try {
            return ResponseEntity.ok(getEmptyMap());
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    @Override
    public ResponseEntity<String> deleteWithId(String id) {
        try {
            return ResponseEntity.ok(getEmptyMap(Long.parseLong(id), null));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    @Override
    public ResponseEntity<String> patch(String body) {
        try {
            return ResponseEntity.ok(getEmptyMap(body));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    @Override
    public ResponseEntity<String> patchWithId(String id, String body) {
        try {
            return ResponseEntity.ok(getEmptyMap(Long.parseLong(id), body));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    @Override
    public ResponseEntity<String> options() {
        try {
            return ResponseEntity.ok(getEmptyMap());
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    @Override
    public ResponseEntity<String> optionsWithId(String id) {
        try {
            return ResponseEntity.ok(getEmptyMap(Long.parseLong(id), null));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    @Override
    public ResponseEntity<String> head() {
        try {
            return ResponseEntity.ok(getEmptyMap());
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    @Override
    public ResponseEntity<String> headWithId(String id) {
        try {
            return ResponseEntity.ok(getEmptyMap(Long.parseLong(id), null));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    @Override
    public ResponseEntity<String> trace() {
        try {
            return ResponseEntity.ok(getEmptyMap());
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    @Override
    public ResponseEntity<String> traceWithId(String id) {
        try {
            return ResponseEntity.ok(getEmptyMap(Long.parseLong(id), null));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

}
