package io.galeb.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import io.galeb.entity.AbstractEntity.EntityStatus;
import io.galeb.entity.Account;

@RepositoryEventHandler(Account.class)
public class AccountHandler {

    private static Log LOGGER = LogFactory.getLog(AccountHandler.class);

    @HandleBeforeCreate
    public void beforeCreate(Account account) {
        LOGGER.info("Account: HandleBeforeCreate");
        account.setStatus(EntityStatus.OK);
    }

    @HandleAfterCreate
    public void afterCreate(Account account) {
        LOGGER.info("Account: HandleAfterCreate");
    }

    @HandleBeforeSave
    public void beforeSave(Account account) {
        LOGGER.info("Account: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(Account account) {
        LOGGER.info("Account: HandleAfterSave");
    }

    @HandleBeforeDelete
    public void beforeDelete(Account account) {
        LOGGER.info("Account: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(Account account) {
        LOGGER.info("Account: HandleAfterDelete");
    }

}
