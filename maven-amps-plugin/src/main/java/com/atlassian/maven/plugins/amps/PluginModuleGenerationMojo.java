package com.atlassian.maven.plugins.amps;

import com.atlassian.maven.plugins.amps.codegen.ConditionFactory;
import com.atlassian.maven.plugins.amps.codegen.ContextProviderFactory;
import com.atlassian.maven.plugins.amps.codegen.PluginModuleSelectionQueryer;
import com.atlassian.maven.plugins.amps.codegen.prompter.PluginModulePrompter;
import com.atlassian.maven.plugins.amps.codegen.prompter.PluginModulePrompterFactory;
import com.atlassian.plugins.codegen.annotations.DependencyDescriptor;
import com.atlassian.plugins.codegen.modules.PluginModuleCreator;
import com.atlassian.plugins.codegen.modules.PluginModuleCreatorFactory;
import com.atlassian.plugins.codegen.modules.PluginModuleLocation;
import com.atlassian.plugins.codegen.modules.PluginModuleProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.XmlStreamWriter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.shade.pom.PomWriter;
import org.apache.maven.project.MavenProject;
import org.jfrog.maven.annomojo.annotations.MojoComponent;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@MojoRequiresDependencyResolution("compile")
@MojoGoal("plugin-module")
public class PluginModuleGenerationMojo extends AbstractProductAwareMojo {

    @MojoComponent
    private PluginModuleSelectionQueryer pluginModuleSelectionQueryer;

    @MojoComponent
    private PluginModulePrompterFactory pluginModulePrompterFactory;

    @MojoComponent
    private PluginModuleCreatorFactory pluginModuleCreatorFactory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        //can't figure out how to get plexus to fire a method after injection, so doing it here
        pluginModulePrompterFactory.setLog(getLog());
        try {
            pluginModulePrompterFactory.scanForPrompters();
        } catch (Exception e) {
            String message = "Error initializing Plugin Module Prompters";
            getLog().error(message);
            throw new MojoExecutionException(message);
        }

        String productId = getProductId();

        MavenProject project = getMavenContext().getProject();
        File javaDir = getJavaSourceRoot(project);
        File testDir = getJavaTestRoot(project);
        File resourcesDir = getResourcesRoot(project);

        try {
            ConditionFactory.locateAvailableConditions(productId,project.getCompileClasspathElements());
        } catch (Exception e) {
            String message = "Error initializing Plugin Module Conditions";
            getLog().error(message);
            //keep going, doesn't matter
        }

        try {
            ContextProviderFactory.locateAvailableContextProviders(productId, project.getCompileClasspathElements());
        } catch (Exception e) {
            String message = "Error initializing Plugin Module Context Providers";
            getLog().error(message);
            //keep going, doesn't matter
        }

        Map<String,String> conditions = ConditionFactory.getAvailableConditions();
        Map<String,String> providers = ContextProviderFactory.getAvailableContextProviders();

        PluginModuleLocation moduleLocation = new PluginModuleLocation.Builder(javaDir)
                .resourcesDirectory(resourcesDir)
                .testDirectory(testDir)
                .templateDirectory(new File(resourcesDir, "templates"))
                .build();

        if (!moduleLocation.getPluginXml().exists()) {
            String message = "Couldn't find the atlassian-plugin.xml, please run this goal in an atlassian plugin project root.";
            getLog().error(message);
            throw new MojoExecutionException(message);
        }

        PluginModuleCreator creator = null;
        try {
            creator = pluginModuleSelectionQueryer.selectModule(pluginModuleCreatorFactory.getModuleCreatorsForProduct(productId));

            PluginModulePrompter modulePrompter = pluginModulePrompterFactory.getPrompterForCreatorClass(creator.getClass());
            if (modulePrompter == null) {
                String message = "Couldn't find an input prompter for: " + creator.getClass().getName();
                getLog().error(message);
                throw new MojoExecutionException(message);
            }

            PluginModuleProperties moduleProps = modulePrompter.getModulePropertiesFromInput(moduleLocation);
            creator.createModule(moduleLocation, moduleProps);

            //edit pom if needed
            addRequiredModuleDependenciesToPOM(project, creator);


        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Error creating plugin module", e);
        }

    }

    private void addRequiredModuleDependenciesToPOM(MavenProject project, PluginModuleCreator creator) {
        List<DependencyDescriptor> descriptors = pluginModuleCreatorFactory.getDependenciesForCreatorClass(creator.getClass());
        boolean modifyPom = false;
        if (descriptors != null && !descriptors.isEmpty()) {
            List<Dependency> originalDependencies = project.getModel().getDependencies();
            for (DependencyDescriptor descriptor : descriptors) {
                Dependency alreadyExisting = (Dependency) CollectionUtils.find(originalDependencies, new DependencyPredicate(descriptor));
                if (null == alreadyExisting) {
                    modifyPom = true;

                    Dependency newDependency = new Dependency();
                    newDependency.setGroupId(descriptor.getGroupId());
                    newDependency.setArtifactId(descriptor.getArtifactId());
                    newDependency.setVersion(descriptor.getVersion());
                    newDependency.setScope(descriptor.getScope());

                    project.getOriginalModel().addDependency(newDependency);
                }
            }
        }

        if (modifyPom) {
            File pom = project.getFile();
            XmlStreamWriter writer = null;
            try {
                writer = new XmlStreamWriter(pom);
                PomWriter.write(writer, project.getOriginalModel(), true);
            } catch (IOException e) {
                getLog().warn("Unable to write plugin-module dependencies to pom.xml", e);
            } finally {
                if (writer != null) {
                    IOUtils.closeQuietly(writer);
                }
            }
        }
    }

    private File getJavaSourceRoot(MavenProject project) {
        return new File(project.getModel().getBuild().getSourceDirectory());
    }

    private File getJavaTestRoot(MavenProject project) {
        return new File(project.getModel().getBuild().getTestSourceDirectory());
    }

    private File getResourcesRoot(MavenProject project) {
        File resourcesRoot = null;
        for (Resource resource : (List<Resource>) project.getModel().getBuild().getResources()) {
            String pathToCheck = "src" + File.separator + "main" + File.separator + "resources";
            if (StringUtils.endsWith(resource.getDirectory(), pathToCheck)) {
                resourcesRoot = new File(resource.getDirectory());
            }
        }
        return resourcesRoot;
    }

    private class DependencyPredicate implements Predicate {
        private DependencyDescriptor depToCheck;

        private DependencyPredicate(DependencyDescriptor depToCheck) {
            this.depToCheck = depToCheck;
        }

        @Override
        public boolean evaluate(Object o) {
            Dependency d = (Dependency) o;
            return (depToCheck.getGroupId().equals(d.getGroupId())
                    && depToCheck.getArtifactId().equals(d.getArtifactId()));
        }
    }
}
