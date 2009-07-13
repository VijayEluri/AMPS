package com.atlassian.maven.plugins.bamboo;

import com.atlassian.maven.plugins.amps.CopyBundledDependenciesMojo;
import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @extendsPlugin amps
 * @goal copy-bundled-dependencies
 */
public class BambooCopyBundledDependenciesMojo extends CopyBundledDependenciesMojo
{
    @Override
    protected String getDefaultProductId() throws MojoExecutionException
    {
        return ProductHandlerFactory.BAMBOO;
    }
}
