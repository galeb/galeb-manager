package io.galeb.manager.scheduler;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class SchedulerConfiguration {

    public static final String GALEB_DISABLE_SCHED = "GALEB_DISABLE_SCHED";
    public static final String GALEB_TIMEOUT_SYNC_FARM = "GALEB_TIMEOUT_SYNC_FARM";

}
