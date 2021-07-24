package org.chtijbug.drools.console.vaadincomponent.leftMenu.Action;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.chtijbug.drools.console.middle.JwtService;
import org.chtijbug.drools.console.service.ProjectPersistService;
import org.chtijbug.drools.console.service.UserConnectedService;
import org.chtijbug.drools.console.service.util.AppContext;
import org.chtijbug.drools.console.vaadincomponent.componentperso.DialogPerso;
import org.chtijbug.drools.console.vaadincomponent.Squelette.SqueletteComposant;
import org.chtijbug.drools.console.vaadincomponent.componentview.AssociateProjectKie;
import org.chtijbug.drools.console.vaadincomponent.componentview.DefineProject;
import org.chtijbug.drools.console.view.DeploymentView;
import org.chtijbug.drools.proxy.persistence.model.ProjectPersist;

public class DeploymentAction extends VerticalLayout {


    private Button definirProject;

    private Button associateKieServer;

    private Button deployer;

    private ProjectPersistService projectPersistService;
    private DeploymentView deploymentView;

    public DeploymentAction(SqueletteComposant squeletteComposant,DeploymentView deploymentView){
        setClassName("leftMenu-global-action");
        this.deploymentView=deploymentView;
        projectPersistService= AppContext.getApplicationContext().getBean(ProjectPersistService.class);

        definirProject =new Button("Define your project", VaadinIcon.CODE.create());
        definirProject.setClassName("leftMenu-global-button");
        definirProject.setEnabled(false);
        add(definirProject);
        definirProject.addClickListener(buttonClickEvent -> {
            active(definirProject);

            ProjectPersist projectPersist=deploymentView.getProjectPersistGrid().getSelectedItems().stream().findFirst().get();

            if(projectPersist!=null) {
                DialogPerso dialog = new DialogPerso();
                dialog.add(new DefineProject(deploymentView,dialog,projectPersist));
                dialog.open();
            }
        });

        associateKieServer =new Button("Associate project/Kie Server", VaadinIcon.RETWEET.create());
        associateKieServer.setClassName("leftMenu-global-button");
        associateKieServer.setEnabled(false);
        add(associateKieServer);
        associateKieServer.addClickListener(buttonClickEvent -> {
            active(associateKieServer);

            ProjectPersist projectPersist=deploymentView.getProjectPersistGrid().getSelectedItems().stream().findFirst().get();

            if(projectPersist!=null) {
                DialogPerso dialog = new DialogPerso();
                dialog.add(new AssociateProjectKie(deploymentView,dialog,projectPersist));
                dialog.open();
            }
        });

        deployer =new Button("Deploy", VaadinIcon.EXTERNAL_LINK.create());
        deployer.setClassName("leftMenu-global-button");
        deployer.setEnabled(false);
        add(deployer);
        deployer.addClickListener(buttonClickEvent -> {
            active(deployer);

            ProjectPersist projectPersist=deploymentView.getProjectPersistGrid().getSelectedItems().stream().findFirst().get();
            if(projectPersist!=null) {
                squeletteComposant.getConsoleDeploy().setTtile(projectPersist.getDeploymentName());
                projectPersistService.deployer(projectPersist,deploymentView,getUI().get());
            }
        });

    }



    private boolean isActive(Button button){
        return button.getClassNames().contains("active");
    }
    private void removeActive(Button button) {

        if(button.getClassNames().contains("active")){
            button.getClassNames().remove("active");
        }
    }
    private void active(Button button){
        removeActive(definirProject);
        removeActive(associateKieServer);
        removeActive(deployer);
        button.getClassNames().add("active");
    }

    public Button getDefinirProject() {
        return definirProject;
    }

    public void setDefinirProject(Button definirProject) {
        this.definirProject = definirProject;
    }

    public Button getAssociateKieServer() {
        return associateKieServer;
    }

    public void setAssociateKieServer(Button associateKieServer) {
        this.associateKieServer = associateKieServer;
    }

    public Button getDeployer() {
        return deployer;
    }

    public void setDeployer(Button deployer) {
        this.deployer = deployer;
    }
}
