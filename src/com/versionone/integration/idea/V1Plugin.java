/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.idea;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.UIUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;

public class V1Plugin implements ProjectComponent, PersistentStateComponent<V1Plugin.Config> {


    private final String[] columnNames = {  "First Name",
                                            "Last Name",
                                            "Sport",
                                            "# of Years",
                                            "Vegetarian"};

    private static final int IDEA_VERSION = 7941;
    private static final boolean IDEA8 = IDEA_VERSION > 7941;

    private static final Logger LOG = Logger.getLogger(V1Plugin.class);
    @NonNls public static final String TOOL_WINDOW_ID = "V1Integration";


    private final Project project;

    private ToolWindow toolWindow;
    private JPanel contentPanel;
    private Config cfg = new Config();


    public V1Plugin(Project project) {
        this.project = project;
    }

    public void projectOpened() {
        String ideaVersion = ApplicationInfo.getInstance().getMajorVersion();
        System.out.println("IDEA version = " + ideaVersion);
        initToolWindow();
    }

    public void projectClosed() {
        unregisterToolWindow();
    }

    public void initComponent() {
        // empty
    }

    public void disposeComponent() {
        // empty
    }

    public String getComponentName() {
        return "V1.ToolWindow";
    }

    private void initToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        contentPanel = createContentPanel();
        toolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.LEFT);
        ContentFactory contentFactory;
//        contentFactory = ContentFactory.SERVICE.getInstance();
        contentFactory = PeerFactory.getInstance().getContentFactory();
        Content content = contentFactory.createContent(contentPanel, "<Project>", false);
        toolWindow.getContentManager().addContent(content);
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(UIUtil.getTreeTextBackground());
        panel.add(new JLabel("Hello World!", JLabel.CENTER), BorderLayout.CENTER);
        JTable table = creatingTable();
        //panel.add(creatingTable());

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane);

        ActionGroup actions = (ActionGroup) ActionManager.getInstance().getAction("V1.ToolWindow");
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("V1.ToolWindow", actions, false);

        panel.add(toolbar.getComponent(), BorderLayout.LINE_START);
        return panel;
    }


    private JTable creatingTable() {


        Object[][] data = {
            {"Mary", "Campione",
             "Snowboarding", new Integer(5), new Boolean(false)},
            {"Alison", "Huml",
             "Rowing", new Integer(3), new Boolean(true)},
            {"Kathy", "Walrath",
             "Knitting", new Integer(2), new Boolean(false)},
            {"Sharon", "Zakhour",
             "Speed reading", new Integer(20), new Boolean(true)},
            {"Philip", "Milne",
             "Pool", new Integer(10), new Boolean(false)}
        };

        JTable table = new JTable(data, columnNames);

        //JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        //container.setLayout();
        //container.add(table.getTableHeader(), BorderLayout.PAGE_START);
        //container.add(table, BorderLayout.CENTER);



        return table;

    }



    private void unregisterToolWindow() {
        ToolWindowManager.getInstance(project).unregisterToolWindow(TOOL_WINDOW_ID);
    }

    /**
     * Temporary method for testing purposes.
     */
    public static void main(String[] args) {
        V1Plugin plugin = new V1Plugin(null);
        JPanel panel = plugin.createContentPanel();
        JFrame frame = new JFrame("IDEA V1 Plugin");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 100));
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public Config getState() {
        return cfg;
    }

    public void loadState(Config state) {
        cfg = state;
    }

    public static class Config {
        public String user, passwd;
    }
}
