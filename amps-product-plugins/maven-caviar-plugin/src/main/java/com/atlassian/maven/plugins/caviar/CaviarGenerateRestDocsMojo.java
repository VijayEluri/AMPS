package com.atlassian.maven.plugins.caviar;

import com.atlassian.maven.plugins.amps.GenerateRestDocsMojo;

import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;

/**
 * @since 3.6.1
 */
@MojoGoal("generate-rest-docs")
@MojoRequiresDependencyResolution("test")
public class CaviarGenerateRestDocsMojo extends GenerateRestDocsMojo
{
}
