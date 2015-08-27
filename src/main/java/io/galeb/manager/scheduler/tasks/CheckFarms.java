package io.galeb.manager.scheduler.tasks;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.Target;
import io.galeb.manager.entity.VirtualHost;
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

               final long virtualhostCount = getVirtualhosts(farm).count();

               getVirtualhosts(farm).forEach(virtualhost -> {
                    Properties properties = new Properties();
                    properties.put("api", farm.getApi());
                    properties.put("name", virtualhost.getName());
                    properties.put("path", "virtualhost");
                    properties.put("id", virtualhost.getId());
                    properties.put("numElements", virtualhostCount);

                    boolean lastStatus = isOk.get();
                    isOk.set(driver.status(properties).equals(StatusFarm.OK) && lastStatus);
                });

                final long ruleCount = getRules(farm).count();

                getRules(farm).forEach(rule -> {
                    Properties properties = new Properties();
                    properties.put("api", farm.getApi());
                    properties.put("name", rule.getName());
                    properties.put("path", "rule");
                    properties.put("id", rule.getId());
                    properties.put("numElements", ruleCount);

                    boolean lastStatus = isOk.get();
                    isOk.set(driver.status(properties).equals(StatusFarm.OK) && lastStatus);
                });

                Map<String, Long> targetsCountMap = getTargets(farm).parallel().collect(
                                Collectors.groupingBy(target -> target.getTargetType().getName(),
                                                      Collectors.counting()));

                getTargets(farm).forEach(target -> {
                    String targetTypeName = target.getTargetType().getName();
                    Properties properties = new Properties();
                    properties.put("api", farm.getApi());
                    properties.put("name", target.getName());
                    properties.put("path", target.getTargetType().getName().toLowerCase());
                    properties.put("id", target.getId());
                    properties.put("numElements", targetsCountMap.get(targetTypeName));

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

    private Stream<Target> getTargets(Farm farm) {
        return StreamSupport.stream(
                targetRepository.findByFarmId(farm.getId()).spliterator(), false);
    }

    private Stream<Rule> getRules(Farm farm) {
        return StreamSupport.stream(
                ruleRepository.findByFarmId(farm.getId()).spliterator(), false)
                .filter(rule -> rule.getParent() != null);
    }

    private Stream<VirtualHost> getVirtualhosts(Farm farm) {
        return StreamSupport.stream(
                virtualHostRepository.findByFarmId(farm.getId()).spliterator(), false);
    }

}
