package com.proftaak.pts4.database;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.proftaak.pts4.rest.HTTPException;

/**
 * Created by Michon on 31-3-2015.
 */
public class EbeanEx {
    /**
     * Like Ebean.find, but safer.
     * <p>
     * Will return null if no object is found.
     */
    public static <T> T find(Class<T> cls, Object pk) throws HTTPException {
        // If no primary key, return null.
        if (pk == null) {
            return null;
        }

        // The primary key is given, so get the object.
        T obj = Ebean.find(cls, pk);

        // The primary key is given, so require the object to be non-null.
        if (obj == null) {
            throw HTTPException.ERROR_OBJECT_NOT_FOUND;
        }
        return obj;
    }

    /**
     * Lookup one object by a given field.
     * <p>
     * Will return null if no object is found, or if multiple objects are found.
     */
    public static <T> T find(Class<T> cls, String field, Object value) {
        return EbeanEx.find(Ebean.find(cls).where().eq(field, value).query());
    }

    /**
     * Like Query.findUnique, but safer.
     * <p>
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
