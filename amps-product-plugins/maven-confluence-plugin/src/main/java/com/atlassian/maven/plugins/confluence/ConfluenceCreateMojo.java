package com.atlassian.maven.plugins.confluence;

import com.atlassian.maven.plugins.amps.CreateMojo;
import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @extendsPlugin amps
 * @goal create
 * @requiresProject false
 */
public class ConfluenceCreateMojo extends CreateMojo
{
    @Override
    protected String getDefaultProductId() throws MojoExecutionException
    {
        return ProductHandlerFactory.CONFLUENCE;
    }
}

