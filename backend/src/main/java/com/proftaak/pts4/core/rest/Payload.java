package com.proftaak.pts4.core.rest;

import java.util.HashMap;
import java.util.Objects;

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
        Object value = this.get(key);
        if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        return -1;
    }

    public Integer getInt(String key, Integer def) {
        Object value = this.getOrDefault(key, def);
        if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        return def;
    }

    public Double getDouble(String key) {
        Object value = this.get(key);
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Double) {
            return (Double) value;
        }
        return -1d;
    }

    public Double getDouble(String key, Double def) {
        Object value = this.getOrDefault(key, def);
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Double) {
            return (Double) value;
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
