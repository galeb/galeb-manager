package io.galeb.engine;

import io.galeb.engine.farm.EntityFarm;
import io.galeb.manager.common.Properties;

public interface Driver {

    public enum StatusFarm {
        OK,
        FAIL,
        UNKNOWN
    }

    public static final String DEFAULT_DRIVER_NAME = "NULL";

    default EntityFarm info(Properties properties) {
        return null;
    }

    default boolean create(Properties properties) {
        return true;
    }

    default boolean update(Properties properties) {
        return true;
    }

    default boolean remove(Properties properties) {
        return true;
    }

    default boolean reload(Properties properties) {
        return true;
    }

    default StatusFarm status(Properties properties) {
        return StatusFarm.UNKNOWN;
    }

}
