package com.atlassian.maven.plugins.caviar;

import org.apache.maven.plugin.MojoExecutionException;

import com.atlassian.maven.plugins.amps.StopMojo;
import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;

/**
 * @since 3.8
 */
public class CaviarStopMojo extends StopMojo
{
    @Override
    protected String getDefaultProductId() throws MojoExecutionException
    {
        return ProductHandlerFactory.CAVIAR;
    }
}
