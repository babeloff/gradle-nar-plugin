package org.babeloff.gradle.plugin.nifi

import org.babeloff.gradle.api.tasks.NifiTaskUnmanaged
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * @author Robert Kühne
 */
class NifiPluginTest extends Specification {

    public static final String PLUGIN = 'org.babeloff.gradle.plugin.nifi'
    public static final String NIFI_TASK = 'nifi'

    private Project project

    def setup() {
        project = ProjectBuilder.builder().build()
    }

    def "create empty project"() {

        when:
        project.apply plugin: PLUGIN

        then:
        project.plugins.hasPlugin(NifiPlugin)
        project.plugins.hasPlugin(JavaPlugin)
    }

    def "project has a nifi task"() {

        when:
        project.apply plugin: PLUGIN

        then:
        project.tasks[NIFI_TASK]
    }

    def "has nifi file extension"() {

        when:
        project.apply plugin: PLUGIN

        then:
        NifiTaskUnmanaged nifi = project.tasks[NIFI_TASK]
        nifi.extension == NIFI_TASK
    }

    def "assemble dependsOn nifi"() {

        when:
        project.apply plugin: PLUGIN

        then:
        project.tasks['assemble'].dependsOn.find { it instanceof Task && it.name == 'nifi' } != null
    }

    def "nifi has no Nifi-Dependency-Id when no nifi dependency is set"() {

        when:
        project.apply plugin: PLUGIN

        then:
        NifiTaskUnmanaged nifi = project.tasks[NIFI_TASK]
        !nifi.manifest.attributes.containsKey('Nifi-Dependency-Id')
    }
}
