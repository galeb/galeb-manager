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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static io.galeb.manager.repository.CommonJpaFilters.IS_GLOBAL_FILTER;
import static io.galeb.manager.repository.CommonJpaFilters.SECURITY_FILTER;

@PreAuthorize("isFullyAuthenticated()")
@RepositoryRestResource(collectionResourceRel = "rule", path = "rule")
public interface RuleRepository extends JpaRepositoryWithFindByName<Rule, Long>,
                                        FarmIDable<Rule> {

    String QUERY_PREFIX = "SELECT DISTINCT e FROM Rule e " +
                        "INNER JOIN e.pool.project.teams t " +
                        "LEFT JOIN t.accounts a WHERE ";

    String NATIVE_QUERY_PREFIX =
                        "SELECT DISTINCT e.* FROM rule e " +
                        "INNER JOIN pool pool_ ON e.pool_id = pool_.id " +
                        "INNER JOIN project p on pool_.project_id=p.id " +
                        "INNER JOIN project_teams teams on p.id=teams.project_id " +
                        "INNER JOIN team t on teams.team_id=t.id " +
                        "LEFT OUTER JOIN account_teams accounts on t.id=accounts.team_id " +
                        "LEFT OUTER JOIN account a on accounts.account_id=a.id ";

    String QUERY_FINDONE = QUERY_PREFIX + "e.id = :id AND " +
                        "(" + SECURITY_FILTER + " OR " + IS_GLOBAL_FILTER + ")";

    String QUERY_FINDBYNAME = QUERY_PREFIX + "e.name = :name AND " +
                        "(" + SECURITY_FILTER + " OR " + IS_GLOBAL_FILTER + ")";

    String QUERY_FINDALL = QUERY_PREFIX + SECURITY_FILTER + " OR " + IS_GLOBAL_FILTER;

    String QUERY_FINDBYPOOLNAME = QUERY_PREFIX + "e.pool.name = :name AND " +
                        "(" + SECURITY_FILTER + " OR " + IS_GLOBAL_FILTER + ")";

    String QUERY_FINDBYNAMECONTAINING = NATIVE_QUERY_PREFIX +
                        "WHERE (e.name LIKE CONCAT('%', :name, '%')) AND " +
                        "(" + SECURITY_FILTER + " OR " + IS_GLOBAL_FILTER + ")"
                        + " ORDER BY e.name";

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

    @Query(value = QUERY_FINDBYNAMECONTAINING + " LIMIT :size", nativeQuery = true)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Iterable<Rule> findByNameContainingWithSize(@Param("name") String name, @Param("size") int size);

    @Query(QUERY_FINDBYPOOLNAME)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<Rule> findByPoolName(@Param("name") String name, Pageable pageable);

}
