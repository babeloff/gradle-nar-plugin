package org.babeloff.gradle.api.extensions

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class GreetingPluginExtension(objects: ObjectFactory)
{
    val message : Property<String> = objects.property(String::class.java)
    val recipient : Property<String> = objects.property(String::class.java)
}
