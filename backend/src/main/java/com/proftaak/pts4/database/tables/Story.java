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
@DatabaseTable(tableName = "userstories")
public class Story {
	/**
	 * The database id of this userstory
	 */
	@DatabaseField(generatedId = true)
	private int id;

    /**
     * The name of this userstory
     */
    @DatabaseField(canBeNull = false)
    private String name;

	/**
	 * The description of this userstory
	 */
	@DatabaseField()
	private String description;

    /**
     * The status of this userstory
     */
    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING)
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
     * @return The DAO for this table
     */
    public static Dao<Story, Integer> getDao() throws FileNotFoundException, SQLException {
        return DaoManager.createDao(DBUtils.getConnectionSource(), Story.class);
    }
}