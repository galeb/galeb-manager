package io.galeb.handler;

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
import org.springframework.jms.core.JmsTemplate;

import io.galeb.engine.farm.RuleEngine;
import io.galeb.entity.Rule;
import io.galeb.entity.Target;
import io.galeb.entity.VirtualHost;
import io.galeb.exceptions.BadRequestException;
import io.galeb.repository.RuleRepository;

@RepositoryEventHandler(Rule.class)
public class RuleHandler extends RoutableToEngine<Rule> {

    private static Log LOGGER = LogFactory.getLog(RuleHandler.class);

    @Autowired
    private JmsTemplate jms;

    @Autowired
    private RuleRepository ruleRepository;

    public RuleHandler() {
        setQueueCreateName(RuleEngine.QUEUE_CREATE);
        setQueueUpdateName(RuleEngine.QUEUE_UPDATE);
        setQueueRemoveName(RuleEngine.QUEUE_REMOVE);
    }

    @Override
    protected void setBestFarm(final Rule rule) throws Exception {
        if (rule.getParent() != null && rule.getTarget() != null) {
            final VirtualHost virtualhost = rule.getParent();
            final Target target           = rule.getTarget();

            final long farmIdVirtualHost = virtualhost.getFarmId();
            final long farmIdTarget      = target.getFarmId();

            if (farmIdVirtualHost != farmIdTarget) {
                String errorMsg = "VirtualHost.farmId is not equal Target.farmId";
                LOGGER.error(errorMsg);
                throw new BadRequestException(errorMsg);
            }
        }
    }

    @HandleBeforeCreate
    public void beforeCreate(Rule rule) throws Exception {
        beforeCreate(rule, LOGGER);
    }

    @HandleAfterCreate
    public void afterCreate(Rule rule) throws Exception {
        afterCreate(rule, jms, LOGGER);
    }

    @HandleBeforeSave
    public void beforeSave(Rule rule) throws Exception {
        beforeSave(rule, ruleRepository, LOGGER);
    }

    @HandleAfterSave
    public void afterSave(Rule rule) throws Exception {
        afterSave(rule, jms, LOGGER);
    }

    @HandleBeforeDelete
    public void beforeDelete(Rule rule) throws Exception {
        beforeDelete(rule, LOGGER);
    }

    @HandleAfterDelete
    public void afterDelete(Rule rule) throws Exception {
        afterDelete(rule, jms, LOGGER);
    }

}
