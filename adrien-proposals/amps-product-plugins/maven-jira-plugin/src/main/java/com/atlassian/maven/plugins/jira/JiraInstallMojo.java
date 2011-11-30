package com.atlassian.maven.plugins.jira;

import com.atlassian.maven.plugins.amps.Jira;
import com.atlassian.maven.plugins.amps.pdk.InstallMojo;
import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;
import org.apache.maven.plugin.MojoExecutionException;

public class JiraInstallMojo extends InstallMojo
{
    @Override
    protected String getDefaultProductId() throws MojoExecutionException
    {
        return Jira.ID;
    }
}
