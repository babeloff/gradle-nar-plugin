package org.babeloff.gradle.plugin.nar;

import org.gradle.api.Project;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import java.io.File;

import static org.gradle.api.reflect.TypeOf.typeOf;

public class DefaultNarPluginConvention extends NarPluginConvention implements HasPublicType {
    private String nifiAppDirName;
    private final Project project;

    public DefaultNarPluginConvention(Project project) {
        this.project = project;
        nifiAppDirName = "src/main/nifi";
    }

    @Override
    public TypeOf<?> getPublicType() {
        return typeOf(NarPluginConvention.class);
    }

    @Override
    public File getNifiAppDir() {
        return project.file(nifiAppDirName);
    }

    @Override
    public String getNifiAppDirName() {
        return nifiAppDirName;
    }

    @Override
    public void setNifiAppDirName(String nifiAppDirName) {
        this.nifiAppDirName = nifiAppDirName;
    }

    @Override
    public Project getProject() {
        return project;
    }
}