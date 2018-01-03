package io.galeb.api.repository;

import io.galeb.core.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "account", collectionResourceRel = "account", itemResourceRel = "account")
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Override
    @PreAuthorize("@authz.check(principal, #account, #this)")
    Account save(@Param("account") Account account);

    @Override
    @PreAuthorize("@authz.check(principal, #id, #this)")
    void delete(@Param("id") Long id);

    @Override
    @PreAuthorize("principal.username == @localAdmin.username or #id == principal.id")
    Account findOne(@Param("id") Long id);

    @Override
    @Query("SELECT a FROM Account a WHERE a.username LIKE ?#{principal.username == @localAdmin.username ? '%' : principal.username}")
    Page<Account> findAll(Pageable pageable);
}
