package org.chtijbug.wildfly.loginmodule;

import com.mongodb.DBRef;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.spi.UsernamePasswordLoginModule;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class KiePlatformLoginModule extends UsernamePasswordLoginModule {

    private String connectionString;
    private String databaseName;
    private MongoClient mongoClient;
    CodecRegistry pojoCodecRegistry;
    MongoDatabase database;


    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);
        connectionString = (String)options.get("connectionString");
        databaseName = (String)options.get("name");

        System.out.println("Pymma Login Module initialized with databaseName = " + connectionString );
        mongoClient = MongoClients.create(connectionString);
        pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        database = mongoClient.getDatabase(databaseName).withCodecRegistry(pojoCodecRegistry);
        System.out.println("All setup");
    }

    @Override
    protected boolean validatePassword(String inputPassword, String expectedPassword) {
        System.out.println( "Pymma KieLogin validate password");

        return inputPassword.equals(expectedPassword);

        //return super.validatePassword(inputPassword, expectedPassword);
    }

    @Override
    protected String getUsersPassword() throws LoginException {
        System.out.format("KiePlatformLoginModule: authenticating user '%s'\n",
                getUsername());
        AtomicReference<String> password= new AtomicReference<>("");
        AtomicReference<String> userWorkbenchName= new AtomicReference<>("");
        MongoCollection<Document> userCollection = database.getCollection("user");
        userCollection.find(eq("login", getUsername())).forEach((Consumer<Document>) doc
                -> password.set((String) doc.get("password")));
        userCollection.find(eq("login", getUsername())).forEach((Consumer<Document>) doc
                -> userWorkbenchName.set((String) doc.get("wbName")));
        String wbName=System.getProperty("org.chtijbug.wbname");
        if (wbName==null || wbName.length()==0)
            wbName="demo";
        if (userWorkbenchName.get()==null || wbName.equals(userWorkbenchName.get())){
            return password.get();
        }else{
            return "";
        }

    }

    @Override
    protected Group[] getRoleSets() throws LoginException {
        SimpleGroup group = new SimpleGroup("Roles");
        AtomicReference<ArrayList<DBRef>> roles= new AtomicReference<ArrayList<DBRef>>(new ArrayList());
        AtomicReference<ArrayList<DBRef>> groups= new AtomicReference<ArrayList<DBRef>>(new ArrayList());

        MongoCollection<Document> userCollection = database.getCollection("user");
        userCollection.find(eq("login", getUsername())).forEach((Consumer<Document>) doc
                -> roles.set((ArrayList) doc.get("userRoles")));
        userCollection.find(eq("login", getUsername())).forEach((Consumer<Document>) doc
                -> groups.set((ArrayList) doc.get("userGroups")));

        MongoCollection<Document> userRolesCollection = database.getCollection("userRoles");
        for (DBRef dbRef : roles.get()){
            Document role = userRolesCollection.find(eq("_id", dbRef.getId())).first();
            group.addMember(new SimplePrincipal((String)role.get("name")));

        }
        MongoCollection<Document> userGroupsCollection = database.getCollection("userGroups");
        for (DBRef dbRef : groups.get()){
            Document userGroupdoc = userGroupsCollection.find(eq("_id", dbRef.getId())).first();
            group.addMember(new SimplePrincipal((String)userGroupdoc.get("name")));
        }
        return new Group[] { group };
    }
}
