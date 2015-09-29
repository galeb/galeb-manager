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

import io.galeb.manager.entity.VirtualHost;

import java.util.List;

@PreAuthorize("isFullyAuthenticated()")
@RepositoryRestResource(collectionResourceRel = "virtualhost", path = "virtualhost")
public interface VirtualHostRepository extends JpaRepository<VirtualHost, Long>,
                                               FarmIDable<VirtualHost> {

    String QUERY_PREFIX = "SELECT v FROM VirtualHost v "
                        + "INNER JOIN v.project.teams t "
                        + "INNER JOIN t.accounts a "
                        + "WHERE ";

    String QUERY_FINDONE = QUERY_PREFIX + "v.id = :id AND "
                        + CommonJpaFilters.SECURITY_FILTER;

    String QUERY_FINDBYNAME = QUERY_PREFIX + "v.name = :name AND "
                        + CommonJpaFilters.SECURITY_FILTER;

    String QUERY_FINDALL = QUERY_PREFIX + CommonJpaFilters.SECURITY_FILTER;

    String QUERY_FINDBYNAMECONTAINING = QUERY_PREFIX + "v.name LIKE CONCAT('%',:name,'%') AND "
                        + CommonJpaFilters.SECURITY_FILTER;

    @Query(QUERY_FINDONE)
    VirtualHost findOne(@Param("id") Long id);

    @Query(QUERY_FINDBYNAME)
    Page<VirtualHost> findByName(@Param("name") String name, Pageable pageable);

    @Query(QUERY_FINDALL)
    Page<VirtualHost> findAll(Pageable pageable);

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Page<VirtualHost> findByFarmId(@Param("id") long id, Pageable pageable);

    @Query(QUERY_FINDBYNAMECONTAINING)
    Page<VirtualHost> findByNameContaining(@Param("name") String name, Pageable pageable);

}
