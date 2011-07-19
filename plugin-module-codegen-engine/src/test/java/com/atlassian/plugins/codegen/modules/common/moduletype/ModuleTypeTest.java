package com.atlassian.plugins.codegen.modules.common.moduletype;

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
public class ModuleTypeTest extends AbstractCodegenTestCase {
    public static final String PACKAGE_NAME = "com.atlassian.plugins.modules";
    private PluginModuleLocation moduleLocation;
    private ModuleTypeProperties props;

    @Before
    public void runGenerator() throws Exception {
        ModuleTypeModuleCreator creator = pluginModuleCreatorRegistry.getModuleCreator(PluginModuleCreatorRegistry.JIRA, ModuleTypeModuleCreator.class);
        moduleLocation = new PluginModuleLocation.Builder(srcDir)
                .resourcesDirectory(resourcesDir)
                .testDirectory(testDir)
                .templateDirectory(templateDir)
                .build();

        props = new ModuleTypeProperties(PACKAGE_NAME + ".DictionaryModuleDescriptor");
        props.setFullyQualifiedInterface(PACKAGE_NAME + ".Dictionary");
        props.setIncludeExamples(false);

        creator.createModule(moduleLocation, props);
    }

    @Test
    public void allFilesAreGenerated() throws Exception {
        String packagePath = PACKAGE_NAME.replaceAll("\\.", File.separator);
        String itPackagePath = "it" + File.separator + packagePath;
        assertTrue("interface class not generated", new File(srcDir, packagePath + File.separator + "Dictionary.java").exists());
        assertTrue("main class not generated", new File(srcDir, packagePath + File.separator + "DictionaryModuleDescriptor.java").exists());
        assertTrue("test class not generated", new File(testDir, packagePath + File.separator + "DictionaryModuleDescriptorTest.java").exists());
        assertTrue("funcTest class not generated", new File(testDir, itPackagePath + File.separator + "DictionaryModuleDescriptorFuncTest.java").exists());
        assertTrue("plugin.xml not generated", new File(resourcesDir, "atlassian-plugin.xml").exists());

    }

    @Test
    public void pluginXmlContainsModule() throws IOException {
        String pluginXmlContent = FileUtils.readFileToString(pluginXml);

        assertTrue("module not found in plugin xml", pluginXmlContent.contains("<module-type"));
        assertTrue("module class not found in plugin xml", pluginXmlContent.contains("class=\"" + PACKAGE_NAME + ".DictionaryModuleDescriptor\""));
    }

}
