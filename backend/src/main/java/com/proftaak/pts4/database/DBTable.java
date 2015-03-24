package com.proftaak.pts4.database;

/**
 * @author Michon
 */
public class DBTable {
    public boolean equals(DBTable that) {
        return this.getClass().equals(that.getClass()) && this.hashCode() == that.hashCode();
    }
}
