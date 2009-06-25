package com.atlassian.maven.plugins.jira;

import com.atlassian.maven.plugins.amps.UnitTestMojo;
import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @extendsPlugin amps
 * @goal unit-test
 */
public class JiraUnitTestMojo extends UnitTestMojo
{
    @Override
    protected String getDefaultProductId() throws MojoExecutionException
    {
        return ProductHandlerFactory.JIRA;
    }
}
