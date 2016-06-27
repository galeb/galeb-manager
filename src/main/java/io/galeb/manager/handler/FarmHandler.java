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

package io.galeb.manager.handler;

import static io.galeb.manager.entity.AbstractEntity.EntityStatus.OK;

import io.galeb.manager.exceptions.ConflictException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;

import io.galeb.manager.entity.Farm;
import io.galeb.manager.repository.FarmRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RepositoryEventHandler(Farm.class)
public class FarmHandler extends AbstractHandler<Farm> {

    private static Log LOGGER = LogFactory.getLog(FarmHandler.class);

    @Autowired
    private FarmRepository farmRepository;

    @Override
    protected void setBestFarm(Farm entity) {
        // its me !!!
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @HandleBeforeCreate
    public void beforeCreate(Farm farm) throws Exception {
        beforeCreate(farm, LOGGER);
        farm.setStatus(OK);
    }

    @HandleAfterCreate
    public void afterCreate(Farm farm) throws Exception {
        afterCreate(farm, LOGGER);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @HandleBeforeSave
    public void beforeSave(Farm farm) throws Exception {
        beforeSave(farm, farmRepository, LOGGER);
        if (!apiIsUnique(farm)) {
            throw new ConflictException("API exist in other farm");
        }
    }

    private boolean apiIsUnique(final Farm farm) {
        final List<String> listOfapis = Arrays.asList(farm.getApi().split(","));
        farmRepository.findAll().stream()
                .filter(otherFarm -> otherFarm != farm)
                .forEach(otherFarm -> {
                    listOfapis.addAll(Arrays.asList(otherFarm.getApi().split(",")));
                });
        final Set<String> setOfApis = new HashSet<>(listOfapis);
        return listOfapis.size() == setOfApis.size();
    }

    @HandleAfterSave
    public void afterSave(Farm farm) throws Exception {
        afterSave(farm, LOGGER);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @HandleBeforeDelete
    public void beforeDelete(Farm farm) throws Exception {
        beforeDelete(farm, LOGGER);
    }

    @HandleAfterDelete
    public void afterDelete(Farm farm) throws Exception {
        afterDelete(farm, LOGGER);
    }

}
