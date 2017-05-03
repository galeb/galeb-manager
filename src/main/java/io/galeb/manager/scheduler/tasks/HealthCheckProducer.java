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

package io.galeb.manager.scheduler.tasks;

import com.google.gson.Gson;
import io.galeb.manager.entity.Pool;
import io.galeb.manager.entity.Target;
import io.galeb.manager.queue.JmsConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.Date;

import static org.apache.activemq.artemis.api.core.Message.HDR_DUPLICATE_DETECTION_ID;

@Component
public class HealthCheckProducer {

    private static final Log LOGGER = LogFactory.getLog(HealthCheckProducer.class);

    @PersistenceContext
    private EntityManager em;

    private final JmsTemplate template;

    @Autowired
    public HealthCheckProducer(@Value("#{jmsConnectionFactory}") ConnectionFactory connectionFactory) {
        this.template = new JmsConfiguration.EfemeralJmsTemplate(connectionFactory);
    }

    @Scheduled(fixedDelay = 10000)
    public void sendTargetsToQueue() {
        em.createQuery("SELECT t FROM Target t", Target.class).getResultList().forEach(target ->
            template.send("galeb-health", session -> {
                String json = new Gson().toJson(copyTarget(target), Target.class);
                Message message = session.createObjectMessage(json);
                String uniqueId = "ID:" + target.getId() + "-" + target.getLastModifiedAt().getTime() + "-" + (System.currentTimeMillis() / 10000L);
                message.setStringProperty("_HQ_DUPL_ID", uniqueId);
                message.setJMSMessageID(uniqueId);
                message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);

                LOGGER.info("JMSMessageID: " + uniqueId + " - Target " + target.getName());
                return message;
            })
        );
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
