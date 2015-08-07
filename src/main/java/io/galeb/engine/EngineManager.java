package io.galeb.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import io.galeb.entity.Project;

@Component
public class EngineManager {

    public static final String PROVIDER_QUEUE = "mailbox-provider";

    private static Log LOGGER = LogFactory.getLog(EngineManager.class);

    @JmsListener(destination = PROVIDER_QUEUE)
    public void receive(Project project) {
        LOGGER.info("Processing "+project.getClass().getSimpleName()+" "+project.getName());
    }

}
