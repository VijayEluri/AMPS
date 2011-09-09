package com.atlassian.maven.plugins.amps;

import com.atlassian.maven.plugins.amps.product.ProductHandler;
import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;
import com.atlassian.maven.plugins.amps.util.ArtifactRetriever;
import com.atlassian.maven.plugins.amps.util.ProjectUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jfrog.maven.annomojo.annotations.MojoComponent;
import org.jfrog.maven.annomojo.annotations.MojoParameter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Base class for webapp mojos
 */
public abstract class AbstractProductHandlerMojo extends AbstractProductHandlerAwareMojo {

    // ------ start inline product context

    private static final String DEFAULT_CONTAINER = "tomcat6x";
    private static final String DEFAULT_SERVER = "localhost";
    private static final String DEFAULT_PRODUCT_DATA_VERSION = "LATEST";
    private static final String DEFAULT_PDK_VERSION = "0.4";
    private static final String DEFAULT_WEB_CONSOLE_VERSION = "1.2.8";

    /**
      * Default product startup timeout: three minutes
     */
    private static final int DEFAULT_PRODUCT_STARTUP_TIMEOUT = 1000 * 60 * 3;

    /**
      * Default product shutdown timeout: three minutes
      */
    private static final int DEFAULT_PRODUCT_SHUTDOWN_TIMEOUT = 1000 * 60 * 3;

    /**
     * Container to run in
     */
    @MojoParameter(expression = "${container}", defaultValue = DEFAULT_CONTAINER)
    protected String containerId;

    /**
     * HTTP port for the servlet containers
     */
    @MojoParameter(expression = "${http.port}", defaultValue = "0")
    private int httpPort;

    /**
     * Application context path
     */
    @MojoParameter(expression = "${context.path}")
    protected String contextPath;

    /**
     * Application server
     */
    @MojoParameter(expression = "${server}", defaultValue = DEFAULT_SERVER)
    protected String server;

    /**
     * Webapp version
     */
    @MojoParameter(expression = "${product.version}")
    private String productVersion;

    /**
     * JVM arguments to pass to cargo
     */
    @MojoParameter(expression = "${jvmargs}")
    protected String jvmArgs;

    /**
     * Product startup timeout in milliseconds
     */
    @MojoParameter(expression = "${product.start.timeout}")
    private int startupTimeout;

    /**
     * Product shutdown timeout in milliseconds
     */
    @MojoParameter(expression = "${product.stop.timeout}")
    private int shutdownTimeout;

    /**
     * System systemProperties to pass to cargo
     *
     * @deprecated Since 3.2, use systemPropertyVariables instead
     */
    @MojoParameter
    @Deprecated
    protected Properties systemProperties = new Properties();

    /**
     * System Properties to pass to cargo using a more familiar syntax.
     *
     * @since 3.2
     */
    @MojoParameter
    protected Map<String, Object> systemPropertyVariables = new HashMap<String, Object>();


    /**
     * A log4j systemProperties file
     */
    @MojoParameter
    protected File log4jProperties;

    /**
     * The test resources version
     * @deprecated Since 3.0-beta2
     */
    @MojoParameter(expression = "${test.resources.version}")
    private String testResourcesVersion;

    /**
     * The test resources version
     */
    @MojoParameter(expression = "${product.data.version}", defaultValue = DEFAULT_PRODUCT_DATA_VERSION)
    private String productDataVersion;

    /**
     * The path to a custom test resources zip
     */
    @MojoParameter(expression = "${product.data.path}")
    private String productDataPath;

    /**
     */
    @MojoParameter
    private List<ProductArtifact> pluginArtifacts = new ArrayList<ProductArtifact>();

    /**
     */
    @MojoParameter
    private List<ProductArtifact> libArtifacts = new ArrayList<ProductArtifact>();

    /**
     */
    @MojoParameter
    private List<ProductArtifact> bundledArtifacts = new ArrayList<ProductArtifact>();

    /**
     * SAL version
     * @deprecated Since 3.2, use {@link #pluginArtifacts} instead
     */
    @MojoParameter
    private String salVersion;

    /**
     * Atlassian Plugin Development Kit (PDK) version
     * @deprecated Since 3.2, use {@link #pluginArtifacts} instead
     */
    @MojoParameter(defaultValue = DEFAULT_PDK_VERSION)
    private String pdkVersion;

    /**
     * Atlassian REST module version
     * @deprecated Since 3.2, use {@link #pluginArtifacts} instead
     */
    @MojoParameter
    private String restVersion;


    /**
     * Felix OSGi web console version
     * @deprecated Since 3.2, use {@link #pluginArtifacts} instead
     */
    @MojoParameter(defaultValue =  DEFAULT_WEB_CONSOLE_VERSION)
    private String webConsoleVersion;

    // ---------------- end product context

    /**
     * Comma-delimited list of plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be
     * ommitted, defaulting to LATEST
     */
    @MojoParameter(expression = "${plugins}")
    private String pluginArtifactsString;

    /**
     * Comma-delimited list of lib artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be
     * ommitted, defaulting to LATEST
     */
    @MojoParameter(expression = "${lib.plugins}")
    private String libArtifactsString;

    /**
     * Comma-delimited list of bundled plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be
     * ommitted, defaulting to LATEST
     */
    @MojoParameter(expression = "${bundled.plugins}")
    private String bundledArtifactsString;

    /**
     * The build directory
     */
    @MojoParameter(expression = "${project.build.directory}", required = true)
    protected File targetDirectory;

    /**
     * The jar name
     */
    @MojoParameter(expression = "${project.build.finalName}", required = true)
    protected String finalName;

    /**
     * If the plugin and optionally its test plugin should be installed
     */
    @MojoParameter (expression = "${install.plugin}", defaultValue = "true")
    protected boolean installPlugin;

    /**
     * The artifact resolver is used to dynamically resolve JARs that have to be in the embedded
     * container's classpaths. Another solution would have been to statitically define them a
     * dependencies in the plugin's POM. Resolving them in a dynamic manner is much better as only
     * the required JARs for the defined embedded container are downloaded.
     */
    @MojoComponent
    protected ArtifactResolver artifactResolver;

    /**
     * The local Maven repository. This is used by the artifact resolver to download resolved
     * JARs and put them in the local repository so that they won't have to be fetched again next
     * time the plugin is executed.
     */
    @MojoParameter(expression = "${localRepository}")
    protected ArtifactRepository localRepository;


    /**
     * The remote Maven repositories used by the artifact resolver to look for JARs.
     */
    @MojoParameter(expression = "${project.remoteArtifactRepositories}")
    protected List repositories;

    /**
     * The artifact factory is used to create valid Maven
     * {@link org.apache.maven.artifact.Artifact} objects. This is used to pass Maven artifacts to
     * the artifact resolver so that it can download the required JARs to put in the embedded
     * container's classpaths.
     */
    @MojoComponent
    protected ArtifactFactory artifactFactory;

    /**
     * A list of product-specific configurations
     */
    @MojoParameter
    protected List<Product> products = new ArrayList<Product>();

    /**
     * File the container logging output will be sent to.
     */
    @MojoParameter
    private String output;


    private Product createDefaultProductContext() throws MojoExecutionException
    {
        Product ctx = new Product();
        ctx.setId(getProductId());
        ctx.setContainerId(containerId);
        ctx.setServer(server);
        ctx.setContextPath(contextPath);
        ctx.setJvmArgs(jvmArgs);
        ctx.setStartupTimeout(startupTimeout);
        ctx.setShutdownTimeout(shutdownTimeout);

        setDefaultSystemProperty(systemPropertyVariables, "atlassian.dev.mode", "true");
        setDefaultSystemProperty(systemPropertyVariables, "java.awt.headless", "true");
        setDefaultSystemProperty(systemPropertyVariables, "plugin.resource.directories", buildResourcesList());

        ctx.setSystemPropertyVariables(systemPropertyVariables);
        ctx.setBundledArtifacts(bundledArtifacts);
        ctx.setLibArtifacts(libArtifacts);
        ctx.setPluginArtifacts(pluginArtifacts);
        ctx.setLog4jProperties(log4jProperties);
        ctx.setHttpPort(httpPort);

        ctx.setVersion(productVersion);
        ctx.setDataVersion(productDataVersion);
        ctx.setDataPath(productDataPath);

        // continue to have these work for now
        ctx.setRestVersion(restVersion);
        ctx.setSalVersion(salVersion);
        ctx.setPdkVersion(pdkVersion);
        ctx.setWebConsoleVersion(webConsoleVersion);

        ctx.setHttpPort(httpPort);
        return ctx;
    }

    /**
     * @return a comma-separated list of resource directories.  If a test plugin is detected, the 
     * test resources directories are included as well.
     */
    private String buildResourcesList()
    {
        // collect all resource directories and make them available for
        // on-the-fly reloading
        StringBuilder resourceProp = new StringBuilder();
        MavenProject mavenProject = getMavenContext().getProject();
        @SuppressWarnings("unchecked") List<Resource> resList = mavenProject.getResources();
        for (int i = 0; i < resList.size(); i++) {
            resourceProp.append(resList.get(i).getDirectory());
            if (i + 1 != resList.size()) {
                resourceProp.append(",");
            }
        }

        if (ProjectUtils.shouldDeployTestJar(getMavenContext()))
        {
            @SuppressWarnings("unchecked") List<Resource> testResList = mavenProject.getTestResources();
            for (int i = 0; i < testResList.size(); i++) {
                if (i == 0 && resourceProp.length() > 0)
                {
                    resourceProp.append(",");
                }
                resourceProp.append(testResList.get(i).getDirectory());
                if (i + 1 != testResList.size()) {
                    resourceProp.append(",");
                }
            }
        }
        return resourceProp.toString();
    }

    private static void setDefaultSystemProperty(final Map<String,Object> props, final String key, final String value)
    {
        if (!props.containsKey(key))
        {
            props.put(key, System.getProperty(key, value));
        }
    }

    private void postProcessProduct(Product product)
    {

        String dversion = System.getProperty("product.data.version", product.getDataVersion());
        String pversion = System.getProperty("product.version", product.getVersion());
        String dpath = System.getProperty("product.data.path", product.getDataPath());

        product.setDataPath(dpath);
        product.setDataVersion(dversion);
        product.setVersion(pversion);
        product.setArtifactRetriever(new ArtifactRetriever(artifactResolver, artifactFactory, localRepository, repositories));

        if (product.getContainerId() == null)
        {
            product.setContainerId(DEFAULT_CONTAINER);
        }

        if (product.getServer() == null)
        {
            product.setServer(DEFAULT_SERVER);
        }

        if (product.getDataVersion() == null)
        {
            product.setDataVersion(DEFAULT_PRODUCT_DATA_VERSION);
        }

        if (product.getPdkVersion() == null)
        {
            product.setPdkVersion(DEFAULT_PDK_VERSION);
        }

        if (product.getWebConsoleVersion() == null)
        {
            product.setWebConsoleVersion(DEFAULT_WEB_CONSOLE_VERSION);
        }
        
        if (product.getOutput() == null)
        {
            product.setOutput(output);
        }

        if (product.getStartupTimeout() <= 0)
        {
            product.setStartupTimeout(DEFAULT_PRODUCT_STARTUP_TIMEOUT);
        }
        
        if (product.getShutdownTimeout() <= 0)
        {
            product.setShutdownTimeout(DEFAULT_PRODUCT_SHUTDOWN_TIMEOUT);
        }
        
        product.setInstanceId(getProductInstanceId(product));
    }

    private List<ProductArtifact> stringToArtifactList(String val, List<ProductArtifact> artifacts)
    {
        if (val == null || val.trim().length() == 0)
        {
            return artifacts;
        }

        for (String ptn : val.split(","))
        {
            String[] items = ptn.split(":");
            if (items.length < 2 || items.length > 3)
            {
                throw new IllegalArgumentException("Invalid artifact pattern: " + ptn);
            }
            String groupId = items[0];
            String artifactId = items[1];
            String version = (items.length == 3 ? items[2] : "LATEST");
            artifacts.add(new ProductArtifact(groupId, artifactId, version));
        }
        return artifacts;
    }

    public final void execute() throws MojoExecutionException, MojoFailureException
    {
        stringToArtifactList(pluginArtifactsString, pluginArtifacts);
        stringToArtifactList(libArtifactsString, libArtifacts);
        stringToArtifactList(bundledArtifactsString, bundledArtifacts);
        systemPropertyVariables.putAll((Map) systemProperties);

        detectDeprecatedVersionOverrides();

        doExecute();
    }

    private void detectDeprecatedVersionOverrides()
    {
        Properties props = getMavenContext().getProject().getProperties();
        for (String deprecatedProperty : new String[] {"sal.version", "rest.version", "web.console.version", "pdk.version"})
        {
            if (props.containsKey(deprecatedProperty))
            {
                getLog().warn("The property '" + deprecatedProperty + "' is no longer usable to override the related bundled plugin." +
                        "  Use <pluginArtifacts> or <libArtifacts> to explicitly override bundled plugins and libraries, respectively.");
            }
        }
    }

    protected Map<String, Product> getProductContexts(MavenGoals goals) throws MojoExecutionException
    {
        Map<String, Product> productMap = new HashMap<String, Product>();


        makeProductsInheritDefaultConfiguration(products, productMap);

        for (Product ctx : productMap.values())
        {
            postProcessProduct(ctx);
            ProductHandler handler = ProductHandlerFactory.create(ctx.getId(), getMavenContext(), goals, getLog());
            ctx.setHttpPort(ctx.getHttpPort() == 0 ? handler.getDefaultHttpPort() : ctx.getHttpPort());
            ctx.setVersion(ctx.getVersion() == null ? "RELEASE" : ctx.getVersion());
            ctx.setContextPath(ctx.getContextPath() == null ? "/" + handler.getId() : ctx.getContextPath());
        }
        return productMap;
    }

    void makeProductsInheritDefaultConfiguration(List<Product> products, Map<String, Product> productMap) throws MojoExecutionException
    {
        productMap.put(getProductId(), createDefaultProductContext());
        if (!products.isEmpty())
        {
            Product defaultProduct = createDefaultProductContext();
            for (Product product : products)
            {
                Product processedProduct = product.merge(defaultProduct);
                String id = getProductInstanceId(processedProduct);
                productMap.put(id, processedProduct);
            }
        }
    }

    private String getProductInstanceId(Product processedProduct)
    {
        return processedProduct.getInstanceId() == null ? processedProduct.getId() : processedProduct.getInstanceId();
    }


    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;
}
