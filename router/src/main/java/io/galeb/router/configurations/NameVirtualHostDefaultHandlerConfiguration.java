/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
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

package io.galeb.router.configurations;

import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.galeb.router.handlers.NameVirtualHostDefaultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NameVirtualHostDefaultHandlerConfiguration {

    private final ApplicationContext context;
    private final ManagerClientCache cache;

    @Autowired
    public NameVirtualHostDefaultHandlerConfiguration(final ApplicationContext context,
                                                      final ManagerClientCache cache) {
        this.context = context;
        this.cache = cache;
    }

    @Bean
    public NameVirtualHostDefaultHandler nameVirtualHostDefaultHandler() {
        return new NameVirtualHostDefaultHandler(context, cache);
    }

}
