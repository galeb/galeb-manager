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

package io.galeb.manager.repository.custom;

import io.galeb.manager.entity.Rule;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;

@Repository
public class VirtualHostRepositoryImpl implements VirtualHostRepositoryCustom {

    private static final Log LOGGER = LogFactory.getLog(VirtualHostRepositoryImpl.class);

    private static final String QUERY_GET_RULES = "SELECT DISTINCT r FROM Rule r " +
                                                 "INNER JOIN r.parents p " +
                                                 "WHERE p.name = :name";

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings("unchecked")
    @Override
    public List<Rule> getRulesFromVirtualHostName(String name) {
        try {
            return em.createQuery(QUERY_GET_RULES).setParameter("name", name).getResultList();
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return Collections.emptyList();
        }
    }
}
