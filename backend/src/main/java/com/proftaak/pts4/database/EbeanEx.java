package com.proftaak.pts4.database;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.proftaak.pts4.rest.HTTPException;

import javax.persistence.JoinColumn;
import java.lang.reflect.Field;
import java.util.stream.Stream;

/**
 * Created by Michon on 31-3-2015.
 */
public class EbeanEx {
    /**
     * Like Ebean.find, but safer.
     * <p>
     * Will return null if no object is found.
     */
    public static <T extends IDatabaseModel> T find(Class<T> cls, Object pk) throws HTTPException {
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
    public static <T extends IDatabaseModel> T find(Class<T> cls, String field, Object value) {
        return EbeanEx.find(Ebean.find(cls).where().eq(field, value).query());
    }

    /**
     * Like Query.findUnique, but safer.
     * <p>
     * Will return null if no object is found, or if multiple objects are found.
     */
    public static <T extends IDatabaseModel> T find(Query<T> query) {
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
    public static <T extends IDatabaseModel> T require(T obj) throws HTTPException {
        if (obj == null) {
            throw HTTPException.ERROR_OBJECT_NOT_FOUND;
        }
        return obj;
    }

    private static <T extends IDatabaseModel, R extends IDatabaseModel> String getFieldNameForRelationship(Class<T> ownedCls, Class<R> owningCls) throws Exception {
        for (Field field : ownedCls.getDeclaredFields()) {
            JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
            if (joinColumnAnnotation != null && owningCls.isAssignableFrom(field.getType())) {
                return joinColumnAnnotation.name();
            }
        }
        throw new Exception(String.format(
                "Unable to detect the relationship describing how %s is owned by %s",
                ownedCls.getName(), owningCls.getName()
        ));
    }

    /**
     * Create a query that will find all models of type A that are owned by the given instances of type B
     *
     * @param ownedCls  The owned model class
     * @param owningCls The owning model class
     * @param owners    The owning instances
     * @param <T>       The owned model class
     * @param <R>       The owning model class
     * @return The query
     */
    @SafeVarargs
    public static <T extends IDatabaseModel, R extends IDatabaseModel> Query<T> queryBelongingTo(Class<T> ownedCls, Class<R> owningCls, R... owners) throws Exception {
        Query<T> query = Ebean.find(ownedCls);
        String fieldName = EbeanEx.getFieldNameForRelationship(ownedCls, owningCls);
        if (owners.length == 1) {
            query.where().eq(fieldName, owners[0].getPK());
        } else {
            query.where().in(fieldName, Stream.of(owners).map(IDatabaseModel::getPK));
        }
        return query;
    }

    /**
     * Create a query that will find all models of type A that are owned by the given instances of type B
     *
     * @param ownedCls  The owned model class
     * @param owningCls The owning model class
     * @param owners    The owning instances
     * @param <T>       The owned model class
     * @param <R>       The owning model class
     * @return The query
     */
    public static <T extends IDatabaseModel, R extends IDatabaseModel> Query<T> queryBelongingTo(Class<T> ownedCls, Class<R> owningCls, Query<R> owningQuery) throws Exception {
        Query<T> query = Ebean.find(ownedCls);
        String fieldName = EbeanEx.getFieldNameForRelationship(ownedCls, owningCls);
        query.where().in(fieldName, owningQuery.select("id"));
        return query;
    }
}
