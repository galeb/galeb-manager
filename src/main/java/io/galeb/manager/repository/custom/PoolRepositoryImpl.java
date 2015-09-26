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

import io.galeb.manager.entity.Pool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class PoolRepositoryImpl extends AbstractRepositoryImplementation<Pool>
                                  implements PoolRepositoryCustom {

    public static final String FIND_ALL = "SELECT p FROM Pool p "
                                            + "INNER JOIN p.project.teams t "
                                            + "INNER JOIN t.accounts a "
                                            + "WHERE 1 = :hasRoleAdmin OR "
                                            + "p.global = TRUE OR "
                                            + "a.name = :principalName";

    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(PoolRepositoryImpl.class);

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private JpaEntityInformation<Pool, ?> entityInformation;

    @PostConstruct
    public void init() {
        entityInformation = JpaEntityInformationSupport.getMetadata(Pool.class, em);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    protected String getFindAllStr() {
        return FIND_ALL;
    }

    @Override
    public Pool getNoParent() {
        Pool noParent = (Pool) em.createQuery("SELECT p FROM Pool p WHERE p.name = 'NoParent'").getSingleResult();
        if (noParent == null)
            System.out.println("NO_PARENT IS NULL. WHY?????");
        return noParent;
    }
}
