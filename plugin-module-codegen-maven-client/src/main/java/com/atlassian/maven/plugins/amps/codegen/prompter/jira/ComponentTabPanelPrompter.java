package com.atlassian.maven.plugins.amps.codegen.prompter.jira;

import com.atlassian.maven.plugins.amps.codegen.annotations.ModuleCreatorClass;
import com.atlassian.maven.plugins.amps.codegen.prompter.AbstractModulePrompter;
import com.atlassian.plugins.codegen.modules.PluginModuleLocation;
import com.atlassian.plugins.codegen.modules.common.Label;
import com.atlassian.plugins.codegen.modules.jira.ComponentTabPanelModuleCreator;
import com.atlassian.plugins.codegen.modules.jira.TabPanelProperties;
import com.atlassian.plugins.codegen.util.ClassnameUtil;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

/**
 * @since 3.6
 */
@ModuleCreatorClass(ComponentTabPanelModuleCreator.class)
public class ComponentTabPanelPrompter extends AbstractModulePrompter<TabPanelProperties>
{

    public ComponentTabPanelPrompter(Prompter prompter)
    {
        super(prompter);

    }

    @Override
    public TabPanelProperties promptForBasicProperties(PluginModuleLocation moduleLocation) throws PrompterException
    {

        TabPanelProperties props;
        boolean useGenericClass = promptForBoolean("Use " + ComponentTabPanelModuleCreator.GENERIC_CLASS + "?", "Y");

        if (useGenericClass)
        {
            props = new TabPanelProperties(ComponentTabPanelModuleCreator.FQ_GENERIC_CLASS);
            props.setModuleNameAndKey(promptNotBlank("Enter Plugin Module Name", "My Component Tab Panel"));
            suppressAdvancedNamePrompt();
        } else
        {
            String className = promptJavaClassname("Enter New Classname", "MyComponentTabPanel");
            String packageName = promptJavaPackagename("Enter Package Name", getDefaultBasePackage() + ".jira.tabpanels");
            String fqClass = ClassnameUtil.fullyQualifiedName(packageName, className);

            props = new TabPanelProperties(fqClass);
        }

        Label label = new Label(props.getModuleKey() + ".label", props.getModuleName());
        props.setLabel(label);

        props.setUseCustomClass(!useGenericClass);

        return props;
    }

    @Override
    public void promptForAdvancedProperties(TabPanelProperties props, PluginModuleLocation moduleLocation) throws PrompterException
    {
        props.setOrder(promptForInt("Order", 10));

        Label label = props.getLabel();
        String labelKey = promptNotBlank("Enter Label Key", props.getLabel()
                .getKey());
        String labelValue = promptNotBlank("Enter Label Value", props.getLabel()
                .getValue());

        label.setKey(labelKey);
        label.setValue(labelValue);

        props.addI18nProperty(labelKey, labelValue);
    }
}