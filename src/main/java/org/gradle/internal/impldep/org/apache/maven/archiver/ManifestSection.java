//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.apache.maven.archiver;

import java.util.HashMap;
import java.util.Map;

public class ManifestSection {
    private String name = null;
    private Map<String, String> manifestEntries = new HashMap();

    public ManifestSection() {
    }

    public void addManifestEntry(String key, String value) {
        this.manifestEntries.put(key, value);
    }

    public Map<String, String> getManifestEntries() {
        return this.manifestEntries;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addManifestEntries(Map<String, String> map) {
        this.manifestEntries.putAll(map);
    }

    public boolean isManifestEntriesEmpty() {
        return this.manifestEntries.isEmpty();
    }
}
