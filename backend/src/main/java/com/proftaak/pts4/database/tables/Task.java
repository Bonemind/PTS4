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
@DatabaseTable(tableName = "tasks")
public class Task {

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_STORY = "story";

    /**
     * The database id of this task
     */
    @DatabaseField(generatedId = true, columnName = FIELD_ID)
    private int id;

    /**
     * The name of this task
     */
    @DatabaseField(canBeNull = false, columnName = FIELD_NAME)
    private String name;

    /**
     * The description of this task
     */
    @DatabaseField(columnName = FIELD_DESCRIPTION)
    private String description;

    /**
     * The status of this task
     */
    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = FIELD_STATUS)
    private SprintStatus status;

    /**
     * The user story of this task.
     */
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = FIELD_STORY)
    private Story story;

	/**
	 * ORM-Lite no-arg constructor
	 */
	public Task() {
	}

    public Task(Story story, String name) {
        this(story, name, null);
    }
    public Task(Story story, String name, String description) {
        this(story, name, description, SprintStatus.DEFINED);
    }
    public Task(Story story, String name, String description, SprintStatus sprintStatus) {
        this.story = story;
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

    public Story getStory() {
        return story;
    }

    /**
     * Get the DAO for this table
     * @return The DAO for this table
     */
    public static Dao<Task, Integer> getDao() throws FileNotFoundException, SQLException {
        return DaoManager.createDao(DBUtils.getConnectionSource(), Task.class);
    }
}