package com.atlassian.maven.plugins.amps.codegen.prompter;

import com.atlassian.maven.plugins.amps.codegen.prompter.PluginModulePrompter;
import com.atlassian.maven.plugins.amps.codegen.prompter.PluginModulePrompterRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: jdoklovic
 */
public class PluginModulePrompterRegistryImpl implements PluginModulePrompterRegistry {

    private Map<Class,PluginModulePrompter> modulePrompters;

    public PluginModulePrompterRegistryImpl() {
        this.modulePrompters = new HashMap<Class, PluginModulePrompter>();
    }

    @Override
    public void registerModulePrompter(Class creatorClass, PluginModulePrompter prompter) {
        modulePrompters.put(creatorClass,prompter);
    }

    @Override
    public PluginModulePrompter getPrompterForCreatorClass(Class clazz) {
        return modulePrompters.get(clazz);
    }

}
