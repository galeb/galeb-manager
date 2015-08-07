package io.galeb.engine;

import java.util.HashMap;
import java.util.Map;

import io.galeb.engine.impl.GalebV3Driver;
import io.galeb.engine.impl.NullDriver;

public class DriverBuilder {

    private static Map<String, Driver> drivers = new HashMap<>();
    static {
        drivers.put(Driver.DEFAULT_DRIVER_NAME, new NullDriver());
        drivers.put(GalebV3Driver.DRIVER_NAME, new GalebV3Driver());
    }

    public static Driver build(String driverName) {
        final Driver driver = drivers.get(driverName);
        if (driver == null) {
            return drivers.get(Driver.DEFAULT_DRIVER_NAME);
        }
        return driver;
    }

}
