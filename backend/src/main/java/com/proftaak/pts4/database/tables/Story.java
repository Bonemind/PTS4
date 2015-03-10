package com.proftaak.pts4.database.tables;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.proftaak.pts4.database.DBUtils;
import com.proftaak.pts4.database.SprintStatus;

import java.io.FileNotFoundException;
import java.sql.SQLException;

/**
 * @author Michon
 */
@DatabaseTable(tableName = "stories")
public class Story {

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_STATUS = "status";

    /**
     * The database id of this userstory
     */
    @DatabaseField(generatedId = true, columnName = FIELD_ID)
    private int id;

    /**
     * The name of this userstory
     */
    @DatabaseField(canBeNull = false, columnName = FIELD_NAME)
    private String name;

    /**
     * The description of this userstory
     */
    @DatabaseField(columnName = FIELD_DESCRIPTION)
    private String description;

    /**
     * The status of this userstory
     */
    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = FIELD_STATUS)
    private SprintStatus status;

    /**
     * ORM-Lite no-arg constructor
     */
    public Story() {
    }

    public Story(String name) {
        this(name, null);
    }

    public Story(String name, String description) {
        this(name, description, SprintStatus.DEFINED);
    }

    public Story(String name, String description, SprintStatus sprintStatus) {
        this.setName(name);
        this.setDescription(description);
        this.setStatus(sprintStatus);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SprintStatus getStatus() {
        return status;
    }

    public void setStatus(SprintStatus sprintStatus) {
        this.status = sprintStatus;
    }

    /**
     * Get the DAO for this table
     *
     * @return The DAO for this table
     */
    public static Dao<Story, Integer> getDao() throws FileNotFoundException, SQLException {
        return DaoManager.createDao(DBUtils.getConnectionSource(), Story.class);
    }
}