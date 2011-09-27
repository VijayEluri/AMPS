package com.atlassian.maven.plugins.amps.codegen.prompter.common.servlet;

import java.util.Map;

import com.atlassian.maven.plugins.amps.codegen.annotations.ModuleCreatorClass;
import com.atlassian.maven.plugins.amps.codegen.prompter.AbstractModulePrompter;
import com.atlassian.plugins.codegen.modules.PluginModuleLocation;
import com.atlassian.plugins.codegen.modules.common.servlet.ServletModuleCreator;
import com.atlassian.plugins.codegen.modules.common.servlet.ServletProperties;
import com.atlassian.plugins.codegen.util.ClassnameUtil;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

/**
 * @since 3.6
 */
@ModuleCreatorClass(ServletModuleCreator.class)
public class ServletModulePrompter extends AbstractModulePrompter<ServletProperties>
{

    public ServletModulePrompter(Prompter prompter)
    {
        super(prompter);

    }

    @Override
    public ServletProperties promptForBasicProperties(PluginModuleLocation moduleLocation) throws PrompterException
    {
        String className = promptJavaClassname("Enter New Classname", "MyServlet");
        String packageName = promptJavaPackagename("Enter Package Name", getDefaultBasePackage() + ".servlet");

        return new ServletProperties(ClassnameUtil.fullyQualifiedName(packageName, className));
    }

    @Override
    public void promptForAdvancedProperties(ServletProperties props, PluginModuleLocation moduleLocation) throws PrompterException
    {
        props.setUrlPattern(getUrlPatternFromUser("/" + props.getClassname()
                .toLowerCase()));

        Map<String, String> initParams = promptForParams("Add Init-Param?");
        if (initParams.size() > 0)
        {
            props.setInitParams(initParams);
        }
    }

    private String getUrlPatternFromUser(String defaultVal) throws PrompterException
    {
        String pattern = promptNotBlank("URL Pattern", defaultVal);

        return pattern;
    }

}