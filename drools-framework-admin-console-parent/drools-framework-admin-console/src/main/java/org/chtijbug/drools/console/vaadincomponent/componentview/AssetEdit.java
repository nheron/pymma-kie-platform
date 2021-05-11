package org.chtijbug.drools.console.vaadincomponent.componentview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.Binder;
import org.chtijbug.drools.console.service.KieRepositoryService;
import org.chtijbug.drools.console.service.UserConnectedService;
import org.chtijbug.drools.console.service.model.kie.KieConfigurationData;
import org.chtijbug.drools.console.service.util.AppContext;
import org.chtijbug.drools.console.vaadincomponent.componentview.service.GuidedDecisionTableModelTransformer;
import org.chtijbug.drools.console.vaadincomponent.componentview.service.GuidedRuleTemplateModelTransformer;
import org.drools.workbench.models.guided.dtable.backend.GuidedDTXMLPersistence;
import org.drools.workbench.models.guided.dtable.shared.model.GuidedDecisionTable52;
import org.drools.workbench.models.guided.template.backend.RuleTemplateModelXMLPersistenceImpl;
import org.drools.workbench.models.guided.template.shared.TemplateModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@StyleSheet("css/accueil.css")
public class AssetEdit extends Grid<HashMap<String, Object>> {

    private transient KieRepositoryService kieRepositoryService;

    private transient KieConfigurationData config;

    private transient UserConnectedService userConnectedService;

    public AssetEdit() {

        setClassName("assetEdit-content");

        this.kieRepositoryService = AppContext.getApplicationContext().getBean(KieRepositoryService.class);
        this.config = AppContext.getApplicationContext().getBean(KieConfigurationData.class);
        this.userConnectedService = AppContext.getApplicationContext().getBean(UserConnectedService.class);

        String assetContent = kieRepositoryService.getAssetSource(config.getKiewbUrl(),
                userConnectedService.getUserConnected().getUserName(),
                userConnectedService.getUserConnected().getUserPassword(),
                userConnectedService.getSpace(),
                userConnectedService.getProject(),
                userConnectedService.getAsset());
        Binder<HashMap<String, Object>> binder = new Binder<>();
        getEditor().setBinder(binder);
        setClassName("grid-perso");
        if (assetContent.startsWith("<decision-table52")){
            GuidedDecisionTable52 model = GuidedDTXMLPersistence.getInstance().unmarshal(assetContent);
            GuidedDecisionTableModelTransformer transform = new GuidedDecisionTableModelTransformer(model, binder, this);
            transform.run();
        }else {
            TemplateModel model = RuleTemplateModelXMLPersistenceImpl.getInstance().unmarshal(assetContent);
            GuidedRuleTemplateModelTransformer transform = new GuidedRuleTemplateModelTransformer(model,binder,this);
            transform.run();

        }
    }

    public AssetEdit(List<HashMap<String, Object>> objects) {

        setClassName("assetEdit-content");

        this.kieRepositoryService = AppContext.getApplicationContext().getBean(KieRepositoryService.class);
        this.config = AppContext.getApplicationContext().getBean(KieConfigurationData.class);
        this.userConnectedService = AppContext.getApplicationContext().getBean(UserConnectedService.class);

        setClassName("grid-perso");

        Binder<HashMap<String, Object>> binder = new Binder<>();
        getEditor().setBinder(binder);

        if(!objects.isEmpty()){
            for(Map.Entry<String,Object> t:objects.get(0).entrySet()){
                addColumn(hashmap -> hashmap.get(t.getKey())).setHeader(t.getKey());

            }
        }

        setItems(objects);
    }


}
