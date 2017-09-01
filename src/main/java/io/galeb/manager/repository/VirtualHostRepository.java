/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.manager.repository;

import io.galeb.manager.entity.Rule;
import io.galeb.manager.repository.custom.VirtualHostRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import io.galeb.manager.entity.VirtualHost;
import org.springframework.transaction.annotation.*;

import java.util.List;
import java.util.Set;

import static io.galeb.manager.repository.CommonJpaFilters.*;

@PreAuthorize("isFullyAuthenticated()")
@RepositoryRestResource(collectionResourceRel = "virtualhost", path = "virtualhost")
public interface VirtualHostRepository extends JpaRepositoryWithFindByName<VirtualHost, Long>,
                                               FarmIDable<VirtualHost>,
                                               VirtualHostRepositoryCustom {

    String QUERY_PREFIX = "SELECT DISTINCT e FROM VirtualHost e " + QUERY_PROJECT_TO_ACCOUNT + " WHERE ";

    String NATIVE_QUERY_PREFIX = "SELECT DISTINCT e.* FROM virtualhost e ";

    String QUERY_FINDONE = QUERY_PREFIX + "e.id = :id AND " + SECURITY_FILTER;

    String QUERY_FINDBYNAME = QUERY_PREFIX + "e.name = :name AND " + SECURITY_FILTER;

    String QUERY_FINDALL = QUERY_PREFIX + SECURITY_FILTER;

    String QUERY_FINDBYNAMECONTAINING = NATIVE_QUERY_PREFIX + NATIVE_QUERY_PROJECT_TO_ACCOUNT
            + "where (e.name like concat('%', :name, '%')) and " + SECURITY_FILTER
            + " ORDER BY e.name";

    @Query(QUERY_FINDONE)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    VirtualHost findOne(@Param("id") Long id);

    @Override
    @Query(QUERY_FINDBYNAME)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<VirtualHost> findByName(@Param("name") String name, Pageable pageable);

    @Query(QUERY_FINDALL)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<VirtualHost> findAll(Pageable pageable);

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Page<VirtualHost> findByFarmId(@Param("id") long id, Pageable pageable);

    @Query(value = QUERY_FINDBYNAMECONTAINING, nativeQuery = true)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Iterable<VirtualHost> findByNameContaining(@Param("name") String name);

    @Query(value = QUERY_FINDBYNAMECONTAINING + " LIMIT :size", nativeQuery = true)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Iterable<VirtualHost> findByNameContainingWithSize(@Param("name") String name, @Param("size") int size);

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    List<VirtualHost> findByEnvironmentName(@Param("name") String name);

    @Modifying
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    List<Rule> getRulesFromVirtualHostName(@Param("name") String name);

    @Modifying
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Set<String> getAllNames(long farmId);

}
