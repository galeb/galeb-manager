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
import static io.galeb.manager.entity.Account.Role.ROLE_ADMIN;

import io.galeb.manager.entity.Account;
import io.galeb.manager.entity.Team;
import io.galeb.manager.exceptions.ForbiddenException;
import io.galeb.manager.repository.AccountRepository;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.security.user.CurrentUser;
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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;
import java.util.stream.*;

@RepositoryEventHandler(Account.class)
public class AccountHandler {

    private static Log LOGGER = LogFactory.getLog(AccountHandler.class);

    @Autowired
    private AccountRepository accountRepository;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @HandleBeforeCreate
    public void beforeCreate(Account account) {
        LOGGER.info("Account: HandleBeforeCreate");
        account.setStatus(OK);
    }

    @HandleAfterCreate
    public void afterCreate(Account account) {
        LOGGER.info("Account: HandleAfterCreate");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @HandleBeforeSave
    public void beforeSave(Account account) {
        LOGGER.info("Account: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(Account account) {
        LOGGER.info("Account: HandleAfterSave");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @HandleBeforeDelete
    public void beforeDelete(Account account) {
        LOGGER.info("Account: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(Account account) {
        LOGGER.info("Account: HandleAfterDelete");
    }

}
