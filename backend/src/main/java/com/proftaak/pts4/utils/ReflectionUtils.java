package com.proftaak.pts4.utils;

import java.lang.reflect.Field;

/**
 * Created by Michon on 25-5-2015.
 */
public class ReflectionUtils {
    /**
     * Get the value of a named field of an object, ignoring access levels
     *
     * @param cls The class in which the field is defined
     * @param key The name of the field
     * @param obj The object to get the field value of
     * @return The value of the field key of object obj
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static <T> Object getFieldValue(Class<T> cls, String key, T obj) throws NoSuchFieldException, IllegalAccessException {
        Field field = cls.getDeclaredField(key);
        field.setAccessible(true);
        return field.get(obj);
    }

    /**
     * Set the value of a named field of an object, ignoring access levels
     *
     * @param cls   The class in which the field is defined
     * @param key   The name of the field
     * @param obj   The object to set the field value of
     * @param value The value to set the field to
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static <T> void setFieldValue(Class<T> cls, String key, T obj, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = cls.getDeclaredField(key);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
