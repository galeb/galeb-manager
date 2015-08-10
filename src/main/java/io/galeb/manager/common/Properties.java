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
