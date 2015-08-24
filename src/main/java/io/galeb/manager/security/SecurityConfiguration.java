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

package io.galeb.manager.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import io.galeb.manager.entity.Account;
import io.galeb.manager.repository.AccountRepository;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    enum AuthMethod {
        LDAP,
        LDAP_TEST,
        DEFAULT
    }

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private Environment env;

    private AuthMethod authMethod;

    @PostConstruct
    public void init() {
        authMethod = AuthMethod.valueOf(env.getRequiredProperty("auth_method"));
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.inMemoryAuthentication()
            .withUser("admin").roles("ADMIN", "USER").password("password");

        switch (authMethod) {
        case LDAP:
            auth.ldapAuthentication()
                .userDetailsContextMapper(userDetailsContextMapper())
                .userDnPatterns(env.getRequiredProperty("ldap.user_dn_patterns"))
                .groupSearchBase(env.getRequiredProperty("ldap.group_search_base"))
                .contextSource()
                .url(env.getRequiredProperty("ldap.url"))
                .managerDn(env.getRequiredProperty("ldap.user"))
                .managerPassword(env.getRequiredProperty("ldap.password"));
            break;

        case LDAP_TEST:
            auth.ldapAuthentication()
                .userDetailsContextMapper(userDetailsContextMapper())
                .userDnPatterns("uid={0},ou=people")
                .groupSearchBase("ou=groups")
                .contextSource()
                .ldif("classpath:test-ldap-server.ldif");
            break;

        default:
            auth.userDetailsService(userDetailsService);
            break;
        }

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .anyRequest().fullyAuthenticated().and()
            .httpBasic().and()
            .csrf().disable();
    }

    @Bean
    public UserDetailsContextMapper userDetailsContextMapper() {
        return new LdapUserDetailsMapper() {
            @Override
            public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
                                                  Collection<? extends GrantedAuthority> authorities) {
                final UserDetails details = super.mapUserFromContext(ctx, username, authorities);
                final Authentication originalAuth = CurrentUser.getCurrentAuth();

                SystemUserService.runAs();
                final Account account = accountRepository.findByName(username);
                SystemUserService.runAs(originalAuth);

                final List<String> localRoles = account.getRoles().stream()
                        .map(role -> role.toString())
                        .collect(Collectors.toList());
                long id = account.getId();

                final Collection<GrantedAuthority> localAuthorities =
                        AuthorityUtils.createAuthorityList(localRoles.toArray(new String[localRoles.size()-1]));
                return new CustomLdapUserDetails((LdapUserDetails) details, localAuthorities, id);
            }
        };
    }

}
