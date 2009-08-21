package com.atlassian.maven.plugins.amps.product;

import com.atlassian.maven.plugins.amps.MavenGoals;
import com.atlassian.maven.plugins.amps.ProductArtifact;
import com.atlassian.maven.plugins.amps.Product;
import com.atlassian.maven.plugins.amps.util.ConfigFileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Arrays;

public class BambooProductHandler extends AbstractWebappProductHandler
{
    public BambooProductHandler(MavenProject project, MavenGoals goals)
    {
        super(project, goals);
    }

    public String getId()
    {
        return "bamboo";
    }

    public ProductArtifact getArtifact()
    {
        return new ProductArtifact("com.atlassian.bamboo", "atlassian-bamboo-web-app", "RELEASE");
    }

    protected Collection<ProductArtifact> getSalArtifacts(String salVersion)
    {
        return Arrays.asList(
                new ProductArtifact("com.atlassian.sal", "sal-api", salVersion),
                new ProductArtifact("com.atlassian.sal", "sal-bamboo-plugin", salVersion));
    }

    public ProductArtifact getTestResourcesArtifact()
    {
        return new ProductArtifact("com.atlassian.bamboo.plugins", "bamboo-plugin-test-resources", "LATEST");
    }

    public int getDefaultHttpPort()
    {
        return 6990;
    }

    public Map<String, String> getSystemProperties()
    {
        return Collections.singletonMap("bamboo.home", getHomeDirectory().getPath());
    }

    public File getPluginsDirectory(final String webappDir, final File homeDir)
    {
        // indicates plugins should be bundled
        return null;
    }

    public List<ProductArtifact> getExtraContainerDependencies()
    {
        return Collections.emptyList();
    }

    public String getBundledPluginPath()
    {
        return "WEB-INF/classes/atlassian-bundled-plugins.zip";
    }

    public File getHomeDirectory()
    {
        return new File(new File(project.getBuild().getDirectory(), getId()), "bamboo-home");
    }

    public void processHomeDirectory(final Product ctx, final File homeDir) throws MojoExecutionException
    {
        ConfigFileUtils.replace(new File(homeDir, "bamboo.cfg.xml"), "@project-dir@", homeDir.getParent());
        ConfigFileUtils.replace(new File(homeDir, "bamboo.cfg.xml"), "${bambooHome}", homeDir.getAbsolutePath());
        ConfigFileUtils.replace(new File(homeDir, "/xml-data/configuration/administration.xml"),
                "http://192.168.15.145:8085", "http://" + ctx.getServer() + ":" + ctx.getHttpPort() + "/" + ctx.getContextPath().replaceAll("^/|/$", ""));
    }

    public List<ProductArtifact> getDefaultPlugins()
    {
        return Collections.singletonList(new ProductArtifact("com.atlassian.pdkinstall", "pdkinstall-plugin", "0.4"));
    }

    public List<ProductArtifact> getDefaultLibPlugins()
    {
        return Collections.emptyList();
    }

    public List<ProductArtifact> getDefaultBundledPlugins()
    {
        return Collections.emptyList();
    }
}
