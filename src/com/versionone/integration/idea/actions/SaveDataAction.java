/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.idea.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.versionone.common.sdk.DataLayerException;
import com.versionone.common.sdk.IDataLayer;
import com.versionone.common.sdk.ValidatorException;
import com.versionone.integration.idea.DetailsComponent;
import com.versionone.integration.idea.ValidationResultDialog;
import com.versionone.integration.idea.TasksComponent;
import org.apache.log4j.Logger;

import javax.swing.*;

/**
 *
 */
public class SaveDataAction extends AnAction {

    private static final Logger LOG = Logger.getLogger(SaveDataAction.class);

    private Project project;

    public void actionPerformed(AnActionEvent e) {
        System.out.println("Save.actionPerformed()");

        final DataContext dataContext = e.getDataContext();
        Project ideaProject = PlatformDataKeys.PROJECT.getData(dataContext);

        if (ideaProject == null && project == null) {
            return;
        } else if (ideaProject == null) {
            ideaProject = project;
        }

        final TasksComponent tc = ideaProject.getComponent(TasksComponent.class);
        final DetailsComponent dc = ideaProject.getComponent(DetailsComponent.class);
        final IDataLayer dataLayer = tc.getDataLayer();
        final ProgressManager progressManager = ProgressManager.getInstance();

        // is exception, text of exception, error or warning, display in standard or in custom dialog
        final Object[] isError = {false, "", false, false};
        tc.removeEdition();
        dc.removeEdition();

        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
            public void run() {
                progressManager.getProgressIndicator().setText("Saving changes to VersionOne");
                try {
                    dataLayer.commitChanges();
                } catch (DataLayerException ex) {
                    isError[0] = true;
                    isError[1] = ex.getMessage();
                    isError[2] = true; //ex.isError();
                    LOG.warn(isError[1], ex);
                } catch(ValidatorException ex) {
                    isError[0] = true;
                    isError[1] = ex.getMessage();
                    isError[2] = true;
                    isError[3] = true;
                    LOG.warn(isError[1], ex);
                } catch (Exception ex) {//TODO should we handle this exception?
                    isError[0] = true;
                    isError[1] = "Error connecting to VersionOne";
                    isError[2] = true;
                    LOG.warn(isError[1], ex);
                }
            }
        },
                "Save changes to VersionOne",
                false,
                ideaProject
        );

        if ((Boolean) isError[3]) {
            ValidationResultDialog dialogForRequiredFields = new ValidationResultDialog((String) isError[1]);
            dialogForRequiredFields.setVisible(true);
            return;
        }

        if ((Boolean) isError[0]) {
            Icon icon = (Boolean) isError[2] ? Messages.getErrorIcon() : Messages.getWarningIcon();
            Messages.showMessageDialog(isError[1].toString(), "Error", icon);
            return;
        }

        //TODO ActionManager.getInstance().getAction("V1.toolRefresh").actionPerformed(e);
        RefreshAction.refreshData(ideaProject, tc, dataLayer, dc, progressManager);
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
