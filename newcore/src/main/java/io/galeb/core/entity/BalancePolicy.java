package io.galeb.core.entity;

import java.util.Set;

public class BalancePolicy {

    private Set<Pool> pools;

    public Set<Pool> getPools() {
        return pools;
    }

    public void setPools(Set<Pool> pools) {
        this.pools = pools;
    }
}
