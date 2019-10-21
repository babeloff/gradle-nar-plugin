//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.archiver;

public class ManifestConfiguration {
    public static final String CLASSPATH_LAYOUT_TYPE_SIMPLE = "simple";
    public static final String CLASSPATH_LAYOUT_TYPE_REPOSITORY = "repository";
    public static final String CLASSPATH_LAYOUT_TYPE_CUSTOM = "custom";
    private String mainClass;
    private String packageName;
    private boolean addClasspath;
    private boolean addExtensions;
    private String classpathPrefix = "";
    private boolean addDefaultSpecificationEntries;
    private boolean addDefaultImplementationEntries;
    /** @deprecated */
    private boolean classpathMavenRepositoryLayout = false;
    private String classpathLayoutType = "simple";
    private String customClasspathLayout;
    private boolean useUniqueVersions = true;

    public ManifestConfiguration() {
    }

    public String getMainClass() {
        return this.mainClass;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public boolean isAddClasspath() {
        return this.addClasspath;
    }

    public boolean isAddDefaultImplementationEntries() {
        return this.addDefaultImplementationEntries;
    }

    public boolean isAddDefaultSpecificationEntries() {
        return this.addDefaultSpecificationEntries;
    }

    public boolean isAddExtensions() {
        return this.addExtensions;
    }

    /** @deprecated */
    public boolean isClasspathMavenRepositoryLayout() {
        return this.classpathMavenRepositoryLayout;
    }

    public void setAddClasspath(boolean addClasspath) {
        this.addClasspath = addClasspath;
    }

    public void setAddDefaultImplementationEntries(boolean addDefaultImplementationEntries) {
        this.addDefaultImplementationEntries = addDefaultImplementationEntries;
    }

    public void setAddDefaultSpecificationEntries(boolean addDefaultSpecificationEntries) {
        this.addDefaultSpecificationEntries = addDefaultSpecificationEntries;
    }

    public void setAddExtensions(boolean addExtensions) {
        this.addExtensions = addExtensions;
    }

    /** @deprecated */
    public void setClasspathMavenRepositoryLayout(boolean classpathMavenRepositoryLayout) {
        this.classpathMavenRepositoryLayout = classpathMavenRepositoryLayout;
    }

    public void setClasspathPrefix(String classpathPrefix) {
        this.classpathPrefix = classpathPrefix;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClasspathPrefix() {
        String cpp = this.classpathPrefix.replaceAll("\\\\", "/");
        if (cpp.length() != 0 && !cpp.endsWith("/")) {
            cpp = cpp + "/";
        }

        return cpp;
    }

    public String getClasspathLayoutType() {
        return "simple".equals(this.classpathLayoutType) && this.classpathMavenRepositoryLayout ? "repository" : this.classpathLayoutType;
    }

    public void setClasspathLayoutType(String classpathLayoutType) {
        this.classpathLayoutType = classpathLayoutType;
    }

    public String getCustomClasspathLayout() {
        return this.customClasspathLayout;
    }

    public void setCustomClasspathLayout(String customClasspathLayout) {
        this.customClasspathLayout = customClasspathLayout;
    }

    public boolean isUseUniqueVersions() {
        return this.useUniqueVersions;
    }

    public void setUseUniqueVersions(boolean useUniqueVersions) {
        this.useUniqueVersions = useUniqueVersions;
    }
}
