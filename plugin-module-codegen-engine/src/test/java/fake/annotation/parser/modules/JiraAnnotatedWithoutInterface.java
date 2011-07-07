package fake.annotation.parser.modules;

import com.atlassian.plugins.codegen.annotations.JiraPluginModuleCreator;

/**
 * @since version
 */
@JiraPluginModuleCreator
public class JiraAnnotatedWithoutInterface {
    public static final String MODULE_NAME = "Not A Plugin Module Creator";

    public String getModuleName() {
        return MODULE_NAME;
    }
}
