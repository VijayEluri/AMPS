package com.atlassian.maven.plugins.amps.codegen;

import com.atlassian.plugins.codegen.modules.PluginModuleCreator;
import org.codehaus.plexus.components.interactivity.PrompterException;

import java.util.Map;

/**
 * @since version
 */
public interface PluginModuleSelectionQueryer {
    public PluginModuleCreator selectModule(Map<Class,PluginModuleCreator> map) throws PrompterException;
}
