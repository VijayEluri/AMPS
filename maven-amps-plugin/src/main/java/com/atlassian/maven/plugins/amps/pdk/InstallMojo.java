package com.atlassian.maven.plugins.amps.pdk;

import com.atlassian.maven.plugins.amps.PdkParams;
import com.atlassian.maven.plugins.amps.product.ProductHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jfrog.maven.annomojo.annotations.MojoGoal;

@MojoGoal("install")
public class InstallMojo extends AbstractPdkMojo
{
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        ensurePluginKeyExists();
        final ProductHandler product = createProductHandler(getProductId());
        getMavenGoals().installPlugin(new PdkParams.Builder()
                .pluginKey(pluginKey)
                .server(server)
                .port(getHttpPort(product))
                .contextPath(getContextPath(product))
                .username(username)
                .password(password)
                .build());
    }
}
