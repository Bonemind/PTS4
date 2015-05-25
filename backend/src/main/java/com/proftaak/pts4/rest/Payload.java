package com.proftaak.pts4.rest;

import com.proftaak.pts4.json.JSONSerializerFactory;
import flexjson.BeanAnalyzer;
import flexjson.BeanProperty;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michon
 */
public class Payload extends HashMap<String, Object> {
    public Payload(HashMap<String, Object> source) {
        super(source);
    }

    public <T> T getEmbeddable(Class<T> cls, String key) {
        // Get the data
        Map<String, Object> objData = (Map<String, Object>) this.get(key);
        if (objData == null) {
            return null;
        }

        // Fill any missing fields
        BeanAnalyzer analyzer = BeanAnalyzer.analyze(cls);
        for (BeanProperty property : analyzer.getProperties()) {
            if (!objData.containsKey(property.getJsonName())) {
                objData.put(property.getJsonName(), null);
            }
        }

        // Convert to object using FlexJSON
        JSONSerializer jsonSerializer = JSONSerializerFactory.createSerializer();
        JSONDeserializer<T> jsonDeserializer = new JSONDeserializer<>();
        return jsonDeserializer.deserialize(jsonSerializer.serialize(objData), cls);
    }

    public <T> T getEmbeddable(Class<T> cls, String key, T def) {
        if (!this.containsKey(key)) {
            return def;
        }

        return this.getEmbeddable(cls, key);
    }

    public String getString(String key) {
        return (String) this.getOrDefault(key, null);
    }

    public String getString(String key, String def) {
        return (String) this.getOrDefault(key, def);
    }

    public Integer getInt(String key) {
        return this.getInt(key, -1);
    }

    public Integer getInt(String key, Integer def) {
        Object value = this.getOrDefault(key, def);
        if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        return def;
    }

    public Double getDouble(String key) {
        return this.getDouble(key, -1.0);
    }

    public Double getDouble(String key, Double def) {
        Object value = this.getOrDefault(key, def);
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        return def;
    }

    public Boolean getBoolean(String key) {
        return (Boolean) this.get(key);
    }

    public Boolean getBoolean(String key, Boolean def) {
        return (Boolean) this.getOrDefault(key, def);
    }
}
