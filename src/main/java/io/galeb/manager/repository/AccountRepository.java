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

package io.galeb.manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import io.galeb.manager.entity.Account;

@RepositoryRestResource(collectionResourceRel = "account", path = "account")
public interface AccountRepository extends PagingAndSortingRepository<Account, Long> {

    @PreAuthorize("hasRole('ROLE_ADMIN') or #id == principal.id")
    @Override
    Account findOne(@Param("id") Long id);

    @Query("SELECT a FROM Account a WHERE "
            + "1 = ?#{hasRole('ROLE_ADMIN') ? 1 : 0 } OR "
            + "a.createdBy = ?#{principal.username} OR "
            + "a.name = ?#{principal.username}")
    @Override
    Iterable<Account> findAll();

    @PreAuthorize("hasRole('ROLE_ADMIN') or #name == principal.username")
    List<Account> findByName(@Param("name") String name);

}
