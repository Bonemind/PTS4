package com.proftaak.pts4.database.tables;

import com.proftaak.pts4.core.flexjson.ToStringTransformer;
import flexjson.JSON;

import javax.jws.soap.SOAPBinding;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Michon
 */
@Entity
@Table(name = "taskprogress")
public class TaskProgress {
    public static final String FIELD_ID = "id";
    public static final String FIELD_TASK = "task_id";
    public static final String FIELD_USER = "user_id";
    public static final String FIELD_EFFORT = "effort";
    public static final String FIELD_TODO = "todo";
    public static final String FIELD_TIMESTAMP = "timestamp";

    /**
     * The database id of this task
     */
    @Id
    @Column(name = FIELD_ID)
    private int id;

    /**
     * The task that this work was for
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_TASK)
    private Task task;

    /**
     * The user that did this work
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_USER)
    private User user;

    /**
     * The amount of work that was done
     */
    @Column(name = FIELD_EFFORT, nullable = false)
    private double effort;

    /**
     * The remaining to-do after this work
     */
    @Column(name = FIELD_TODO, nullable = false)
    private double todo;

    /**
     * When this work was done
     */
    @JSON(transformer = ToStringTransformer.class)
    @Column(name = FIELD_TIMESTAMP, nullable = false)
    private LocalDateTime timestamp;

    public TaskProgress() {

    }

    public TaskProgress(Task task, User user, double effort, double todo) {
        this.task = task;
        this.user = user;
        this.effort = effort;
        this.timestamp = LocalDateTime.now();

        task.getProgress().add(this);
        if (todo > 0) {
            // Todo is set, so set the todo of the task as well.
            this.todo = todo;
            task.setTodo(todo);
        } else {
            // No todo set, let the task determine the todo.
            this.todo = task.getTodo();
        }
    }

    public int getId() {
        return this.id;
    }

    public Task getTask() {
        return task;
    }

    public User getUser() {
        return this.user;
    }

    public double getEffort() {
        return this.effort;
    }

    public double getTodo() {
        return this.todo;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }
}
