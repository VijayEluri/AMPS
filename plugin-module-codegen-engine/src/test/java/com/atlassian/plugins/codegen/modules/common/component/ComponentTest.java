package com.atlassian.plugins.codegen.modules.common.component;

import java.io.File;
import java.util.regex.Matcher;

import com.atlassian.plugins.codegen.AbstractCodegenTestCase;
import com.atlassian.plugins.codegen.modules.PluginModuleLocation;

import org.dom4j.Document;
import org.dom4j.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since 3.6
 */
public class ComponentTest extends AbstractCodegenTestCase<ComponentProperties>
{
    public static final String PACKAGE_NAME = "com.atlassian.plugins.component";

    @Before
    public void runGenerator() throws Exception
    {
        setCreator(new ComponentModuleCreator());
        setModuleLocation(new PluginModuleLocation.Builder(srcDir)
                .resourcesDirectory(resourcesDir)
                .testDirectory(testDir)
                .templateDirectory(templateDir)
                .build());

        setProps(new ComponentProperties(PACKAGE_NAME + ".CustomComponent"));

        props.setIncludeExamples(false);

    }

    @Test
    public void allFilesAreGenerated() throws Exception
    {
        props.setFullyQualifiedInterface(PACKAGE_NAME + ".CustomInterface");
        props.setGenerateClass(true);
        props.setGenerateInterface(true);
        creator.createModule(moduleLocation, props);

        String packagePath = PACKAGE_NAME.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
        String itPackagePath = "it" + File.separator + packagePath;
        assertTrue("main class not generated", new File(srcDir, packagePath + File.separator + "CustomComponent.java").exists());
        assertTrue("interface not generated", new File(srcDir, packagePath + File.separator + "CustomInterface.java").exists());
        assertTrue("test class not generated", new File(testDir, packagePath + File.separator + "CustomComponentTest.java").exists());
        assertTrue("funcTest class not generated", new File(testDir, itPackagePath + File.separator + "CustomComponentFuncTest.java").exists());
        assertTrue("main class not generated", new File(resourcesDir, "atlassian-plugin.xml").exists());

    }

    @Test
    public void componentAdded() throws Exception
    {
        props.setFullyQualifiedInterface(PACKAGE_NAME + ".CustomInterface");
        props.setGenerateClass(true);
        props.setGenerateInterface(true);
        creator.createModule(moduleLocation, props);
        Document pluginDoc = getXmlDocument(pluginXml);

        String compXPath = "/atlassian-plugin/component[@name='Custom Component' and @key='custom-component' and @i18n-name-key='custom-component.name' and @class='" + PACKAGE_NAME + ".CustomComponent']";
        String compIfaceXPath = "interface[text() = '" + PACKAGE_NAME + ".CustomInterface']";

        Node compNode = pluginDoc.selectSingleNode(compXPath);
        assertNotNull("component not found", compNode);
        assertNotNull("interface not found", compNode.selectSingleNode(compIfaceXPath));

    }

}
