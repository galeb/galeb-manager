package io.galeb.manager.engine.impl;

import io.galeb.manager.engine.Driver;

public class NullDriver implements Driver {

    @Override
    public String toString() {
        return DEFAULT_DRIVER_NAME;
    }

}
