package org.babeloff.gradle.api.plugins;


import org.gradle.api.artifacts.DependencyConstraint;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.capabilities.Capability;
import org.gradle.api.internal.attributes.AttributeContainerInternal;
import org.gradle.api.internal.component.SoftwareComponentInternal;
import org.gradle.api.internal.component.UsageContext;
import org.gradle.api.plugins.internal.AbstractUsageContext;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

public class NifiApplication implements SoftwareComponentInternal {
    private final UsageContext nifiArchiveUsage;
    private final PublishArtifact warArtifact;
    private final String variantName;

    @Inject
    public NifiApplication(PublishArtifact warArtifact, String variantName, AttributeContainer attributes) {
        this.warArtifact = warArtifact;
        this.variantName = variantName;
        this.nifiArchiveUsage = new NifiApplication.NifiArchiveUsageContext(attributes);
    }

    @Override
    public String getName() {
        return "nifi";
    }

    @Override
    public Set<UsageContext> getUsages() {
        return Collections.singleton(nifiArchiveUsage);
    }

    private class NifiArchiveUsageContext extends AbstractUsageContext {
        public NifiArchiveUsageContext(AttributeContainer attributes) {
            super(((AttributeContainerInternal)attributes).asImmutable(), Collections.singleton(warArtifact));
        }

        @Override
        public String getName() {
            return variantName;
        }

        @Override
        public Set<ModuleDependency> getDependencies() {
            return Collections.emptySet();
        }

        @Override
        public Set<? extends DependencyConstraint> getDependencyConstraints() {
            return Collections.emptySet();
        }

        @Override
        public Set<? extends Capability> getCapabilities() {
            return Collections.emptySet();
        }

        @Override
        public Set<ExcludeRule> getGlobalExcludes() {
            return Collections.emptySet();
        }
    }
}
