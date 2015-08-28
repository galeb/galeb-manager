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
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import io.galeb.manager.entity.Target;
import io.galeb.manager.repository.custom.TargetRepositoryCustom;

@PreAuthorize("isFullyAuthenticated()")
@RepositoryRestResource(collectionResourceRel = "target", path = "target")
public interface TargetRepository extends PagingAndSortingRepository<Target, Long>,
                                          FarmIDable<Target>,
                                          TargetRepositoryCustom {

    @Override
    @Query("SELECT ta FROM Target ta "
           + "INNER JOIN ta.project.teams t "
           + "INNER JOIN t.accounts a "
           + "WHERE ta.id = :id AND "
               + "(1 = ?#{hasRole('ROLE_ADMIN') ? 1 : 0} OR "
               + "a.name = ?#{principal.username})")
    Target findOne(@Param("id") Long id);

    @Override
    @Query
    Target findByName(@Param("name") String name);

    @Override
    @Query
    Page<Target> findAll(Pageable pageable);

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Iterable<Target> findByFarmId(long id);

    @Query("SELECT ta FROM Target ta "
            + "INNER JOIN ta.project.teams t "
            + "INNER JOIN t.accounts a "
            + "WHERE ta.targetType.name = :name AND "
                + "(1 = ?#{hasRole('ROLE_ADMIN') ? 1 : 0} OR "
                + "a.name = ?#{principal.username})")
    Iterable<Target> findByTargetTypeName(@Param("name") String name);
}
