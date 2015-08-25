package io.galeb.manager.scheduler.tasks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.StreamSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.Driver;
import io.galeb.manager.engine.Driver.StatusFarm;
import io.galeb.manager.engine.DriverBuilder;
import io.galeb.manager.engine.farm.FarmEngine;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.RuleRepository;
import io.galeb.manager.repository.TargetRepository;
import io.galeb.manager.repository.VirtualHostRepository;
import io.galeb.manager.security.CurrentUser;
import io.galeb.manager.security.SystemUserService;

@Component
public class CheckFarms {

    private static final Log LOGGER = LogFactory.getLog(CheckFarms.class);

    @Autowired
    FarmRepository farmRepository;

    @Autowired
    VirtualHostRepository virtualHostRepository;

    @Autowired
    RuleRepository ruleRepository;

    @Autowired
    TargetRepository targetRepository;

    @Autowired
    JmsTemplate jms;

    @Scheduled(fixedRate = 10000)
    private void run() {
        LOGGER.debug("TASK checkFarm executing");

        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        StreamSupport.stream(farmRepository.findAll().spliterator(), false)
                                .filter(farm -> !farm.getStatus().equals(EntityStatus.DISABLED))
                                .forEach(farm -> {

            final Driver driver = DriverBuilder.getDriver(farm);
            AtomicBoolean isOk = new AtomicBoolean(true);

            if (!farm.getStatus().equals(EntityStatus.ERROR)) {

                virtualHostRepository.findByFarmId(farm.getId()).forEach(virtualhost -> {

                    Properties properties = new Properties();
                    properties.put("api", farm.getApi());
                    properties.put("name", virtualhost.getName());
                    properties.put("path", "virtualhost");
                    properties.put("id", virtualhost.getId());

                    boolean lastStatus = isOk.get();
                    isOk.set(driver.status(properties).equals(StatusFarm.OK) && lastStatus);
                });

                ruleRepository.findByFarmId(farm.getId()).forEach(rule -> {

                    Properties properties = new Properties();
                    properties.put("api", farm.getApi());
                    properties.put("name", rule.getName());
                    properties.put("path", "rule");
                    properties.put("id", rule.getId());

                    boolean lastStatus = isOk.get();
                    isOk.set(driver.status(properties).equals(StatusFarm.OK) && lastStatus);
                });

                targetRepository.findByFarmId(farm.getId()).forEach(target -> {

                    Properties properties = new Properties();
                    properties.put("api", farm.getApi());
                    properties.put("name", target.getName());
                    properties.put("path", target.getTargetType().getName().toLowerCase());
                    properties.put("id", target.getId());

                    boolean lastStatus = isOk.get();
                    isOk.set(driver.status(properties).equals(StatusFarm.OK) && lastStatus);
                });

            } else {
                isOk.set(false);
            }

            farm.setStatus(isOk.get() ? EntityStatus.OK : EntityStatus.ERROR);
            if (!isOk.get()) {
                jms.convertAndSend(FarmEngine.QUEUE_RELOAD, farm);
            } else {
                LOGGER.info("FARM STATUS OK: "+farm.getName()+" ["+farm.getApi()+"]");
            }
        });
        SystemUserService.runAs(currentUser);

        LOGGER.debug("TASK checkFarm finished");
    }
}
