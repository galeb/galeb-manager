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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import io.galeb.manager.entity.Rule;
import org.springframework.transaction.annotation.*;

import static io.galeb.manager.repository.CommonJpaFilters.IS_GLOBAL_FILTER;
import static io.galeb.manager.repository.CommonJpaFilters.SECURITY_FILTER;

@PreAuthorize("isFullyAuthenticated()")
@RepositoryRestResource(collectionResourceRel = "rule", path = "rule")
public interface RuleRepository extends JpaRepositoryWithFindByName<Rule, Long>,
                                        FarmIDable<Rule> {

    String QUERY_PROJECT_TO_ACCOUNT = "INNER JOIN e.pool.project.teams t "
                        + "LEFT JOIN t.accounts a ";

    String NATIVE_QUERY_PREFIX = "select * from rule e ";

    // TODO: Define Native SQL
    String NATIVE_QUERY_PROJECT_TO_ACCOUNT = "";

    String QUERY_PREFIX = "SELECT e FROM Rule e " + QUERY_PROJECT_TO_ACCOUNT + "WHERE ";

    String QUERY_FINDONE = QUERY_PREFIX + "e.id = :id AND "
                        + "(" + SECURITY_FILTER + " OR "
                        + IS_GLOBAL_FILTER + ")";

    String QUERY_FINDBYNAME = QUERY_PREFIX + "e.name = :name AND "
                        + "(" + SECURITY_FILTER + " OR "
                        + IS_GLOBAL_FILTER + ")";

    String QUERY_FINDALL = QUERY_PREFIX + SECURITY_FILTER + " OR "
                        + IS_GLOBAL_FILTER;

    String QUERY_FINDBYPOOLNAME = QUERY_PREFIX + "e.pool.name = :name AND "
                        + "(" + SECURITY_FILTER + " OR "
                        + IS_GLOBAL_FILTER + ")";

    String QUERY_FINDBYNAMECONTAINING = NATIVE_QUERY_PREFIX + NATIVE_QUERY_PROJECT_TO_ACCOUNT +
            "where (e.name like concat('%', :name, '%')) and " +
            SECURITY_FILTER + IS_GLOBAL_FILTER;

    @Query(QUERY_FINDONE)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Rule findOne(@Param("id") Long id);

    @Override
    @Query(QUERY_FINDBYNAME)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<Rule> findByName(@Param("name") String name, Pageable pageable);

    @Query(QUERY_FINDALL)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<Rule> findAll(Pageable pageable);

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<Rule> findByFarmId(@Param("id") long id, Pageable pageable);

    @Query(value = QUERY_FINDBYNAMECONTAINING, nativeQuery = true)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Iterable<Rule> findByNameContaining(@Param("name") String name);

    @Query(QUERY_FINDBYPOOLNAME)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<Rule> findByPoolName(@Param("name") String name, Pageable pageable);

}
