
import java.text.SimpleDateFormat
import java.util.Date

// https://github.com/gradle-plugins/toolbox

plugins {
    id ("java")
    id ("java-gradle-plugin")
    id ("maven-publish")
    id ("idea")
    `build-scan`
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


gradlePlugin {
    //  testSourceSets (sourceSets . functionalTest)
    plugins {
        create("NarPlugin") {
            id = "org.babeloff.nar-plugin"
            implementationClass = "org.babeloff.gradle.api.plugins.NarPlugin"
        }
    }
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}

publishing {
//    publications {
//        mavenJava(MavenPublication) {
//            from components . java
//
//                    artifact sourceJar
//
//                    pom.withXml {
//                        Node root = asNode ()
//                        root.appendNode("name", "Gradle Nar Plugin")
//                        root.appendNode("description", "Gradle plugin to support development of Apache NiFi nar archives")
//                        root.appendNode("url", "https://github.com/babeloff/nar-gradle-plugin")
//                        root.appendNode("inceptionYear", "2015")
//
//                        def scm = root . appendNode ("scm")
//                        scm.appendNode("url", "https://github.com/babeloff/nar-gradle-plugin")
//                        scm.appendNode("connection", "scm:https://github.com/babeloff/nar-gradle-plugin.git")
//                        scm.appendNode("developerConnection", "scm:git:https://github.com/babeloff/nar-gradle-plugin.git")
//
//                        def license = root . appendNode ("licenses").appendNode("license")
//                        license.appendNode("name", "The Apache Software License, Version 2.0")
//                        license.appendNode("url", "http://www.apache.org/licenses/LICENSE-2.0.txt")
//                        license.appendNode("distribution", "repo")
//
//                        def developers = root . appendNode ("developers")
//                        def rkuehne = developers . appendNode ("developer")
//                        rkuehne.appendNode("id", "rkuehne")
//                        rkuehne.appendNode("name", "Robert Kühne")
//                        rkuehne.appendNode("email", "phreed@gmail.com")
//                    }
//        }
//    }
}

//    afterReleaseBuild.dependsOn bintrayUpload
//
//            bintray {
//                user = project.hasProperty("bintrayUser") ? project.getProperty("bintrayUser") : null
//                key = project.hasProperty("bintrayKey") ? project.getProperty("bintrayKey") : null
//                publications = ["mavenJava"]
//                pkg {
//                    repo = "gradle-plugins"
//                    name = "nar-gradle-plugin"
//                    licenses = ["Apache-2.0"]
//                    vcsUrl = "https://github.com/babeloff/nar-gradle-plugin"
//                }
//            }

