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

package io.galeb.api.repository.custom;

import io.galeb.api.services.GenericDaoService;
import io.galeb.api.services.StatusService;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Environment;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Set;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
public class EnvironmentRepositoryImpl extends AbstractRepositoryImplementation<Environment> implements EnvironmentRepositoryCustom, WithRoles {

    @Autowired
    private GenericDaoService genericDaoService;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(Environment.class, genericDaoService);
        setStatusService(statusService);
    }

    @Override
    protected Set<Environment> getAllEnvironments(AbstractEntity entity) {
        return Collections.singleton((Environment)entity);
    }

    @Override
    public Set<String> roles(Object criteria) {
        return Collections.emptySet();
    }
}
