package org.chtijbug.drools.console.vaadincomponent.leftMenu.Action;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.chtijbug.drools.console.service.IndexerService;
import org.chtijbug.drools.console.service.util.AppContext;
import org.chtijbug.drools.console.vaadincomponent.componentperso.DialogPerso;
import org.chtijbug.drools.console.vaadincomponent.componentperso.TextFieldPerso;
import org.chtijbug.drools.console.view.ActionLoggingView;
import org.chtijbug.drools.console.view.LoggingView;
import org.chtijbug.drools.proxy.persistence.model.BusinessTransactionPersistence;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public class ActionLogging extends VerticalLayout {
    private static String active="active";

    private Button viewAction;

    private TextFieldPerso transactionIdSearch;

    private transient IndexerService indexerService;

    public ActionLogging(LoggingView loggingView){

        indexerService= AppContext.getApplicationContext().getBean(IndexerService.class);

        setClassName("leftMenu-global-action");

        transactionIdSearch=new TextFieldPerso("Search by TransactionId","",VaadinIcon.SEARCH.create());
        transactionIdSearch.getTextField().setValueChangeMode(ValueChangeMode.EAGER);
        transactionIdSearch.getTextField().addValueChangeListener(textFieldStringComponentValueChangeEvent -> {

            try {

                if (textFieldStringComponentValueChangeEvent.getValue().isEmpty()) {
                    loggingView.getTitle().setText("Logging : ");
                    loggingView.getGridLogging().setDataProvider(indexerService.getBusinessTransactionPersistenceRepository().findAll(PageRequest.of(0, 100)).getContent());
                } else {
                    List<BusinessTransactionPersistence> b = indexerService.getBusinessTransactionPersistenceRepository().findAllByTransactionId(textFieldStringComponentValueChangeEvent.getValue(), PageRequest.of(0, 100));

                    if (b != null) {
                        loggingView.getTitle().setText("Logging : " + textFieldStringComponentValueChangeEvent.getValue());
                        loggingView.getGridLogging().setDataProvider(b);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                transactionIdSearch.getTextField().setValue("");
                Notification.show("Valeur invalide");
            }
        });
        add(transactionIdSearch);

        viewAction =new Button("View Details", VaadinIcon.INFO.create());
        viewAction.setClassName("leftMenu-global-button");
        viewAction.setEnabled(false);

        add(viewAction);

        viewAction.addClickListener(buttonClickEvent -> {
            active(viewAction);

            BusinessTransactionPersistence b=loggingView.getGridLogging().getSelectedItems().stream().findFirst().get();

            if(b!=null) {
                DialogPerso dialog = new DialogPerso();

                dialog.add(new ActionLoggingView(b,dialog));
                dialog.open();
            }

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
        removeActive(viewAction);
        button.getClassNames().add(active);
    }

    public Button getViewAction() {
        return viewAction;
    }

    public void setViewAction(Button viewAction) {
        this.viewAction = viewAction;
    }
}
