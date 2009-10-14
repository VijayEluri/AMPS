package com.atlassian.sdk.accept;

import static com.atlassian.sdk.accept.SdkHelper.runSdkScript;

import java.io.IOException;
import java.io.File;

public class TestClover extends SdkTestBase
{

    public void testConfluence() throws IOException, InterruptedException
    {
        createAndRunClover("confluence");
    }

    private void createAndRunClover(String productId)
            throws IOException, InterruptedException
    {
        final String prefix = "create-and-run-clover";
        File appDir = SdkHelper.createPlugin(productId, baseDir, sdkHome, prefix);

        runSdkScript(sdkHome, appDir, "atlas-clover");

        File pluginJar = new File(new File(appDir, "target"), prefix + "-" + productId + "-plugin-1.0-SNAPSHOT.jar");
        assertTrue(pluginJar.exists());

        final File cloverDir = SdkHelper.file(appDir, "target", "site", "clover");
        final File cloverHtmlReport = SdkHelper.file(cloverDir, "index.html");
        final File cloverXmlReport = SdkHelper.file(cloverDir,  "clover.xml");
        assertTrue(cloverHtmlReport.exists());
        assertTrue(cloverXmlReport.exists());

    }

}
