package com.atlassian.plugins.codegen.modules.jira;

import com.atlassian.plugins.codegen.annotations.Dependencies;
import com.atlassian.plugins.codegen.annotations.Dependency;
import com.atlassian.plugins.codegen.annotations.JiraPluginModuleCreator;
import com.atlassian.plugins.codegen.modules.AbstractPluginModuleCreator;
import com.atlassian.plugins.codegen.modules.PluginModuleLocation;

/**
 * @since 3.6
 */
@JiraPluginModuleCreator
@Dependencies({
        @Dependency(groupId = "org.mockito", artifactId = "mockito-all", version = "1.8.5", scope = "test")
})
public class KeyboardShortcutModuleCreator extends AbstractPluginModuleCreator<KeyboardShortcutProperties>
{

    public static final String MODULE_NAME = "Keyboard Shortcut";
    private static final String TEMPLATE_PREFIX = "templates/jira/keyboard/";

    private static final String PLUGIN_MODULE_TEMPLATE = TEMPLATE_PREFIX + "keyboard-shortcut-plugin.xml.vtl";

    @Override
    public void createModule(PluginModuleLocation location, KeyboardShortcutProperties props) throws Exception
    {

        addModuleToPluginXml(PLUGIN_MODULE_TEMPLATE, location, props);
    }


    @Override
    public String getModuleName()
    {
        return MODULE_NAME;
    }
}
