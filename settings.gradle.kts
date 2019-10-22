// https://docs.gradle.org/current/javadoc/org/gradle/plugin/management/PluginManagementSpec.html
// https://docs.gradle.com/enterprise/gradle-plugin/#upgrading_to_gradle_6

pluginManagement {

    plugins {
        id("com.gradle.plugin-publish") version "0.10.1"
    }

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

