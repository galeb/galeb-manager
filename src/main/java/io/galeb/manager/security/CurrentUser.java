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
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.galeb.manager.entity.Account;

public class CurrentUser implements UserDetails {

    private static final long serialVersionUID = -403060077273343289L;

    private final Long id;

    private final User user;

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();


    public static Authentication getCurrentAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public CurrentUser(String username,
                       String password,
                       boolean enabled,
                       boolean accountNonExpired,
                       boolean credentialsNonExpired,
                       boolean accountNonLocked,
                       Collection<? extends GrantedAuthority> authorities,
                       Long id) {
        user = new User(username,
                        ENCODER.encode(password),
                        enabled,
                        accountNonExpired,
                        credentialsNonExpired,
                        accountNonLocked,
                        authorities);
        this.id = id;
    }

    public CurrentUser(Account account) {

        user = new User(account.getName(),
                        account.getPassword(),
                        AuthorityUtils.createAuthorityList(account.getRoles().stream()
                                          .map(role -> role.toString()).collect(Collectors.toList())
                                          .toArray(new String[account.getRoles().size()-1]))
              );
        id = account.getId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    public void eraseCredentials() {
        user.eraseCredentials();
    }

    @Override
    public boolean equals(Object obj) {
        return user.equals(obj);
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }

    @Override
    public String toString() {
        return user.toString();
    }

}
