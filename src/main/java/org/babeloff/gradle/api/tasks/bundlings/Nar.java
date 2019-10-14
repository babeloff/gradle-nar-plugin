package org.babeloff.gradle.api.tasks.bundlings;


import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.copy.DefaultCopySpec;
import org.gradle.api.java.archives.internal.DefaultManifest;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.util.ConfigureUtil;
import org.gradle.api.tasks.bundling.Jar;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Assembles a NAR archive.
 *
 * @author Fred Eisele
 */
public class Nar extends Jar {
    public static final String NAR_EXTENSION = "nar";

    private File nifiXml;
    private FileCollection classpath;
    private final DefaultCopySpec nifiInf;

    private static final String SERVICES_DIRECTORY = "META-INF/services/";
    private static final String DOCUMENTATION_WRITER_CLASS_NAME = "org.apache.nifi.documentation.xml.XmlDocumentationWriter";

    private static final String[] DEFAULT_EXCLUDES = new String[]{"**/package.html"};
    private static final String[] DEFAULT_INCLUDES = new String[]{"**/**"};

    private static final String BUILD_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";


    @Internal
    public List<Object> bundledDependencies;

    @Internal
    Configuration parentNarConfiguration;

    public Nar() {
        getArchiveExtension().set(NAR_EXTENSION);
        setMetadataCharset(DefaultManifest.DEFAULT_CONTENT_CHARSET);
        // Add these as separate specs, so they are not affected by the changes to the main spec

        nifiInf = (DefaultCopySpec) getRootSpec()
                .addChildBeforeSpec(getMainSpec())
                .into("WEB-INF");
        
        nifiInf.into("classes", spec -> spec.from((Callable<Iterable<File>>) () -> {
            FileCollection classpath = getClasspath();
            return classpath != null ? classpath.filter(File::isDirectory) : Collections.<File>emptyList();
        }));
        
        nifiInf.into("lib", spec -> spec.from((Callable<Iterable<File>>) () -> {
            FileCollection classpath = getClasspath();
            return classpath != null ? classpath.filter(File::isFile) : Collections.<File>emptyList();
        }));
        
        nifiInf.into("", spec -> {
            spec.from((Callable<File>) Nar.this::getNifiXml);
            spec.rename(name -> "nifi.xml");
        });

        bundledDependencies = new ArrayList<>();
//        configureBundledDependencies();
//        configureManifest();
//        configureParentNarManifestEntry();
    }

    @Internal
    public CopySpec getNifiInf() {
        return nifiInf.addChild();
    }

    /**
     * Adds some content to the {@code WEB-INF} directory for this NAR archive.
     *
     * <p>
     *     The given closure is executed to configure a {@link CopySpec}.
     *     The {@code CopySpec} is passed to the closure as its delegate.
     * </p>
     *
     * @param configureClosure The closure to execute
     * @return The newly created {@code CopySpec}.
     */
    public CopySpec nifiInf(Closure configureClosure) {
        return ConfigureUtil.configure(configureClosure, getNifiInf());
    }

    /**
     * Adds some content to the {@code WEB-INF} directory for this NAR archive.
     *
     * <p>The given action is executed to configure a {@link CopySpec}.</p>
     *
     * @param configureAction The action to execute
     * @return The newly created {@code CopySpec}.
     * @since 3.5
     */
    public CopySpec nifiInf(Action<? super CopySpec> configureAction) {
        CopySpec nifiInf = getNifiInf();
        configureAction.execute(nifiInf);
        return nifiInf;
    }

    /**
     * Returns the classpath to include in the NAR archive.
     * Any JAR or ZIP files in this classpath are included in the {@code WEB-INF/lib} directory.
     * Any directories in this classpath are included in the {@code WEB-INF/classes} directory.
     *
     * @return The classpath. Returns an empty collection when there is no classpath to include in the NAR.
     */
    @Nullable
    @Optional
    @Classpath
    public FileCollection getClasspath() {
        return classpath;
    }

    /**
     * Sets the classpath to include in the NAR archive.
     *
     * @param classpath The classpath. Must not be null.
     * @since 4.0
     */
    public void setClasspath(FileCollection classpath) {
        setClasspath((Object) classpath);
    }

    /**
     * Sets the classpath to include in the NAR archive.
     *
     * @param classpath The classpath. Must not be null.
     */
    public void setClasspath(Object classpath) {
        this.classpath = getProject().files(classpath);
    }

    /**
     * Adds files to the classpath to include in the NAR archive.
     *
     * @param classpath The files to add. These are evaluated as per {@link org.gradle.api.Project#files(Object...)}
     */
    public void classpath(Object... classpath) {
        FileCollection oldClasspath = getClasspath();
        this.classpath = getProject().files(oldClasspath != null ? oldClasspath : new ArrayList(), classpath);
    }

    /**
     * Returns the {@code nifi.xml} file to include in the NAR archive.
     * When {@code null}, no {@code nifi.xml} file is included in the NAR.
     *
     * @return The {@code nifi.xml} file.
     */
    @Nullable
    @Optional
    @PathSensitive(PathSensitivity.NONE)
    @InputFile
    public File getNifiXml() {
        return nifiXml;
    }

    /**
     * Sets the {@code nifi.xml} file to include in the NAR archive.
     * When {@code null}, no {@code nifi.xml} file is included in the NAR.
     *
     * @param nifiXml The {@code nifi.xml} file. Maybe null.
     */
    public void setNifiXml(@Nullable File nifiXml) {
        this.nifiXml = nifiXml;
    }


//    /**
//     * Specify which dependencies are to be included in the NAR.
//     * Bundled-dependencies
//     *
//     * The bundled-dependencies contains the actual jar files that will
//     * be used by the processor and accompanying controller services
//     * (if the NAR contains a controller service).
//     * These jar files will also be loaded in the ClassLoader that is dedicated to that processor.
//     *
//     * The specification for NAR can be found here:
//     * * http://nifi.apache.org/docs/nifi-docs/html/developer-guide.html#nars
//     * * http://maven-nar.github.io/
//     *
//     */
//    private void configureBundledDependencies() {
//        metaInf( () ->
//        {
//            into("META-INF/bundled-dependencies", () -> {
//                from( "META-INF", { () -> bundledDependencies })
//            })
//        });
//    }
//
//    private void configureManifest() {
//        project.afterEvaluate {
//            configure {
//                Attributes attr = manifest.attributes
//                attr.putIfAbsent(NarManifestEntry.NAR_GROUP.manifestKey, project.group);
//                attr.putIfAbsent(NarManifestEntry.NAR_ID.manifestKey, project.name);
//                attr.putIfAbsent(NarManifestEntry.NAR_VERSION.manifestKey, project.version);
//            }
//        }
//    }

//    private Task configureParentNarManifestEntry() {
//        project.afterEvaluate {
//            configure {
//                if (parentNarConfiguration == null) { return; }
//
//                if (parentNarConfiguration.size() > 1) {
//                    throw new RuntimeException("Only one parent nar dependency allowed in nar configuration but found ${parentNarConfiguration.size()} configurations");
//                }
//
//                if (parentNarConfiguration.size() == 1) {
//                    Dependency parentNarDependency = parentNarConfiguration.allDependencies.first();
//                    Attributes attr = manifest.attributes;
//                    attr.putIfAbsent(NarManifestEntry.NAR_DEPENDENCY_GROUP.manifestKey, parentNarDependency.group);
//                    attr.putIfAbsent(NarManifestEntry.NAR_DEPENDENCY_ID.manifestKey, parentNarDependency.name);
//                    attr.putIfAbsent(NarManifestEntry.NAR_DEPENDENCY_VERSION.manifestKey, parentNarDependency.version);
//                }
//            }
//        }
//        return null;
//    }
}


