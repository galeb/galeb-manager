package io.galeb.engine;

import java.util.HashMap;
import java.util.Map;

import io.galeb.engine.impl.NullProvisioning;

public class ProvisioningBuilder {

    private static Map<String, Provisioning> provisionings = new HashMap<>();
    static {
        provisionings.put(Provisioning.DEFAULT_PROVISIONING_NAME, new NullProvisioning());
    }

    public static Provisioning build(String provisioningName) {
        final Provisioning provisioning = provisionings.get(provisioningName);
        if (provisioning==null) {
            return provisionings.get(Provisioning.DEFAULT_PROVISIONING_NAME);
        }
        return provisioning;
    }

}
