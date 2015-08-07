package io.galeb.engine;

import io.galeb.engine.farm.EntityFarm;
import io.galeb.entity.AbstractEntity;

public interface Driver {

    public enum StatusFarm {
        OK,
        FAIL,
        UNKNOWN
    }

    public static final String DEFAULT_DRIVER_NAME = "NULL";

    default Driver setParams(String... param) {
        return this;
    }

    default EntityFarm info(AbstractEntity<?> entity) {
        return null;
    }

    default boolean create(AbstractEntity<?> entity) {
        return true;
    }

    default boolean update(AbstractEntity<?> entity) {
        return true;
    }

    default boolean remove(AbstractEntity<?> entity) {
        return true;
    }

    default boolean reload() {
        return true;
    }

    default StatusFarm status() {
        return StatusFarm.UNKNOWN;
    }

}
