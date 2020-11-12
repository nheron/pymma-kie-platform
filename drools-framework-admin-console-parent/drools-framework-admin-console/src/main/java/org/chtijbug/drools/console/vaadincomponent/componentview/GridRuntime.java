package org.chtijbug.drools.console.vaadincomponent.componentview;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import org.chtijbug.drools.console.service.ProjectPersistService;
import org.chtijbug.drools.console.service.RuntimeService;
import org.chtijbug.drools.console.service.util.AppContext;
import org.chtijbug.drools.proxy.persistence.model.ProjectPersist;
import org.chtijbug.drools.proxy.persistence.model.RuntimePersist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridRuntime extends Grid<RuntimePersist> {

    private TextField runtimeName;

    private TextField hostName;

    private TextField branch;

    private TextField version;

    private TextField status;

    private String strRuntimeName = "Runtime Name";

    private String strHostName = "Hostname";

    private String strBranch = "Branch";

    private String strVersion = "Version";

    private String strStatus = "State";

    private transient ProjectPersistService projectPersistService;
    private transient RuntimeService runtimeService;

    private ListDataProvider<RuntimePersist> dataProvider;
    private ConfigurableFilterDataProvider<RuntimePersist, Void, SerializablePredicate<RuntimePersist>> filterDataProvider;

    private ProjectPersist projectPersist = null;
    private GridSelectionModel<RuntimePersist> selectionModel;

    public GridRuntime(ProjectPersist projectPersist) {
        this.projectPersist = projectPersist;
        init();


    }

    public GridRuntime() {
        init();

    }

    private void init() {
        projectPersistService = AppContext.getApplicationContext().getBean(ProjectPersistService.class);
        runtimeService = AppContext.getApplicationContext().getBean(RuntimeService.class);

        setClassName("deployment-grid-perso");
        selectionModel = setSelectionMode(SelectionMode.MULTI);
        selectionModel.deselectAll();
        Grid.Column<RuntimePersist> runtimeNameCo = addColumn(runtimePersist -> runtimePersist.getServerName());
        runtimeName = new TextField(strRuntimeName);
        runtimeName.setValueChangeMode(ValueChangeMode.EAGER);
        runtimeName.addValueChangeListener(e -> {
            refreshtGrid(runtimeName.getValue(), strRuntimeName);
        });
        runtimeNameCo.setHeader(runtimeName);

        Grid.Column<RuntimePersist> hostnameCo = addColumn(runtimePersist -> runtimePersist.getServerUrl());
        hostName = new TextField(strHostName);
        hostName.setValueChangeMode(ValueChangeMode.EAGER);
        hostName.addValueChangeListener(e -> {
            refreshtGrid(hostName.getValue(), strHostName);
        });
        hostnameCo.setHeader(hostName);

        Grid.Column<RuntimePersist> versionCo = addColumn(runtimePersist -> runtimePersist.getVersion());
        version = new TextField(strVersion);
        version.setValueChangeMode(ValueChangeMode.EAGER);
        version.addValueChangeListener(e -> {
            refreshtGrid(version.getValue(), strVersion);
        });
        versionCo.setHeader(version);
        Grid.Column<RuntimePersist> statusCo = addColumn(runtimePersist -> runtimePersist.getBranch());
        branch = new TextField(strBranch);
        branch.setValueChangeMode(ValueChangeMode.EAGER);
        branch.addValueChangeListener(e -> {
            refreshtGrid(branch.getValue(), strBranch);
        });
        statusCo.setHeader(branch);

        Grid.Column<RuntimePersist> branchCo = addColumn(runtimePersist -> runtimePersist.getStatus());
        status = new TextField(strStatus);
        status.setValueChangeMode(ValueChangeMode.EAGER);
        status.addValueChangeListener(e -> {
            refreshtGrid(status.getValue(), strVersion);
        });
        branchCo.setHeader(status);

        addColumn(new ComponentRenderer<>(runtimePersist -> {
            List<String> serverList = new ArrayList<>();
            serverList.add(runtimePersist.getServerName());
            List<ProjectPersist> projectPersists = projectPersistService.getProjectRepository().findByServerNamesIn(serverList);

            Label label = new Label(projectPersists != null ? projectPersists.size() + "" : "0");

            return label;
        })).setHeader("associated projects");
        setDataProvider();
    }

    private void refreshtGrid(String value, String type) {

        filterDataProvider.setFilter(filterGrid(value.toUpperCase(), type));
        getDataProvider().refreshAll();
    }

    private SerializablePredicate<RuntimePersist> filterGrid(String value, String type) {
        SerializablePredicate<RuntimePersist> columnPredicate = null;
        if (value.equals(" ") || type.equals(" ")) {
            columnPredicate = runtimePersist -> (true);
        } else {
            if (type.equals(strHostName)) {
                columnPredicate = runtimePersist -> (runtimePersist.getServerUrl() != null && runtimePersist.getServerUrl().toUpperCase().contains(value));

            } else if (type.equals(strRuntimeName)) {
                columnPredicate = runtimePersist -> (runtimePersist.getServerName() != null && runtimePersist.getServerName().toUpperCase().contains(value));

            } else if (type.equals(strVersion)) {
                columnPredicate = runtimePersist -> (runtimePersist.getVersion() != null && runtimePersist.getVersion().toUpperCase().contains(value));
            }
        }
        return columnPredicate;
    }

    public void setDataProvider() {

        List<RuntimePersist> runtimePersists = runtimeService.getRuntimeRepository().findAll();
        if (runtimePersists != null) {
            List<RuntimePersist> runtimeToShow = new ArrayList<>();
            Map<String, String> urlMap = new HashMap<>();
            for (RuntimePersist runtimePersist : runtimePersists) {
                if (urlMap.containsKey(runtimePersist.getServerName()) == false) {

                    urlMap.put(runtimePersist.getServerName(), runtimePersist.getServerUrl());
                    RuntimePersist runtimePersist1 = runtimePersist.duplicate();
                    if (projectPersist != null) {
                        runtimeToShow.add(runtimePersist1);
                        if (projectPersist.getServerNames().contains(runtimePersist1.getServerName())) {
                            selectionModel.select(runtimePersist1);
                        }
                    } else {
                        runtimeToShow.add(runtimePersist1);
                    }
                }
            }

            dataProvider = new ListDataProvider<>(runtimeToShow);
            filterDataProvider = dataProvider.withConfigurableFilter();
            setDataProvider(filterDataProvider);
            for (RuntimePersist runtimePersist : dataProvider.getItems()) {
                if (projectPersist.getServerNames().contains(runtimePersist.getServerName())) {
                    selectionModel.select(runtimePersist);
                }
            }

            reinitFilter();

        }


}

    public void reinitFilter() {
        hostName.setValue("");
        version.setValue("");
        runtimeName.setValue("");
    }
}
