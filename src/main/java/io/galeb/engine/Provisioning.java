package io.galeb.engine;

import io.galeb.manager.common.Properties;

public interface Provisioning {

    public static final String DEFAULT_PROVISIONING_NAME = "NULL";

    default boolean create(Properties properties) {
        return true;
    }

    default boolean remove(Properties properties) {
        return true;
    }
}
