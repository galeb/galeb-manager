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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_name_environment", columnNames = { "name" }) })
public class Environment extends AbstractEntity<Environment> {

    private static final long serialVersionUID = 5596582746795373016L;

    @JsonIgnore
    @OneToMany(mappedBy = "environment")
    private final Set<Farm> farms = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "environment")
    private final Set<VirtualHost> virtualhosts = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "environment")
    private final Set<Target> targets = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "environment")
    private final Set<Pool> pools = new HashSet<>();

    public Environment(String name) {
        setName(name);
    }

    protected Environment() {
        //
    }

    @JsonIgnore
    protected Farm getFarm(long farmId) {
        return farms.stream().filter(f -> f.getId() == farmId).findAny().orElseThrow(() -> new RuntimeException("Farm " + farmId + " not found"));
    }
}
