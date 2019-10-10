
// https://github.com/gradle-plugins/toolbox

plugins {
    java
    `java-gradle-plugin`
}



gradlePlugin {
    plugins {
        create("NarPlugin") {
            id = "nar-plugin"
            implementationClass = "de.fanero.gradle.plugin.nar.Nar"
        }
    }
}



//
//apply plugin: "groovy"
//apply plugin: "idea"
//apply plugin: "maven-publish"
//apply plugin: "java-gradle-plugin"
//
//group = "de.fanero.gradle.plugin.nar"
//
//sourceCompatibility = 1.7
//

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
    //testCompile (group="org.hamcrest", name="hamcrest-integration", version="1.3")
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
    wrapper {
        distributionType = Wrapper.DistributionType.ALL
    }

    test {
        useJUnitPlatform {
            includeTags ("fast", "smoke & feature-a")
            // excludeTags "slow", "ci"
            includeEngines ("junit-jupiter")
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
//    functionalTest (type: Test) {
//        description = "Runs the functional tests."
//        group = "verification"
//        testClassesDirs = sourceSets.functionalTest.output.classesDirs
//        classpath = sourceSets.functionalTest.runtimeClasspath
//        mustRunAfter test
//    }
//
//    check.dependsOn functionalTest
//
//            gradlePlugin {
//                testSourceSets sourceSets . functionalTest
//            }
//
//    task sourceJar (type: Jar) {
//    classifier = "sources"
//    from sourceSets . main . allJava
//}
//
//    publishing {
//        publications {
//            mavenJava(MavenPublication) {
//                from components . java
//
//                        artifact sourceJar
//
//                        pom.withXml {
//                            Node root = asNode ()
//                            root.appendNode("name", "Gradle Nar Plugin")
//                            root.appendNode("description", "Gradle plugin to support development of Apache NiFi nar archives")
//                            root.appendNode("url", "https://github.com/sponiro/gradle-nar-plugin")
//                            root.appendNode("inceptionYear", "2015")
//
//                            def scm = root . appendNode ("scm")
//                            scm.appendNode("url", "https://github.com/sponiro/gradle-nar-plugin")
//                            scm.appendNode("connection", "scm:https://github.com/sponiro/gradle-nar-plugin.git")
//                            scm.appendNode("developerConnection", "scm:git:https://github.com/sponiro/gradle-nar-plugin.git")
//
//                            def license = root . appendNode ("licenses").appendNode("license")
//                            license.appendNode("name", "The Apache Software License, Version 2.0")
//                            license.appendNode("url", "http://www.apache.org/licenses/LICENSE-2.0.txt")
//                            license.appendNode("distribution", "repo")
//
//                            def developers = root . appendNode ("developers")
//                            def rkuehne = developers . appendNode ("developer")
//                            rkuehne.appendNode("id", "rkuehne")
//                            rkuehne.appendNode("name", "Robert Kühne")
//                            rkuehne.appendNode("email", "sponiro@gmail.com")
//                        }
//            }
//        }
//    }
//
//    afterReleaseBuild.dependsOn bintrayUpload
//
//            bintray {
//                user = project.hasProperty("bintrayUser") ? project.getProperty("bintrayUser") : null
//                key = project.hasProperty("bintrayKey") ? project.getProperty("bintrayKey") : null
//                publications = ["mavenJava"]
//                pkg {
//                    repo = "gradle-plugins"
//                    name = "gradle-nar-plugin"
//                    licenses = ["Apache-2.0"]
//                    vcsUrl = "https://github.com/sponiro/gradle-nar-plugin"
//                }
//            }
}
