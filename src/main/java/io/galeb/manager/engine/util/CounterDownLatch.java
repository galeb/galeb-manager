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
import java.util.Objects;

import static java.lang.System.currentTimeMillis;

public class CounterDownLatch {

    private static final Map<String, Map.Entry<Long, Integer>> mapOfDiffCounters =
                Collections.synchronizedMap(new HashMap<>());

    private CounterDownLatch() {
        // singleton?
    }

    public static synchronized int decrementDiffCounter(String key) {
        int oldValue = -1;
        if (refreshAndCheckContainsKey(key)) {
           final Map.Entry entry = mapOfDiffCounters.get(key);
            oldValue = (int) entry.getValue();
            if (oldValue > 0 ) {
                put(key, --oldValue);
            } else {
                oldValue = reset(key);
            }
        }
        return oldValue;
    }

    public static synchronized Integer refreshAndGet(String key) {
        final Map.Entry entry = mapOfDiffCounters.get(key);
        if (entry != null) {
            int value = (Integer) entry.getValue();
            put(key, value);
            return value;
        }
        return null;
    }

    public static synchronized Integer put(String key, Integer value) {
        final Map.Entry entry = mapOfDiffCounters.put(key, new KV(currentTimeMillis(), value));
        return (Integer) (entry != null ? entry.getValue() : null);
    }

    public static synchronized Integer remove(String key) {
        final Map.Entry entry = mapOfDiffCounters.remove(key);
        return (Integer) (entry != null ? entry.getValue() : null);
    }

    public static synchronized boolean refreshAndCheckContainsKey(String key) {
        return mapOfDiffCounters.containsKey(key);
    }

    public static synchronized int reset(String key) {
        put(key, 0);
        return 0;
    }

    private static class KV implements Map.Entry<Long, Integer> {

        private final long timestamp;
        private int value;

        public KV(Long timestamp, Integer value ) {
            this.value = value;
            this.timestamp = timestamp;
        }

        @Override
        public Long getKey() {
            return timestamp;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public Integer setValue(Integer value) {
            int oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            KV kv = (KV) o;
            return timestamp == kv.timestamp &&
                   value == kv.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp, value);
        }
    }
}
