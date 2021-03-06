/**
 * (C) Copyright IBM Corporation 2014, 2017.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wasdev.wlp.maven.plugins;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Settings;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.mojo.pluginsupport.MojoSupport;
import org.codehaus.mojo.pluginsupport.ant.AntHelper;

/**
 * Liberty Abstract Mojo Support
 * 
 */
public abstract class AbstractLibertySupport extends MojoSupport {
    /**
     * Maven Project
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project = null;
    
    @Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
    protected ArtifactRepository artifactRepository = null;
    
    /**
     * The build settings.
     */
    @Parameter(defaultValue = "${settings}", required = true, readonly = true)
    protected Settings settings;
    
    @Component(role = AntHelper.class)
    protected AntHelper ant;
    
    @Component
    protected RepositorySystem repositorySystem;
    
    @Component
    protected ProjectBuilder mavenProjectBuilder;
    
    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession session;
    
    @Parameter(property = "reactorProjects", required = true, readonly = true)
    protected List<MavenProject> reactorProjects;
    
    protected MavenProject getProject() {
        return project;
    }
    
    protected ArtifactRepository getArtifactRepository() {
        return artifactRepository;
    }
    
    protected void init() throws MojoExecutionException, MojoFailureException {
        super.init();
        // Initialize ant helper instance
        ant.setProject(getProject());
    }
    
    protected boolean isReactorMavenProject(Artifact artifact) {
        for (MavenProject p : reactorProjects) {
            if (p.getGroupId().equals(artifact.getGroupId()) && p.getArtifactId().equals(artifact.getArtifactId())
                    && p.getVersion().equals(artifact.getVersion())) {
                return true;
            }
        }
        return false;
    }
    
    protected MavenProject getReactorMavenProject(Artifact artifact) {
        for (MavenProject p : reactorProjects) {
            // Support loose configuration to all sub-module projects in the reactorProjects object. 
            // Need to be able to retrieve all transitive dependencies in these projects.
            if (p.getGroupId().equals(artifact.getGroupId()) && p.getArtifactId().equals(artifact.getArtifactId())
                    && p.getVersion().equals(artifact.getVersion())) {
                p.setArtifactFilter(new ArtifactFilter() {
                    @Override
                    public boolean include(Artifact artifact) {
                        if ("compile".equals(artifact.getScope()) || "runtime".equals(artifact.getScope())) {
                            return true;
                        }
                        return false;
                    }
                });
                return p;
            }
        }
        
        return null;
    }
}
