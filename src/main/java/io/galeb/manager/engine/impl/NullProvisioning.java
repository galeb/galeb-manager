package io.galeb.manager.engine.impl;

import io.galeb.manager.engine.Provisioning;

public class NullProvisioning implements Provisioning {

    @Override
    public String toString() {
        return DEFAULT_PROVISIONING_NAME;
    }

}
