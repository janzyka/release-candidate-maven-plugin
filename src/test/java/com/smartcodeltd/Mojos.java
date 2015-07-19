package com.smartcodeltd;

import com.smartcodeltd.sugar.Property;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.junit.Assert;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.smartcodeltd.sugar.Property.property;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;


public class Mojos extends MojoRule {
    private final String mojoName;
    private final TestProjectResources resources;

    public Mojos(String mojoName, TestProjectResources resources) {
        this.mojoName = mojoName;
        this.resources = resources;
    }

    public Mojo forProject(String projectName) throws Exception {
        return forProject(projectName, defaultPropertiesFor(projectName));
    }

    public MavenProject mavenProjectFor(String projectName) throws Exception {
        return mavenProjectFor(resources.fileIn(projectName, "pom.xml"), defaultPropertiesFor(projectName));
    }

    public Mojo forProject(String projectName, List<Property> properties) throws Exception {
        File pom = testPomFor(projectName);

        Mojo mojo = lookupConfiguredMojo(mavenProjectFor(pom, properties), mojoName);
        assertThat(mojo, not(nullValue()));

        return mojo;
    }

    public MavenProject mavenProjectFor(File pom, List<Property> properties) throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(pom));

        for (Property p : properties) {
            model.addProperty(p.name, p.value);
        }

        model.setRepositories(new ArrayList<Repository>());

        MavenProject project = new MavenProject(model);

        project.setPluginArtifactRepositories(new ArrayList<ArtifactRepository>());

        project.setFile(pom);


//        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
//        request.setBaseDirectory( pom.getParentFile() );
//        ProjectBuildingRequest configuration = request.getProjectBuildingRequest();
//
//        configuration.setRepositorySession(newSession());
//
//        MavenProject project = lookup( ProjectBuilder.class ).build( pom, configuration ).getProject();
//        Assert.assertNotNull(project);
//
//        for (Property p : properties) {
//            project.getModel().addProperty(p.name, p.value);
//        }

        return project;
    }

//    private RepositorySystemSession newSession( )
//    {
//        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
//
//        RepositorySystem system = locator.getService( RepositorySystem.class );
//
//        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
//
//        LocalRepository localRepo = new LocalRepository( "target/local-repo" );
//        session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );
//
//        return session;
//    }


    public void given(Mojo aMojo, String field, Object value) throws IllegalAccessException {
        setVariableValueToObject(aMojo, field, value);
    }

    // --

    private List<Property> defaultPropertiesFor(final String project) throws IOException {
        return new ArrayList<Property>() {{
            add(property("project.basedir", resources.baseDirectoryOf(project).getCanonicalPath()));
        }};
    }

    private File testPomFor(String projectName) throws IOException {
        File projectDirectory = resources.baseDirectoryFor(projectName);
        File pom = new File(projectDirectory, "pom.xml");

        assertThat(pom, not(nullValue()));
        assertThat(pom.exists(), is(true));

        return pom;
    }
}