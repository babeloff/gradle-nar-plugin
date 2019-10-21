
import java.text.SimpleDateFormat
import java.util.Date

// https://github.com/gradle-plugins/toolbox

plugins {
    id ("java")
    id ("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.10.1"
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
    // implementation("com.gradle.publish:plugin-publish-plugin:0.10.1")

    testCompile(gradleTestKit())
    testImplementation(group="org.junit.jupiter", name="junit-jupiter-api", version="5.5.2")
    testRuntimeOnly(group="org.junit.jupiter", name="junit-jupiter-engine", version="5.5.2")
    testCompile (group="org.spockframework", name="spock-core", version="1.3-groovy-2.5")


    //implementation (group="org.apache.nifi", name="nifi-properties", version="1.9.2")
    implementation (group="org.apache.nifi", name="nifi-nar-utils", version="1.9.2")
//    implementation (group="org.apache.nifi", name="nifi-api", version="1.9.2")
//    implementation (group="org.apache.nifi", name="nifi-mock", version="1.9.2")
//    implementation (group="org.apache.nifi", name="nifi-processor-utils", version="1.9.2")
//    implementation (group="org.apache.nifi", name="nifi-framework-api", version="1.9.2")
//    implementation (group="org.apache.nifi", name="nifi-record", version="1.9.2")
//    implementation (group="org.apache.nifi", name="nifi-standard-utils", version="1.9.2")
//    implementation (group="org.apache.nifi", name="nifi-framework-nar-utils", version="1.9.2")
//    implementation (group="org.apache.nifi", name="nifi-framework-nar", version="1.9.2")
//    implementation (group="org.apache.nifi", name="nifi-ui-extension", version="1.9.2")

//    implementation (group="org.codehaus.plexus", name="plexus-archiver", version="4.1.0")
    // implementation (group="org.codehaus.plexus", name="plexus-interpolation", version="1.26")
//    implementation (group="org.codehaus.plexus", name="plexus-utils", version="3.3.0")

}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

//
//sourceSets {
//    integrationTest {
//        groovy.srcDir file("src/integTest/groovy")
//        resources.srcDir file("src/integTest/resources")
//        compileClasspath += sourceSets.main.output + configurations.testRuntime
//        runtimeClasspath += output + compileClasspath
//    }
//    functionalTest {
//        groovy.srcDir file("src/funcTest/groovy")
//        resources.srcDir file("src/funcTest/resources")
//        compileClasspath += sourceSets.main.output + configurations.testRuntime
//        runtimeClasspath += output + compileClasspath
//    }
//}
//

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

//    named<Test>("integrationTest") {
//        description = "Runs the integration tests."
//        group = "verification"
//        testClassesDirs = sourceSets.integrationTest.output.classesDirs
//        classpath = sourceSets.integrationTest.runtimeClasspath
//        mustRunAfter test
//    }
//
//    val functionalTest by registering(Test::class) {
//        description = "Runs the functional tests."
//        group = "verification"
//        testClassesDirs = sourceSets.functionalTest.output.classesDirs
//        classpath = sourceSets.functionalTest.runtimeClasspath
//        mustRunAfter test
//    }
//
//    check.dependsOn (functionalTest)
//
//    named<Jar>("sourceJar") {
//        classifier = "sources"
//        from (sourceSets . main . allJava)
//    }
}

/**
 * You will need an API Key.
 *  * https://plugins.gradle.org/user/register
 */
pluginBundle {
    website = "https://github.com/babeloff/nar-gradle-plugin"
    vcsUrl = "https://github.com/babeloff/nar-gradle-plugin"
    description = "Nifi Archive (NAR) generator."
    tags = listOf("nifi", "archive", "nar", "pulsar")

    plugins {
        create("NarPlugin") {
            displayName = "Gradle Nifi Archive (NAR) plugin"
        }
    }
}


gradlePlugin {
    //  testSourceSets (sourceSets . functionalTest)
    plugins {
        create("NarPlugin") {
            id = "org.babeloff.nar-plugin"
            implementationClass = "org.babeloff.gradle.api.plugins.NarPlugin"
            displayName = "NAR"
            description = "Generate Nifi Archive (NAR)"
        }
    }
}
