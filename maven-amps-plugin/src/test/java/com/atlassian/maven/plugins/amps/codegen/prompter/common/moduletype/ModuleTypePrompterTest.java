package com.atlassian.maven.plugins.amps.codegen.prompter.common.moduletype;

import com.atlassian.maven.plugins.amps.codegen.prompter.AbstractPrompterTest;
import com.atlassian.maven.plugins.amps.codegen.prompter.PluginModulePrompter;
import com.atlassian.plugins.codegen.modules.PluginModuleProperties;
import com.atlassian.plugins.codegen.modules.common.moduletype.ModuleTypeProperties;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since version
 */
public class ModuleTypePrompterTest extends AbstractPrompterTest {
    public static final String INTERFACE_CLASS = "Dictionary";
    public static final String PACKAGE = "com.atlassian.plugins.modules";
    public static final String CLASSNAME = "DictionaryModuleDescriptor";
    public static final String MODULE_NAME = "Dictionary Module Descriptor";
    public static final String MODULE_KEY = "dictionary-module-descriptor";
    public static final String DESCRIPTION = "The Dictionary Module Descriptor Plugin";
    public static final String I18N_NAME_KEY = "dictionary-module-descriptor.name";
    public static final String I18N_DESCRIPTION_KEY = "dictionary-module-descriptor.description";

    public static final String ADV_MODULE_NAME = "My Awesome Plugin";
    public static final String ADV_MODULE_KEY = "awesome-module";
    public static final String ADV_DESCRIPTION = "The Awesomest Plugin Ever";
    public static final String ADV_I18N_NAME_KEY = "awesome-plugin.name";
    public static final String ADV_I18N_DESCRIPTION_KEY = "pluginus-awesomeous.description";

    @Test
    public void basicPropertiesAreValid() throws PrompterException {
        when(prompter.prompt("Enter Interface name", "MYModule")).thenReturn(INTERFACE_CLASS);
        when(prompter.prompt("Enter Interface package", "com.atlassian.plugins.modules")).thenReturn(PACKAGE);
        when(prompter.prompt("Enter Class name", CLASSNAME)).thenReturn(CLASSNAME);
        when(prompter.prompt("Enter Package Name", PACKAGE)).thenReturn(PACKAGE);
        when(prompter.prompt("Show Advanced Setup?", PluginModulePrompter.YN_ANSWERS, "N")).thenReturn("N");
        when(prompter.prompt("Include Example Code?", PluginModulePrompter.YN_ANSWERS, "N")).thenReturn("N");

        ModuleTypePrompter modulePrompter = new ModuleTypePrompter(prompter);
        ModuleTypeProperties props = modulePrompter.getModulePropertiesFromInput(moduleLocation);

        assertEquals("wrong interface", INTERFACE_CLASS, props.getProperty(ModuleTypeProperties.INTERFACE_CLASS));
        assertEquals("wrong interface package", PACKAGE, props.getProperty(ModuleTypeProperties.INTERFACE_PACKAGE));
        assertEquals("wrong class", CLASSNAME, props.getProperty(PluginModuleProperties.CLASSNAME));
        assertEquals("wrong class package", PACKAGE, props.getProperty(PluginModuleProperties.PACKAGE));
        assertEquals("wrong module name", MODULE_NAME, props.getProperty(PluginModuleProperties.MODULE_NAME));
        assertEquals("wrong module key", MODULE_KEY, props.getProperty(PluginModuleProperties.MODULE_KEY));
        assertEquals("wrong description", DESCRIPTION, props.getProperty(PluginModuleProperties.DESCRIPTION));
        assertEquals("wrong i18n name key", I18N_NAME_KEY, props.getProperty(PluginModuleProperties.NAME_I18N_KEY));
        assertEquals("wrong i18n desc key", I18N_DESCRIPTION_KEY, props.getProperty(PluginModuleProperties.DESCRIPTION_I18N_KEY));
    }

    @Test
    public void advancedPropertiesAreValid() throws PrompterException {
        when(prompter.prompt("Enter Interface name", "MYModule")).thenReturn(INTERFACE_CLASS);
        when(prompter.prompt("Enter Interface package", "com.atlassian.plugins.modules")).thenReturn(PACKAGE);
        when(prompter.prompt("Enter Class name", CLASSNAME)).thenReturn(CLASSNAME);
        when(prompter.prompt("Enter Package Name", PACKAGE)).thenReturn(PACKAGE);
        when(prompter.prompt("Show Advanced Setup?", PluginModulePrompter.YN_ANSWERS, "N")).thenReturn("Y");

        when(prompter.prompt("Plugin Name", MODULE_NAME)).thenReturn(ADV_MODULE_NAME);
        when(prompter.prompt("Plugin Key", MODULE_KEY)).thenReturn(ADV_MODULE_KEY);
        when(prompter.prompt("Plugin Description", DESCRIPTION)).thenReturn(ADV_DESCRIPTION);
        when(prompter.prompt("i18n Name Key", I18N_NAME_KEY)).thenReturn(ADV_I18N_NAME_KEY);
        when(prompter.prompt("i18n Description Key", I18N_DESCRIPTION_KEY)).thenReturn(ADV_I18N_DESCRIPTION_KEY);

        when(prompter.prompt("Include Example Code?", PluginModulePrompter.YN_ANSWERS, "N")).thenReturn("N");

        ModuleTypePrompter modulePrompter = new ModuleTypePrompter(prompter);
        ModuleTypeProperties props = modulePrompter.getModulePropertiesFromInput(moduleLocation);

        assertEquals("wrong adv interface", INTERFACE_CLASS, props.getProperty(ModuleTypeProperties.INTERFACE_CLASS));
        assertEquals("wrong adv interface package", PACKAGE, props.getProperty(ModuleTypeProperties.INTERFACE_PACKAGE));
        assertEquals("wrong adv class", CLASSNAME, props.getProperty(PluginModuleProperties.CLASSNAME));
        assertEquals("wrong adv package", PACKAGE, props.getProperty(PluginModuleProperties.PACKAGE));
        assertEquals("wrong adv module name", ADV_MODULE_NAME, props.getProperty(PluginModuleProperties.MODULE_NAME));
        assertEquals("wrong adv module key", ADV_MODULE_KEY, props.getProperty(PluginModuleProperties.MODULE_KEY));
        assertEquals("wrong adv description", ADV_DESCRIPTION, props.getProperty(PluginModuleProperties.DESCRIPTION));
        assertEquals("wrong adv i18n name key", ADV_I18N_NAME_KEY, props.getProperty(PluginModuleProperties.NAME_I18N_KEY));
        assertEquals("wrong adv i18n desc key", ADV_I18N_DESCRIPTION_KEY, props.getProperty(PluginModuleProperties.DESCRIPTION_I18N_KEY));
    }
}
