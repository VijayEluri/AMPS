package com.atlassian.maven.plugins.amps.codegen.prompter.common.servlet;

import com.atlassian.maven.plugins.amps.codegen.annotations.ModuleCreatorClass;
import com.atlassian.maven.plugins.amps.codegen.prompter.AbstractModulePrompter;
import com.atlassian.plugins.codegen.modules.PluginModuleLocation;
import com.atlassian.plugins.codegen.modules.common.servlet.ServletFilterModuleCreator;
import com.atlassian.plugins.codegen.modules.common.servlet.ServletFilterProperties;
import com.atlassian.plugins.codegen.util.ClassnameUtil;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since version
 */
@ModuleCreatorClass(ServletFilterModuleCreator.class)
public class ServletFilterModulePrompter extends AbstractModulePrompter<ServletFilterProperties> {

    public ServletFilterModulePrompter(Prompter prompter) {
        super(prompter);

    }

    @Override
    public ServletFilterProperties promptForBasicProperties(PluginModuleLocation moduleLocation) throws PrompterException {
        String className = promptJavaClassname("Enter New Classname", "MyServletFilter");
        String packageName = promptJavaPackagename("Enter Package Name", getDefaultBasePackage() + ".servlet.filter");

        return new ServletFilterProperties(ClassnameUtil.fullyQualifiedName(packageName, className));
    }

    @Override
    public void promptForAdvancedProperties(ServletFilterProperties props, PluginModuleLocation moduleLocation) throws PrompterException {
        props.setUrlPattern(getUrlPatternFromUser());
        props.setLocation(getLocationFromUser(props.allowedLocations()));
        props.setWeight(promptForInt("Location Weight", 100));

        List<String> dispatchers = promptForDispatchers(props.allowedDispatchers());
        if (dispatchers.size() > 0) {
            props.setDispatchers(dispatchers);
        }

        Map<String, String> initParams = promptForInitParams();
        if (initParams.size() > 0) {
            props.setInitParams(initParams);
        }
    }

    private String getUrlPatternFromUser() throws PrompterException {
        String pattern = promptNotBlank("URL Pattern", "/*");

        return pattern;
    }

    private String getLocationFromUser(List<String> allowedLocations) throws PrompterException {
        StringBuilder locationQuery = new StringBuilder("Choose Filter Chain Location\n");
        List<String> indexChoices = new ArrayList<String>(allowedLocations.size());
        int index = 1;
        for (String location : allowedLocations) {
            String strIndex = Integer.toString(index);
            locationQuery.append(strIndex + ": " + location + "\n");
            indexChoices.add(strIndex);
            index++;
        }

        locationQuery.append("Choose a number: ");
        String locationAnswer = prompt(locationQuery.toString(), indexChoices, "4");

        return allowedLocations.get(Integer.parseInt(locationAnswer) - 1);
    }

    private List<String> promptForDispatchers(List<String> allowedDispatchers) throws PrompterException {
        List<String> dispatchers = new ArrayList<String>();
        List<String> mutableValues = new ArrayList<String>(allowedDispatchers);

        promptForDispatcher(dispatchers, mutableValues);

        return dispatchers;
    }

    private void promptForDispatcher(List<String> dispatchers, List<String> allowedDispatchers) throws PrompterException {
        boolean addDispatcher = promptForBoolean("Add Dispatcher?", "N");

        if (addDispatcher) {
            StringBuilder dispatcherQuery = new StringBuilder("Choose A Dispatcher\n");
            List<String> indexChoices = new ArrayList<String>(allowedDispatchers.size());
            int index = 1;
            for (String dispatcher : allowedDispatchers) {
                String strIndex = Integer.toString(index);
                dispatcherQuery.append(strIndex + ": " + dispatcher + "\n");
                indexChoices.add(strIndex);
                index++;
            }

            dispatcherQuery.append("Choose a number: ");
            String dispatcherAnswer = prompt(dispatcherQuery.toString(), indexChoices, "1");
            int selectedIndex = Integer.parseInt(dispatcherAnswer) - 1;

            String selectedDispatcher = allowedDispatchers.get(selectedIndex);

            dispatchers.add(selectedDispatcher);
            allowedDispatchers.remove(selectedIndex);

            promptForDispatcher(dispatchers, allowedDispatchers);
        }
    }

    private Map<String, String> promptForInitParams() throws PrompterException {
        Map<String, String> params = new HashMap<String, String>();
        promptForInitParam(params);

        return params;
    }

    private void promptForInitParam(Map<String, String> params) throws PrompterException {
        StringBuffer addBuffer = new StringBuffer();
        if (params.size() > 0) {
            addBuffer.append("init-params:\n");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                addBuffer.append(entry.getKey()).append("->").append(entry.getValue()).append("\n");
            }
        }
        addBuffer.append("Add Init-Param?");
        boolean addParam = promptForBoolean(addBuffer.toString(), "N");

        if (addParam) {
            String key = promptNotBlank("param key");
            String value = promptNotBlank("param value");
            params.put(key, value);
            promptForInitParam(params);
        }
    }
}
