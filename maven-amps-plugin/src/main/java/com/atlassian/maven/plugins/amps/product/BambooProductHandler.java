package com.atlassian.maven.plugins.amps.product;

import com.atlassian.maven.plugins.amps.MavenGoals;
import com.atlassian.maven.plugins.amps.Product;
import com.atlassian.maven.plugins.amps.ProductArtifact;
import com.atlassian.maven.plugins.amps.util.ConfigFileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.*;

public class BambooProductHandler extends AbstractWebappProductHandler
{
    public BambooProductHandler(MavenProject project, MavenGoals goals)
    {
        super(project, goals, new BambooPluginProvider());
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

    public Map<String, String> getSystemProperties(Product ctx)
    {
        Map<String, String> systemProperties = new HashMap<String, String>();
        systemProperties.put("bamboo.home", getHomeDirectory(ctx).getPath());
        systemProperties.put("org.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES", "false");
        return Collections.unmodifiableMap(systemProperties);
    }

    @Override
    public File getUserInstalledPluginsDirectory(final File webappDir, final File homeDir)
    {
        return new File(homeDir, "plugins");
    }

    public List<ProductArtifact> getExtraContainerDependencies()
    {
        return Collections.emptyList();
    }

    public String getBundledPluginPath(Product ctx)
    {
        return "WEB-INF/classes/atlassian-bundled-plugins.zip";
    }

    public void processHomeDirectory(final Product ctx, final File homeDir) throws MojoExecutionException
    {
        ConfigFileUtils.replace(new File(homeDir, "bamboo.cfg.xml"), "@project-dir@", homeDir.getParent());
        ConfigFileUtils.replace(new File(homeDir, "bamboo.cfg.xml"), "/bamboo-home/", "/home/");
        ConfigFileUtils.replace(new File(homeDir, "bamboo.cfg.xml"), "${bambooHome}", homeDir.getAbsolutePath());
        // The regex in the following search text is used to match IPv4 ([^:]+) or IPv6 (\[.+]) addresses.
        ConfigFileUtils.replaceAll(new File(homeDir, "/xml-data/configuration/administration.xml"),
                "http://(?:[^:]+|\\[.+]):8085", "http://" + ctx.getServer() + ":" + ctx.getHttpPort() + "/" + ctx.getContextPath().replaceAll("^/|/$", ""));

        File dbLog = new File(homeDir, "database/defaultdb.log");
        if (dbLog.exists())
        {
            ConfigFileUtils.replace(dbLog, "${bambooHome}", homeDir.getAbsolutePath());
        }
        File dbScript = new File(homeDir, "database/defaultdb.script");
        if (dbScript.exists())
        {
            ConfigFileUtils.replace(dbScript, "${bambooHome}", homeDir.getAbsolutePath());
        }

    }

    public List<ProductArtifact> getDefaultLibPlugins()
    {
        return Collections.emptyList();
    }

    public List<ProductArtifact> getDefaultBundledPlugins()
    {
        return Collections.emptyList();
    }

    private static class BambooPluginProvider extends AbstractPluginProvider
    {

        @Override
        protected Collection<ProductArtifact> getSalArtifacts(String salVersion)
        {
            return Arrays.asList(
                new ProductArtifact("com.atlassian.sal", "sal-api", salVersion),
                new ProductArtifact("com.atlassian.sal", "sal-bamboo-plugin", salVersion));
        }

    }
}
