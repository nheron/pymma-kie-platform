package org.chtijbug.drools.console.vaadincomponent.componentview;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.value.ValueChangeMode;
import io.jsonwebtoken.Claims;
import org.chtijbug.drools.console.middle.JwtService;
import org.chtijbug.drools.console.service.ProjectPersistService;
import org.chtijbug.drools.console.service.RuntimeService;
import org.chtijbug.drools.console.service.util.AppContext;
import org.chtijbug.drools.console.vaadincomponent.componentperso.ComboBoxPerso;
import org.chtijbug.drools.console.vaadincomponent.componentperso.TextFieldPerso;
import org.chtijbug.drools.console.view.DeploymentView;
import org.chtijbug.drools.proxy.persistence.model.ProjectPersist;
import org.vaadin.olli.ClipboardHelper;

public class DefineProject extends VerticalLayout {

    //COMPONENT

    private ComboBoxPerso mainClass;

    private TextFieldPerso processID;

    private TextFieldPerso nameDeploy;

    private Label label;

    private Label label2;

    private Button valider;

    private Checkbox disableRuleLoggingCheckbox;

    private Checkbox enableHotDeployCheckbox;
    private Checkbox useJWTToConnectCheckbox;

    private TextFieldPerso jwtTokenTextField;

    private TextFieldPerso jwtPeriod;

    private Button createJWTButton;

    private Button copyJWTTextButton;

    private transient ProjectPersistService projectPersistService;

    private transient RuntimeService runtimeService;

    private boolean createMode;

    private JwtService jwtService;

    public DefineProject(DeploymentView deploymentView,Dialog dialog, ProjectPersist projectPersist){

        jwtService = AppContext.getApplicationContext().getBean(JwtService.class);
        runtimeService = AppContext.getApplicationContext().getBean(RuntimeService.class);
        projectPersistService=AppContext.getApplicationContext().getBean(ProjectPersistService.class);

        setClassName("creation-runtime-content");

        label=new Label("Define your project : "+projectPersist.getProjectName());
        label.setClassName("creation-runtime-title");
        add(label);

        label2=new Label("this step is essential before you can work on your project");
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

        enableHotDeployCheckbox = new Checkbox("Enable Hot deployment (needs 2 runtimes)");
        if (projectPersist.isEnableHotDeploy()){
            enableHotDeployCheckbox.setValue(true);
        }else{
            enableHotDeployCheckbox.setValue(false);
        }
        enableHotDeployCheckbox.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<Checkbox, Boolean> checkboxBooleanComponentValueChangeEvent) {
                projectPersist.setEnableHotDeploy(checkboxBooleanComponentValueChangeEvent.getValue());
            }
        });
        add(enableHotDeployCheckbox);

        disableRuleLoggingCheckbox = new Checkbox("Disable Rule logging");
        if (projectPersist.isDisableRuleLogging()){
            disableRuleLoggingCheckbox.setValue(true);
        }else{
            disableRuleLoggingCheckbox.setValue(false);
        }
        disableRuleLoggingCheckbox.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<Checkbox, Boolean> checkboxBooleanComponentValueChangeEvent) {
                projectPersist.setDisableRuleLogging(disableRuleLoggingCheckbox.getValue());
            }
        });
        add(disableRuleLoggingCheckbox);

        useJWTToConnectCheckbox =new Checkbox("Use JWT token to connect to API");
        add(useJWTToConnectCheckbox);



        jwtTokenTextField=new TextFieldPerso("JWT Token for API","",VaadinIcon.TASKS.create());

        if (projectPersist.getJwtAPIToken()!= null
                && projectPersist.getJwtAPIToken().length()>0){
            jwtTokenTextField.getTextField().setValue(projectPersist.getJwtAPIToken());
        }
        jwtTokenTextField.getTextField().setEnabled(false);


        add(jwtTokenTextField);
        copyJWTTextButton = new Button("Copy JWT token");
        ClipboardHelper clipboardHelper = new ClipboardHelper(projectPersist.getJwtAPIToken(), copyJWTTextButton);
        add(clipboardHelper);
        jwtPeriod=new TextFieldPerso("JWT validity for API","",VaadinIcon.TASKS.create());
        jwtPeriod.setEnabled(false);
        if (projectPersist.getJwtAPIToken()!= null
                && projectPersist.getJwtAPIToken().length()>0){
            try {
                Claims claims = jwtService.decodeJWT(projectPersist.getJwtAPIToken());
                jwtPeriod.getTextField().setValue(claims.getExpiration().toString());
            }catch (Exception e){

            }
            jwtTokenTextField.getTextField().setValue(projectPersist.getJwtAPIToken());
        }
        add(jwtPeriod);
        createJWTButton = new Button("Generate JWT token for one year");
        createJWTButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                String token = jwtService.createJWT(projectPersist, 1000*3600*24*(long)365);
                projectPersist.setJwtAPIToken(token);
                jwtTokenTextField.getTextField().setValue(token);
                Claims claims = jwtService.decodeJWT(projectPersist.getJwtAPIToken());
                jwtPeriod.getTextField().setValue(claims.getExpiration().toString());
            }
        });
        add(createJWTButton);
        if (projectPersist.isUseJWTToConnect()){
            useJWTToConnectCheckbox.setValue(true);
        }else{
            useJWTToConnectCheckbox.setValue(false);
            jwtTokenTextField.getTextField().setValue("");
            jwtTokenTextField.setEnabled(false);
            projectPersist.setJwtAPIToken(null);
            createJWTButton.setEnabled(false);
            jwtPeriod.getTextField().setValue("");
            copyJWTTextButton.setEnabled(false);
        }
        useJWTToConnectCheckbox.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<Checkbox, Boolean> checkboxBooleanComponentValueChangeEvent) {
                projectPersist.setUseJWTToConnect(useJWTToConnectCheckbox.getValue());
                if (!useJWTToConnectCheckbox.getValue()){
                    jwtTokenTextField.getTextField().setValue("");
                    createJWTButton.setEnabled(false);
                    projectPersist.setJwtAPIToken(null);
                    jwtPeriod.getTextField().setValue("");
                    copyJWTTextButton.setEnabled(false);
                }else{
                    jwtTokenTextField.setEnabled(true);
                    copyJWTTextButton.setEnabled(true);
                    createJWTButton.setEnabled(true);
                }
            }
        });

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
            runtimeService.updateRuntimes(projectPersist);
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
    public void verifyToken(){

            valider.setEnabled(true);

    }

}
