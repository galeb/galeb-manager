/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2015 Globo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package io.galeb.manager.repository;

import io.galeb.manager.entity.Pool;
import io.galeb.manager.repository.custom.PoolRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static io.galeb.manager.repository.CommonJpaFilters.*;

@PreAuthorize("isFullyAuthenticated()")
@RepositoryRestResource(collectionResourceRel = "pool", path = "pool")
public interface PoolRepository extends JpaRepositoryWithFindByName<Pool, Long>,
                                        FarmIDable<Pool>,
                                        PoolRepositoryCustom {
    String NO_PARENT_NAME = "NoParent";

    String QUERY_PREFIX = "SELECT DISTINCT e FROM Pool e " + QUERY_PROJECT_TO_ACCOUNT + " WHERE ";

    String NATIVE_QUERY_PREFIX = "SELECT DISTINCT e.* FROM pool e " + NATIVE_QUERY_PROJECT_TO_ACCOUNT;

    String QUERY_FINDONE = QUERY_PREFIX + "e.id = :id AND " +
                        "(" + SECURITY_FILTER + " OR " + IS_GLOBAL_FILTER + ")";

    String QUERY_FINDBYNAME = QUERY_PREFIX + "e.name = :name AND " +
                        "(" + SECURITY_FILTER + " OR " + IS_GLOBAL_FILTER + ")";

    String QUERY_FINDALL = QUERY_PREFIX + SECURITY_FILTER + " OR " + IS_GLOBAL_FILTER;

    String QUERY_FINDBYNAMECONTAINING = NATIVE_QUERY_PREFIX +
                        "WHERE (e.name LIKE concat('%', :name, '%')) AND " +
                        "(" + SECURITY_FILTER + " OR " + IS_GLOBAL_FILTER + ")"
                        + " ORDER BY e.name";

    @Query(QUERY_FINDONE)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Pool findOne(@Param("id") Long id);

    @Override
    @Query(QUERY_FINDBYNAME)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<Pool> findByName(@Param("name") String name, Pageable pageable);

    @Query(QUERY_FINDALL)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<Pool> findAll(Pageable pageable);

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<Pool> findByFarmId(@Param("id") long id, Pageable pageable);

    @Modifying
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Pool getNoParent();

    @Query(value = QUERY_FINDBYNAMECONTAINING, nativeQuery = true)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Iterable<Pool> findByNameContaining(@Param("name") String name);

    @Query(value = QUERY_FINDBYNAMECONTAINING + " LIMIT :size", nativeQuery = true)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Iterable<Pool> findByNameContainingWithSize(@Param("name") String name, @Param("size") int size);

}
