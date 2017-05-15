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

package io.galeb.manager.healthcheck;

import com.google.gson.Gson;
import io.galeb.manager.entity.Pool;
import io.galeb.manager.entity.Target;
import io.galeb.manager.queue.JmsConfiguration;
import io.galeb.manager.repository.TargetRepository;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.security.user.CurrentUser;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import static org.apache.activemq.artemis.api.core.Message.HDR_DUPLICATE_DETECTION_ID;

@Service
public class HealthCheckService {

    private static final String QUEUE_HEALTH_CALLBACK = "health-callback";
    private static final int    PAGE_SIZE             = 100;
    private static final Log    LOGGER                = LogFactory.getLog(HealthCheckService.class);

    @PersistenceContext
    private EntityManager em;

    private final TargetRepository targetRepository;
    private final JmsTemplate template;

    @Autowired
    public HealthCheckService(@Value("#{jmsConnectionFactory}") ConnectionFactory connectionFactory, TargetRepository targetRepository) {
        this.template = new JmsConfiguration.EfemeralJmsTemplate(connectionFactory);
        this.targetRepository = targetRepository;
    }

    @Scheduled(fixedDelay = 10000)
    public void sendTargetsToQueue() {
        final String schedId = UUID.randomUUID().toString();
        LOGGER.info("[sch " + schedId + "] Sending targets to queue galeb-health");
        long start = System.currentTimeMillis();
        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicBoolean hasFail = new AtomicBoolean(false);
        final Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        final long size = targetRepository.count();
        int page = 0;
        while (page <= size/PAGE_SIZE) {
            Page<Target> targetsPage = targetRepository.findAll(new PageRequest(page, PAGE_SIZE));
            try {
                StreamSupport.stream(targetsPage.spliterator(), false).forEach(target -> {
                    try {
                        MessageCreator messageCreator = session -> {
                            counter.incrementAndGet();
                            String json = "{}";
                            try {
                                json = new Gson().toJson(copyTarget(target), Target.class);
                            } catch (Exception e) {
                                LOGGER.error(ExceptionUtils.getStackTrace(e));
                            }
                            Message message = session.createObjectMessage(json);
                            String uniqueId = "ID:" + target.getId() + "-" + target.getLastModifiedAt().getTime() + "-" + (System.currentTimeMillis() / 10000L);
                            message.setStringProperty("_HQ_DUPL_ID", uniqueId);
                            message.setJMSMessageID(uniqueId);
                            message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);

                            LOGGER.debug("JMSMessageID: " + uniqueId + " - Target " + target.getName());
                            return message;
                        };
                        template.send("galeb-health", messageCreator);
                    } catch (Exception e) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e));
                    }
                });
            } catch (Exception e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
                hasFail.set(true);
            } finally {
                page++;
            }
            if (hasFail.get()) break;
        }
        SystemUserService.runAs(currentUser);
        LOGGER.info("[sch " + schedId + "] Sent " + counter.get() + " targets to queue galeb-health " +
                "[" + (System.currentTimeMillis() - start) + " ms] (before to start this task: " + size + " targets from db)");
    }

    @SuppressWarnings("unused")
    @JmsListener(destination = QUEUE_HEALTH_CALLBACK)
    @Transactional
    public void healthCallback(String targetStr) {
        try {
            final Target targetCopy = new Gson().fromJson(targetStr, Target.class);
            final Target target = em.find(Target.class, targetCopy.getId());
            target.setProperties(targetCopy.getProperties());
            em.merge(target);
            LOGGER.warn("Healthcheck: target " + target.getName() + " updated. New status detailed: " + target.getProperties().get("status_detailed"));
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private Target copyTarget(final Target target) {
        Pool pool = new Pool(target.getParent().getName());
        pool.setProperties(target.getParent().getProperties());
        Target targetCopy = new Target(target.getName()) {
            @Override
            public Date getCreatedAt() {
                return target.getCreatedAt();
            }

            @Override
            public Date getLastModifiedAt() {
                return target.getLastModifiedAt();
            }

            @Override
            public EntityStatus getStatus() {
                return target.getStatus();
            }

            @Override
            public Long getVersion() {
                return target.getVersion();
            }

            @Override
            public int getHash() {
                return target.getHash();
            }
        };
        targetCopy.setId(target.getId());
        targetCopy.setProperties(target.getProperties());
        targetCopy.setParent(pool);
        return targetCopy;
    }

}
