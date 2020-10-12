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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.GroupImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.config.ConfigProperties;
import org.uberfire.ext.security.management.api.*;
import org.uberfire.ext.security.management.api.exception.SecurityManagementException;
import org.uberfire.ext.security.management.impl.GroupManagerSettingsImpl;
import org.uberfire.ext.security.management.impl.SearchResponseImpl;
import org.uberfire.ext.security.management.search.GroupsIdentifierRuntimeSearchEngine;
import org.uberfire.ext.security.management.search.IdentifierRuntimeSearchEngine;
import org.uberfire.ext.security.management.util.SecurityManagementUtils;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

/**
 * <p>Groups manager service provider implementation for Apache tomcat, when using default realm based on properties files.</p>
 * @since 0.8.0
 */
public class KiePlatformGroupManager  implements GroupManager, ContextualManager {

    private static final Logger LOG = LoggerFactory.getLogger(KiePlatformGroupManager.class);



    IdentifierRuntimeSearchEngine<Group> groupsSearchEngine;

    private MongoClient mongoClient;
    private CodecRegistry pojoCodecRegistry;
    private MongoDatabase database;


    public KiePlatformGroupManager() {
        this(new ConfigProperties(System.getProperties()));
    }


    public KiePlatformGroupManager(final Map<String, String> gitPrefs) {
        this(new ConfigProperties(gitPrefs));
    }

    public KiePlatformGroupManager(final ConfigProperties gitPrefs) {
      //  loadConfig(gitPrefs);
    }

    public void setMongo (MongoClient mongoClient,CodecRegistry pojoCodecRegistry,MongoDatabase database){
        this.mongoClient=mongoClient;
        this.pojoCodecRegistry = pojoCodecRegistry;
        this.database=database;

    }
    @Override
    public void initialize(UserSystemManager userSystemManager) throws Exception {
        groupsSearchEngine = new GroupsIdentifierRuntimeSearchEngine();
    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public SearchResponse<Group> search(SearchRequest request) throws SecurityManagementException {
        MongoCollection<Document> userCollection = database.getCollection("userGroups");
        BasicDBObject regexQuery = new BasicDBObject();
        regexQuery.put("name", new BasicDBObject("$regex", request.getSearchPattern() + ".*").append("$options", "i"));
        List<Group> groups = new ArrayList<>();
        long totalNumber = userCollection.countDocuments(regexQuery);
        FindIterable<Document> documents = userCollection.find(regexQuery).skip(request.getPageSize() * (request.getPage() - 1)).limit(request.getPageSize());
        documents.forEach((Block<? super Document>) document -> {
            String groupName = document.getString("name");
            Group group = new GroupImpl(groupName);
            groups.add(group);
        });
        boolean hasNextPage = true;
        if ((request.getPageSize() * (request.getPage()) > totalNumber)) {
            hasNextPage = false;
        }
        SearchResponse<Group> response = new SearchResponseImpl(groups, request.getPage(), request.getPageSize(), Long.valueOf(totalNumber).intValue(), hasNextPage);
        return response;
    }

    @Override
    public Group get(String identifier) throws SecurityManagementException {
        MongoCollection<Document> userCollection = database.getCollection("userGroups");
        List<Group> groups = new ArrayList<>();
        userCollection.find(eq("name", identifier)).forEach((Block<? super Document>) document -> {
            String groupName = document.getString("name");
            Group group = new GroupImpl(groupName);
            groups.add(group);
        });
        if (groups.size() == 1) {
            return groups.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<Group> getAll() throws SecurityManagementException {
        List<Group> groups = new ArrayList<>();
        MongoCollection<Document> userGroupsCollection = database.getCollection("userGroups");
        userGroupsCollection.find().forEach((Block<? super Document>) document -> {
            String groupName = document.getString("name");
            Group group = new GroupImpl(groupName);
            groups.add(group);
        });
        return groups;
    }


    @Override
    public Group create(Group entity) throws SecurityManagementException {
        return entity;
    }

    @Override
    public Group update(Group entity) throws SecurityManagementException {
      return entity;
    }

    @Override
    public void delete(String... identifiers) throws SecurityManagementException {

    }

    @Override
    public GroupManagerSettings getSettings() {
         final Map<Capability, CapabilityStatus> capabilityStatusMap = new HashMap<Capability, CapabilityStatus>(8);
        for (final Capability capability : SecurityManagementUtils.GROUPS_CAPABILITIES) {
            capabilityStatusMap.put(capability,
                                    getCapabilityStatus(capability));
        }
        return new GroupManagerSettingsImpl(capabilityStatusMap,
                                            true);
    }

    protected CapabilityStatus getCapabilityStatus(Capability capability) {

        if (capability != null) {
            switch (capability) {
                case CAN_SEARCH_GROUPS:
                case CAN_ADD_GROUP:
                case CAN_UPDATE_GROUP:
                case CAN_READ_GROUP:
                case CAN_DELETE_GROUP:
                    return CapabilityStatus.ENABLED;
            }
        }
         return CapabilityStatus.UNSUPPORTED;
    }

    @Override
    public void assignUsers(String name,
                            Collection<String> users) throws SecurityManagementException {

    }
}
