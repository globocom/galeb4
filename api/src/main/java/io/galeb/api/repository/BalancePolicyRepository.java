package io.galeb.api.repository;

import io.galeb.core.entity.BalancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "balancepolicy", collectionResourceRel = "balancepolicy", itemResourceRel = "balancepolicy")
public interface BalancePolicyRepository extends JpaRepository<BalancePolicy, Long> {

    @Override
    @PreAuthorize("@authz.check(principal, #balancePolicy, #this)")
    BalancePolicy save(@Param("balancePolicy") BalancePolicy balancePolicy);

    @Override
    @PreAuthorize("@authz.check(principal, #this)")
    void delete(Long id);

}
