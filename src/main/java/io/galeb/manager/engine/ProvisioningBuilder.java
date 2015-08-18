/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2015 Globo.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.galeb.manager.engine;

import java.util.HashMap;
import java.util.Map;

import io.galeb.manager.engine.impl.NullProvisioning;

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
