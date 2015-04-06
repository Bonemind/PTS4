package com.proftaak.pts4.database;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.proftaak.pts4.core.rest.HTTPException;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Created by Michon on 31-3-2015.
 */
public class EbeanEx {
    /**
     * Like Ebean.find, but safer.
     *
     * Will return null if no object is found.
     */
    public static <T> T find(Class<T> cls, Object pk) {
        T obj = null;
        try {
            obj = Ebean.find(cls, pk);
        } catch (NullPointerException e) {
        }
        return obj;
    }

    /**
     * Lookup one object by a given field.
     *
     * Will return null if no object is found, or if multiple objects are found.
     */
    public static <T> T find(Class<T> cls, String field, Object value) {
        return EbeanEx.find(Ebean.find(cls).where().eq(field, value).query());
    }

    /**
     * Like Query.findUnique, but safer.
     *
     * Will return null if no object is found, or if multiple objects are found.
     */
    public static <T> T find(Query<T> query) {
        T obj = null;
        try {
            obj = query.findUnique();
        } catch (NullPointerException e) {
        }
        return obj;
    }

    /**
     * If the argument obj is null, throw HTTPException.ERROR_OBJECT_NOT_FOUND. Else, simply return the object.
     */
    public static <T> T require(T obj) throws HTTPException {
        if (obj == null) {
            throw HTTPException.ERROR_OBJECT_NOT_FOUND;
        }
        return obj;
    }
}
