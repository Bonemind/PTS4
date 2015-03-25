package com.proftaak.pts4.core.restlet;

/**
 * @author Michon
 */
public enum ScopeRole {
    /**
     * This role will be present if the user is logged in
     */
    USER,

    /**
     * This role will be present if the user has one of the following roles:
     * DEVELOPER
     * SCRUM_MASTER
     * PRODUCT_OWNER
     */
    TEAM_MEMBER,

    /**
     * This role will be present if the user is a member of the currently relevant team
     * This role will also be present if the user has the SCRUM_MASTER role
     *
     * If there is no currently relevant team, this role will not be present
     */
    DEVELOPER,

    /**
     * This role will be present if the user is the scrum master of the currently relevant team
     *
     * If there is no currently relevant team, this role will not be present
     */
    SCRUM_MASTER,

    /**
     * This role will be present if the user is the product owner of the currently relevant project
     *
     * If there is no currently relevant project, this role will not be present
     */
    PRODUCT_OWNER;
}
