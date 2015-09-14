package io.galeb.manager.repository;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.entity.TargetType;
import io.galeb.manager.security.SystemUserService;

@Service
public class TargetTypePreLoad {

    public static final String TARGET_BACKENDPOOL_NAME = "BackendPool";
    public static final String TARGET_BACKEND_NAME = "Backend";

    @Autowired
    private TargetTypeRepository targetTypeRepository;

    @PostConstruct
    private void init() {
        SystemUserService.runAs();

        TargetType backendPool = new TargetType(TARGET_BACKENDPOOL_NAME).setStatus(EntityStatus.OK);
        TargetType backend = new TargetType(TARGET_BACKEND_NAME).setStatus(EntityStatus.OK);

        targetTypeRepository.save(backend);
        targetTypeRepository.save(backendPool);

        SystemUserService.clearContext();
    }

}
