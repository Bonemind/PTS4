package com.proftaak.pts4;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.DBUtils;
import com.proftaak.pts4.database.tables.Team;
import com.proftaak.pts4.imports.RallyImporter;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Michon on 22-6-2015.
 */
public class TestMain {
    public static void main(String[] args) throws Exception {
        // Prepare the database
        DBUtils.init();

        // Get the files
        Set<File> files = new TreeSet<>();
        files.add(new File("C:/Users/Michon/Downloads/Defects (1).xml"));
        files.add(new File("C:/Users/Michon/Downloads/Stories.xml"));
        files.add(new File("C:/Users/Michon/Downloads/Iterations.xml"));
        files.add(new File("C:/Users/Michon/Downloads/Tasks.xml"));

        // Get the team
        Team team = Ebean.find(Team.class, 1);

        // Import?
        RallyImporter.importRally(files, team);
    }
}
