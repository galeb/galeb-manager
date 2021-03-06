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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import io.galeb.manager.entity.Account;
import org.springframework.transaction.annotation.*;

import java.util.List;

@PreAuthorize("isFullyAuthenticated()")
@RepositoryRestResource(collectionResourceRel = "account", path = "account")
public interface AccountRepository extends JpaRepository<Account, Long> {

    String QUERY_PREFIX = "SELECT DISTINCT a FROM Account a WHERE ";

    String NATIVE_QUERY_PREFIX = "SELECT DISTINCT a.* FROM account a WHERE ";

    String QUERY_FINDALL = QUERY_PREFIX + CommonJpaFilters.SECURITY_FILTER;

    String QUERY_FINDBYNAME = QUERY_PREFIX + "a.name = :name AND "
                        + CommonJpaFilters.SECURITY_FILTER;

    String QUERY_FINDBYNAMECONTAINING = NATIVE_QUERY_PREFIX + "a.name LIKE CONCAT('%',:name,'%') AND "
                        + CommonJpaFilters.SECURITY_FILTER + " ORDER BY a.name";

    @PreAuthorize("hasRole('ROLE_ADMIN') or #id == principal.id")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Account findOne(@Param("id") Long id);

    @Query(QUERY_FINDALL)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<Account> findAll(Pageable pageable);

    @Query(QUERY_FINDBYNAME)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<Account> findByName(@Param("name") String name, Pageable pageable);

    @Query(value = QUERY_FINDBYNAMECONTAINING, nativeQuery = true)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Iterable<Account> findByNameContaining(@Param("name") String name);

    @Query(value = QUERY_FINDBYNAMECONTAINING + " LIMIT :size", nativeQuery = true)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Iterable<Account> findByNameContainingWithSize(@Param("name") String name, @Param("size") int size);
}
