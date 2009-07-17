package com.atlassian.maven.plugins.amps.osgi;

import aQute.lib.osgi.Constants;
import com.atlassian.maven.plugins.amps.AbstractAmpsMojo;
import static com.atlassian.maven.plugins.amps.util.FileUtils.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@MojoGoal("generate-manifest")
public class GenerateManifestMojo extends AbstractAmpsMojo
{
    /**
     * The BND instructions for the bundle.
     */
    @MojoParameter
    private Map instructions = new HashMap();

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        final MavenProject project = getMavenContext().getProject();
        if (!instructions.isEmpty() || OsgiHelper.isAtlassianPlugin(project))
        {
            getLog().info("Generating a manifest for this plugin");

            if (!instructions.containsKey(Constants.EXPORT_PACKAGE))
            {
                instructions.put(Constants.EXPORT_PACKAGE, "");
            }

            File metainfLib = file(project.getBuild().getOutputDirectory(), "META-INF", "lib");
            if (metainfLib.exists())
            {
                StringBuilder sb = new StringBuilder(".");
                for (File lib : metainfLib.listFiles())
                {
                    sb.append(",").append("META-INF/lib/" + lib.getName());
                }
                instructions.put(Constants.BUNDLE_CLASSPATH, sb.toString());
            }
            getMavenGoals().generateManifest(instructions);
        }
        else
        {
            getLog().info("No manifest instructions found, skipping manifest generation");
        }
    }
}
