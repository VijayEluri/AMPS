package com.atlassian.plugins.codegen.modules;

import com.atlassian.plugins.codegen.util.ClassnameUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Properties;

/**
 * Author: jdoklovic
 */
public class BasicClassModuleProperties extends AbstractClassBasedModuleProperties {

    public BasicClassModuleProperties() {
        this("MyPluginModule");
    }

    public BasicClassModuleProperties(String fqClassName) {
        super();

        setFullyQualifiedClassname(fqClassName);

        String classname = getProperty(CLASSNAME);
        setModuleName(ClassnameUtil.camelCaseToSpaced(classname));
        setModuleKey(ClassnameUtil.camelCaseToDashed(classname).toLowerCase());
        setDescription("The " + getProperty(MODULE_NAME) + " Plugin");
        setNameI18nKey(getProperty(MODULE_KEY) + ".name");
        setDescriptionI18nKey(getProperty(MODULE_KEY) + ".description");

        addI18nProperty(getProperty(DESCRIPTION_I18N_KEY), getProperty(DESCRIPTION));
        addI18nProperty(getProperty(NAME_I18N_KEY), getProperty(MODULE_NAME));

    }


}
