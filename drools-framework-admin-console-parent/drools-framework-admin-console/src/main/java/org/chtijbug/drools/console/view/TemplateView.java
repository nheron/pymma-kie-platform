package org.chtijbug.drools.console.view;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import org.chtijbug.drools.console.service.KieRepositoryService;
import org.chtijbug.drools.console.service.ProjectPersistService;
import org.chtijbug.drools.console.service.UserConnectedService;
import org.chtijbug.drools.console.service.model.UserConnected;
import org.chtijbug.drools.console.service.model.kie.KieConfigurationData;
import org.chtijbug.drools.console.service.util.AppContext;
import org.chtijbug.drools.console.vaadincomponent.componentperso.DialogPerso;
import org.chtijbug.drools.console.vaadincomponent.leftMenu.Action.TemplatesAction;
import org.chtijbug.drools.proxy.persistence.model.ProjectPersist;
import org.chtijbug.drools.proxy.persistence.model.UserGroups;
import org.chtijbug.drools.proxy.persistence.repository.UserGroupsRepository;
import org.chtijbug.guvnor.server.jaxrs.jaxb.Asset;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@StyleSheet("css/accueil.css")
public class TemplateView extends VerticalLayout {

    public static final String PAGE_NAME = "Templates";

    private transient KieConfigurationData config;

    private transient KieRepositoryService kieRepositoryService;

    private transient UserConnected userConnected;

    private transient UserConnectedService userConnectedService;

    private transient UserGroupsRepository userGroupsRepository;

    private transient ProjectPersistService projectPersistService;

    private ListDataProvider<Asset> dataProvider;

    private Grid<Asset> assetListGrid;

    private TextField searchTemplate;

    private ConfigurableFilterDataProvider<Asset, Void, SerializablePredicate<Asset>> filterDataProvider;

    private TemplatesAction templatesAction;

    public TemplateView() {

        setClassName("template-content");

        dataProvider = new ListDataProvider<>(new ArrayList<>());
        filterDataProvider = dataProvider.withConfigurableFilter();

        this.kieRepositoryService = AppContext.getApplicationContext().getBean(KieRepositoryService.class);
        this.userConnectedService = AppContext.getApplicationContext().getBean(UserConnectedService.class);
        this.userGroupsRepository = AppContext.getApplicationContext().getBean(UserGroupsRepository.class);
        this.userConnected = userConnectedService.getUserConnected();
        this.projectPersistService = AppContext.getApplicationContext().getBean(ProjectPersistService.class);
        this.config = AppContext.getApplicationContext().getBean(KieConfigurationData.class);

        assetListGrid = new Grid<>();
        assetListGrid.setClassName("templates-grid-perso");
        assetListGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        Grid.Column<Asset> assetColumn = assetListGrid.addColumn(Asset::getTitle);
        searchTemplate = new TextField("title");
        searchTemplate.setValueChangeMode(ValueChangeMode.EAGER);
        searchTemplate.addValueChangeListener(e ->
                refreshtGrid(searchTemplate.getValue(), "title")
        );
        assetColumn.setHeader(searchTemplate);
        add(assetListGrid);

        assetListGrid.addSelectionListener(selectionEvent ->
                templatesAction.getEdit().setEnabled(assetListGrid.getSelectedItems() != null)
        );
    }

    public void setDataProvider(ComboBox<ProjectPersist> spaceSelection) {
        ProjectPersist response = spaceSelection.getValue();
        if (response != null) {
            UserGroups projectGroups = userGroupsRepository.findUserGroupsByProjectPersist(response);
            String workspaceName = projectGroups.getWorkspaceUserGroup().getSpaceName();
            List<Asset> tmp = kieRepositoryService.getListAssets(config.getKiewbUrl(),
                    userConnected.getUserName(),
                    userConnected.getUserPassword(),
                    workspaceName,
                    projectGroups.getProjectName());
            List<Asset> result = new ArrayList<>();
            for (Asset asset : tmp) {
                if (asset.getTitle().endsWith(".template")
                        || asset.getTitle().endsWith(".gdst")) {
                    result.add(asset);
                }
            }
            dataProvider = new ListDataProvider<>(result);
            filterDataProvider = dataProvider.withConfigurableFilter();
            assetListGrid.setDataProvider(filterDataProvider);
            reinitFilter();
        } else {
            List<Asset> result = new ArrayList<>();
            dataProvider = new ListDataProvider<>(result);
            filterDataProvider = dataProvider.withConfigurableFilter();
            assetListGrid.setDataProvider(filterDataProvider);
            reinitFilter();
        }
    }

    public void refreshList(ComboBox<ProjectPersist> spaceSelection) {
        spaceSelection.setItems(projectPersistService.findProjectsConnectedUser().values());
    }

    public void edit(ComboBox<ProjectPersist> spaceSelection) {
        Set<Asset> selectedElements = assetListGrid.getSelectedItems();
        if (selectedElements.toArray().length > 0) {
            Optional<Asset> assetOptional = selectedElements.stream().findFirst();
            if (assetOptional.isPresent()) {
                String assetName = assetOptional.get().getTitle();
                if (assetName != null) {
                    userConnectedService.addAssetToSession(assetName);
                    UserGroups projectGroups = userGroupsRepository.findUserGroupsByProjectPersist(spaceSelection.getValue());
                    String workspaceName = projectGroups.getWorkspaceUserGroup().getSpaceName();
                    userConnectedService.addProjectToSession(projectGroups.getProjectName());
                    userConnectedService.addSpaceToSession(workspaceName);
                    DialogPerso dialog = new DialogPerso();

                    dialog.add(new EditTemplateView(dialog, assetName));
                    dialog.open();
                }
            }
        }
    }

    private void refreshtGrid(String value, String type) {

        filterDataProvider.setFilter(filterGrid(value.toUpperCase(), type));
        assetListGrid.getDataProvider().refreshAll();
    }

    private SerializablePredicate<Asset> filterGrid(String value, String type) {
        SerializablePredicate<Asset> columnPredicate = null;
        if (value.equals(" ") || type.equals(" ")) {
            columnPredicate = asset -> (true);
        } else {
            if (type.equals("Asset Title")) {
                columnPredicate = asset -> (asset.getTitle().contains(value));
            }
        }
        return columnPredicate;
    }

    public UserConnectedService getUserConnectedService() {
        return userConnectedService;
    }

    public void setUserConnectedService(UserConnectedService userConnectedService) {
        this.userConnectedService = userConnectedService;
    }

    public void duplicate() {
        //NOP
    }

    public void reinitFilter() {
        searchTemplate.setValue("");
    }

    public TemplatesAction getTemplatesAction() {
        return templatesAction;
    }

    public void setTemplatesAction(TemplatesAction templatesAction) {
        this.templatesAction = templatesAction;
    }
}
