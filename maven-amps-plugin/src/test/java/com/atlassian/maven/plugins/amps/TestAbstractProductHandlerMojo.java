package com.atlassian.maven.plugins.amps;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAbstractProductHandlerMojo
{
    @Test
    public void testMakeProductsInheritDefaultConfiguration() throws Exception
    {
        SomeMojo mojo = new SomeMojo("foo");

        Product fooProd = new Product();
        fooProd.setInstanceId("foo");
        fooProd.setVersion("1.0");

        Product barProd = new Product();
        barProd.setInstanceId("bar");
        barProd.setVersion("2.0");

        Map<String,Product> prodMap = new HashMap<String, Product>();
        mojo.makeProductsInheritDefaultConfiguration(asList(fooProd, barProd), prodMap);
        assertEquals(2, prodMap.size());
        assertEquals("1.0", prodMap.get("foo").getVersion());
        assertEquals("/foo", prodMap.get("foo").getContextPath());
        assertEquals("2.0", prodMap.get("bar").getVersion());
        assertEquals("/foo", prodMap.get("bar").getContextPath());
    }

    @Test
    public void testMakeProductsInheritDefaultConfigurationDifferentInstanceIds() throws Exception
    {
        SomeMojo mojo = new SomeMojo("baz");

        Product fooProd = new Product();
        fooProd.setInstanceId("foo");
        fooProd.setVersion("1.0");

        Product barProd = new Product();
        barProd.setInstanceId("bar");
        barProd.setVersion("2.0");

        Map<String,Product> prodMap = new HashMap<String, Product>();
        mojo.makeProductsInheritDefaultConfiguration(asList(fooProd, barProd), prodMap);
        assertEquals(3, prodMap.size());
        assertEquals("1.0", prodMap.get("foo").getVersion());
        assertEquals("/foo", prodMap.get("foo").getContextPath());
        assertEquals("2.0", prodMap.get("bar").getVersion());
        assertEquals("/foo", prodMap.get("bar").getContextPath());
        assertEquals(null, prodMap.get("baz").getVersion());
        assertEquals("/foo", prodMap.get("baz").getContextPath());
    }

    @Test
    public void testMakeProductsInheritDefaultConfigurationNoProducts() throws Exception
    {
        SomeMojo mojo = new SomeMojo("foo");

        Map<String,Product> prodMap = new HashMap<String, Product>();
        mojo.makeProductsInheritDefaultConfiguration(Collections.<Product>emptyList(), prodMap);
        assertEquals(1, prodMap.size());
        assertEquals("/foo", prodMap.get("foo").getContextPath());
    }

    public static class SomeMojo extends AbstractProductHandlerMojo
    {
        private final String defaultProductId;

        public SomeMojo(String defaultProductId)
        {
            this.defaultProductId = defaultProductId;
            contextPath = "/foo";
        }

        @Override
        protected String getDefaultProductId() throws MojoExecutionException
        {
            return defaultProductId;
        }

        @Override
        protected void doExecute() throws MojoExecutionException, MojoFailureException
        {

        }

        @Override
        protected MavenContext getMavenContext()
        {
            MavenProject project = mock(MavenProject.class);
            Build build = mock(Build.class);
            when(build.getTestOutputDirectory()).thenReturn(".");
            when(project.getBuild()).thenReturn(build);
            when(project.getBasedir()).thenReturn(new File("."));
            return new MavenContext(project, null, null, (PluginManager) null, null);
        }

        @Override
        protected PluginInformation getPluginInformation() {
            return new PluginInformation("test-product", "Test SDK Version");
        }
    }
}