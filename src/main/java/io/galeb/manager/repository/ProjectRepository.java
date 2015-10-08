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

import io.galeb.manager.entity.Project;
import org.springframework.transaction.annotation.*;

@PreAuthorize("isFullyAuthenticated()")
@RepositoryRestResource(collectionResourceRel = "project", path = "project")
public interface ProjectRepository extends JpaRepository<Project, Long> {

    String QUERY_PREFIX = "SELECT p FROM Project p "
                        + "INNER JOIN p.teams t "
                        + "LEFT JOIN t.accounts a "
                        + "WHERE ";

    String QUERY_FINDONE = QUERY_PREFIX + "p.id = :id AND "
                        + CommonJpaFilters.SECURITY_FILTER;

    String QUERY_FINDALL = QUERY_PREFIX + CommonJpaFilters.SECURITY_FILTER;

    String QUERY_FINDBYNAME = QUERY_PREFIX + "p.name = :name AND "
                        + CommonJpaFilters.SECURITY_FILTER;

    String QUERY_FINDBYNAMECONTAINING = QUERY_PREFIX + "p.name LIKE CONCAT('%',:name,'%') AND "
                        + CommonJpaFilters.SECURITY_FILTER;

    @Query(QUERY_FINDONE)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Project findOne(@Param("id") Long id);

    @Query(QUERY_FINDALL)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<Project> findAll(Pageable pageable);

    @Query(QUERY_FINDBYNAME)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<Project> findByName(@Param("name") String name, Pageable pageable);

    @Query(QUERY_FINDBYNAMECONTAINING)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<Project> findByNameContaining(@Param("name") String name, Pageable pageable);

}
