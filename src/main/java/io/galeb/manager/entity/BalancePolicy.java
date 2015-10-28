/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2015 Globo.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.galeb.manager.entity;

import java.util.*;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

@Entity
@Table(name = "balancepolicy", uniqueConstraints = { @UniqueConstraint(name = "UK_name_balancepolicy", columnNames = { "name" }) })
public class BalancePolicy extends AbstractEntity<BalancePolicy> {

    private static final long serialVersionUID = 5596582746795373030L;

    @ManyToOne
    @JoinColumn(name = "balancepolicytype_id", nullable = false, foreignKey=@ForeignKey(name="FK_balancepolicy_balancepolicytype"))
    @JsonProperty(required = true)
    private BalancePolicyType balancePolicyType;

    @OneToMany(mappedBy = "balancePolicy")
    private final Set<Pool> pools = new HashSet<>();

    public BalancePolicy(String name, BalancePolicyType balancePolicyType) {
        Assert.hasText(name);
        Assert.notNull(balancePolicyType);
        setName(name);
        this.balancePolicyType = balancePolicyType;
    }

    protected BalancePolicy() {
        // Hibernate Requirement
    }

    public BalancePolicyType getBalancePolicyType() {
        return balancePolicyType;
    }

    public BalancePolicy setBalancePolicyType(BalancePolicyType balancePolicyType) {
        Assert.notNull(balancePolicyType);
        updateHash();
        this.balancePolicyType = balancePolicyType;
        return this;
    }

    public Set<Pool> getPools() {
        return pools;
    }

    public BalancePolicy setPools(Set<Pool> pools) {
        if (pools != null) {
            updateHash();
            this.pools.clear();
            this.pools.addAll(pools);
        }
        return this;
    }
}
