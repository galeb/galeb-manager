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

package io.galeb.manager.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

public class Properties {

    private Map<String, Object> properties = new HashMap<>();

    public Object put(String key, Object value) {
        return properties.put(key, value);
    }

    public Object getOrDefault(String key, final Object obj) {
        Object value = properties.get(key);
        return value == null ? obj : value;
    }

    public int getOrDefault(String key, Integer def) {
        Object value = properties.get(key);
        return value == null ? def : Integer.parseInt(String.valueOf(value));
    }

    public Object remove(String key) {
        return properties.remove(key);
    }

    public void clear() {
        properties.clear();
    }

    public Set<String> keySet() {
        return properties.keySet();
    }

    public Collection<Object> values() {
        return properties.values();
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public boolean containKey(String key) {
        return properties.containsKey(key);
    }

    public boolean containValue(Object obj) {
        return properties.containsValue(obj);
    }

    public Stream<Entry<String, Object>> stream() {
        return properties.entrySet().stream();
    }

}
