package org.chtijbug.drools.console.vaadincomponent.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.chtijbug.drools.console.service.KieRepositoryService;
import org.chtijbug.drools.console.service.ProjectPersistService;
import org.chtijbug.drools.console.service.UserConnectedService;
import org.chtijbug.drools.console.service.model.UserConnected;
import org.chtijbug.drools.console.service.model.kie.KieConfigurationData;
import org.chtijbug.drools.console.service.util.AppContext;
import org.chtijbug.drools.console.util.PasswordValidator;
import org.chtijbug.drools.console.vaadincomponent.componentperso.PasswordFieldPerso;
import org.chtijbug.drools.console.vaadincomponent.componentperso.TextFieldPerso;

@StyleSheet("css/accueil.css")
public class FormulaireComposant extends VerticalLayout {

    //Composant

    private Label title;

    private TextFieldPerso username;

    private PasswordFieldPerso password;

    private Button login;

    private Button forgotPassword;

    //METIER

    private Binder<UserConnected> userConnectedBinder;

    private KieRepositoryService kieRepositoryService;

    private KieConfigurationData configKie;

    private UserConnectedService userConnectedService;

    private ProjectPersistService projectPersistService;

    public FormulaireComposant(){

        projectPersistService=AppContext.getApplicationContext().getBean(ProjectPersistService.class);
        kieRepositoryService= AppContext.getApplicationContext().getBean(KieRepositoryService.class);
        configKie= AppContext.getApplicationContext().getBean(KieConfigurationData.class);
        userConnectedService=AppContext.getApplicationContext().getBean(UserConnectedService.class);

        setClassName("login-application-layout");

        userConnectedBinder=new Binder<>();
        userConnectedBinder.setBean(new UserConnected());

        //FORMULAIRE

        title=new Label("Sign in ");
        title.setClassName("login-application-title");
        add(title);

        username=new TextFieldPerso("Username","", VaadinIcon.USER.create());
        username.getTextField().setValueChangeMode(ValueChangeMode.EAGER);
        username.getTextField().addValueChangeListener(textFieldStringComponentValueChangeEvent -> verifyValidity());
        userConnectedBinder.forField(username.getTextField())
                .bind(
                        userConnected -> userConnected.getUserName(),
                        (userConnected, s) -> userConnected.setUserName(s));

        add(username);

        password=new PasswordFieldPerso("Password", VaadinIcon.PASSWORD.create());
        password.getPasswordField().setValueChangeMode(ValueChangeMode.EAGER);
        password.getPasswordField().addValueChangeListener(textFieldStringComponentValueChangeEvent -> verifyValidity());
        userConnectedBinder.forField(password.getPasswordField())
                .withValidator(new PasswordValidator("Ce n'est pas un password valide"))
                .bind(
                        userConnected -> userConnected.getUserPassword(),
                        (userConnected, s) -> userConnected.setUserPassword(s));



        add(password);

        forgotPassword=new Button("Forgot password?");
        forgotPassword.setClassName("footer-button");
        add(forgotPassword);

        login=new Button("Connexion");
        login.setEnabled(false);
        login.setClassName("login-application-connexion");
        login.addClickListener(buttonClickEvent ->{
            Boolean test=connexion();

            if(test){
                getUI().get().navigate("accueil");
            }else {
                login.setEnabled(false);
                username.getTextField().setValue("");
                password.getPasswordField().setValue("");
            }
        });
        add(login);
        if (configKie.getPassword()!= null) {
            password.getPasswordField().setValue(configKie.getPassword());
        }
        if (configKie.getUserName()!= null) {
            username.getTextField().setValue(configKie.getUserName());
        }

    }
    public void verifyValidity(){
        if(!username.getTextField().isInvalid()&&username.getTextField().getValue()!=null&&!username.getTextField().isEmpty()&&
            !password.getPasswordField().isInvalid()&&password.getPasswordField().getValue()!=null&&!password.getPasswordField().isEmpty()){
            login.setEnabled(true);
        }else {
            login.setEnabled(false);
        }
    }
    public boolean connexion(){
        UserConnected connected = kieRepositoryService.login(
                configKie.getKiewbUrl(),
                userConnectedBinder.getBean().getUserName(),
                userConnectedBinder.getBean().getUserPassword(),
                configKie.getName());

        if(connected!=null) {
            connected.setConnected(true);
            userConnectedService.addUserToSession(connected);
            //projectPersistService.saveIfnotExist(connected.getProjectResponses(),configKie.getName());

            return true;
        }else {
            return false;
        }
    }

    public Label getTitle() {
        return title;
    }

    public void setTitle(Label title) {
        this.title = title;
    }

    public TextFieldPerso getUsername() {
        return username;
    }

    public void setUsername(TextFieldPerso username) {
        this.username = username;
    }

    public PasswordFieldPerso getPassword() {
        return password;
    }

    public void setPassword(PasswordFieldPerso password) {
        this.password = password;
    }

    public Button getLogin() {
        return login;
    }

    public void setLogin(Button login) {
        this.login = login;
    }

    public Button getForgotPassword() {
        return forgotPassword;
    }

    public void setForgotPassword(Button forgotPassword) {
        this.forgotPassword = forgotPassword;
    }
}
