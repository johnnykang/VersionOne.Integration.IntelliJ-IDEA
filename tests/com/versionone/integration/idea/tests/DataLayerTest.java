/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.idea.tests;

import com.versionone.common.sdk.APIDataLayer;
import com.versionone.common.sdk.IDataLayer;
import com.versionone.common.sdk.ProjectTreeNode;
import com.versionone.common.sdk.TasksProperties;
import com.versionone.integration.idea.WorkspaceSettings;
import com.versionone.integration.idea.V1PluginException;
import com.versionone.om.V1Instance;
import com.versionone.om.Project;
import com.versionone.om.Story;
import com.versionone.om.Task;
import com.versionone.om.Iteration;
import com.versionone.om.Schedule;
import com.versionone.om.filters.TaskFilter;
import com.versionone.DB;
import com.versionone.Duration;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.Enumeration;
import java.util.Date;
import java.util.Collection;
import java.math.BigDecimal;

public class DataLayerTest {
    private IDataLayer data;
    final WorkspaceSettings settings = new WorkspaceSettings();
    private String SCOPE_ZERO = "Scope:0";

    @Before
    public void before() {
        settings.v1Path = "http://eval.versionone.net/ExigenTest/";
        //settings.v1Path = "http://jsdksrv01/VersionOne/";
        settings.user = "admin";
        settings.passwd = "admin";
        settings.projectName = "System (All Projects)";
        data = new APIDataLayer(settings);
    }

//    /**
//     * Integrational test. Need access to V1 server.
//     */
//    @Test
//    public void testGetMainData() {
//        int list = data.getTasksCount();
//        for (int i = 0; i < list; i++) {
//            System.out.print("* ");
//            for (TasksProperties property : TasksProperties.values()) {
//                System.out.print(data.getTaskPropertyValue(i, property) + "\t|");
//            }
//            System.out.println();
//        }
//    }

    /**
     * Integration test. Need access to V1 server.
     * @throws com.versionone.integration.idea.V1PluginException
     */
    @Test
    public void testGetProjects() throws V1PluginException {
        ProjectTreeNode projects = data.getProjects();

        //for(Project project : projects) {

        System.out.println(projects.toString() + " --- " + projects.getToken() + " child:" + projects.getChildCount());
        displayAllProjects(projects.children(), 0);

        //}
    }

    private void displayAllProjects(Enumeration projectTreeNodes, int pos) {
        while (projectTreeNodes.hasMoreElements()) {
            ProjectTreeNode project = (ProjectTreeNode) projectTreeNodes.nextElement();
            System.out.print(pos);
            for (int i = 0; i < pos * 2; i++) {
                System.out.print("-");
            }

            if (project.getParent() != null) {
                System.out.println(project.toString() + " --- " + project.getToken() + " child:" + project.getChildCount() + " data:" + ((ProjectTreeNode) project.getParent()).getToken());
            }
            if (project.getAllowsChildren()) {
                displayAllProjects(project.children(), pos + 1);
            }
        }
    }


    /**
     * Integrational test. Need access to V1 server.
     * @throws Exception if any error occure
     */
    @Test
    public void testGettingData() throws Exception {
        Project project = null;
        Story story = null;
        Task task = null;
        Schedule schedule = null;
        Iteration iteration = null;
        final String addName = "getting";
        final String projectName = "Project " + addName + " " + new Date().toString();
        final String storyName = "Story " + addName + " " + new Date().toString();
        final String taskName = "Task " + addName + " " + new Date().toString();
        final String taskDesc = "Description " + addName + " " + new Date().toString();
        final String itarationName = "Iteration for test " + addName;
        final String scheduleName = "Schedule " + addName;
        final Double toDO = 5D;
        final Double detailEstimate = 5D;
        final String status = "In Progress";

        try {
            final V1Instance v1 = new V1Instance(settings.v1Path, settings.user, settings.passwd);
            schedule = v1.create().schedule(scheduleName, new Duration(14, Duration.Unit.Days), new Duration(0, Duration.Unit.Days) );

            final Project rootProject = v1.get().projectByID(SCOPE_ZERO);
            project = rootProject.createSubProject(projectName, DB.DateTime.now());
            project.setSchedule(schedule);
            project.save();
            
            final Date dateStart = new Date();
            final Date dateEnd = new Date();
            dateEnd.setTime(new Date().getTime() + 100*60*60*24*31);
            iteration = project.createIteration(itarationName, new DB.DateTime(dateStart), new DB.DateTime(dateEnd));
            iteration.activate();
            iteration.save();

            story = project.createStory(storyName);
            story.setIteration(iteration);
            story.save();

            task = story.createTask(taskName);
            task.setToDo(toDO);
            task.setDetailEstimate(detailEstimate);
            task.setDescription(taskDesc);
            task.getStatus().setCurrentValue(status);
            task.save();

            settings.projectToken = project.getID().toString();
            data = new APIDataLayer(settings);

            Assert.assertEquals(1, data.getTasksCount());
            Assert.assertEquals(taskDesc, data.getTaskPropertyValue(0, TasksProperties.DESCRIPTION));
            Assert.assertEquals(taskName, data.getTaskPropertyValue(0, TasksProperties.TITLE));
            Assert.assertEquals(storyName, data.getTaskPropertyValue(0, TasksProperties.PARENT));
            Assert.assertEquals(status, data.getTaskPropertyValue(0, TasksProperties.STATUS_NAME).toString());
            Assert.assertEquals(itarationName, data.getTaskPropertyValue(0, TasksProperties.SPRINT));
            Assert.assertEquals(toDO, Double.parseDouble(data.getTaskPropertyValue(0, TasksProperties.TO_DO).toString()), 0.0001);
            Assert.assertEquals(detailEstimate, Double.parseDouble(data.getTaskPropertyValue(0, TasksProperties.DETAIL_ESTIMATE).toString()), 0.0001);
        }
        finally {
            if (task != null) {
                task.delete();
            }

            if (story != null) {
                story.setIteration(null);
                story.delete();
            }

            if (project != null) {
                project.setSchedule(null);
                project.delete();
            }

            if (iteration != null) {
                iteration.delete();
            }

            if (schedule != null) {
                schedule.delete();
            }

        }
    }



    /**
     * Integrational test. Need access to V1 server.
     * @throws Exception if any error occure
     */
    @Test
    public void testChangeData() throws Exception {
        Project project = null;
        Story story = null;
        Task task = null;
        Schedule schedule = null;
        Iteration iteration = null;
        final String addName = "changing";
        final String projectName = "Project " + addName + " " + new Date().toString();
        final String storyName = "Story " + addName + " " + new Date().toString();
        final String taskName = "Task " + addName + " " + new Date().toString();
        final String taskDesc = "Description " + addName + " " + new Date().toString();
        final String itarationName = "Iteration for test " + addName;
        final String scheduleName = "Schedule " + addName;
        final String status = "In Progress";
        final Double toDO = 5D;
        final Double detailEstimate = 5D;

        final String newTextInFileds = " this fields was changed";
        final String newStatus = "Completed";
        final BigDecimal effort = BigDecimal.valueOf(3.5);

        try {
            final V1Instance v1 = new V1Instance(settings.v1Path, settings.user, settings.passwd);
            schedule = v1.create().schedule(scheduleName, new Duration(14, Duration.Unit.Days), new Duration(0, Duration.Unit.Days) );

            final Project rootProject = v1.get().projectByID(SCOPE_ZERO);
            project = rootProject.createSubProject(projectName, DB.DateTime.now());
            project.setSchedule(schedule);
            project.save();

            final Date dateStart = new Date();
            final Date dateEnd = new Date();
            dateEnd.setTime(new Date().getTime() + 100*60*60*24*31);
            iteration = project.createIteration(itarationName, new DB.DateTime(dateStart), new DB.DateTime(dateEnd));
            iteration.activate();
            iteration.save();

            story = project.createStory(storyName);
            story.setIteration(iteration);
            story.save();

            task = story.createTask(taskName);
            task.setToDo(toDO);
            task.setDetailEstimate(detailEstimate);
            task.setDescription(taskDesc);
            task.getStatus().setCurrentValue(status);
            task.save();

            final String storyId = story.getID().toString();

            settings.projectToken = project.getID().toString();
            data = new APIDataLayer(settings);

            data.setTaskPropertyValue(0, TasksProperties.DESCRIPTION, taskDesc + newTextInFileds);
            data.setTaskPropertyValue(0, TasksProperties.TITLE, taskName + newTextInFileds);
            data.setTaskPropertyValue(0, TasksProperties.STATUS_NAME, newStatus);
            data.setTaskPropertyValue(0, TasksProperties.EFFORT, effort);
            data.commitChangedTaskData();
            data.refresh();

            Assert.assertEquals(1, data.getTasksCount());
            Assert.assertEquals(taskDesc + newTextInFileds, data.getTaskPropertyValue(0, TasksProperties.DESCRIPTION));
            Assert.assertEquals(taskName + newTextInFileds, data.getTaskPropertyValue(0, TasksProperties.TITLE));
            //Assert.assertEquals(newStatus, data.getTaskPropertyValue(0, TasksProperties.STATUS));
            Assert.assertEquals(newStatus, data.getTaskPropertyValue(0, TasksProperties.STATUS_NAME));
            //Assert.assertEquals(effort, data.getTaskPropertyValue(0, TasksProperties.DONE));
            Assert.assertEquals(effort.doubleValue(), Double.parseDouble(data.getTaskPropertyValue(0, TasksProperties.DONE).toString()), 0.0001);


            final V1Instance v1Test = new V1Instance(settings.v1Path, settings.user, settings.passwd);
            TaskFilter filter = new TaskFilter();
            filter.status.add(newStatus);
            Collection<Task> tasks  = v1Test.get().tasks(filter);
            Assert.assertEquals(1, tasks.size());
            Task taskTest = tasks.iterator().next();
            Assert.assertEquals(taskDesc + newTextInFileds, taskTest.getDescription());
            Assert.assertEquals(taskName + newTextInFileds, taskTest.getName());
            Assert.assertEquals(newStatus, taskTest.getStatus().getCurrentValue());
            Assert.assertEquals(effort.doubleValue(), taskTest.getDone(), 0.0001);


        }
        finally {
            if (task != null) {
                task.delete();
            }

            if (story != null) {
                story.setIteration(null);
                story.delete();
            }

            if (project != null) {
                project.setSchedule(null);
                project.delete();
            }

            if (iteration != null) {
                iteration.delete();
            }

            if (schedule != null) {
                schedule.delete();
            }

        }
    }



    
//    @Test
//    public void sdkTest() {
//        WorkspaceSettings cfg = WorkspaceSettings.getInstance();
//        V1Instance v1 = new V1Instance(cfg.v1Path, cfg.user, cfg.passwd);
//        Project project = v1.get().projectByName(cfg.projectName);
//        final Collection<Project> childProjects = project.getThisAndAllChildProjects();
//        final TaskFilter filter = new TaskFilter();
//
//        for (Project prj : childProjects) {
//            if (prj.isActive()) {
//                filter.project.add(prj);
//                break;
//            }
//        }
//        filter.getState().add(BaseAssetFilter.State.Active);
//        Collection<Task> tasks = v1.get().tasks(filter);
//
//        for (Task task : tasks) {
//
//            for (int i=0; i<100;i++){
//                String name = task.getName();
//
//                System.out.println(name);
//            }
//        }
//    }

    /**
     * Temporary method for testing purposes. TODO delete
     */
/*
    public static void main(String[] args) throws ConnectException {
        TasksComponent plugin = new TasksComponent(null);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JTree(DataLayer.getInstance().getProjects()));
        JFrame frame = new JFrame("IDEA V1 Plugin");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(200, 800));
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }
*/
}
