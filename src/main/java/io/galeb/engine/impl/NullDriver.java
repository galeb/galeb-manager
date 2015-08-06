package io.galeb.engine.impl;

import io.galeb.engine.Driver;

public class NullDriver implements Driver {

    @Override
    public String toString() {
        return DEFAULT_DRIVER_NAME;
    }

}
