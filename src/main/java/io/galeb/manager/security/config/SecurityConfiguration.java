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

package io.galeb.manager.security.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.galeb.manager.security.services.*;
import io.galeb.manager.security.user.*;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import io.galeb.manager.entity.Account;
import io.galeb.manager.repository.AccountRepository;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String INTERNAL_PASSWORD = "INTERNAL_PASSWORD";

    private static final Log LOGGER = LogFactory.getLog(SecurityConfiguration.class);

    private static final PageRequest PAGE_REQUEST = new PageRequest(0, 99999);

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    enum AuthMethod {
        LDAP,
        LDAP_TEST,
        DEFAULT
    }

    @Autowired private UserDetailsService userDetailsService;
    @Autowired private AccountRepository accountRepository;
    @Autowired private Environment env;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        AuthMethod authMethod;
        try {
            authMethod = AuthMethod.valueOf(System.getProperty("auth_method", AuthMethod.DEFAULT.toString()));
            LOGGER.info("Using "+authMethod.toString()+" Authentication Method.......");
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            throw e;
        }

        String internalPass = System.getProperty(INTERNAL_PASSWORD, System.getenv(INTERNAL_PASSWORD));
        internalPass = internalPass == null ? UUID.randomUUID().toString() : internalPass;
        auth.inMemoryAuthentication()
            .withUser("admin").roles("ADMIN", "USER").password(internalPass);
        LOGGER.info("secret: " + internalPass);

        String userDnPatternsEnv = System.getProperty("io.galeb.manager.ldap.user_dn_patterns.env", "GALEB_LDAP_DN");
        String userDnPatterns = System.getenv(userDnPatternsEnv);
        userDnPatterns = userDnPatterns != null ? userDnPatterns : "uid={0},ou=people";

        String groupSearchBaseEnv = System.getProperty("io.galeb.manager.ldap.group_search_base.env", "GALEB_LDAP_GROUP_SEARCH");
        String groupSearchBase = System.getenv(groupSearchBaseEnv);
        groupSearchBase = groupSearchBase != null ? groupSearchBase : "ou=groups";

        String urlEnv = System.getProperty("io.galeb.manager.ldap.url.env", "GALEB_LDAP_URL");
        String url = System.getenv(urlEnv);
        url = url != null ? url : "ldap://localhost:389";

        String usernameEnvName = System.getProperty("io.galeb.manager.ldap.username.env", "GALEB_LDAP_USER");
        String username = System.getenv(usernameEnvName);
        username = username != null ? username : "root";

        String passwordEnvName = System.getProperty("io.galeb.manager.ldap.password.env", "GALEB_LDAP_PASS");
        String password = System.getenv(passwordEnvName);
        password = password != null ? password : "";

        switch (authMethod) {
        case LDAP:
            auth.ldapAuthentication()
                .userDetailsContextMapper(userDetailsContextMapper())
                .userDnPatterns(env.getProperty("ldap.user_dn_patterns", userDnPatterns))
                .groupSearchBase(env.getProperty("ldap.group_search_base", groupSearchBase))
                .contextSource()
                .url(env.getProperty("ldap.url", url))
                .managerDn(env.getProperty("ldap.user", username))
                .managerPassword(env.getProperty("ldap.password", password));
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
            LOGGER.info("Using only AUTH DEFAULT");
        }
        auth.userDetailsService(userDetailsService).passwordEncoder(ENCODER);

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);
        http.authorizeRequests().antMatchers(HttpMethod.GET, "/healthcheck","/virtualhostscached/*").permitAll();
        http.authorizeRequests().anyRequest().fullyAuthenticated()
            .and()
            .logout().deleteCookies("JSESSIONID","SPRING_SECURITY_REMEMBER_ME_COOKIE")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/")
            .and()
            .requestCache().requestCache(new NullRequestCache())
            .and()
            .httpBasic()
            .and()
            .csrf().disable();
    }

    @Bean
    public UserDetailsContextMapper userDetailsContextMapper() {
        return new LdapUserDetailsMapper() {
            @Override
            public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
                                                  Collection<? extends GrantedAuthority> authorities) {
                final UserDetails details = super.mapUserFromContext(ctx, username, authorities);
                final Optional<Authentication> originalAuth = Optional.ofNullable(CurrentUser.getCurrentAuth());
                SystemUserService.runAs();
                Optional<Account> account = getAccountFromDatabase(username);
                List<String> localRoles = new ArrayList<>();
                Optional<String> email;
                if (account.isPresent()) {
                    localRoles = account.get().getRoles().stream()
                            .map(Enum::toString)
                            .collect(Collectors.toList());
                    email = Optional.ofNullable(account.get().getEmail());
                } else {
                    localRoles.add("ROLE_USER");
                    email = getEmailFromLdap(ctx, originalAuth);
                    account = newAccount(username, localRoles, email);
                }
                long id = account.isPresent()? account.get().getId() : Long.MAX_VALUE;
                SystemUserService.runAs(originalAuth.orElse(null));
                final Collection<GrantedAuthority> localAuthorities = getGrantedAuthorities(localRoles);
                return new CustomLdapUserDetails((LdapUserDetails) details, localAuthorities, id, email.orElse(""));
            }
        };
    }

    private Collection<GrantedAuthority> getGrantedAuthorities(final List<String> localRoles) {
        return AuthorityUtils.createAuthorityList(localRoles.toArray(new String[localRoles.size()-1]));
    }

    private Optional<String> getEmailFromLdap(final DirContextOperations ctx, final Optional<Authentication> originalAuth) {
        Optional<String> email;
        Optional<Object> principalObj = Optional.ofNullable(originalAuth.isPresent() ? originalAuth.get().getPrincipal() : Optional.empty());
        if (principalObj.isPresent() && principalObj.get().getClass() == InetOrgPerson.class) {
            InetOrgPerson principal = (InetOrgPerson)principalObj.get();
            email = Optional.ofNullable(principal.getMail());
        } else {
            email = Optional.ofNullable(ctx.getStringAttribute("mail"));
        }
        return email;
    }

    private Optional<Account> newAccount(String username, final List<String> localRoles, final Optional<String> email) {
        Optional<Account> account = Optional.of(new Account().setPassword(UUID.randomUUID().toString())
                                                             .setEmail(email.orElse(""))
                                                             .setTeams(Collections.emptySet())
                                                             .setRoles(localRoles.stream().map(Account.Role::valueOf)
                                                                        .collect(Collectors.toSet()))
                                                             .setName(username));
        account.ifPresent(a -> accountRepository.saveAndFlush(a));
        account = getAccountFromDatabase(username);
        return account;
    }

    private Optional<Account> getAccountFromDatabase(String username) {
        final Page<Account> accountPage = accountRepository.findByName(username, PAGE_REQUEST);
        return accountPage.iterator().hasNext() ? Optional.of(accountPage.iterator().next()) : Optional.empty();
    }

}
