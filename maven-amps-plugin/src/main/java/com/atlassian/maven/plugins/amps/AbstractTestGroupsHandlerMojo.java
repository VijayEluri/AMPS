package com.atlassian.maven.plugins.amps;

import com.atlassian.maven.plugins.amps.product.ProductHandler;
import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;
import org.apache.maven.plugin.MojoExecutionException;
import org.jfrog.maven.annomojo.annotations.MojoParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractTestGroupsHandlerMojo extends AbstractProductHandlerMojo
{
    protected static final String NO_TEST_GROUP = "__no_test_group__";

    /**
     * The list of configured test groups
     */
    @MojoParameter
    private List<TestGroup> testGroups = new ArrayList<TestGroup>();

    protected final List<TestGroup> getTestGroups()
    {
        return testGroups;
    }

    protected final List<ProductExecution> getTestGroupProductExecutions(String testGroupId) throws MojoExecutionException
    {
        // Create a container object to hold product-related stuff
        List<ProductExecution> products = new ArrayList<ProductExecution>();
        int dupCounter = 0;
        Set<String> uniqueProductIds = new HashSet<String>();
        for (String productId : getTestGroupProductIds(testGroupId))
        {
            Product ctx = getProductContexts(getMavenGoals()).get(productId);
            if (ctx == null)
            {
                throw new MojoExecutionException("The test group '" + testGroupId + "' refers to a product '" + productId
                    + "' that doesn't have an associated <product> configuration.");
            }
            ProductHandler productHandler = createProductHandler(ctx.getId());

            // Give unique ids to duplicate product instances
            if (uniqueProductIds.contains(productId))
            {
                ctx.setInstanceId(productId + "-" + dupCounter++);
            }
            else
            {
                uniqueProductIds.add(productId);
            }
            products.add(new ProductExecution(ctx, productHandler));
        }

        return products;
    }

    private List<String> getTestGroupProductIds(String testGroupId) throws MojoExecutionException
    {
        List<String> productIds = new ArrayList<String>();
        if (NO_TEST_GROUP.equals(testGroupId))
        {
            productIds.add(getProductId());
        }

        for (TestGroup group : testGroups)
        {
            if (group.getId().equals(testGroupId))
            {
                productIds.addAll(group.getProductIds());
            }
        }
        if (ProductHandlerFactory.getIds().contains(testGroupId) && !productIds.contains(testGroupId))
        {
            productIds.add(testGroupId);
        }

        if (productIds.isEmpty())
        {
            List<String> validTestGroups = new ArrayList<String>();
            for (TestGroup group: testGroups)
            {
                validTestGroups.add(group.getId());
            }
            throw new MojoExecutionException("Unknown test group ID: " + testGroupId
                + " Detected IDs: " + Arrays.toString(validTestGroups.toArray()));
        }

        return productIds;
    }

}
