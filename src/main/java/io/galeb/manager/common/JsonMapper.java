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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.galeb.manager.entity.AbstractEntity;

public class JsonMapper {

    private final JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(false);
    private ObjectNode node = jsonNodeFactory.objectNode();
    private ObjectMapper mapper = new ObjectMapper();

    public JsonMapper() {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public JsonMapper putString(String key, String value) {
        node.put(key, value);
        return this;
    }

    public JsonMapper putLong(String key, Long value) {
        node.put(key, value);
        return this;
    }

    public JsonMapper putDouble(String key, Double value) {
        node.put(key, value);
        return this;
    }

    public JsonMapper putInt(String key, Integer value) {
        node.put(key, value);
        return this;
    }

    public JsonMapper putFloat(String key, Float value) {
        node.put(key, value);
        return this;
    }

    public JsonMapper addToNode(String nodeName, String key, String value) {
        ObjectNode aNode = (ObjectNode) node.get(nodeName);
        if (aNode==null) {
            aNode = jsonNodeFactory.objectNode();
        }
        aNode.put(key, value);
        node.set(nodeName, aNode);
        return this;
    }

    public JsonMapper removeFromNode(String nodeName, String key, String value) {
        ObjectNode aNode = (ObjectNode) node.get(nodeName);
        if (aNode!=null) {
            aNode.remove(key);
            node.set(nodeName, aNode);
        }
        return this;
    }


    public void clear() {
        node = jsonNodeFactory.objectNode();
    }

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(node);
        } catch (JsonProcessingException ignore) {
            // NULL
        }
        return "{}";
    }

    public JsonMapper makeJson(AbstractEntity<?> entity) throws JsonProcessingException {

        JsonMapper json = new JsonMapper();
        json.putString("id", entity.getName());
        json.putLong("pk", entity.getId());
        json.putInt("version", entity.getHash());
        entity.getProperties().entrySet().stream().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            json.addToNode("properties", key, value);
        });

        return json;
    }

}
