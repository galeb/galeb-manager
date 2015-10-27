/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2015 Globo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package io.galeb.manager.entity;

import javax.persistence.*;

@Embeddable
public class RuleOrder {

    private long ruleId;
    private int ruleOrder;

    public RuleOrder(long ruleId, int ruleOrder) {
        this.ruleId = ruleId;
        this.ruleOrder = ruleOrder;
    }

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    public int getRuleOrder() {
        return ruleOrder;
    }

    public void setRuleOrder(int ruleOrder) {
        this.ruleOrder = ruleOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        RuleOrder ruleOrder = (RuleOrder) o;
        return ruleId == ruleOrder.ruleId;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(ruleId).hashCode();
    }
}
