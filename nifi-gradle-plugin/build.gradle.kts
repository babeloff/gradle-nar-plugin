
import java.text.SimpleDateFormat
import java.util.Date

// https://github.com/gradle-plugins/toolbox

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
    id ("idea")
    `maven-publish`
}

group = "org.babeloff"
version = "2019.10.0"

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    compileOnly(gradleApi())

    testImplementation(gradleTestKit())
    testImplementation(group="org.junit.jupiter", name="junit-jupiter-api", version="5.5.2")
    testRuntimeOnly(group="org.junit.jupiter", name="junit-jupiter-engine", version="5.5.2")

    implementation (group="org.apache.nifi", name="nifi-nar-utils", version="1.9.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    withType<JavaCompile> {
        //options. = "1.8"
    }

    withType<Wrapper> {
        gradleVersion = findProperty("gradleLatestVersion") as? String ?: gradle.gradleVersion
        distributionType = Wrapper.DistributionType.ALL
    }

    named<Jar>("jar") {
        manifest {
            attributes["Implementation-Title"] = "Gradle Docker plugin"
            attributes["Implementation-Version"] = project.version
            attributes["Built-By"] = System.getProperty("user.name")
            attributes["Built-Date"] = SimpleDateFormat("MM/dd/yyyy").format(Date())
            attributes["Built-JDK"] = System.getProperty("java.version")
            attributes["Built-Gradle"] = gradle.gradleVersion
        }
    }

    test {
        useJUnitPlatform {
            includeTags("fast", "smoke & feature-a")
            // excludeTags "slow", "ci"
            includeEngines("junit-jupiter")
            // excludeEngines "junit-vintage"
        }
    }

    val plugin by registering(GradleBuild::class) {
        dir = file("plugin")
        tasks = listOf("publish")
    }

    val consumer by registering(GradleBuild::class) {
        dir = file("consumer")
        tasks = listOf("myCopyTask")
    }

    consumer {
        dependsOn(plugin)
    }

}

/**
 * You will need an API Key.
 *  * https://plugins.gradle.org/user/register
 */
pluginBundle {
    website = "https://github.com/babeloff/nifi-gradle-plugin"
    vcsUrl = "https://github.com/babeloff/nifi-gradle-plugin"
    description = "Nifi Archive (NIFI) generator."
    tags = listOf("nifi", "archive", "nar", "pulsar", "managed")

    plugins {
        create("NifiPlugin") {
            displayName = "Gradle Nifi Archive (NAR) plugin"
        }
    }
}


gradlePlugin {
    //  testSourceSets (sourceSets . functionalTest)
    plugins {
        create("NifiPlugin") {
            id = "org.babeloff.nifi-plugin"
            implementationClass = "org.babeloff.gradle.api.plugins.NifiPlugin"
            displayName = "NIFI"
            description = "Generate Nifi Archive (NAR)"
        }
    }
}
