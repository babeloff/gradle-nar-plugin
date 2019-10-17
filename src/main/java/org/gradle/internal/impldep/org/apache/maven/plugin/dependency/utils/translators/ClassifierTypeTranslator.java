//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils.translators;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;
import org.gradle.internal.impldep.org.apache.maven.artifact.factory.ArtifactFactory;
import org.gradle.internal.impldep.org.apache.maven.plugin.logging.Log;

public class ClassifierTypeTranslator implements ArtifactTranslator
{
    private String classifier;
    private String type;
    private ArtifactFactory factory;

    public ClassifierTypeTranslator(String theClassifier, String theType, ArtifactFactory theFactory) {
        this.classifier = theClassifier;
        this.type = theType;
        this.factory = theFactory;
    }

    public Set<Artifact> translate(Set<Artifact> artifacts, Log log) {
        log.debug("Translating Artifacts using Classifier: " + this.classifier + " and Type: " + this.type);
        Set<Artifact> results = new HashSet();
        Iterator i$ = artifacts.iterator();

        while(i$.hasNext()) {
            Artifact artifact = (Artifact)i$.next();
            String useType;
            if (StringUtils.isNotEmpty(this.type)) {
                useType = this.type;
            } else {
                useType = artifact.getType();
            }

            String useClassifier;
            if (StringUtils.isNotEmpty(this.classifier)) {
                useClassifier = this.classifier;
            } else {
                useClassifier = artifact.getClassifier();
            }

            Artifact newArtifact = this.factory.createArtifactWithClassifier(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), useType, useClassifier);
            newArtifact.setScope(artifact.getScope());
            results.add(newArtifact);
        }

        return results;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String theType) {
        this.type = theType;
    }

    public String getClassifier() {
        return this.classifier;
    }

    public void setClassifier(String theClassifier) {
        this.classifier = theClassifier;
    }
}
