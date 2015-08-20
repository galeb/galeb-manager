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

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.galeb.manager.entity.Account;
import io.galeb.manager.repository.AccountRepository;

@Service
public class CurrentUserDetailsService implements UserDetailsService {

    public static final UsernamePasswordAuthenticationToken SYSTEM_USER =
                            new UsernamePasswordAuthenticationToken("system", UUID.randomUUID().toString(),
                                    AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER"));
    @Autowired
    AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        final Authentication originalAuth = getCurrentAuth();
        runAsSystem();
        Account account = accountRepository.findByName(userName);
        runAs(originalAuth);
        if (account == null) {
            throw new UsernameNotFoundException("Account "+userName+" NOT FOUND");
        }
        return new CurrentUser(account);
    }

    private Authentication getCurrentAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private void runAsSystem() {
        runAs(SYSTEM_USER);
    }

    private void runAs(final Authentication authentication) {
        clearContext();
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void clearContext() {
        SecurityContextHolder.clearContext();
    }

}
