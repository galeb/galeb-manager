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

import java.util.stream.Collectors;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import io.galeb.manager.entity.Account;

public class CurrentUser extends User {

    private static final long serialVersionUID = -403060077273343289L;

    private Long id;

    public CurrentUser(Account account) {
        super(account.getName(),
              "password",
              AuthorityUtils.createAuthorityList(account.getRoles().stream()
                      .map(role -> role.toString()).collect(Collectors.toList())
                      .toArray(new String[account.getRoles().size()-1]))
              );
        id = account.getId();
    }

    public Long getId() {
        return id;
    }

}
