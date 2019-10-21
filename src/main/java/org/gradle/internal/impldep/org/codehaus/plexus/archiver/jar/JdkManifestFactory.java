//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.jar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.ArchiverException;
import org.gradle.internal.impldep.org.codehaus.plexus.util.IOUtil;

public class JdkManifestFactory {
    JdkManifestFactory() {
    }

    public static Manifest getDefaultManifest() throws ArchiverException {
        try {
            String defManifest = "/org/gradle/internal/impldep/org/codehaus/plexus/archiver/jar/defaultManifest.mf";
            InputStream in = JdkManifestFactory.class.getResourceAsStream(defManifest);
            if (in == null) {
                throw new ArchiverException("Could not find default manifest: " + defManifest);
            } else {
                Manifest var3;
                try {
                    Manifest defaultManifest = new Manifest(in);
                    defaultManifest.getMainAttributes().putValue("Created-By", System.getProperty("java.vm.version") + " (" + System.getProperty("java.vm.vendor") + ")");
                    var3 = defaultManifest;
                } finally {
                    IOUtil.close(in);
                }

                return var3;
            }
        } catch (IOException var8) {
            throw new ArchiverException("Unable to read default manifest", var8);
        }
    }

    public static void merge(Manifest target, Manifest other, boolean overwriteMain) {
        if (other != null) {
            Attributes mainAttributes = target.getMainAttributes();
            if (overwriteMain) {
                mainAttributes.clear();
                mainAttributes.putAll(other.getMainAttributes());
            } else {
                mergeAttributes(mainAttributes, other.getMainAttributes());
            }

            Iterator i$ = other.getEntries().entrySet().iterator();

            while(i$.hasNext()) {
                Entry<String, Attributes> o = (Entry)i$.next();
                Attributes ourSection = target.getAttributes((String)o.getKey());
                Attributes otherSection = (Attributes)o.getValue();
                if (ourSection == null) {
                    if (otherSection != null) {
                        target.getEntries().put(o.getKey(), (Attributes)otherSection.clone());
                    }
                } else {
                    mergeAttributes(ourSection, otherSection);
                }
            }
        }

    }

    public static void mergeAttributes(Attributes target, Attributes section) {
        Iterator i$ = section.keySet().iterator();

        while(i$.hasNext()) {
            Object o = i$.next();
            Name key = (Name)o;
            Object value = section.get(o);
            target.put(key, value);
        }

    }
}
