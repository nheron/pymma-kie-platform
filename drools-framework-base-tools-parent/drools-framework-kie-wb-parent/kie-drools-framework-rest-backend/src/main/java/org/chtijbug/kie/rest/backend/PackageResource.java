package org.chtijbug.kie.rest.backend;


import org.chtijbug.guvnor.server.jaxrs.api.UserLoginInformation;
import org.chtijbug.guvnor.server.jaxrs.jaxb.Asset;
import org.chtijbug.guvnor.server.jaxrs.jaxb.Package;
import org.chtijbug.guvnor.server.jaxrs.model.DependencyData;
import org.chtijbug.guvnor.server.jaxrs.model.PlatformProjectData;
import org.chtijbug.guvnor.server.jaxrs.model.WorkspaceAuthData;
import org.chtijbug.kie.rest.backend.service.AssetService;
import org.guvnor.common.services.project.model.GAV;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.WorkspaceProject;
import org.guvnor.common.services.project.service.WorkspaceProjectService;
import org.guvnor.rest.backend.UserManagementResourceHelper;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.GroupImpl;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.authz.AuthorizationPolicyStorage;
import org.uberfire.backend.authz.AuthorizationService;
import org.uberfire.backend.events.AuthorizationPolicySavedEvent;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.base.options.CommentedOption;
import org.uberfire.java.nio.file.DirectoryStream;
import org.uberfire.java.nio.file.Paths;
import org.uberfire.security.authz.AuthorizationPolicy;
import org.uberfire.security.authz.Permission;
import org.uberfire.security.authz.PermissionManager;
import org.uberfire.security.impl.authz.AuthorizationPolicyBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.security.Principal;
import java.util.*;

@Path("/chtijbug")
@Named
@ApplicationScoped
public class PackageResource {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PackageResource.class);

    @Context
    protected UriInfo uriInfo;

    @Context
    protected SecurityContext sc;

    @Inject
    @Named("ioStrategy")
    private IOService ioService;
    @Inject
    private OrganizationalUnitService organizationalUnitService;
    @Inject
    private RepositoryService repositoryService;
    @Inject
    private WorkspaceProjectService projectService;
    private RestTypeDefinition dotFileFilter = new RestTypeDefinition();
    @Inject
    private DataModelerService dataModelerService;
    @Inject
    private WorkspaceProjectService workspaceProjectService;
    @Inject
    private AssetService assetService;
    @Inject
    private PermissionManager permissionManager;
    @Inject
    private AuthorizationService authorizationService;

    @Inject
    private UserManagementResourceHelper userManagementResourceHelper;

    @Inject
    private AuthorizationPolicyStorage authorizationPolicyStorage;
    @Inject
    private Event<AuthorizationPolicySavedEvent> savedEvent;

    public PackageResource() {
        System.out.println("coucou");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/login")
    public UserLoginInformation login() {

        UserLoginInformation userLoginInformation = new UserLoginInformation();

        userLoginInformation.setUsername(sc.getUserPrincipal().getName());
        for (String role : PermissionConstants.tableauChaine) {
            if (sc.isUserInRole(role) == true) {
                userLoginInformation.getRoles().add(role);
            }
        }
        return userLoginInformation;

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/content")
    public UserLoginInformation getUserContent() {

        UserLoginInformation userLoginInformation = new UserLoginInformation();

        userLoginInformation.setUsername(sc.getUserPrincipal().getName());
        for (String role : PermissionConstants.tableauChaine) {
            if (sc.isUserInRole(role) == true) {
                userLoginInformation.getRoles().add(role);
            }
        }
        userLoginInformation.setProjects(assetService.getAllProjects());
        return userLoginInformation;

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/detailedSpaces")
    // @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Collection<PlatformProjectData> getProjects() {
        logger.debug("-----getSpaces--- ");
        return assetService.getAllProjects();
    }

    @GET
    @Path("{organizationalUnitName}/{repositoryName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Collection<Package> getPackagesAsJAXB(@PathParam("organizationalUnitName") String organizationalUnitName, @PathParam("repositoryName") String repositoryName) {
        OrganizationalUnit organizationalUnit = organizationalUnitService.getOrganizationalUnit(organizationalUnitName);
        Collection<Repository> repositories = organizationalUnit.getRepositories();
        for (Repository repository : repositories) {
            if (repository.getAlias().equals(repositoryName)) {

                Optional<Branch> branch = repository.getDefaultBranch();
                Collection<WorkspaceProject> projects = projectService.getAllWorkspaceProjects(organizationalUnit);
                Collection<Package> packages = new ArrayList<>();
                for (WorkspaceProject project : projects) {
                    Package aPackage = new Package();
                    aPackage.setTitle(project.getName());
                    aPackage.setGroupID(project.getMainModule().getPom().getGav().getGroupId());
                    aPackage.setArtifactID(project.getMainModule().getPom().getGav().getArtifactId());
                    aPackage.setVersion(project.getMainModule().getPom().getGav().getVersion());

                    packages.add(aPackage);
                }
                return packages;
            }
        }
        return null;
    }


    @GET
    @Path("{organizationalUnitName}/{projectName}/assets")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Collection<Asset> getAssetsAsJAXB(
            @PathParam("organizationalUnitName") String organizationalUnitName, @PathParam("projectName") String projectName) {
        try {
            List<Asset> contentList = new LinkedList<>();
            WorkspaceProject project = assetService.getProject(organizationalUnitName, projectName);
            if (project != null) {
                org.uberfire.backend.vfs.Path rootPath = project.getRootPath();
                org.uberfire.java.nio.file.Path nioPath = Paths.get(rootPath.toURI());
                DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream = ioService.newDirectoryStream(nioPath);
                assetService.getContent(directoryStream, contentList);
            }


            return contentList;
        } catch (RuntimeException e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("{organizationalUnitName}/{projectName}/assets/{assetName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Collection<Asset> getAssetAsJaxB(
            @PathParam("organizationalUnitName") String organizationalUnitName, @PathParam("projectName") String projectName, @PathParam("assetName") String assetName) {
        List<Asset> resultList = new LinkedList<>();
        try {
            WorkspaceProject project = assetService.getProject(organizationalUnitName, projectName);
            if (project != null) {
                List<Asset> contentList = new LinkedList<>();
                org.uberfire.backend.vfs.Path rootPath = project.getRootPath();
                org.uberfire.java.nio.file.Path nioPath = Paths.get(rootPath.toURI());
                DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream = ioService.newDirectoryStream(nioPath);
                assetService.getContent(directoryStream, contentList);
                for (Asset asset : contentList) {
                    if (asset.getTitle().equals(assetName)) {
                        resultList.add(asset);
                    }
                }
            }
            return resultList;
        } catch (RuntimeException e) {
            throw new WebApplicationException(e);
        }

    }

    @GET
    @Path("{organizationalUnitName}/{projectName}/assets/{assetName}/source")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String getAssetSource(
            @PathParam("organizationalUnitName") String organizationalUnitName, @PathParam("projectName") String projectName, @PathParam("assetName") String assetName) {
        List<Asset> resultList = new LinkedList<>();
        String result = "";
        try {
            WorkspaceProject project = assetService.getProject(organizationalUnitName, projectName);
            if (project != null) {
                org.uberfire.backend.vfs.Path rootPath = project.getRootPath();
                org.uberfire.java.nio.file.Path nioPath = Paths.get(rootPath.toURI());
                DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream = ioService.newDirectoryStream(nioPath);
                result = assetService.getContentSource(directoryStream, assetName);
            }
            return result;
        } catch (RuntimeException e) {
            throw new WebApplicationException(e);
        }

    }

    @PUT
    @Path("{organizationalUnitName}/{projectName}/asset/{assetName}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateAssetFromJAXB(@Context HttpHeaders headers,
                                        @PathParam("organizationalUnitName") String organizationalUnitName, @PathParam("projectName") String projectName,
                                        @PathParam("assetName") String assetName, String asset) {
        return updateAssetContent(headers, organizationalUnitName, projectName, assetName, asset, true);
    }

    @POST
    @Path("{organizationalUnitName}/{projectName}/newAsset")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Asset createAssetFromSourceAndJAXB(
            @PathParam("organizationalUnitName") String organizationalUnitName, @PathParam("projectName") String projectName, Asset asset) {
        try {
            WorkspaceProject project = assetService.getProject(organizationalUnitName, projectName);
            if (project != null) {
                org.uberfire.backend.vfs.Path rootPath = project.getRootPath();
                org.uberfire.java.nio.file.Path nioPathDirectory = Paths.get(rootPath.toURI());
                DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream = null;
                try {
                    directoryStream = ioService.newDirectoryStream(nioPathDirectory);
                    org.uberfire.java.nio.file.Path directoryWhereCreateAsset = assetService.getDirectoryElementPath(directoryStream, asset.getTitle());
                    if (directoryWhereCreateAsset != null) {
                        final org.uberfire.java.nio.file.Path nioPath = Paths.get(directoryWhereCreateAsset.toUri());
                        if (ioService.exists(nioPath)) {
                            throw new FileAlreadyExistsException(nioPath.toString());
                        }
                        CommentedOption commentedOption = new CommentedOption(asset.getComment());
                        ioService.write(nioPath, asset.getContent().getBytes(), commentedOption);
                    }
                } catch (Exception e) {

                } finally {
                    if (directoryStream != null) {
                        directoryStream.close();
                    }
                }


            }
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
        return null;
    }

    @POST
    @Path("{organizationalUnitName}/{projectName}/asset/{assetName}/source")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response createAssetSource(@Context HttpHeaders headers,
                                      @PathParam("organizationalUnitName") String organizationalUnitName, @PathParam("projectName") String projectName, @PathParam("assetName") String assetName, String content) {

        return updateAssetContent(headers, organizationalUnitName, projectName, assetName, content, true);
    }

    @PUT
    @Path("{organizationalUnitName}/{projectName}/asset/{assetName}/source")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response updateAssetSource(@Context HttpHeaders headers,
                                      @PathParam("organizationalUnitName") String organizationalUnitName, @PathParam("projectName") String projectName, @PathParam("assetName") String assetName, String content) {

        return updateAssetContent(headers, organizationalUnitName, projectName, assetName, content, false);
    }

    @DELETE
    @Path("{organizationalUnitName}/{projectName}/asset/{assetName}/source")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response deleteAssetSource(@Context HttpHeaders headers,
                                      @PathParam("organizationalUnitName") String organizationalUnitName, @PathParam("projectName") String projectName, @PathParam("assetName") String assetName, String content) {

        WorkspaceProject project = assetService.getProject(organizationalUnitName, projectName);

        if (project != null) {
            // Optional<Branch> rr = project.getRepository().getBranch("ee");
            // org.uberfire.backend.vfs.Path tata = rr.get().getPath();
            org.uberfire.backend.vfs.Path rootPath = project.getRootPath();
            org.uberfire.java.nio.file.Path nioPath = Paths.get(rootPath.toURI());

            DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream = ioService.newDirectoryStream(nioPath);
            org.uberfire.java.nio.file.Path elementToDelete = assetService.getFileElementPath(directoryStream, assetName);
            if (elementToDelete == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            } else {

                File fileToUpdate = elementToDelete.toFile();
                if (fileToUpdate.isFile()) {
                    content = content.replace("\"", "");
                    ioService.delete(elementToDelete);
                    logger.debug("Returning OK response with content '{}'", content);
                    return Response.status(Response.Status.NO_CONTENT).build();
                } else {
                    return Response.status(Response.Status.NOT_MODIFIED).entity("Asset is not a file").build();
                }

            }
        } else {
            logger.info("Project {} or Organization {} not found ", projectName, organizationalUnitName);
            return Response.status(Response.Status.NOT_FOUND).entity("Project  or Organization  not found").build();
        }
    }


    private Response updateAssetContent(HttpHeaders headers, String organizationalUnitName, String projectName, String assetName, String content, boolean isCreate) {

        try {

            WorkspaceProject project = assetService.getProject(organizationalUnitName, projectName);

            if (project != null) {
                // Optional<Branch> rr = project.getRepository().getBranch("ee");
                // org.uberfire.backend.vfs.Path tata = rr.get().getPath();
                org.uberfire.backend.vfs.Path rootPath = project.getRootPath();
                org.uberfire.java.nio.file.Path nioPath = Paths.get(rootPath.toURI());

                DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream = ioService.newDirectoryStream(nioPath);
                org.uberfire.java.nio.file.Path elementToUpdate = assetService.getFileElementPath(directoryStream, assetName);
                if (elementToUpdate != null && isCreate) {
                    return Response.status(Response.Status.CONFLICT).build();
                } else if (elementToUpdate != null && !isCreate) {
                    File fileToUpdate = elementToUpdate.toFile();
                    if (fileToUpdate.isFile()) {
                        content = content.replace("\"", "");
                        ioService.write(elementToUpdate, content);
                        logger.debug("Returning OK response with content '{}'", content);
                        return Response.status(Response.Status.ACCEPTED).build();
                    } else {
                        return Response.status(Response.Status.NOT_MODIFIED).entity("Asset is not a file").build();
                    }
                } else {//
                    if (isCreate) {
                        String targetName = projectName.replace("-", "_").replace(" ", "_");
                        org.uberfire.java.nio.file.Path ressourcesPath = nioPath.resolve("src/main/resources");
                        if (assetName.contains(".java")){
                            ressourcesPath = nioPath.resolve("src/main/java");
                            content=content.replace("\""," ").replace("\\n"," ").replace("\\t"," ");

                        }

                        DirectoryStream<org.uberfire.java.nio.file.Path> directoryStreamBase = ioService.newDirectoryStream(ressourcesPath);
                        org.uberfire.java.nio.file.Path directoryWhereCreateAsset = assetService.getRuleDirectoryByName(directoryStreamBase, targetName);

                        if (directoryWhereCreateAsset != null) {
                            URI parentURI = directoryWhereCreateAsset.toUri();
                            URI uri = new URI(parentURI.getScheme(), parentURI.getUserInfo(), parentURI.getHost(), parentURI.getPort(), parentURI.getPath() + "/" + assetName, parentURI.getQuery(), parentURI.getFragment());
                            final org.uberfire.java.nio.file.Path nioPathWhere = Paths.get(uri);
                            CommentedOption commentedOption = new CommentedOption("Created from rest");
                            ioService.write(nioPathWhere, content.getBytes(), commentedOption);
                            return Response.status(Response.Status.CREATED).build();
                        } else {
                            return Response.status(Response.Status.NOT_FOUND).entity("no Rule package").build();
                        }
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity("Asset not found").build();
                    }
                }
            } else {
                logger.info("Project {} or Organization {} not found ", projectName, organizationalUnitName);
                return Response.status(Response.Status.NOT_FOUND).entity("Project  or Organization  not found").build();
            }
        } catch (RuntimeException | URISyntaxException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }

    @PUT
    @Path("{organizationalUnitName}/{projectName}/dependency")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response updateProjectDependencies(@Context HttpHeaders headers,
                                              @PathParam("organizationalUnitName") String organizationalUnitName,
                                              @PathParam("projectName") String projectName, PlatformProjectData request) {

        try {

            WorkspaceProject project = assetService.getProject(organizationalUnitName, projectName);
            List<DependencyData> toAdd = new ArrayList<>();
            if (project != null) {
                POM pom = project.getMainModule().getPom();
                for (DependencyData dependencyData : request.getDependencies()) {
                    for (GAV element : pom.getDependencies().getGavs()) {
                        if (element.getGroupId().equals(dependencyData.getGroupId())
                                && element.getArtifactId().equals(dependencyData.getArtifactId())
                                && element.getVersion().equals(dependencyData.getVersion())) {
                            toAdd.add(dependencyData);
                            break;
                        }
                    }
                }
                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append("\n");
                for (DependencyData dependencyData : toAdd) {

                    stringBuilder.append("<dependency>").append("\n");
                    stringBuilder.append("\t").append("<groupId>").append(dependencyData.getGroupId()).append("</groupId>").append("\n");
                    stringBuilder.append("\t").append("<artifactId>").append(dependencyData.getArtifactId()).append("</artifactId>").append("\n");
                    stringBuilder.append("\t").append("<version>").append(dependencyData.getVersion()).append("</version>").append("\n");
                    stringBuilder.append("</dependency>").append("\n").append("\n");
                }
                stringBuilder.append("\n");
                org.uberfire.backend.vfs.Path pomPath = project.getMainModule().getPomXMLPath();
                org.uberfire.java.nio.file.Path nioPath = Paths.get(pomPath.toURI());
                String pomContent = ioService.readAllString(nioPath);
                int dependInt = pomContent.indexOf("/dependencies");
                String newPomContent = pomContent.substring(0, dependInt - 1) + stringBuilder.toString() + pomContent.substring(dependInt - 1, pomContent.length());
                CommentedOption commentedOption = new CommentedOption("Added from rest");
                ioService.write(nioPath, newPomContent.getBytes(), commentedOption);
                if (request.getkModule() != null) {
                    String kbase = "kbase";
                    if (request.getkModule().getKbase() != null
                            && !request.getkModule().getKbase().isEmpty()) {
                        kbase = request.getkModule().getKbase();
                    }
                    String basePackage = pom.getGav().getGroupId() + "." + projectName.replace("-", "_");

                    org.uberfire.backend.vfs.Path rootPath = project.getRootPath();
                    org.uberfire.java.nio.file.Path nioRootPath = Paths.get(rootPath.toURI());
                    DirectoryStream<org.uberfire.java.nio.file.Path> directoryRootStream = ioService.newDirectoryStream(nioRootPath);
                    org.uberfire.java.nio.file.Path kmodulePath = assetService.findFileByName(directoryRootStream, "kmodule.xml");
                    String kmoduleContent = ioService.readAllString(kmodulePath);
                    /**
                     * <?xml version="1.0" encoding="UTF-8"?>
                     * <kmodule xmlns="http://jboss.org/kie/6.0.0/kmodule">
                     *     <kbase name="kbase-extension"  packages="com.adeo.lys.rules" includes="kbase-base">
                     *          <ksession name="session-extension" type="stateful" default="false" clockType="realtime"/>
                     *     </kbase>
                     * </kmodule>
                     */
                    StringBuilder kModuleBuilder = new StringBuilder();
                    kModuleBuilder.append("<kmodule xmlns=\"http://jboss.org/kie/6.0.0/kmodule\">").append("\n");
                    kModuleBuilder.append("\t").append("<kbase name=\"").append(kbase).append("\"   default=\"true\" eventProcessingMode=\"stream\" equalsBehavior=\"identity\" packages=\"").append(basePackage).append("\" includes=\"").append(request.getkModule().getKbaseToInclude()).append("\">").append("\n");
                    kModuleBuilder.append("\t").append("\t").append("  <ksession name=\"session-extension\" type=\"stateful\" default=\"false\" clockType=\"realtime\"/>").append("\n");
                    kModuleBuilder.append("\t").append("</kbase>").append("\n");
                    kModuleBuilder.append("</kmodule>").append("\n");
                    kmoduleContent = kModuleBuilder.toString();
                    CommentedOption commentedOption2 = new CommentedOption("Added from rest");
                    ioService.write(kmodulePath, kmoduleContent.getBytes(), commentedOption2);
                    logger.info("Kmodule updated");
                }
                return Response.status(Response.Status.CREATED).entity(request).build();
            } else {
                logger.info("Project {} or Organization {} not found ", projectName, organizationalUnitName);
                return Response.status(Response.Status.NOT_FOUND).entity(request).build();
            }
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/auth")
    public AuthorizationPolicy getAuth() {
        Principal Principam = sc.getUserPrincipal();
        AuthorizationPolicy authorizationPolicy = this.permissionManager.getAuthorizationPolicy();
        return authorizationPolicy;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/auth/{groupName}/{organisationUnit}")
    public Response createGroupAuthorization(@Context HttpHeaders headers,
                                             @PathParam("groupName") String groupName,
                                             @PathParam("organisationUnit") String organisationUnit) {

        Group targetGroup = null;
        AuthorizationPolicy storedPolicies = this.authorizationPolicyStorage.loadPolicy();
        for (Group group : storedPolicies.getGroups()) {
            if (group.getName().equals(groupName)) {
                targetGroup = group;
            }
        }

        if (targetGroup == null) {
            targetGroup = new GroupImpl(groupName);
            AuthorizationPolicyBuilder groupPermissionBuilder = permissionManager.newAuthorizationPolicy().group(groupName);


            groupPermissionBuilder = groupPermissionBuilder.permission("editor.read", true);
           // groupPermissionBuilder = groupPermissionBuilder.permission("dataobject.edit", true);
            //groupPermissionBuilder = groupPermissionBuilder.permission("editor.read.BPMNDiagramEditor", true);
            //groupPermissionBuilder = groupPermissionBuilder.permission("editor.read.CaseManagementDiagramEditor", true);
            //groupPermissionBuilder = groupPermissionBuilder.permission("editor.read.GuidedDecisionTreeEditorPresenter", true);
            //groupPermissionBuilder = groupPermissionBuilder.permission("editor.read.GuidedScoreCardEditor", true);
            //groupPermissionBuilder = groupPermissionBuilder.permission("editor.read.ScoreCardXLSEditor", true);

            groupPermissionBuilder = groupPermissionBuilder.permission("globalExperimentalFeatures.edit", true);
            // groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "globalpreferences.edit", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("guideddecisiontable.edit.columns", true);
            groupPermissionBuilder = groupPermissionBuilder.permission("jar.download", true);
            //  groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "orgunit.create", false);
            //  groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "orgunit.delete", false);
            //  groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "orgunit.read", false);

            groupPermissionBuilder = groupPermissionBuilder.permission("orgunit.read." + organisationUnit, true);
            //   groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "orgunit.update", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("orgunit.update." + organisationUnit, true);
            // groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "perspective.create", false);
            // groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "perspective.delete", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("perspective.read", true);
            // groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "perspective.update", false);
            //  groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "planner.available", false);
            // groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "profilepreferences.edit", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("project.build", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("project.create", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("project.delete", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("project.read", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("project.release", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("project.update", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("repository.build", true);
            groupPermissionBuilder = groupPermissionBuilder.permission("repository.configure", true);
            groupPermissionBuilder = groupPermissionBuilder.permission("repository.create", true);
            groupPermissionBuilder = groupPermissionBuilder.permission("repository.delete", true);
            groupPermissionBuilder = groupPermissionBuilder.permission("repository.read", true);
            groupPermissionBuilder = groupPermissionBuilder.permission("repository.update", true);
            //groupPermissionBuilder = groupPermissionBuilder.priority(-10);

            for (Permission p : groupPermissionBuilder.build().getPermissions(targetGroup).collection()) {
                storedPolicies.addPermission(targetGroup, p);
            }
            storedPolicies.setHomePerspective(targetGroup,"AuthoringPerspective");
            storedPolicies.setPriority(targetGroup,-10);
            this.authorizationPolicyStorage.savePolicy(storedPolicies);
            permissionManager.setAuthorizationPolicy(storedPolicies);
            savedEvent.fire(new AuthorizationPolicySavedEvent(storedPolicies));
        } else {
            targetGroup = new GroupImpl(groupName);
            AuthorizationPolicyBuilder groupPermissionBuilder = permissionManager.newAuthorizationPolicy().group(groupName);


            groupPermissionBuilder = groupPermissionBuilder.permission("editor.read", true);
            // groupPermissionBuilder = groupPermissionBuilder.permission("dataobject.edit", true);
            //groupPermissionBuilder = groupPermissionBuilder.permission("editor.read.BPMNDiagramEditor", true);
            //groupPermissionBuilder = groupPermissionBuilder.permission("editor.read.CaseManagementDiagramEditor", true);
            //groupPermissionBuilder = groupPermissionBuilder.permission("editor.read.GuidedDecisionTreeEditorPresenter", true);
            //groupPermissionBuilder = groupPermissionBuilder.permission("editor.read.GuidedScoreCardEditor", true);
            //groupPermissionBuilder = groupPermissionBuilder.permission("editor.read.ScoreCardXLSEditor", true);

            groupPermissionBuilder = groupPermissionBuilder.permission("globalExperimentalFeatures.edit", true);
            // groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "globalpreferences.edit", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("guideddecisiontable.edit.columns", true);
            groupPermissionBuilder = groupPermissionBuilder.permission("jar.download", true);
            //  groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "orgunit.create", false);
            //  groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "orgunit.delete", false);
            //  groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "orgunit.read", false);

            groupPermissionBuilder = groupPermissionBuilder.permission("orgunit.read." + organisationUnit, true);
            //   groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "orgunit.update", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("orgunit.update." + organisationUnit, true);
            // groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "perspective.create", false);
            // groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "perspective.delete", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("perspective.read", true);
            // groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "perspective.update", false);
            //  groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "planner.available", false);
            // groupPermissionBuilder = groupPermissionBuilder.permission(groupPermissionbase + "profilepreferences.edit", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("project.build", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("project.create", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("project.delete", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("project.read", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("project.release", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("project.update", false);
            groupPermissionBuilder = groupPermissionBuilder.permission("repository.build", true);
            groupPermissionBuilder = groupPermissionBuilder.permission("repository.configure", true);
            groupPermissionBuilder = groupPermissionBuilder.permission("repository.create", true);
            groupPermissionBuilder = groupPermissionBuilder.permission("repository.delete", true);
            groupPermissionBuilder = groupPermissionBuilder.permission("repository.read", true);
            groupPermissionBuilder = groupPermissionBuilder.permission("repository.update", true);
            //groupPermissionBuilder = groupPermissionBuilder.priority(-10);

            for (Permission p : groupPermissionBuilder.build().getPermissions(targetGroup).collection()) {
                storedPolicies.addPermission(targetGroup, p);
            }
            storedPolicies.setHomePerspective(targetGroup,"AuthoringPerspective");
            storedPolicies.setPriority(targetGroup,-10);
            this.authorizationPolicyStorage.savePolicy(storedPolicies);
            permissionManager.setAuthorizationPolicy(storedPolicies);
            savedEvent.fire(new AuthorizationPolicySavedEvent(storedPolicies));
        }
        WorkspaceAuthData result=new WorkspaceAuthData();
        return Response.status(Response.Status.OK).entity(result).build();
    }


}
