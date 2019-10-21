//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.components.io.attributes;

public class Java7Reflector {
    private static final boolean isJava7;

    public Java7Reflector() {
    }

    public static boolean isJava7() {
        return isJava7;
    }

    static {
        boolean isJava7x = true;

        try {
            Class.forName("java.nio.file.Files");
        } catch (Exception var2) {
            isJava7x = false;
        }

        isJava7 = isJava7x;
    }
}
