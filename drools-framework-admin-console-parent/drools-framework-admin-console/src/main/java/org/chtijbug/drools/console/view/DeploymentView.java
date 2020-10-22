package org.chtijbug.drools.console.view;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.communication.PushMode;
import org.chtijbug.drools.console.AddLog;
import org.chtijbug.drools.console.service.ProjectPersistService;
import org.chtijbug.drools.console.service.util.AppContext;
import org.chtijbug.drools.console.vaadincomponent.Squelette.SqueletteComposant;
import org.chtijbug.drools.console.vaadincomponent.leftMenu.Action.DeploymentAction;
import org.chtijbug.drools.proxy.persistence.model.ProjectPersist;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

@StyleSheet("css/accueil.css")
public class DeploymentView extends VerticalLayout implements AddLog {

    public static final String PAGE_NAME = "Deployment";

    //GRID composant
    private static final String STR_NAME_DEPLOY = "Deploy name";
    private static final String STR_NAME_PROJECT = "Project name";
    private static final String STR_GROUPE_ID = "Groupe ID";
    private static final String STR_BRANCH_NAME = "Branch Name";
    private static final String STR_ARTEFACT_ID = "Artefact ID";

    //TEXTFIELD search
    private static final String STR_PROCESS_ID = "Process ID";
    private static final String STR_SERVER_NAME = "Server Name";
    private static final String STR_STATUS = "Status";
    private Grid<ProjectPersist> projectPersistGrid;

    private ConfigurableFilterDataProvider<ProjectPersist, Void, SerializablePredicate<ProjectPersist>> filterDataProvider;
    private DeploymentAction deploymentAction;

    //CONSTANTE Textfield
    private TextField nameDeploy;
    private TextField nameProject;
    private TextField groupeId;
    private TextField branchName;
    private TextField artifactId;
    private TextField processId;
    private TextField serverName;
    private ComboBox status;

    //SERVICE
    private transient ProjectPersistService projectPersistService;

    private SqueletteComposant squeletteComposant;

    public DeploymentView(SqueletteComposant squeletteComposant) {

        this.squeletteComposant = squeletteComposant;
        projectPersistService = AppContext.getApplicationContext().getBean(ProjectPersistService.class);

        setClassName("deployment-content");

        add(new Label("Project"));

        projectPersistGrid = new Grid<>();
        projectPersistGrid.setClassName("deployment-grid-perso");
        projectPersistGrid.setSelectionMode(Grid.SelectionMode.SINGLE);


        Grid.Column<ProjectPersist> deployNameCo = projectPersistGrid.addColumn(projectPersist -> projectPersist.getDeploymentName());
        nameDeploy = new TextField(STR_NAME_DEPLOY);
        nameDeploy.setValueChangeMode(ValueChangeMode.EAGER);
        nameDeploy.addValueChangeListener(e ->
                refreshtGrid(nameDeploy.getValue(), STR_NAME_DEPLOY)
        );
        deployNameCo.setHeader(nameDeploy);

        Grid.Column<ProjectPersist> nameProjectCo = projectPersistGrid.addColumn(projectPersist -> projectPersist.getProjectName());
        nameProject = new TextField(STR_NAME_PROJECT);
        nameProject.setValueChangeMode(ValueChangeMode.EAGER);
        nameProject.addValueChangeListener(e -> {
            refreshtGrid(nameProject.getValue(), STR_NAME_PROJECT);
        });
        nameProjectCo.setHeader(nameProject);

        projectPersistGrid.addColumn(projectPersist -> projectPersist.getMainClass()).setHeader("ClassName")
                .setComparator((projectPersist, t1) -> projectPersist.getMainClass().compareTo(t1.getMainClass()));

        Grid.Column<ProjectPersist> branchCo = projectPersistGrid.addColumn(projectPersist -> projectPersist.getBranch());
        branchName = new TextField(STR_BRANCH_NAME);
        branchName.setValueChangeMode(ValueChangeMode.EAGER);
        branchName.addValueChangeListener(e -> {
            refreshtGrid(groupeId.getValue(), STR_BRANCH_NAME);
        });
        branchCo.setHeader(branchName);


        Grid.Column<ProjectPersist> groupIdCo = projectPersistGrid.addColumn(projectPersist -> projectPersist.getGroupID());
        groupeId = new TextField(STR_GROUPE_ID);
        groupeId.setValueChangeMode(ValueChangeMode.EAGER);
        groupeId.addValueChangeListener(e -> {
            refreshtGrid(groupeId.getValue(), STR_GROUPE_ID);
        });
        groupIdCo.setHeader(groupeId);

        Grid.Column<ProjectPersist> artifactIDCO = projectPersistGrid.addColumn(projectPersist -> projectPersist.getArtifactID());
        artifactId = new TextField(STR_ARTEFACT_ID);
        artifactId.setValueChangeMode(ValueChangeMode.EAGER);
        artifactId.addValueChangeListener(e -> {
            refreshtGrid(artifactId.getValue(), STR_ARTEFACT_ID);
        });
        artifactIDCO.setHeader(artifactId);

        Grid.Column<ProjectPersist> processIDco = projectPersistGrid.addColumn(projectPersist -> projectPersist.getProcessID());
        processId = new TextField(STR_PROCESS_ID);
        processId.setValueChangeMode(ValueChangeMode.EAGER);
        processId.addValueChangeListener(e ->
                refreshtGrid(processId.getValue(), STR_PROCESS_ID));
        processIDco.setHeader(processId);


        Grid.Column<ProjectPersist> serverNameCo = projectPersistGrid.addColumn(projectPersist -> {
            String result = null;
            for (String servName : projectPersist.getServerNames()) {
                if (result != null) {
                    result = result + ":" + servName;
                } else {
                    result = servName;
                }
            }
            return result;
        });
        serverName = new TextField(STR_SERVER_NAME);
        serverName.setValueChangeMode(ValueChangeMode.EAGER);
        serverName.addValueChangeListener(e ->
                refreshtGrid(serverName.getValue(), STR_SERVER_NAME)
        );
        serverNameCo.setHeader(serverName);


        projectPersistGrid.addColumn(projectPersist -> projectPersist.getProjectVersion()).setHeader("Version")
                .setComparator((projectPersist, t1) -> projectPersist.getProjectVersion().compareTo(t1.getProjectVersion()));


        Grid.Column<ProjectPersist> statusCo = projectPersistGrid.addColumn(projectPersist -> projectPersist.getStatus());
        status = new ComboBox(STR_PROCESS_ID);
        status.setClassName("deployment-combobox");

        ArrayList<String> tmp = new ArrayList<>();
        tmp.add(ProjectPersist.DEFINI);
        tmp.add(ProjectPersist.Deployable);
        tmp.add(ProjectPersist.ADEFINIR);
        tmp.add(" ");
        status.setItems(tmp);
        status.addValueChangeListener(e ->
                refreshtGrid(status.getValue() != null ? status.getValue().toString() : " ", STR_STATUS)
        );
        statusCo.setHeader(status);

        add(projectPersistGrid);

        setDataProvider();

        projectPersistGrid.addSelectionListener(selectionEvent -> {

            if (selectionEvent.getFirstSelectedItem().isPresent()) {
                majAction(selectionEvent.getFirstSelectedItem().get());
            } else {
                getDeploymentAction().getAssociateKieServer().setEnabled(false);
                getDeploymentAction().getDefinirProject().setEnabled(false);
                getDeploymentAction().getDeployer().setEnabled(false);
            }
        });
    }

    private void refreshtGrid(String value, String type) {

        filterDataProvider.setFilter(filterGrid(value.toUpperCase(), type));
        projectPersistGrid.getDataProvider().refreshAll();
    }

    private SerializablePredicate<ProjectPersist> filterGrid(String value, String type) {
        SerializablePredicate<ProjectPersist> columnPredicate = null;

        if (value.equals("") || value.equals(" ") || type.equals(" ")) {
            columnPredicate = asset -> (true);
        } else {
            if (type.equals(STR_ARTEFACT_ID)) {
                columnPredicate = asset -> (
                        asset.getArtifactID() != null && asset.getArtifactID().toUpperCase().contains(value.toUpperCase()));
            } else if (type.equals(STR_GROUPE_ID)) {
                columnPredicate = asset -> (asset.getGroupID() != null && asset.getGroupID().toUpperCase().contains(value.toUpperCase()));
            } else if (type.equals(STR_NAME_DEPLOY)) {
                columnPredicate = asset -> (asset.getDeploymentName() != null && asset.getDeploymentName().toUpperCase().contains(value.toUpperCase()));
            } else if (type.equals(STR_NAME_PROJECT)) {
                columnPredicate = asset -> (asset.getProjectName() != null && asset.getProjectName().toString().toUpperCase().contains(value.toUpperCase()));
            } else if (type.equals(STR_PROCESS_ID)) {
                columnPredicate = asset -> (asset.getProcessID() != null && asset.getProcessID().toUpperCase().contains(value.toUpperCase()));
            } else if (type.equals(STR_STATUS)) {
                columnPredicate = asset -> (asset.getStatus() != null && asset.getStatus().equalsIgnoreCase(value));
            }
        }
        return columnPredicate;
    }

    public void setDataProvider() {

        Map<String, ProjectPersist> projectPersists = projectPersistService.findProjectsConnectedUser();
        if (projectPersists != null) {
            ListDataProvider<ProjectPersist> dataProvider = new ListDataProvider<>(projectPersists.values());
            filterDataProvider = dataProvider.withConfigurableFilter();
            projectPersistGrid.setDataProvider(filterDataProvider);
            reinitFilter();

        }
    }

    public void reinitFilter() {

        artifactId.setValue("");
        groupeId.setValue("");
        processId.setValue("");
        nameProject.setValue("");
        nameDeploy.setValue("");
    }

    public void majAction(ProjectPersist projectPersist) {
        if (projectPersist.getStatus().equals(ProjectPersist.DEFINI)) {

            getDeploymentAction().getAssociateKieServer().setEnabled(true);
            getDeploymentAction().getDefinirProject().setEnabled(true);
            getDeploymentAction().getDeployer().setEnabled(false);

        } else if (projectPersist.getStatus().equals(ProjectPersist.ADEFINIR)) {

            getDeploymentAction().getAssociateKieServer().setEnabled(false);
            getDeploymentAction().getDefinirProject().setEnabled(true);
            getDeploymentAction().getDeployer().setEnabled(false);
        } else if (projectPersist.getStatus().equals(ProjectPersist.Deployable)) {

            getDeploymentAction().getAssociateKieServer().setEnabled(true);
            getDeploymentAction().getDefinirProject().setEnabled(true);
            getDeploymentAction().getDeployer().setEnabled(true);

        }
    }

    @Override
    public void addRow(String textToAdd, UI ui) {

        ui.access(() -> {

            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setClassName("console-row");
            Label date = new Label(new Date() + " : ");
            date.setClassName("console-date");
            horizontalLayout.add(date);
            horizontalLayout.add(new Label(textToAdd));
            squeletteComposant.getConsoleDeploy().getLogContent().getElement().getNode().markAsDirty();
            squeletteComposant.getConsoleDeploy().getLogContent().add(horizontalLayout);
        });

        ui.getSession().lock();
        try {
            ui.getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
            ui.push();

        } catch (Exception e) {
            e.printStackTrace();
            ui.getSession().unlock();

        } finally {
            ui.getSession().unlock();
        }
    }

    public Grid<ProjectPersist> getProjectPersistGrid() {
        return projectPersistGrid;
    }

    public void setProjectPersistGrid(Grid<ProjectPersist> projectPersistGrid) {
        this.projectPersistGrid = projectPersistGrid;
    }

    public DeploymentAction getDeploymentAction() {
        return deploymentAction;
    }

    public void setDeploymentAction(DeploymentAction deploymentAction) {
        this.deploymentAction = deploymentAction;
    }
}
