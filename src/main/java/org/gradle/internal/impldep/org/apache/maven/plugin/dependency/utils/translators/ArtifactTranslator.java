//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.translators;

import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;
import org.gradle.internal.impldep.org.apache.maven.plugin.logging.Log;

import java.util.Set;

public interface ArtifactTranslator {
    Set<Artifact> translate(Set<Artifact> var1, Log var2);
}
