package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_rulegroup_name", columnNames = { "name" }) })
public class RuleGroup extends AbstractEntity implements WithStatus {

    @OneToMany(mappedBy = "ruleGroup")
    private Set<VirtualHost> virtualhosts = new HashSet<>();

    @ElementCollection
    @JoinColumn(nullable = false)
    private Map<Integer, Rule> rules = new HashMap<>();

    @Column(nullable = false)
    private String name;

    @Transient
    private Status status = Status.UNKNOWN;

    public Set<VirtualHost> getVirtualhosts() {
        return virtualhosts;
    }

    public void setVirtualhosts(Set<VirtualHost> virtualhosts) {
        if (virtualhosts != null) {
            this.virtualhosts.clear();
            this.virtualhosts.addAll(virtualhosts);
        }
    }

    public Map<Integer, Rule> getRules() {
        return rules;
    }

    public void setRules(Map<Integer, Rule> rules) {
        if (rules != null) {
            this.rules.clear();
            this.rules.putAll(rules);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.hasText(name, "name is not valid");
        this.name = name;
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
