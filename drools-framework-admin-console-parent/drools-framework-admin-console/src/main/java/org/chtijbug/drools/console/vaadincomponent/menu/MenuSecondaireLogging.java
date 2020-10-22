package org.chtijbug.drools.console.vaadincomponent.menu;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.chtijbug.drools.console.vaadincomponent.Squelette.SqueletteComposant;
import org.chtijbug.drools.console.vaadincomponent.leftMenu.Action.ActionLogging;
import org.chtijbug.drools.console.view.LoggingView;

@StyleSheet("css/accueil.css")
public class MenuSecondaireLogging extends HorizontalLayout {
    private static String active="active";

    private Button logging;

    public MenuSecondaireLogging(SqueletteComposant squeletteComposant){
        setVisible(false);

        setClassName("menu-secondaire-content");


        logging =new Button("Gestion-logging", VaadinIcon.OFFICE.create());
        logging.setClassName("menu-secondaire-button");
        add(logging);
        logging.addClickListener(buttonClickEvent -> {
            if(!isActive(logging)) {
                active(logging);
            }
            LoggingView loggingView=new LoggingView();

            ActionLogging actionLogging=new ActionLogging(loggingView);
            loggingView.setActionLogging(actionLogging);
            squeletteComposant.navigate(loggingView,LoggingView.PAGE_NAME,actionLogging);
        });
    }

    private boolean isActive(Button button){
        return button.getClassNames().contains(active);
    }
    private void removeActive(Button button) {

        if(button.getClassNames().contains(active)){
            button.getClassNames().remove(active);
        }
    }
    private void active(Button button){
        removeActive(logging);

        button.getClassNames().add(active);
    }

    public Button getLogging() {
        return logging;
    }

    public void setLogging(Button logging) {
        this.logging = logging;
    }
}
