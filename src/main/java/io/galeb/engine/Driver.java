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
        return false;
    }

    default boolean update(AbstractEntity<?> entity) {
        return false;
    }

    default boolean remove(AbstractEntity<?> entity) {
        return false;
    }

    default boolean reload() {
        return false;
    }

    default StatusFarm status() {
        return StatusFarm.UNKNOWN;
    }

}
