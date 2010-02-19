package com.atlassian.maven.plugins.amps;

import com.atlassian.maven.plugins.amps.product.ProductHandler;
import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jfrog.maven.annomojo.annotations.MojoComponent;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;

import java.io.File;
import java.util.Properties;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Run the integration tests against the webapp
 */
@MojoGoal("integration-test")
@MojoRequiresDependencyResolution("test")
public class IntegrationTestMojo extends AbstractProductHandlerMojo
{
    /**
     * Pattern for to use to find integration tests
     */
    @MojoParameter(expression = "${functional.test.pattern}")
    private String functionalTestPattern = "it/**";

    /**
     * The directory containing generated test classes of the project being tested.
     */
    @MojoParameter(expression = "${project.build.testOutputDirectory}", required = true)
    private File testClassesDirectory;

    /**
     * The list of specific test groups to execute
     */
    @MojoParameter
    private List<TestGroup> testGroups = new ArrayList<TestGroup>();

    /**
     * Whether the reference application will not be started or not
     */
    @MojoParameter(expression = "${no.webapp}", defaultValue = "false")
    private boolean noWebapp = false;

    @MojoComponent
    private ArtifactHandlerManager artifactHandlerManager;

    @MojoParameter(expression="${maven.test.skip}", defaultValue = "false")
    private boolean testsSkip = false;

    @MojoParameter(expression="${skipTests}", defaultValue = "false")
    private boolean skipTests = false;

    protected void doExecute() throws MojoExecutionException
    {
        final MavenProject project = getMavenContext().getProject();

        // workaround for MNG-1682/MNG-2426: force maven to install artifact using the "jar" handler
        project.getArtifact().setArtifactHandler(artifactHandlerManager.getArtifactHandler("jar"));

        if (!new File(testClassesDirectory, "it").exists())
        {
            getLog().info("No integration tests found");
            return;
        }

        if (skipTests || testsSkip)
        {
            getLog().info("Integration tests skipped");
            return;
        }

        final MavenGoals goals = getMavenGoals();
        final String pluginJar = targetDirectory.getAbsolutePath() + "/" + finalName + ".jar";

        runTestsForTestGroup(getProductId(), goals, pluginJar, systemProperties);

        for (String testGroupId : getTestGroupIds())
        {
            if (containsTests(testGroupId) && !testGroupId.equals(getProductId()))
            {
                runTestsForTestGroup(testGroupId, goals, pluginJar, systemProperties);
            }
        }
    }

    private Set<String> getTestGroupIds()
    {
        Set<String> ids = new HashSet<String>();
        ids.addAll(ProductHandlerFactory.getIds());
        for (TestGroup group : testGroups)
        {
            ids.add(group.getId());
        }
        return ids;
    }

    private Set<String> getProductIdsForTestGroup(String testGroupId) throws MojoExecutionException
    {
        Set<String> productIds = new HashSet<String>();

        for (TestGroup group : testGroups)
        {
            if (group.getId().equals(testGroupId))
            {
                productIds.addAll(group.getProductIds());
            }
        }
        if (ProductHandlerFactory.getIds().contains(testGroupId))
        {
            productIds.add(testGroupId);
        }

        if (productIds.isEmpty())
        {
            throw new MojoExecutionException("Unknown test group id");
        }

        return productIds;
    }

    private void runTestsForTestGroup(String testGroupId, MavenGoals goals, String pluginJar, Properties systemProperties) throws MojoExecutionException
    {
        Set<String> productIds = getProductIdsForTestGroup(testGroupId);

        // Create a container object to hold product-related stuff
        List<TestGroupProductExecution> products = new ArrayList<TestGroupProductExecution>();
        for (String productId : productIds)
        {
            ProductHandler product = ProductHandlerFactory.create(productId, getMavenContext().getProject(), goals, getLog());
            Product ctx = getProductContexts(goals).get(productId);
            products.add(new TestGroupProductExecution(ctx, product));
        }

        // Install the plugin in each product and start it
        for (TestGroupProductExecution testGroupProductExecution : products)
        {
            ProductHandler productHandler = testGroupProductExecution.getProductHandler();
            Product product = testGroupProductExecution.getProduct();
            product.setInstallPlugin(installPlugin);

            int actualHttpPort = 0;
            if (!noWebapp)
            {
                actualHttpPort = productHandler.start(product);
            }

            if (products.size() == 1)
            {
                systemProperties.put("http.port", String.valueOf(actualHttpPort));
                systemProperties.put("context.path", product.getContextPath());
            }
            // hard coded system properties...
            systemProperties.put("http." + product.getId() + ".port", String.valueOf(actualHttpPort));
            systemProperties.put("context." + product.getId() + ".path", product.getContextPath());
            systemProperties.put("plugin.jar", pluginJar);
        }

        // Actually run the tests
        goals.runTests(getProductId(), containerId, functionalTestPattern, systemProperties);

        // Shut all products down
        for (TestGroupProductExecution testGroupProductExecution : products)
        {
            ProductHandler productHandler = testGroupProductExecution.getProductHandler();
            Product product = testGroupProductExecution.getProduct();
            if (!noWebapp)
            {
                productHandler.stop(product);
            }
        }
    }

    private boolean containsTests(String type)
    {
        return scanFile(new File(testClassesDirectory, "it"), type);
    }

    private boolean scanFile(File file, String type)
    {
        if (file.isDirectory())
        {
            if (file.getName().equals(type))
            {
                return true;
            }
            else
            {
                for (File kid : file.listFiles())
                {
                    if (scanFile(kid, type))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static class TestGroupProductExecution
    {
        private final Product product;
        private final ProductHandler productHandler;

        public TestGroupProductExecution(Product product, ProductHandler productHandler)
        {
            this.product = product;
            this.productHandler = productHandler;
        }

        public ProductHandler getProductHandler()
        {
            return productHandler;
        }

        public Product getProduct()
        {
            return product;
        }
    }
}