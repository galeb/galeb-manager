/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2016 Globo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package io.galeb.manager.engine.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CounterDownLatch {

    public static final Map<String, Integer> mapOfDiffCounters =
                Collections.synchronizedMap(new HashMap<>());

    public static int decrementDiffCounter(String key) {
        int oldValue = -1;
        if (mapOfDiffCounters.containsKey(key)) {
            oldValue = mapOfDiffCounters.get(key);
            if (oldValue > 0 ) {
                mapOfDiffCounters.put(key, --oldValue);
            } else {
                mapOfDiffCounters.put(key, 0);
                return 0;
            }
        }
        return oldValue;
    }

    private CounterDownLatch() {
        // singleton?
    }
}
