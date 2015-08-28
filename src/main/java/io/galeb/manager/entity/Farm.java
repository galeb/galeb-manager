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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.springframework.util.Assert;

@Entity
public class Farm extends AbstractEntity<Farm> {

    private static final long serialVersionUID = 5596582746795373017L;

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false)
    private String api;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Environment environment;

    @OneToOne
    @JoinColumn(nullable = false)
    private Provider provider;

    @Column
    private boolean autoReload = true;

    public Farm(String name,String domain, String api, Environment environment, Provider provider) {
        Assert.hasText(domain);
        Assert.hasText(api);
        Assert.notNull(environment);
        Assert.notNull(provider);
        setName(name);
        this.domain = domain;
        this.api = api;
        this.environment = environment;
        this.provider = provider;
    }

    protected Farm() {
        //
    }

    public String getDomain() {
        return domain;
    }

    public Farm setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getApi() {
        return api;
    }

    public Farm setApi(String api) {
        this.api = api;
        return this;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Farm setEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }

    public Provider getProvider() {
        return provider;
    }

    public Farm setProvider(Provider provider) {
        this.provider = provider;
        return this;
    }

    public boolean isAutoReload() {
        return autoReload;
    }

    public Farm setAutoReload(boolean autoReload) {
        this.autoReload = autoReload;
        return this;
    }
}
