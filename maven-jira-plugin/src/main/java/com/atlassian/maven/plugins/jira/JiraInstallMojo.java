package com.atlassian.maven.plugins.jira;

import com.atlassian.maven.plugins.amps.pdk.InstallMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @extendsPlugin amps
 * @goal install
 */
public class JiraInstallMojo extends InstallMojo
{
    @Override
    protected String getProductId() throws MojoExecutionException
    {
        return "jira";
    }
}
