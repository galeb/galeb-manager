package io.galeb.manager.engine.util;

import io.galeb.core.model.Entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ManagerToFarmConverter {

    public static final List<Class<? extends Entity>> FARM_ENTITIES_LIST =
                Arrays.asList(io.galeb.core.model.VirtualHost.class,
                        io.galeb.core.model.Rule.class,
                        io.galeb.core.model.BackendPool.class,
                        io.galeb.core.model.Backend.class);

    public static final Map<String, Class<? extends Entity>> FARM_ENTITY_MAP = FARM_ENTITIES_LIST.stream()
            .collect(Collectors.toMap(clazz -> clazz.getSimpleName().toLowerCase(), clazz -> clazz));

    public static final Map<String, Class<? extends Entity>> MANAGER_TO_FARM_ENTITY_MAP = new HashMap<>();

    public static final Map<String, Class<?>> FARM_TO_MANAGER_ENTITY_MAP = new HashMap<>();

    static {
        MANAGER_TO_FARM_ENTITY_MAP.put(io.galeb.manager.entity.VirtualHost.class.getSimpleName().toLowerCase(), io.galeb.core.model.VirtualHost.class);
        MANAGER_TO_FARM_ENTITY_MAP.put(io.galeb.manager.entity.Pool.class.getSimpleName().toLowerCase(), io.galeb.core.model.BackendPool.class);
        MANAGER_TO_FARM_ENTITY_MAP.put(io.galeb.manager.entity.Rule.class.getSimpleName().toLowerCase(), io.galeb.core.model.Rule.class);
        MANAGER_TO_FARM_ENTITY_MAP.put(io.galeb.manager.entity.Target.class.getSimpleName().toLowerCase(), io.galeb.core.model.Backend.class);
        MANAGER_TO_FARM_ENTITY_MAP.put(io.galeb.manager.entity.Farm.class.getSimpleName().toLowerCase(), io.galeb.core.model.Farm.class);

        FARM_TO_MANAGER_ENTITY_MAP.put(io.galeb.core.model.VirtualHost.class.getSimpleName().toLowerCase(), io.galeb.manager.entity.VirtualHost.class);
        FARM_TO_MANAGER_ENTITY_MAP.put(io.galeb.core.model.BackendPool.class.getSimpleName().toLowerCase(), io.galeb.manager.entity.Pool.class);
        FARM_TO_MANAGER_ENTITY_MAP.put(io.galeb.core.model.Rule.class.getSimpleName().toLowerCase(), io.galeb.manager.entity.Rule.class);
        FARM_TO_MANAGER_ENTITY_MAP.put(io.galeb.core.model.Backend.class.getSimpleName().toLowerCase(), io.galeb.manager.entity.Target.class);
        FARM_TO_MANAGER_ENTITY_MAP.put(io.galeb.core.model.Farm.class.getSimpleName().toLowerCase(), io.galeb.manager.entity.Farm.class);
    }

    private ManagerToFarmConverter() {
        // static only
    }
}
