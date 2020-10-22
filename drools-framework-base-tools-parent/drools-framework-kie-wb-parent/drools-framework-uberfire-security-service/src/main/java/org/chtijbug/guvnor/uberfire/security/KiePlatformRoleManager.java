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
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.config.ConfigProperties;
import org.uberfire.ext.security.management.api.*;
import org.uberfire.ext.security.management.api.exception.SecurityManagementException;
import org.uberfire.ext.security.management.impl.RoleManagerSettingsImpl;
import org.uberfire.ext.security.management.impl.SearchResponseImpl;
import org.uberfire.ext.security.management.util.SecurityManagementUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

/**
 * <p>Groups manager service provider implementation for Apache tomcat, when using default realm based on properties files.</p>
 *
 * @since 0.8.0
 */
public class KiePlatformRoleManager implements RoleManager, ContextualManager {

    private static final Logger LOG = LoggerFactory.getLogger(KiePlatformRoleManager.class);


    private MongoClient mongoClient;
    private CodecRegistry pojoCodecRegistry;
    private MongoDatabase database;

    public KiePlatformRoleManager() {
        this(new ConfigProperties(System.getProperties()));
    }


    public KiePlatformRoleManager(final Map<String, String> gitPrefs) {
        this(new ConfigProperties(gitPrefs));
    }

    public KiePlatformRoleManager(final ConfigProperties gitPrefs) {
        //  loadConfig(gitPrefs);
    }

    public void setMongo(MongoClient mongoClient, CodecRegistry pojoCodecRegistry, MongoDatabase database) {
        this.mongoClient = mongoClient;
        this.pojoCodecRegistry = pojoCodecRegistry;
        this.database = database;

    }

    @Override
    public void initialize(UserSystemManager userSystemManager) throws Exception {

    }

    @Override
    public void destroy() throws Exception {

    }


    @Override
    public SearchResponse<Role> search(SearchRequest request) throws SecurityManagementException {
        MongoCollection<Document> roleCollection = database.getCollection("userRoles");
        BasicDBObject regexQuery = new BasicDBObject();
        regexQuery.put("name", new BasicDBObject("$regex", request.getSearchPattern() + ".*").append("$options", "i"));
        List<Role> roles = new ArrayList<>();
        long totalNumber = roleCollection.countDocuments(regexQuery);
        FindIterable<Document> documents = roleCollection.find(regexQuery).skip(request.getPageSize() * (request.getPage() - 1)).limit(request.getPageSize());
        documents.forEach((Block<? super Document>) document -> {
            String roleName = document.getString("name");
            Role role = new RoleImpl(roleName);
            roles.add(role);
        });
        boolean hasNextPage = true;
        if ((request.getPageSize() * (request.getPage()) > totalNumber)) {
            hasNextPage = false;
        }
        SearchResponse<Role> response = new SearchResponseImpl(roles, request.getPage(), request.getPageSize(), Long.valueOf(totalNumber).intValue(), hasNextPage);
        return response;
    }

    @Override
    public Role get(String identifier) throws SecurityManagementException {
        MongoCollection<Document> userCollection = database.getCollection("userRoles");
        List<Role> roles = new ArrayList<>();
        userCollection.find(eq("name", identifier)).forEach((Block<? super Document>) document -> {
            String roleName = document.getString("name");
            Role role = new RoleImpl(roleName);
            roles.add(role);
        });
        if (roles.size() == 1) {
            return roles.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<Role> getAll() throws SecurityManagementException {

        MongoCollection<Document> userRolesCollection = database.getCollection("userRoles");
        List<Role> roles = new ArrayList<>();
        userRolesCollection.find().forEach((Block<? super Document>) document -> {
            String roleName = document.getString("name");
            RoleImpl role = new RoleImpl(roleName);
            roles.add(role);
        });

        return roles;
    }

    @Override
    public Role create(Role entity) throws SecurityManagementException {
        return entity;
    }

    @Override
    public Role update(Role entity) throws SecurityManagementException {
        return entity;
    }


    @Override
    public void delete(String... identifiers) throws SecurityManagementException {

    }

    @Override
    public RoleManagerSettings getSettings() {
        final Map<Capability, CapabilityStatus> capabilityStatusMap = new HashMap<>(8);
        for (final Capability capability : SecurityManagementUtils.ROLES_CAPABILITIES) {
            capabilityStatusMap.put(capability,
                    getCapabilityStatus(capability));
        }
        return new RoleManagerSettingsImpl(capabilityStatusMap);
    }

    protected CapabilityStatus getCapabilityStatus(Capability capability) {

        if (capability != null) {
            switch (capability) {
                case CAN_SEARCH_ROLES:
                case CAN_READ_ROLE:
                    return CapabilityStatus.ENABLED;
                case CAN_ADD_ROLE:
                case CAN_UPDATE_ROLE:
                case CAN_DELETE_ROLE:
                    return CapabilityStatus.UNSUPPORTED;
            }
        }

        return CapabilityStatus.UNSUPPORTED;
    }


}
