package org.chtijbug.guvnor.uberfire.security;


import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.uberfire.ext.security.management.api.GroupManager;
import org.uberfire.ext.security.management.api.RoleManager;
import org.uberfire.ext.security.management.api.UserManagementService;
import org.uberfire.ext.security.management.api.UserManager;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Dependent
@Named(value = "PymmaKieSecurityService")
public class KiePlatformSecurityService implements UserManagementService {

    KiePlatformUserManager userManager;
    KiePlatformGroupManager groupManager;
    KiePlatformRoleManager roleManager;

    private String connectionString;
    private String databaseName;
    private MongoClient mongoClient;
    private CodecRegistry pojoCodecRegistry;
    private MongoDatabase database;



    @Inject
    public KiePlatformSecurityService(KiePlatformUserManager userManager,
                                      KiePlatformGroupManager groupManager,
                                      KiePlatformRoleManager roleManager) {
        //-DconnectionString=localhost:28017 -Ddatabase=businessProxyDB

        this.connectionString = System.getProperty("connectionString");
        this.databaseName=System.getProperty("name");
        System.out.println("KiePlatformSecurityService initialized with databaseName = " + connectionString );
        this.mongoClient = MongoClients.create(connectionString);
        this.pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        this.database = mongoClient.getDatabase(databaseName).withCodecRegistry(pojoCodecRegistry);
        System.out.println("All setup");
        this.userManager = userManager;
        this.groupManager = groupManager;
        this.roleManager = roleManager;
        this.userManager.setMongo(mongoClient,pojoCodecRegistry,database);
        this.groupManager.setMongo(mongoClient,pojoCodecRegistry,database);
        this.roleManager.setMongo(mongoClient,pojoCodecRegistry,database);

    }


    @Override
    public UserManager users() {
        return userManager;
    }

    @Override
    public GroupManager groups() {
        return groupManager;
    }

    @Override
    public RoleManager roles() {
        return roleManager;
    }
}
