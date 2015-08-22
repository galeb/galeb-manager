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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.springframework.util.Assert;

@Entity
public class Rule extends AbstractEntity<Rule> {

    private static final long serialVersionUID = 5596582746795373020L;

    @OneToOne
    @JoinColumn(nullable = false)
    private RuleType ruleType;

    @ManyToOne
    @JoinColumn(nullable = false)
    private VirtualHost parent;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Target target;

    public Rule(String name, RuleType ruleType, Environment environment, VirtualHost parent, Target target) {
        Assert.notNull(ruleType);
        Assert.notNull(parent);
        Assert.notNull(target);
        setName(name);
        this.ruleType = ruleType;
        this.parent = parent;
        this.target = target;
    }

    protected Rule() {
        //
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public Rule setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
        return this;
    }

    public VirtualHost getParent() {
        return parent;
    }

    public Rule setParent(VirtualHost parent) {
        Assert.notNull(parent);
        this.parent = parent;
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public Rule setTarget(Target target) {
        Assert.notNull(target);
        this.target = target;
        return this;
    }

}
