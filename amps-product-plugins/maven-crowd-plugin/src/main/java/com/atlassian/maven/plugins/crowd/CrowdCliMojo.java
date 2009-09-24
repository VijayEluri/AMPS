package com.atlassian.maven.plugins.crowd;

import com.atlassian.maven.plugins.amps.cli.CliMojo;
import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;
import org.apache.maven.plugin.MojoExecutionException;
import org.jfrog.maven.annomojo.annotations.MojoGoal;

@MojoGoal("cli")
public class CrowdCliMojo extends CliMojo
{
    @Override
    protected String getDefaultProductId() throws MojoExecutionException
    {
        return ProductHandlerFactory.CROWD;
    }
}
