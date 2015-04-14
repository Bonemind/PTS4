package com.proftaak.pts4.core.rest;

import java.util.HashMap;

/**
 * @author Michon
 */
public class Payload extends HashMap<String, Object> {
    public Payload(HashMap<String, Object> source) {
        super(source);
    }

    public String getString(String key) {
        return (String) this.get(key);
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
