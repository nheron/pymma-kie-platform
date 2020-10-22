/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.chtijbug.guvnor.uberfire.security;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBRef;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.GroupImpl;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.config.ConfigProperties;
import org.uberfire.ext.security.management.api.*;
import org.uberfire.ext.security.management.api.exception.SecurityManagementException;
import org.uberfire.ext.security.management.impl.SearchResponseImpl;
import org.uberfire.ext.security.management.impl.UserManagerSettingsImpl;
import org.uberfire.ext.security.management.util.SecurityManagementUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.mongodb.client.model.Filters.eq;

/**
 * <p>Users manager service provider implementation for Apache tomcat, when using default realm based on properties files.</p>
 *
 * @since 0.8.0
 */
public class KiePlatformUserManager implements UserManager, ContextualManager {

    private static final Logger LOG = LoggerFactory.getLogger(KiePlatformUserManager.class);


    private MongoClient mongoClient;
    private CodecRegistry pojoCodecRegistry;
    private MongoDatabase database;

    public KiePlatformUserManager() {
        this(new ConfigProperties(System.getProperties()));
    }

    public KiePlatformUserManager(final Map<String, String> gitPrefs) {
        this(new ConfigProperties(gitPrefs));
    }

    public KiePlatformUserManager(final ConfigProperties gitPrefs) {
        //loadConfig(gitPrefs);
    }

    public void setMongo(MongoClient mongoClient, CodecRegistry pojoCodecRegistry, MongoDatabase database) {
        this.mongoClient = mongoClient;
        this.pojoCodecRegistry = pojoCodecRegistry;
        this.database = database;

    }

    @Override
    public void initialize(final UserSystemManager userSystemManager) throws Exception {
        System.out.println("All setup");
    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public SearchResponse<User> search(SearchRequest request) throws SecurityManagementException {

        MongoCollection<Document> userCollection = database.getCollection("user");
        BasicDBObject regexQuery = new BasicDBObject();
        regexQuery.put("login", new BasicDBObject("$regex", request.getSearchPattern() + ".*").append("$options", "i"));
        List<User> users = new ArrayList<>();
        long totalNumber = userCollection.countDocuments(regexQuery);
        FindIterable<Document> documents = userCollection.find(regexQuery).skip(request.getPageSize() * (request.getPage() - 1)).limit(request.getPageSize());
        documents.forEach((Block<? super Document>) document -> {
            String userName = document.getString("login");
            User user = fillUser(userName, document);
            users.add(user);
        });
        boolean hasNextPage = true;
        if ((request.getPageSize() * (request.getPage()) > totalNumber)) {
            hasNextPage = false;
        }
        SearchResponse<User> response = new SearchResponseImpl(users, request.getPage(), request.getPageSize(), Long.valueOf(totalNumber).intValue(), hasNextPage);
        return response;
    }

    @Override
    public User get(String identifier) throws SecurityManagementException {
        MongoCollection<Document> userCollection = database.getCollection("user");
        List<User> users = new ArrayList<>();
        userCollection.find(eq("login", identifier)).forEach((Block<? super Document>) document -> {
            String userName = document.getString("login");
            User user = fillUser(userName, document);
            users.add(user);
        });
        if (users.size() == 1) {
            return users.get(0);
        } else {
            return new UserImpl(identifier);
        }
    }

    @Override
    public List<User> getAll() throws SecurityManagementException {
        List<User> users = new ArrayList<>();
        MongoCollection<Document> userCollection = database.getCollection("user");
        userCollection.find().forEach((Block<? super Document>) document -> {
            String userName = document.getString("login");
            User user = fillUser(userName, document);
            users.add(user);
        });
        return users;
    }

    private User fillUser(String userName, Document document) {

        AtomicReference<ArrayList<DBRef>> roles = new AtomicReference<ArrayList<DBRef>>(new ArrayList());
        AtomicReference<ArrayList<DBRef>> groups = new AtomicReference<ArrayList<DBRef>>(new ArrayList());
        roles.set((ArrayList) document.get("userRoles"));
        groups.set((ArrayList) document.get("userGroups"));
        List<Role> roleList = new ArrayList<>();
        for (DBRef dbRef : roles.get()) {
            Document roleDocument = Utils.getDocumentFromRef(dbRef, database);
            Role role = new RoleImpl(roleDocument.getString("name"));
            roleList.add(role);
        }
        List<Group> groupList = new ArrayList<>();
        for (DBRef dbRef : groups.get()) {
            Document groupDocument = Utils.getDocumentFromRef(dbRef, database);
            Group group = new GroupImpl(groupDocument.getString("name"));
            groupList.add(group);
        }
        User user = new UserImpl(userName, roleList, groupList);
        return user;
    }

    @Override
    public User create(User entity) throws SecurityManagementException {
        save(entity, true);
        return entity;
    }

    @Override
    public User update(User entity) throws SecurityManagementException {
        save(entity, false);
        return entity;
    }

    private void save(User entity, boolean isCreated) throws SecurityManagementException {
        MongoCollection<Document> userCollection = database.getCollection("user");
        MongoCollection<Document> userGroupsCollection = database.getCollection("userGroups");
        MongoCollection<Document> userRolesCollection = database.getCollection("userRoles");
        AtomicReference<ArrayList<DBRef>> roles = new AtomicReference<>(new ArrayList<>());
        AtomicReference<ArrayList<DBRef>> groups = new AtomicReference<>(new ArrayList<>());
        ArrayList<Document> users = new ArrayList<>();
        if (isCreated) {
            userCollection.find(eq("login", entity.getIdentifier())).forEach((Block<? super Document>) document -> {
                throw new SecurityManagementException("Existing identifier " + entity.getIdentifier());
            });
        } else {
            userCollection.find(eq("login", entity.getIdentifier())).forEach((Block<? super Document>) document -> {
                users.add(document);
            });
            if (users.size() == 0) {
                throw new SecurityManagementException("unknown identifier " + entity.getIdentifier());
            }
            if (users.size() > 1) {
                throw new SecurityManagementException("existing multiple times with  identifier " + entity.getIdentifier());
            }
        }


        for (Group group : entity.getGroups()) {

            userGroupsCollection.find(eq("name", group.getName())).forEach((Block<? super Document>) document -> {
                DBRef dbRef = new DBRef("userGroups", document.get("_id"));
                groups.get().add(dbRef);
            });
        }

        for (Role role : entity.getRoles()) {
            userRolesCollection.find(eq("name", role.getName())).forEach((Block<? super Document>) document -> {
                DBRef dbRef = new DBRef("userRoles", document.get("_id"));
                roles.get().add(dbRef);
            });
        }

        if (isCreated) {
            Document userDocument = new Document();
            userDocument.append("login", entity.getIdentifier());
            userDocument.append("password", entity.getIdentifier());
            userDocument.append("userRoles", roles);
            userDocument.append("userGroups", groups);

            userCollection.insertOne(userDocument);
        } else {
            userCollection.find(eq("login", entity.getIdentifier())).forEach((Block<? super Document>) document -> {
                document.append("userRoles", roles);
                document.append("userGroups", groups);
                userCollection.replaceOne(eq("login", entity.getIdentifier()), document);
            });
        }

    }

    @Override
    public void delete(String... identifiers) throws SecurityManagementException {


    }

    @Override
    public UserManagerSettings getSettings() {
        final Map<Capability, CapabilityStatus> capabilityStatusMap = new HashMap<Capability, CapabilityStatus>(8);
        for (final Capability capability : SecurityManagementUtils.USERS_CAPABILITIES) {
            capabilityStatusMap.put(capability,
                    getCapabilityStatus(capability));
        }
        return new UserManagerSettingsImpl(capabilityStatusMap,
                null);
    }

    @Override
    public void assignGroups(String username,
                             Collection<String> groups) throws SecurityManagementException {

    }

    @Override
    public void assignRoles(String username,
                            Collection<String> roles) throws SecurityManagementException {


    }

    private void doAssignGroups(String username,
                                Collection<String> ids) throws SecurityManagementException {

    }

    @Override
    public void changePassword(String username,
                               String newPassword) throws SecurityManagementException {
        MongoCollection<Document> userCollection = database.getCollection("user");

        userCollection.find(eq("login", username)).forEach((Block<? super Document>) document -> {
            document.append("password", newPassword);
            userCollection.replaceOne(eq("login", username), document);
        });


    }

    protected CapabilityStatus getCapabilityStatus(Capability capability) {

        if (capability != null) {
            switch (capability) {
                case CAN_SEARCH_USERS:
                case CAN_ADD_USER:
                case CAN_UPDATE_USER:
                case CAN_DELETE_USER:
                case CAN_READ_USER:
                case CAN_MANAGE_ATTRIBUTES:
                case CAN_ASSIGN_GROUPS:

                case CAN_ASSIGN_ROLES:
                case CAN_CHANGE_PASSWORD:
                    return CapabilityStatus.ENABLED;
            }
        }

        return CapabilityStatus.UNSUPPORTED;
    }
}
