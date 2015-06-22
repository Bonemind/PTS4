package com.proftaak.pts4.database.tables;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.rest.HTTPException;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.util.HttpStatus;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Created by Michon on 25-5-2015.
 */
@Embeddable
public class KanbanRules {
    public static final String FIELD_MAX_IN_PROGRESS = "max_in_progress";
    public static final String FIELD_MAX_DONE = "max_done";

    /**
     * The maximum number of stories that can be IN_PROGRESS at the same time
     */
    @Column(name = FIELD_MAX_IN_PROGRESS)
    private Integer maxInProgress;

    /**
     * The maximum number of stories that can be DONE at the same time
     */
    @Column(name = FIELD_MAX_DONE)
    private Integer maxDone;

    public KanbanRules() {
    }

    public Integer getMaxInProgress() {
        return this.maxInProgress;
    }

    public void setMaxInProgress(Integer maxInProgress) {
        this.maxInProgress = maxInProgress;
    }

    public Integer getMaxDone() {
        return this.maxDone;
    }

    public void setMaxDone(Integer maxDone) {
        this.maxDone = maxDone;
    }

    /**
     * Check whether there is room for a story in it's status column
     *
     * @param story The story to check
     * @throws com.proftaak.pts4.rest.HTTPException If there is no room
     */
    public void enforceRoomFor(Story story) throws HTTPException {
        // Determine the max for the status
        Integer max = null;
        switch (story.getStatus()) {
            case IN_PROGRESS:
                max = this.getMaxInProgress();
                break;

            case DONE:
                max = this.getMaxDone();
                break;

            default:
                break;
        }

        // Enforce the max, if any
        if (max != null) {
            int inStatus = Ebean.find(Story.class)
                .where()
                .ne(Story.FIELD_ID, story.getId())
                .eq(Story.FIELD_STATUS, story.getStatus())
                .in(Story.FIELD_PROJECT, Ebean.find(Project.class)
                        .select(Project.FIELD_ID)
                        .where()
                        .eq(Project.FIELD_TEAM, story.getProject().getTeam().getPK())
                        .query()
                )
                .findRowCount();

            if (inStatus >= max) {
                throw new HTTPException(String.format(
                    "There are already %d stories in %s, no more are allowed",
                    inStatus, StringUtils.capitalize(story.getStatus().toString())
                ), HttpStatus.CONFLICT_409);
            }
        }
    }
}
