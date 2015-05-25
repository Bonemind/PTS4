package com.proftaak.pts4.database;

import flexjson.JSON;

/**
 * Created by Michon on 13-4-2015.
 */
public interface DatabaseModel<T> {
    /**
     * Get the primary key of this object
     *
     * @return The primary key of this object
     */
    @JSON(include = false)
    T getPK();
}
