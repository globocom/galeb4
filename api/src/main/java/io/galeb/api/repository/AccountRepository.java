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

package io.galeb.api.repository;

import io.galeb.api.repository.custom.AccountRepositoryCustom;
import io.galeb.core.entity.Account;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@SuppressWarnings({"unused", "unchecked"})
@RepositoryRestResource(path = "account", collectionResourceRel = "account", itemResourceRel = "account")
public interface AccountRepository extends JpaRepository<Account, Long>, AccountRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(#account, #this)")
    @Caching(evict = {
        @CacheEvict(value = "cache_mergeAllRolesOf", allEntries = true),
        @CacheEvict(value = "cache_projectLinkedToAccount", allEntries = true),
        @CacheEvict(value = "cache_roleGroupsFromProject", allEntries = true),
        @CacheEvict(value = "cache_teamLinkedToAccount", allEntries = true),
        @CacheEvict(value = "cache_userDetailsDao", key = "#p0.username")
    })
    Account save(@Param("account") Account account);

    @Override
    @RestResource(exported = false)
    @Caching(evict = {
        @CacheEvict(value = "cache_mergeAllRolesOf", allEntries = true),
        @CacheEvict(value = "cache_projectLinkedToAccount", allEntries = true),
        @CacheEvict(value = "cache_roleGroupsFromProject", allEntries = true),
        @CacheEvict(value = "cache_teamLinkedToAccount", allEntries = true),
        @CacheEvict(value = "cache_userDetailsDao", key = "#p0.username")
    })
    Account saveByPass(@Param("account") Account account);

    @Override
    @PreAuthorize("@perm.allowDelete(#id, #this)")
    @Caching(evict = {
        @CacheEvict(value = "cache_mergeAllRolesOf", allEntries = true),
        @CacheEvict(value = "cache_projectLinkedToAccount", allEntries = true),
        @CacheEvict(value = "cache_roleGroupsFromProject", allEntries = true),
        @CacheEvict(value = "cache_teamLinkedToAccount", allEntries = true),
        @CacheEvict(value = "cache_userDetailsDao", allEntries = true)
    })
    void delete(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(#id, #this)")
    Account findOne(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(null , #this)")
    Page<Account> findAll(Pageable pageable);

    @PreAuthorize("@perm.allowView(null , #this)")
    Account findByUsername(@Param("username") String username);

    @PreAuthorize("@perm.allowView(null , #this)")
    Page<Account> findByUsernameContaining(@Param("username") String username, Pageable pageable);
}
