package org.chtijbug.drools.console.vaadincomponent.componentview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.chtijbug.drools.console.service.ProjectPersistService;
import org.chtijbug.drools.console.service.util.AppContext;
import org.chtijbug.drools.console.vaadincomponent.componentperso.ComboBoxPerso;
import org.chtijbug.drools.console.vaadincomponent.componentperso.TextFieldPerso;
import org.chtijbug.drools.console.view.DeploymentView;
import org.chtijbug.drools.proxy.persistence.model.ProjectPersist;

public class DefineProject extends VerticalLayout {

    //COMPONENT

    private ComboBoxPerso mainClass;

    private TextFieldPerso processID;

    private TextFieldPerso nameDeploy;

    private Label label;

    private Label label2;

    private Button valider;

    private transient ProjectPersistService projectPersistService;
    private boolean createMode;

    public DefineProject(DeploymentView deploymentView,Dialog dialog, ProjectPersist projectPersist){


        projectPersistService=AppContext.getApplicationContext().getBean(ProjectPersistService.class);

        setClassName("creation-runtime-content");

        label=new Label("Define your project : "+projectPersist.getProjectName());
        label.setClassName("creation-runtime-title");
        add(label);

        label2=new Label("this step is essential before you can associate your project with a workbench");
        label2.setClassName("creation-runtime-title2");
        add(label2);

        nameDeploy=new TextFieldPerso("Deployment Name ","",VaadinIcon.FILE_TEXT.create());
        nameDeploy.getTextField().setRequired(true);
        if (projectPersist.getDeploymentName()!=null
                && projectPersist.getDeploymentName().length()>0){
            nameDeploy.getTextField().setValue((projectPersist.getDeploymentName()));
        }
        nameDeploy.getTextField().addValueChangeListener(textFieldStringComponentValueChangeEvent -> {
            verify();
            projectPersist.setDeploymentName(nameDeploy.getTextField().getValue().replaceAll(" ","_"));
            if(projectPersistService.getProjectRepository().findByDeploymentName(projectPersist.getDeploymentName())!=null){
                projectPersist.setDeploymentName(null);
                nameDeploy.getTextField().setValue("");
                Notification.show("Attention : a project already has this deployment name");
            }
        });

        add(nameDeploy);

        mainClass=new ComboBoxPerso("MainClass", VaadinIcon.TREE_TABLE.create());
        mainClass.getComboBox().setItems(projectPersist.getClassNameList());
        mainClass.getComboBox().setRequired(true);
        if (projectPersist.getMainClass()!= null
                && projectPersist.getMainClass().length()>0){
            mainClass.getComboBox().setValue("class="+projectPersist.getMainClass());
        }
        mainClass.getComboBox().addValueChangeListener(textFieldStringComponentValueChangeEvent -> {
            verify();
            String mainClassName=(String)mainClass.getComboBox().getValue();
            if (mainClassName.indexOf("=")!= -1){
                mainClassName = mainClassName.substring(mainClassName.indexOf("=")+1);
            }
             projectPersist.setMainClass(mainClassName);
        });

        add(mainClass);

        processID=new TextFieldPerso("Process ID","",VaadinIcon.TASKS.create());
        processID.getTextField().setRequired(true);
        if (projectPersist.getProcessID()!= null
                && projectPersist.getProcessID().length()>0){
            processID.getTextField().setValue(projectPersist.getProcessID());
        }
        processID.getTextField().setValueChangeMode(ValueChangeMode.EAGER);
        processID.getTextField().addValueChangeListener(textFieldStringComponentValueChangeEvent -> {
            verify();
            projectPersist.setProcessID(processID.getTextField().getValue());
        });

        add(processID);

        valider=new Button("Save");
        if (projectPersist.getProcessID()!= null
                && projectPersist.getProcessID().length()>0){
            valider.setEnabled(true);
            createMode=false;
        }else {
            valider.setEnabled(false);
            createMode=true;
        }
        valider.setClassName("login-application-connexion");
        valider.addClickListener(buttonClickEvent -> {
            if (createMode) {
                projectPersist.setStatus(ProjectPersist.DEFINI);
            }
            projectPersistService.getProjectRepository().save(projectPersist);
            deploymentView.setDataProvider();
            dialog.close();
        });
        add(valider);

    }
    public void verify(){
        if(nameDeploy.getTextField().isInvalid()||nameDeploy.getTextField().getValue().isEmpty()||nameDeploy.getTextField().getValue()==null&&
                processID.getTextField().isInvalid()||processID.getTextField().getValue().isEmpty()||processID.getTextField().getValue()==null&&
                mainClass.getComboBox().isInvalid()||mainClass.getComboBox().getValue()==null){
            valider.setEnabled(false);
        }else {
            valider.setEnabled(true);
        }
    }

}
