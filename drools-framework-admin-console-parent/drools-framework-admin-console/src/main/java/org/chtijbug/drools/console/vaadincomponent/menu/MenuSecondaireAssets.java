package org.chtijbug.drools.console.vaadincomponent.menu;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.chtijbug.drools.console.vaadincomponent.Squelette.SqueletteComposant;
import org.chtijbug.drools.console.vaadincomponent.leftMenu.Action.TemplatesAction;
import org.chtijbug.drools.console.view.TemplateView;

@StyleSheet("css/accueil.css")
public class MenuSecondaireAssets extends HorizontalLayout {

    private Button assetsView;

    public MenuSecondaireAssets(SqueletteComposant squeletteComposant){
        setVisible(false);

        setClassName("menu-secondaire-content");

        assetsView =new Button("Templates",VaadinIcon.ARCHIVE.create());
        assetsView.setClassName("menu-secondaire-button");
        add(assetsView);
        assetsView.addClickListener(buttonClickEvent -> {
            if(!isActive(assetsView)) {
                active(assetsView);
            }
            TemplateView templateView=new TemplateView();
            TemplatesAction templatesAction=new TemplatesAction(templateView);
            templateView.setTemplatesAction(templatesAction);
            squeletteComposant.navigate(templateView,TemplateView.PAGE_NAME,templatesAction);
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
        removeActive(assetsView);

        button.getClassNames().add("active");
    }
}
