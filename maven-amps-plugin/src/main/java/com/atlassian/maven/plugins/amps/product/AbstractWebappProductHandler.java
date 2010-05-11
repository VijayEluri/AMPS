package com.atlassian.maven.plugins.amps.product;

import com.atlassian.maven.plugins.amps.MavenGoals;
import com.atlassian.maven.plugins.amps.Product;
import com.atlassian.maven.plugins.amps.ProductArtifact;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.atlassian.maven.plugins.amps.util.FileUtils.doesFileNameMatchArtifact;
import static com.atlassian.maven.plugins.amps.util.ZipUtils.unzip;

public abstract class AbstractWebappProductHandler extends AbstractProductHandler
{
    private final PluginProvider pluginProvider;

    public AbstractWebappProductHandler(final MavenProject project, final MavenGoals goals, PluginProvider pluginProvider)
    {
        super(project, goals);
        this.pluginProvider = pluginProvider;
    }

    public int start(final Product ctx) throws MojoExecutionException
    {
        // Copy the webapp war to target
        final File webappWar = goals.copyWebappWar(ctx.getId(), getBaseDirectory(),
                new ProductArtifact(getArtifact().getGroupId(), getArtifact().getArtifactId(), ctx.getVersion()));

        final File homeDir = extractAndProcessHomeDirectory(ctx);

        final File combinedWebappWar = addArtifactsAndOverrides(ctx, homeDir, webappWar);

        final Map<String, String> properties = mergeSystemProperties(ctx);

        return goals.startWebapp(ctx.getId(), combinedWebappWar, properties, getExtraContainerDependencies(), ctx);
    }

    public void stop(final Product ctx) throws MojoExecutionException
    {
        goals.stopWebapp(getId(), ctx.getContainerId());
    }

    private File addArtifactsAndOverrides(final Product ctx, final File homeDir, final File webappWar) throws MojoExecutionException
    {
        try
        {
            final String webappDir = new File(getBaseDirectory(), "webapp").getAbsolutePath();
            if (!new File(webappDir).exists())
            {
                unzip(webappWar, webappDir);
            }

            File pluginsDir = getPluginsDirectory(webappDir, homeDir);
            final File bundledPluginsDir = new File(getBaseDirectory(), "bundled-plugins");

            bundledPluginsDir.mkdir();
            // add bundled plugins
            final File bundledPluginsZip = new File(webappDir, getBundledPluginPath(ctx));
            if (bundledPluginsZip.exists())
            {
                unzip(bundledPluginsZip, bundledPluginsDir.getPath());
            }

            if (isStaticPlugin())
            {
                pluginsDir = new File(webappDir, "WEB-INF/lib");
            }

            if (pluginsDir == null)
            {
                pluginsDir = bundledPluginsDir;
            }

            pluginsDir.mkdirs();

            // add this plugin itself if enabled
            if (ctx.isInstallPlugin())
            {
                addThisPluginToDirectory(pluginsDir);
            }

            // add plugins2 plugins if necessary
            if (!isStaticPlugin())
            {
                addArtifactsToDirectory(goals, pluginProvider.provide(ctx), pluginsDir);
            }

            // add plugins1 plugins
            List<ProductArtifact> artifacts = new ArrayList<ProductArtifact>();
            artifacts.addAll(getDefaultLibPlugins());
            artifacts.addAll(ctx.getLibArtifacts());
            addArtifactsToDirectory(goals, artifacts, new File(webappDir, "WEB-INF/lib"));

            artifacts = new ArrayList<ProductArtifact>();
            artifacts.addAll(getDefaultBundledPlugins());
            artifacts.addAll(ctx.getBundledArtifacts());

            addArtifactsToDirectory(goals, artifacts, bundledPluginsDir);

            if (bundledPluginsDir.list().length > 0)
            {
                com.atlassian.core.util.FileUtils.createZipFile(bundledPluginsDir, bundledPluginsZip);
            }

            // add log4j.properties file if specified
            if (ctx.getLog4jProperties() != null)
            {
                FileUtils.copyFile(ctx.getLog4jProperties(), new File(webappDir, "WEB-INF/classes/log4j.properties"));
            }

            // override war files
            try
            {
                addOverrides(new File(webappDir), ctx.getId());
            }
            catch (IOException e)
            {
                throw new MojoExecutionException("Unable to override WAR files using src/test/resources/" + ctx.getId() + "-app", e);
            }

            final File warFile = new File(webappWar.getParentFile(), getId() + ".war");
            com.atlassian.core.util.FileUtils.createZipFile(new File(webappDir), warFile);
            return warFile;

        }
        catch (final Exception e)
        {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private File getBaseDirectory()
    {
        final File dir = new File(project.getBuild().getDirectory(), getId());
        dir.mkdir();
        return dir;
    }

    private File extractAndProcessHomeDirectory(final Product ctx) throws MojoExecutionException
    {
        if (getTestResourcesArtifact() != null)
        {

            final File outputDir = getBaseDirectory();
            final File homeDir = new File(outputDir, "home");

            // Only create the home dir if it doesn't exist
            if (!homeDir.exists())
            {

                //find and extract productHomeZip
                final File productHomeZip = getProductHomeZip(ctx, outputDir);
                extractProductHomeZip(productHomeZip, homeDir, ctx, outputDir);

                // just in case
                homeDir.mkdir();
                processHomeDirectory(ctx, homeDir);
            }

            // Always override files regardless of home directory existing or not
            try
            {
                overrideAndPatchHomeDir(homeDir, ctx.getId());
            }
            catch (IOException e)
            {
                throw new MojoExecutionException("Unable to override files using src/test/resources", e);
            }

            return homeDir;
        } else
        {
            return getHomeDirectory();
        }
    }

    private File getProductHomeZip(final Product ctx, final File outputDir) throws MojoExecutionException
    {
        File productHomeZip = null;
        String dpath = ctx.getProductDataPath();

        //use custom zip if supplied
        if (StringUtils.isNotBlank(dpath))
        {
            File customHomeZip = new File(dpath);

            if (customHomeZip.exists())
            {
                productHomeZip = customHomeZip;
            }
        }

        //if we didn't find a custom zip, use the default
        if (productHomeZip == null)
        {
            productHomeZip = goals.copyHome(outputDir,
                    new ProductArtifact(
                            getTestResourcesArtifact().getGroupId(),
                            getTestResourcesArtifact().getArtifactId(),
                            ctx.getProductDataVersion()));
        }

        return productHomeZip;
    }

    private void extractProductHomeZip(File productHomeZip, File homeDir,
                                       Product ctx, File outputDir)
            throws MojoExecutionException
    {
        final File tmpDir = new File(getBaseDirectory(), "tmp-resources");
        tmpDir.mkdir();

        try
        {
            unzip(productHomeZip, tmpDir.getPath());
            FileUtils.copyDirectory(tmpDir.listFiles()[0], outputDir, true);
            File tmp = new File(outputDir, ctx.getId() + "-home");
            boolean result = tmp.renameTo(homeDir);
            if (!result)
            {
                throw new IOException("Rename " + tmp.getPath() + " to " + homeDir.getPath() + " unsuccessful");
            }
        }
        catch (final IOException ex)
        {
            throw new MojoExecutionException("Unable to copy home directory", ex);
        }
    }

    private void overrideAndPatchHomeDir(File homeDir, final String productId) throws IOException
    {
        final File srcDir = new File(project.getBasedir(), "src/test/resources/" + productId + "-home");
        final File outputDir = new File(getBaseDirectory(), "home");
        if (srcDir.exists() && outputDir.exists())
        {
            FileUtils.copyDirectory(srcDir, homeDir);
        }
    }

    private void addOverrides(File webappDir, final String productId) throws IOException
    {
        final File srcDir = new File(project.getBasedir(), "src/test/resources/" + productId + "-app");
        if (srcDir.exists() && webappDir.exists())
        {
            FileUtils.copyDirectory(srcDir, webappDir);
        }
    }

    private void addArtifactsToDirectory(final MavenGoals goals, final List<ProductArtifact> artifacts, final File pluginsDir) throws MojoExecutionException
    {
        // first remove plugins from the webapp that we want to update
        if (pluginsDir.isDirectory() && pluginsDir.exists())
        {
            for (final Iterator<?> iterateFiles = FileUtils.iterateFiles(pluginsDir, null, false); iterateFiles.hasNext();)
            {
                final File file = (File) iterateFiles.next();
                for (final ProductArtifact webappArtifact : artifacts)
                {
                    if (!file.isDirectory() && doesFileNameMatchArtifact(file.getName(), webappArtifact.getArtifactId()))
                    {
                        file.delete();
                    }
                }
            }
        }
        // copy the all the plugins we want in the webapp
        if (!artifacts.isEmpty())
        {
            goals.copyPlugins(pluginsDir, artifacts);
        }
    }

    protected abstract void processHomeDirectory(Product ctx, File homeDir) throws MojoExecutionException;

    protected abstract ProductArtifact getTestResourcesArtifact();

    protected abstract Collection<ProductArtifact> getDefaultBundledPlugins();

    protected abstract Collection<ProductArtifact> getDefaultLibPlugins();

    protected abstract String getBundledPluginPath(Product ctx);

    protected abstract File getPluginsDirectory(String webappDir, File homeDir);

    protected abstract List<ProductArtifact> getExtraContainerDependencies();

    protected abstract ProductArtifact getArtifact();

}
