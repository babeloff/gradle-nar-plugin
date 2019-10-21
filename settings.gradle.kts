
// https://docs.gradle.org/current/javadoc/org/gradle/plugin/management/PluginManagementSpec.html

pluginManagement {

    resolutionStrategy {

    }

    repositories {
        jcenter()
        gradlePluginPortal()
    }
}

rootProject.name = "nar-gradle-plugin"

plugins {
    id("com.gradle.enterprise").version("3.0")
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"

        if (!System.getenv("CI").isNullOrEmpty()) {
            publishAlways()
            tag("CI")
        }
    }
}

