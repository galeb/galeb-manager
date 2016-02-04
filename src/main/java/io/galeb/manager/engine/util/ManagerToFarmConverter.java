package io.galeb.manager.engine.util;

import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Farm;
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;
import io.galeb.manager.entity.Pool;
import io.galeb.manager.entity.Target;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ManagerToFarmConverter {

    public static final List<Class<? extends Entity>> FARM_ENTITIES_LIST =
                Arrays.asList(VirtualHost.class, Rule.class, BackendPool.class, Backend.class);

    public static final Map<String, Class<? extends Entity>> FARM_ENTITY_MAP = FARM_ENTITIES_LIST.stream()
            .collect(Collectors.toMap(clazz -> clazz.getSimpleName().toLowerCase(), clazz -> clazz));

    public static final Map<String, Class<? extends Entity>> MANAGER_TO_FARM_ENTITY_MAP = new HashMap<>();

    public static final Map<String, Class<?>> FARM_TO_MANAGER_ENTITY_MAP = new HashMap<>();

    static {
        MANAGER_TO_FARM_ENTITY_MAP.put(VirtualHost.class.getSimpleName().toLowerCase(), VirtualHost.class);
        MANAGER_TO_FARM_ENTITY_MAP.put(Pool.class.getSimpleName().toLowerCase(), BackendPool.class);
        MANAGER_TO_FARM_ENTITY_MAP.put(Rule.class.getSimpleName().toLowerCase(), Rule.class);
        MANAGER_TO_FARM_ENTITY_MAP.put(Target.class.getSimpleName().toLowerCase(), Backend.class);
        MANAGER_TO_FARM_ENTITY_MAP.put(Farm.class.getSimpleName().toLowerCase(), Farm.class);

        FARM_TO_MANAGER_ENTITY_MAP.put(VirtualHost.class.getSimpleName().toLowerCase(), VirtualHost.class);
        FARM_TO_MANAGER_ENTITY_MAP.put(BackendPool.class.getSimpleName().toLowerCase(), Pool.class);
        FARM_TO_MANAGER_ENTITY_MAP.put(Rule.class.getSimpleName().toLowerCase(), Rule.class);
        FARM_TO_MANAGER_ENTITY_MAP.put(Backend.class.getSimpleName().toLowerCase(), Target.class);
        FARM_TO_MANAGER_ENTITY_MAP.put(Farm.class.getSimpleName().toLowerCase(), Farm.class);
    }

    private ManagerToFarmConverter() {
        // static only
    }
}
