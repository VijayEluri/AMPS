package com.atlassian.plugins.codegen.modules.common.component;

import com.atlassian.plugins.codegen.AbstractCodegenTestCase;
import com.atlassian.plugins.codegen.modules.PluginModuleCreator;
import com.atlassian.plugins.codegen.modules.PluginModuleCreatorRegistry;
import com.atlassian.plugins.codegen.modules.PluginModuleLocation;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
//TODO: update test to use Dom4J
/**
 * @since version
 */
public class ComponentTest extends AbstractCodegenTestCase {
    public static final String PACKAGE_NAME = "com.atlassian.plugins.component";
    private PluginModuleLocation moduleLocation;
    private ComponentProperties props;
    ComponentModuleCreator creator;

    @Before
    public void runGenerator() throws Exception {
        creator = pluginModuleCreatorRegistry.getModuleCreator(PluginModuleCreatorRegistry.JIRA, ComponentModuleCreator.class);
        moduleLocation = new PluginModuleLocation.Builder(srcDir)
                .resourcesDirectory(resourcesDir)
                .testDirectory(testDir)
                .templateDirectory(templateDir)
                .build();

        props = new ComponentProperties(PACKAGE_NAME + ".CustomComponent");
        props.setIncludeExamples(false);

    }

    @Test
    public void allFilesAreGenerated() throws Exception {
        props.setFullyQualifiedInterface(PACKAGE_NAME + ".CustomInterface");
        props.setGenerateClass(true);
        props.setGenerateInterface(true);
        creator.createModule(moduleLocation, props);

        String packagePath = PACKAGE_NAME.replaceAll("\\.", File.separator);
        String itPackagePath = "it" + File.separator + packagePath;
        assertTrue("main class not generated", new File(srcDir, packagePath + File.separator + "CustomComponent.java").exists());
        assertTrue("interface not generated", new File(srcDir, packagePath + File.separator + "CustomInterface.java").exists());
        assertTrue("test class not generated", new File(testDir, packagePath + File.separator + "CustomComponentTest.java").exists());
        assertTrue("funcTest class not generated", new File(testDir, itPackagePath + File.separator + "CustomComponentFuncTest.java").exists());
        assertTrue("main class not generated", new File(resourcesDir, "atlassian-plugin.xml").exists());

    }

    @Test
    public void pluginXmlContainsModule() throws Exception {
        props.setFullyQualifiedInterface(PACKAGE_NAME + ".CustomInterface");
        props.setGenerateClass(true);
        props.setGenerateInterface(true);
        creator.createModule(moduleLocation, props);
        String pluginXmlContent = FileUtils.readFileToString(pluginXml);

        assertTrue("module not found in plugin xml", pluginXmlContent.contains("<component"));
        assertTrue("module class not found in plugin xml", pluginXmlContent.contains("class=\"" + PACKAGE_NAME + ".CustomComponent\""));
    }

}
