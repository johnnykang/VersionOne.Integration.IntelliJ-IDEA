/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.idea.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.versionone.common.sdk.DataLayerException;
import com.versionone.common.sdk.IDataLayer;
import com.versionone.integration.idea.DetailsComponent;
import com.versionone.integration.idea.TasksComponent;

import javax.swing.*;

import org.apache.log4j.Logger;

public class RefreshAction extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(RefreshAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project ideaProject = resolveProject(e);

        if (ideaProject == null) {
            return;
        }
        final TasksComponent tc = resolveTasksComponent(ideaProject);
        final IDataLayer dataLayer = tc.getDataLayer();
        
        if (dataLayer.hasChanges()) {
            int confirmRespond = Messages.showDialog("You have pending changes that will be overwritten.\nDo you want to continue?", "Refresh Warning", new String[]{"Yes", "No"}, 1, Messages.getQuestionIcon());
            if (confirmRespond == 1) {
                return;
            }
        }
        final DetailsComponent dc = resolveDetailsComponent(ideaProject);
        final ProgressManager progressManager = ProgressManager.getInstance();
        refreshData(ideaProject, tc, dataLayer, dc, progressManager);
    }

    static void refreshData(Project ideaProject, TasksComponent tc, final IDataLayer dataLayer, DetailsComponent dc, final ProgressManager progressManager) {
        final Exception[] isError = {null};
        tc.removeEdition();
        dc.removeEdition();

        ProgressManager.getInstance().runProcessWithProgressSynchronously(
                new Runnable() {
                    public void run() {
                        progressManager.getProgressIndicator().setText("Updating VersionOne Task List");
                        try {
                            dataLayer.reconnect();
                        } catch (DataLayerException ex) {
                            LOG.warn("Failed to refresh workitems.", ex);
                            isError[0] = ex;
                        }
                    }
                },
                "VersionOne Workitem List Refreshing",
                false,
                ideaProject
        );

        if (isError[0] != null) {
            Icon icon = Messages.getErrorIcon();
            Messages.showMessageDialog(isError[0].getMessage(), "Error", icon);
            return;
        }

        tc.refresh();
        tc.update();
        dc.setItem(tc.getCurrentItem());
        dc.update();
    }
}
