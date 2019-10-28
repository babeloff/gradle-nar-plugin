package org.babeloff.gradle.plugin.nifi

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.jar.Manifest
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * @author Robert Kühne
 */
class NifiPluginFuncTest extends Specification {

    private static final TEST_BASE_NAME = 'nifi-test'
    private static final TEST_VERSION = '1.0'

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile
    File settingsFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
plugins {
    id 'org.babeloff.gradle.plugin.nifi'
}
nifi {
    baseName '${TEST_BASE_NAME}'
}
group = 'org.babeloff.test'
version = '${TEST_VERSION}'
"""
        settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
rootProject.name = "nifi-test"
"""
    }

    def "test simple nifi"() {

        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('nifi')
                .withPluginClasspath()
                .build()

        Manifest manifest = extractManifest()

        then:
        manifest != null
        manifest.getMainAttributes().getValue('Nifi-Group') == 'org.babeloff.test'
        manifest.getMainAttributes().getValue('Nifi-Id') == 'nifi-test'
        manifest.getMainAttributes().getValue('Nifi-Version') == '1.0'
        manifest.getMainAttributes().getValue('Nifi-Dependency-Id') == null
    }

    def "test parent nifi entry"() {

        buildFile << """
repositories {
    mavenCentral()
}
dependencies {
    nifi 'org.apache.nifi:nifi-standard-services-api-nifi:0.2.1'
}
"""
        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('nifi')
                .withPluginClasspath()
                .build()

        Manifest manifest = extractManifest()

        then:
        manifest != null
        manifest.getMainAttributes().getValue('Nifi-Group') == 'org.babeloff.test'
        manifest.getMainAttributes().getValue('Nifi-Id') == 'nifi-test'
        manifest.getMainAttributes().getValue('Nifi-Version') == '1.0'
        manifest.getMainAttributes().getValue('Nifi-Dependency-Group') == 'org.apache.nifi'
        manifest.getMainAttributes().getValue('Nifi-Dependency-Id') == 'nifi-standard-services-api-nifi'
        manifest.getMainAttributes().getValue('Nifi-Dependency-Version') == '0.2.1'
    }

    def "test multiple parent nifi entries"() {

        buildFile << """
repositories {
    mavenCentral()
}
dependencies {
    nifi 'org.apache.nifi:nifi-standard-services-api-nifi:0.2.1'
    nifi 'org.apache.nifi:nifi-enrich-nifi:1.5.0'
}
"""
        expect:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('nifi')
                .withPluginClasspath()
                .buildAndFail()
    }

    def "test bundled jar dependencies"() {

        buildFile << """
repositories {
    mavenCentral()
}
dependencies {
    compile group: 'commons-io', name: 'commons-io', version: '2.2'
}
"""
        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('nifi')
                .withPluginClasspath()
                .build()

        then:
        countBundledJars() == 2
    }

    def "test override of manifest configuration"() {

        buildFile << """
repositories {
    mavenCentral()
}
dependencies {
    nifi 'org.apache.nifi:nifi-standard-services-api-nifi:0.2.1'
}
nifi {
    manifest {
        attributes 'Nifi-Group': 'group-override', 'Nifi-Id': 'id-override', 'Nifi-Version': 'version-override'
        attributes 'Nifi-Dependency-Group': 'Nifi-Dependency-Group-override', 'Nifi-Dependency-Id': 'Nifi-Dependency-Id-override', 'Nifi-Dependency-Version': 'Nifi-Dependency-Version-override'
    }
}
"""
        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('nifi')
                .withPluginClasspath()
                .build()

        Manifest manifest = extractManifest()

        then:
        manifest.getMainAttributes().getValue('Nifi-Group') == 'group-override'
        manifest.getMainAttributes().getValue('Nifi-Id') == 'id-override'
        manifest.getMainAttributes().getValue('Nifi-Version') == 'version-override'
        manifest.getMainAttributes().getValue('Nifi-Dependency-Group') == 'Nifi-Dependency-Group-override'
        manifest.getMainAttributes().getValue('Nifi-Dependency-Id') == 'Nifi-Dependency-Id-override'
        manifest.getMainAttributes().getValue('Nifi-Dependency-Version') == 'Nifi-Dependency-Version-override'
    }

    def "test override bundled dependencies"() {
        buildFile << """
nifi {
    bundledDependencies = [jar]
}
"""
        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('nifi')
                .withPluginClasspath()
                .build()

        then:
        countBundledJars() == 1
    }

    def "test empty bundled dependencies"() {
        buildFile << """
nifi {
    bundledDependencies = null
}
"""
        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('nifi')
                .withPluginClasspath()
                .build()

        then:
        countBundledJars() == 0
    }

    def "test remove parent configuration"() {
        buildFile << """
nifi {
    parentNifiConfiguration = null
}
"""
        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('nifi')
                .withPluginClasspath()
                .build()

        Manifest manifest = extractManifest()

        then:
        countBundledJars() == 1
        manifest.getMainAttributes().getValue('Nifi-Dependency-Group') == null
        manifest.getMainAttributes().getValue('Nifi-Dependency-Id') == null
        manifest.getMainAttributes().getValue('Nifi-Dependency-Version') == null
    }

    int countBundledJars() {
        int counter = 0
        Pattern pattern = Pattern.compile('^META-INF/bundled-dependencies/.+$')
        eachZipEntry { ZipInputStream zip, ZipEntry entry ->
            if (pattern.matcher(entry.name).matches()) {
                println entry.name
                counter++
            }
            true
        }
        counter
    }

    Manifest extractManifest() {
        Manifest manifest = null
        eachZipEntry { ZipInputStream zip, ZipEntry entry ->
            if (entry.name == 'META-INF/MANIFEST.MF') {
                manifest = new Manifest(zip)
                return false
            } else {
                return true
            }
        }

        manifest
    }

    private void eachZipEntry(Closure closure) {
        nifiFile().withInputStream {
            ZipInputStream zip = new ZipInputStream(it)
            ZipEntry entry = zip.nextEntry
            while (entry != null) {
                def result = closure(zip, entry)
                if (!result) {
                    break
                }
                entry = zip.nextEntry
            }
        }
    }

    private File nifiFile() {
        new File(testProjectDir.root, "build/libs/${TEST_BASE_NAME}-${TEST_VERSION}.nifi")
    }
}
