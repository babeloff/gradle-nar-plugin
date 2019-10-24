package org.babeloff.gradle.api.plugins

import org.babeloff.gradle.api.extensions.GreetingPluginExtension
import org.babeloff.gradle.api.tasks.GreetingTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class GreetingPlugin : Plugin<Project>
{
    companion object {
        val EVAL_GROUP = "examplePlugin"; // BasePlugin.BUILD_GROUP;
        val EXTENSION_NAME = "example";
    }

    override fun apply(project: Project) {

        val extension = project
                .convention
                .create("greeting", GreetingPluginExtension::class.java)

        val tasks = project.tasks
        val miscDir = File("misc/test_resources")


        tasks.register("goodbye", DefaultTask::class.java) {
            group = EVAL_GROUP

            doFirst {
                System.out.printf("primero-bye %s, %s!\n", extension.message, extension.recipient)
            }
            doLast {
                System.out.printf("ultimo-bye %s, %s!\n", extension.message, extension.recipient)
            }
        }

        tasks.register("hello", GreetingTask::class.java)  {
            group = EVAL_GROUP
            message.set(extension.message)
            recipient.set(extension.recipient)

            inFile.set(java.io.File(miscDir, "hello_infile.foo"))

            doFirst {
                java.lang.System.out.println("first");
            }
            doLast {
                java.lang.System.out.println("last");
            }
        }
    }
}

