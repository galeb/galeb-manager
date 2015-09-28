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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import io.galeb.manager.entity.Rule;

import java.util.List;

@PreAuthorize("isFullyAuthenticated()")
@RepositoryRestResource(collectionResourceRel = "rule", path = "rule")
public interface RuleRepository extends JpaRepository<Rule, Long>,
                                        FarmIDable<Rule> {

    String QUERY_PREFIX = "SELECT r FROM Rule r "
                        + "INNER JOIN r.pool.project.teams t "
                        + "INNER JOIN t.accounts a "
                        + "WHERE ";

    String QUERY_FINDONE = QUERY_PREFIX + "r.id = :id AND "
                        + "(1 = ?#{hasRole('ROLE_ADMIN') ? 1 : 0} OR "
                        + "r.global = TRUE OR "
                        + "a.id = ?#{principal.id})";

    String QUERY_FINDBYNAME = QUERY_PREFIX + "r.name = :name AND "
                        + "(1 = ?#{hasRole('ROLE_ADMIN') ? 1 : 0} OR "
                        + "r.global = TRUE OR "
                        + "a.id = ?#{principal.id})";

    String QUERY_FINDALL = QUERY_PREFIX + "1 = ?#{hasRole('ROLE_ADMIN') ? 1 : 0} OR "
                        + "r.global = TRUE OR "
                        + "a.id = ?#{principal.id}";

    String QUERY_FINDBYPOOLNAME = QUERY_PREFIX + "r.pool.name = :name AND "
                        + "(1 = ?#{hasRole('ROLE_ADMIN') ? 1 : 0} OR "
                        + "r.global = TRUE OR "
                        + "a.id = ?#{principal.id})";

    @Query(QUERY_FINDONE)
    Rule findOne(@Param("id") Long id);

    @Query(QUERY_FINDBYNAME)
    Page<Rule> findByName(@Param("name") String name, Pageable pageable);

    @Query(QUERY_FINDALL)
    Page<Rule> findAll(Pageable pageable);

    @Query(QUERY_FINDALL)
    List<Rule> findAll(Sort sort);

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Page<Rule> findByFarmId(@Param("id") long id, Pageable pageable);

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Page<Rule> findByNameContaining(@Param("name") String name, Pageable pageable);

    @Query(QUERY_FINDBYPOOLNAME)
    Page<Rule> findByPoolName(@Param("name") String name, Pageable pageable);

}
