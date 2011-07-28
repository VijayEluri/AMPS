package com.atlassian.plugins.codegen.modules.jira;

import com.atlassian.plugins.codegen.modules.BasicClassModuleProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @since version
 */
public class JqlFunctionProperties extends BasicClassModuleProperties {

    public JqlFunctionProperties() {
        this("MyJqlFunction");
    }

    public JqlFunctionProperties(String fqClassName) {
        super(fqClassName);
    }
}
