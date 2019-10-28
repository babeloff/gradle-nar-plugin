package org.babeloff.gradle.plugin.nifi

import org.babeloff.gradle.api.tasks.NifiTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import spock.lang.Specification

/**
 * @author Robert Kühne
 */
class NifiPluginTest extends Specification {

    public static final String PLUGIN = 'org.babeloff.gradle.plugin.nifi'
    public static final String NIFI_TASK = 'nifi'

    @Test
    public void nifiPluginSmokeTest() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'org.babeloff.nifi-plugin'

        assertTrue(project.tasks.nifi instanceof NifiTask)
    }

}
