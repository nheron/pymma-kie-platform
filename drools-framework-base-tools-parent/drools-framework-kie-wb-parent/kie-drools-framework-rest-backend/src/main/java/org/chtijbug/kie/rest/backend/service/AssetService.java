package org.chtijbug.kie.rest.backend.service;

import org.chtijbug.guvnor.server.jaxrs.jaxb.Asset;
import org.chtijbug.guvnor.server.jaxrs.model.PlatformProjectData;
import org.chtijbug.kie.rest.backend.RestTypeDefinition;
import org.guvnor.common.services.project.model.Module;
import org.guvnor.common.services.project.model.WorkspaceProject;
import org.guvnor.common.services.project.service.WorkspaceProjectService;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.PublicURI;
import org.guvnor.structure.repositories.RepositoryService;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.screens.datamodeller.model.EditorModelContent;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.impl.DataModelImpl;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.DirectoryStream;
import org.uberfire.java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class AssetService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AssetService.class);

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

    public List<PlatformProjectData> getAllProjects() {
        final List<PlatformProjectData> spaces = new ArrayList<>();
        for (OrganizationalUnit ou : organizationalUnitService.getOrganizationalUnits()) {
            spaces.addAll(getSpace(ou));
        }

        return spaces;
    }

    private List<PlatformProjectData> getSpace(OrganizationalUnit ou) {

        final List<PlatformProjectData> repoNames = new ArrayList<>();
        try {
            for (WorkspaceProject workspaceProject : workspaceProjectService.getAllWorkspaceProjects(ou)) {
                for (Branch branch : workspaceProject.getRepository().getBranches()) {
                    repoNames.add(getProjectResponse(workspaceProject, branch));
                }
            }
        }catch (Exception e){
            logger.info("getSpace error "+ou.getName(),e);
        }
        return repoNames;
    }

    public void todo(){
       // workspaceProjectService.
    }



    private PlatformProjectData getProjectResponse(WorkspaceProject workspaceProject, Branch branch) {
        final PlatformProjectData projectResponse = new PlatformProjectData();
        projectResponse.setName(workspaceProject.getName());
        projectResponse.setSpaceName(workspaceProject.getOrganizationalUnit().getName());
        String wbName=System.getProperty("org.chtijbug.wbname");
        if (wbName==null || wbName.length()==0)
            wbName="demo";
        projectResponse.setWbName(wbName);
        if (workspaceProject.getMainModule() != null) {
            Module kmodule = workspaceProject.getMainModule();
            org.uberfire.backend.vfs.Path importVFPath = PathFactory.newPath("project.imports", branch.getPath().toURI() + "project.imports");
            EditorModelContent econtent = dataModelerService.loadContent(importVFPath);
            //EditorModelContent econtent = dataModelerService.loadContent(((KieModule) kmodule).getImportsPath());
            DataModelImpl econtentDataModel = (DataModelImpl) econtent.getDataModel();
            List<DataObject> dataObjects = econtentDataModel.getExternalClasses();
            for (DataObject dataObject : dataObjects) {
                System.out.println(dataObject.toString());
                String className = "class=" + dataObject.getPackageName() + "." + dataObject.getName();
                projectResponse.getJavaClasses().add(className);

            }
            projectResponse.setArtifactId(workspaceProject.getMainModule().getPom().getGav().getArtifactId());
            projectResponse.setGroupId(workspaceProject.getMainModule().getPom().getGav().getGroupId());
            projectResponse.setVersion(workspaceProject.getMainModule().getPom().getGav().getVersion());
            projectResponse.setDescription(workspaceProject.getMainModule().getPom().getDescription());
            projectResponse.setBranch(branch.getName());
        }

        final ArrayList<org.guvnor.rest.client.PublicURI> publicURIs = new ArrayList<>();

        for (PublicURI publicURI : workspaceProject.getRepository().getPublicURIs()) {
            final org.guvnor.rest.client.PublicURI responseURI = new org.guvnor.rest.client.PublicURI();
            responseURI.setProtocol(publicURI.getProtocol());
            responseURI.setUri(publicURI.getURI());
            publicURIs.add(responseURI);
        }

        projectResponse.setPublicURIs(publicURIs);
        return projectResponse;
    }


    public WorkspaceProject getProject(String organizationalUnitName, String projectName) {
        OrganizationalUnit organizationalUnit = organizationalUnitService.getOrganizationalUnit(organizationalUnitName);
        if (organizationalUnit==null){
            return null;
        }
        Collection<WorkspaceProject> workspaceProjects = projectService.getAllWorkspaceProjects(organizationalUnit);
        if (workspaceProjects==null){
            return null;
        }

        for (WorkspaceProject project : workspaceProjects) {
            if (project.getName().equals(projectName)) {
                return project;
            }
        }
        // }
        //}
        return null;
    }


    private void getContentSource(DirectoryStream<Path> directoryStream, Asset asset, List<org.uberfire.java.nio.file.Path> pathLinkedList) {
        for (org.uberfire.java.nio.file.Path elementPath : directoryStream) {
            if (org.uberfire.java.nio.file.Files.isDirectory(elementPath)) {
                DirectoryStream<org.uberfire.java.nio.file.Path> adirectoryStream = ioService.newDirectoryStream(elementPath);
                getContentSource(adirectoryStream, asset, pathLinkedList);
            } else {
                if (dotFileFilter.accept(elementPath.getFileName().toString())) {
                    Map<String, Object> listAttributes = ioService.readAttributes(elementPath);
                    if (asset.getTitle().equals(elementPath.getFileName().toString())) {
                        pathLinkedList.add(elementPath);
                    }
                }
            }
        }
    }

    public void getContent(DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream, Collection<Asset> contentList) {
        for (org.uberfire.java.nio.file.Path elementPath : directoryStream) {
            if (org.uberfire.java.nio.file.Files.isDirectory(elementPath)) {
                DirectoryStream<org.uberfire.java.nio.file.Path> adirectoryStream = ioService.newDirectoryStream(elementPath);
                getContent(adirectoryStream, contentList);
            } else {
                if (dotFileFilter.accept(elementPath.getFileName().toString())) {
                    Map<String, Object> listAttributes = ioService.readAttributes(elementPath);
                    Asset asset = new Asset();
                    asset.setTitle(elementPath.getFileName().toString());
                    asset.setDirectory(elementPath.getParent().toString());
                    asset.setRefLink(elementPath.getFileName().toUri());
                    contentList.add(asset);
                }
            }
        }
    }

    public String getContentSource(DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream, String assetName) {
        for (org.uberfire.java.nio.file.Path elementPath : directoryStream) {
            if (org.uberfire.java.nio.file.Files.isDirectory(elementPath)) {
                DirectoryStream<org.uberfire.java.nio.file.Path> adirectoryStream = ioService.newDirectoryStream(elementPath);
                String result = getContentSource(adirectoryStream, assetName);
                if (result != null && result.length() > 0) {
                    return result;
                }
            } else {

                if (elementPath.getFileName().toString().startsWith(".") == false) {
                    if (elementPath.getFileName().toString().equals(assetName)) {
                        return ioService.readAllString(elementPath);
                    }
                }
            }
        }
        return null;
    }

    public org.uberfire.java.nio.file.Path getFileElementPath(DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream, String assetName) {
        for (org.uberfire.java.nio.file.Path elementPath : directoryStream) {
            if (org.uberfire.java.nio.file.Files.isDirectory(elementPath)) {
                DirectoryStream<org.uberfire.java.nio.file.Path> adirectoryStream = ioService.newDirectoryStream(elementPath);
                org.uberfire.java.nio.file.Path foundElementPath = getFileElementPath(adirectoryStream, assetName);
                if (foundElementPath != null) {
                    return foundElementPath;
                }
            } else {
                if (dotFileFilter.accept(elementPath.getFileName().toString())
                        && elementPath.getFileName().toString().contains(assetName)) {
                    return elementPath;
                }
            }
        }
        return null;
    }
    public org.uberfire.java.nio.file.Path findFileByName(DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream, String assetName) {
        for (org.uberfire.java.nio.file.Path elementPath : directoryStream) {
            if (org.uberfire.java.nio.file.Files.isDirectory(elementPath)) {
                DirectoryStream<org.uberfire.java.nio.file.Path> adirectoryStream = ioService.newDirectoryStream(elementPath);
                org.uberfire.java.nio.file.Path foundElementPath = findFileByName(adirectoryStream, assetName);
                if (foundElementPath != null) {
                    return foundElementPath;
                }
            } else {
                if (elementPath.getFileName().toString().contains(assetName)) {
                    return elementPath;
                }
            }
        }
        return null;
    }

    public org.uberfire.java.nio.file.Path getRuleDirectory(DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream, String assetName) {
        for (org.uberfire.java.nio.file.Path elementPath : directoryStream) {
            if (org.uberfire.java.nio.file.Files.isDirectory(elementPath)) {
                DirectoryStream<org.uberfire.java.nio.file.Path> adirectoryStream = ioService.newDirectoryStream(elementPath);
                org.uberfire.java.nio.file.Path foundElementPath = getRuleDirectory(adirectoryStream, assetName);
                if (foundElementPath != null) {
                    return foundElementPath;
                }
            } else {
                if (dotFileFilter.acceptDroolsFile(elementPath.getFileName().toString())) {
                    return elementPath;
                }
            }
        }
        return null;
    }

    public org.uberfire.java.nio.file.Path getRuleDirectoryByName(DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream, String assetName) {
        for (org.uberfire.java.nio.file.Path elementPath : directoryStream) {
            if (elementPath.getFileName().toString().equals(assetName)) {
                return elementPath;
            }
            if (org.uberfire.java.nio.file.Files.isDirectory(elementPath)) {
                DirectoryStream<org.uberfire.java.nio.file.Path> adirectoryStream = ioService.newDirectoryStream(elementPath);
                org.uberfire.java.nio.file.Path foundElementPath = getRuleDirectoryByName(adirectoryStream, assetName);
                if (foundElementPath != null) {
                    return foundElementPath;
                }
            } else {
                if (elementPath.getFileName().toString().equals(assetName)) {
                    return elementPath;
                }
            }
        }
        return null;
    }

    public org.uberfire.java.nio.file.Path getDirectoryElementPath(DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream, String assetName) {
        for (org.uberfire.java.nio.file.Path elementPath : directoryStream) {
            if (org.uberfire.java.nio.file.Files.isDirectory(elementPath)) {
                DirectoryStream<org.uberfire.java.nio.file.Path> adirectoryStream = null;
                try {
                    adirectoryStream = ioService.newDirectoryStream(elementPath);
                    if (elementPath.getFileName().toString().equals(assetName)) {
                        return elementPath;
                    }
                    org.uberfire.java.nio.file.Path foundElementPath = getDirectoryElementPath(adirectoryStream, assetName);
                    if (foundElementPath != null) {
                        return foundElementPath;
                    }
                } catch (Exception e) {

                } finally {
                    if (adirectoryStream != null) {
                        adirectoryStream.close();
                    }
                }
            }
        }
        return null;
    }

}
