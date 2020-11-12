package org.chtijbug.drools.console.vaadincomponent.componentview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.chtijbug.drools.console.service.ProjectPersistService;
import org.chtijbug.drools.console.service.util.AppContext;
import org.chtijbug.drools.console.view.DeploymentView;
import org.chtijbug.drools.proxy.persistence.model.ProjectPersist;
import org.chtijbug.drools.proxy.persistence.model.RuntimePersist;

import java.util.ArrayList;
import java.util.List;

public class AssociateProjectKie extends VerticalLayout {

    private Label label;

    private Label label2;

    private GridRuntime gridRuntime;

    private Button associer;

    private transient ProjectPersistService projectPersistService;

    public AssociateProjectKie(DeploymentView deploymentView, Dialog dialog, ProjectPersist projectPersist) {

        projectPersistService = AppContext.getApplicationContext().getBean(ProjectPersistService.class);

        setClassName("creation-runtime-content");

        label = new Label("Define the runtime(s) for the project : " + projectPersist.getProjectName());
        label.setClassName("creation-runtime-title");
        add(label);

        label2 = new Label("This step is essential to be able to execute the rules defined in the project");
        label2.setClassName("creation-runtime-title2");
        add(label2);

        gridRuntime = new GridRuntime(projectPersist);
        add(gridRuntime);

        associer = new Button("Link and Save");
        associer.setEnabled(false);
        associer.setClassName("login-application-connexion");
        add(associer);
        associer.addClickListener(buttonClickEvent -> {
            List<RuntimePersist> lstToSave = new ArrayList<>();
            List<RuntimePersist> lstToDelete = new ArrayList<>();
            for (RuntimePersist runtimePersist : gridRuntime.getSelectedItems()) {
                lstToSave.add(runtimePersist);
            }
            for (RuntimePersist runtime : gridRuntime.getRuntimeSelected()){
                boolean found=false;
                for (RuntimePersist runtimePersist : gridRuntime.getSelectedItems()) {
                    if (runtimePersist.getServerName().equals(runtime.getServerName())
                        && runtimePersist.getServerPort().equals(runtime.getServerPort())){
                        found=true;
                    }

                }
                if (!found){
                    lstToDelete.add(runtime);
                }
            }
            if (!lstToDelete.isEmpty()){
                projectPersistService.removeAssociation(projectPersist,lstToDelete);
            }
            if (!lstToSave.isEmpty()) {

                projectPersistService.associate(projectPersist, lstToSave);

            }
            if (!lstToDelete.isEmpty() || !lstToSave.isEmpty()){
                deploymentView.setDataProvider();
                dialog.close();
            }
        });

        gridRuntime.addSelectionListener(selectionEvent -> {
            associer.setEnabled(selectionEvent.getFirstSelectedItem().isPresent());
        });


    }
}
