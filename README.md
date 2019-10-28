# nifi-gradle-plugin

A gradle plugin to create nifi files for [Apache nifi](http://nifi.apache.org).

Originally this was based on gradle-nifi-plugin but it has substantially deviated from that source.
THIS IS NOT READY FOR USE BY ANYBODY!


## Installation
To use the plugin, add the bintray repository to your script and add the plugin dependency:

```kotlin
buildscript {
    repositories {
        mavenCentral()
        maven {
            url = uri("http://dl.bintray.com/babeloff/gradle-plugins")
        }
    }
    dependencies {
        classpath (group="org.babeloff.gradle.plugin.nifi", name="nifi-gradle-plugin", version="2019.10.0")
    }
}

apply(plugin="org.babeloff.gradle.plugin.nifi")
```
## Usage

Run `gradle nifi` to execute the nifi task and create a nifi archive.

## Configuration

This plugin depends on the JavaPlugin.
If it does not exist it will add it to the build.

### Nifi Task
The plugin adds a new preconfigured task of type Nifi named `nifi` to the project.
The Nifi class extends Jar and can be modified as such.
The task is configured to add all runtime dependencies and the jar archive itself to the nifi archive.

### Nifi Parent
Nifi archives can have a parent nifi.
The parent is optional and there can be at maximum one parent.
The parent relationship is added to the manifest.
To tell the plugin to add a parent you have to add a nifi dependency to the nifi configuration.
The nifi configuration is created by the plugin.
Add the parent nifi like this:

```kotlin
dependencies {
    nifi("org.apache.nifi:nifi-standard-services-api-nifi:0.2.1")
}
```

If you add more than one dependency, it will complain and crash the build.

## Manifest

The manifest of a nifi file contains properties to identify the nifi file and a parent nifi.
This plugin configures the manifest generation to contain the values from the project name, group and version.
The same goes for the nifi parent.

### Default manifest values

Manifest Property Key | Value
--- | ---
Nifi-Group | project.group
Nifi-Id | project.name
Nifi-Version | project.version
Nifi-Dependency-Group | nifi config group
Nifi-Dependency-Id | nifi config name
Nifi-Dependency-Version | nifi config version

### Override manifest values
The plugin respects manifest overrides from the user.
For example:

```kotlin
nifi {
    manifest {
        attributes (
            "Nifi-Group" to "overriden-nifi-group-value"
        )
    }
}
```

A full description can be found at https://docs.gradle.org/current/userguide/java_plugin.html#sec:jar.

## Shortcomings

The goal is to be feature compatible with the reference implementation nifi-nifi-maven-plugin.

* https://github.com/apache/nifi-maven
* https://cwiki.apache.org/confluence/display/NIFI/Maven+Projects+for+Extensions
* https://issues.apache.org/jira/projects/NIFI
* https://gitbox.apache.org/repos/asf?p=nifi-maven.git

```kotlin
configurations {
    compileOnly.extendsFrom(configurations.nifi)
}
```


### Service Locator

Apache nifi uses the [ServiceLocator](http://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html) to define processors. 
The [Processor API](https://nifi.apache.org/docs/nifi-docs/html/developer-guide.html#processor_api)
explains in detail how this works. Basically, you have to add a file in `META-INF/services` directory named
`org.apache.nifi.processor.Processor`. This text file contains a fully-qualified class names of your processors.
One per line.

