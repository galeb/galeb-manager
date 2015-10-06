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

package io.galeb.manager.queue;

import io.galeb.manager.queue.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.config.*;
import org.springframework.amqp.rabbit.connection.*;
import org.springframework.amqp.rabbit.core.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

import java.util.*;

@Configuration
@EnableRabbit
public class AmqpConfigurator {

    public static final String DISABLE_QUEUE = "DISABLE_QUEUE";

    @Value("${rabbitmq.setMaxConcurrentConsumers}")
    private Integer setMaxConcurrentConsumers;

    @Value("${rabbitmq.setConcurrentConsumers}")
    private Integer setConcurrentConsumers;

    @Value("${rabbitmq.setConsecutiveActiveTrigger}")
    private Integer setConsecutiveActiveTrigger;

    @Value("${rabbitmq.setConsecutiveIdleTrigger}")
    private Integer setConsecutiveIdleTrigger;

    List<Queue> queues() {
        return Arrays.asList(

            new Queue(FarmQueue.QUEUE_CREATE, false, false, true),
            new Queue(FarmQueue.QUEUE_REMOVE, false, false, true),
            new Queue(FarmQueue.QUEUE_UPDATE, false, false, true),
            new Queue(FarmQueue.QUEUE_SYNC,   false, false,  true),
            new Queue(FarmQueue.QUEUE_CALLBK, false, false, true),

            new Queue(PoolQueue.QUEUE_CREATE, false, false, true),
            new Queue(PoolQueue.QUEUE_REMOVE, false, false, true),
            new Queue(PoolQueue.QUEUE_UPDATE, false, false, true),
            new Queue(PoolQueue.QUEUE_CALLBK, false, false, true),

            new Queue(RuleQueue.QUEUE_CREATE, false, false, true),
            new Queue(RuleQueue.QUEUE_REMOVE, false, false, true),
            new Queue(RuleQueue.QUEUE_UPDATE, false, false, true),
            new Queue(RuleQueue.QUEUE_CALLBK, false, false, true),

            new Queue(TargetQueue.QUEUE_CREATE, false, false, true),
            new Queue(TargetQueue.QUEUE_REMOVE, false, false, true),
            new Queue(TargetQueue.QUEUE_UPDATE, false, false, true),
            new Queue(TargetQueue.QUEUE_CALLBK, false, false, true),

            new Queue(VirtualHostQueue.QUEUE_CREATE, false, false, true),
            new Queue(VirtualHostQueue.QUEUE_REMOVE, false, false, true),
            new Queue(VirtualHostQueue.QUEUE_UPDATE, false, false, true),
            new Queue(VirtualHostQueue.QUEUE_CALLBK, false, false, true)

        );
    }
    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        final RabbitAdmin amqpAdmin = new RabbitAdmin(connectionFactory());
        queues().stream().forEach(amqpAdmin::declareQueue);
        return amqpAdmin;
    }

    @Bean(name = "rabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrentConsumers(setConcurrentConsumers != null ? setConcurrentConsumers : 100);
        factory.setMaxConcurrentConsumers(setMaxConcurrentConsumers != null ? setMaxConcurrentConsumers : 100);
        factory.setConsecutiveActiveTrigger(setConsecutiveActiveTrigger != null ? setConsecutiveActiveTrigger : 1);
        factory.setConsecutiveIdleTrigger(setConsecutiveIdleTrigger != null ? setConsecutiveIdleTrigger : 20);
        return factory;
    }

    @Bean(name = "simpleListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory simpleListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setConsecutiveActiveTrigger(setConsecutiveActiveTrigger != null ? setConsecutiveActiveTrigger : 1);
        factory.setConsecutiveIdleTrigger(1);
        return factory;
    }

    private ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory();
    }



}
