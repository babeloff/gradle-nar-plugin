//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.plugin.dependency.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.gradle.internal.impldep.org.apache.maven.artifact.Artifact;
import org.gradle.internal.impldep.org.apache.maven.plugin.logging.Log;

public final class DependencyUtil {
    public DependencyUtil() {
    }

    public static String getFormattedFileName(Artifact artifact, boolean removeVersion) {
        return getFormattedFileName(artifact, removeVersion, false);
    }

    public static String getFormattedFileName(Artifact artifact, boolean removeVersion, boolean prependGroupId) {
        return getFormattedFileName(artifact, removeVersion, prependGroupId, false);
    }

    public static String getFormattedFileName(Artifact artifact, boolean removeVersion, boolean prependGroupId, boolean useBaseVersion) {
        return getFormattedFileName(artifact, removeVersion, prependGroupId, useBaseVersion, false);
    }

    public static String getFormattedFileName(Artifact artifact, boolean removeVersion, boolean prependGroupId, boolean useBaseVersion, boolean removeClassifier) {
        StringBuilder destFileName = new StringBuilder();
        if (prependGroupId) {
            destFileName.append(artifact.getGroupId()).append(".");
        }

        String versionString;
        if (!removeVersion) {
            if (useBaseVersion) {
                versionString = "-" + artifact.getBaseVersion();
            } else {
                versionString = "-" + artifact.getVersion();
            }
        } else {
            versionString = "";
        }

        String classifierString = "";
        if (!removeClassifier && StringUtils.isNotEmpty(artifact.getClassifier())) {
            classifierString = "-" + artifact.getClassifier();
        }

        destFileName.append(artifact.getArtifactId()).append(versionString);
        destFileName.append(classifierString).append(".");
        destFileName.append(artifact.getArtifactHandler().getExtension());
        return destFileName.toString();
    }

    public static File getFormattedOutputDirectory(boolean useSubdirsPerScope, boolean useSubdirsPerType, boolean useSubdirPerArtifact, boolean useRepositoryLayout, boolean removeVersion, File outputDirectory, Artifact artifact) {
        StringBuilder sb = new StringBuilder(128);
        if (useRepositoryLayout) {
            sb.append(artifact.getGroupId().replace('.', File.separatorChar)).append(File.separatorChar);
            sb.append(artifact.getArtifactId()).append(File.separatorChar);
            sb.append(artifact.getBaseVersion()).append(File.separatorChar);
        } else {
            if (useSubdirsPerScope) {
                sb.append(artifact.getScope()).append(File.separatorChar);
            }

            if (useSubdirsPerType) {
                sb.append(artifact.getType()).append("s").append(File.separatorChar);
            }

            if (useSubdirPerArtifact) {
                String artifactString = getDependencyId(artifact, removeVersion);
                sb.append(artifactString).append(File.separatorChar);
            }
        }

        return new File(outputDirectory, sb.toString());
    }

    private static String getDependencyId(Artifact artifact, boolean removeVersion) {
        StringBuilder sb = new StringBuilder();
        sb.append(artifact.getArtifactId());
        if (!removeVersion) {
            sb.append("-");
            sb.append(artifact.getVersion());
        }

        if (StringUtils.isNotEmpty(artifact.getClassifier())) {
            sb.append("-");
            sb.append(artifact.getClassifier());
        }

        if (!StringUtils.equals(artifact.getClassifier(), artifact.getType())) {
            sb.append("-");
            sb.append(artifact.getType());
        }

        return sb.toString();
    }

    public static synchronized void write(String string, File file, boolean append, Log log) throws IOException {
        file.getParentFile().mkdirs();
        FileWriter writer = null;

        try {
            writer = new FileWriter(file, append);
            writer.write(string);
        } finally {
            IOUtil.close(writer);
        }

    }

    public static synchronized void log(String string, Log log) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(string));

        String line;
        while((line = reader.readLine()) != null) {
            log.info(line);
        }

        reader.close();
    }

    public static String[] tokenizer(String str) {
        return StringUtils.split(cleanToBeTokenizedString(str), ",");
    }

    public static String cleanToBeTokenizedString(String str) {
        String ret = "";
        if (!StringUtils.isEmpty(str)) {
            ret = str.trim().replaceAll("[\\s]*,[\\s]*", ",");
        }

        return ret;
    }
}
