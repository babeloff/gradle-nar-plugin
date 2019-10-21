//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.gradle.internal.impldep.org.codehaus.plexus.archiver.jar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import org.gradle.internal.impldep.org.codehaus.plexus.archiver.ArchiverException;
import org.gradle.api.java.archives.ManifestException;
import org.gradle.internal.impldep.org.codehaus.plexus.util.IOUtil;

public class Manifest extends java.util.jar.Manifest implements Iterable<String> {
    private static final String ATTRIBUTE_NAME = "Name";
    private static final String ATTRIBUTE_FROM = "From";
    private static final String DEFAULT_MANIFEST_VERSION = "1.0";
    private static final int MAX_LINE_LENGTH = 72;
    private static final int MAX_SECTION_LENGTH = 70;
    static final String EOL = "\r\n";
    private Manifest.Section mainSection = new Manifest.Section();

    private static Collection<String> getKeys(Attributes attributes) {
        Collection<String> result = new ArrayList();
        Iterator i$ = attributes.keySet().iterator();

        while(i$.hasNext()) {
            Object objectObjectEntry = i$.next();
            result.add(objectObjectEntry.toString());
        }

        return result;
    }

    public Iterator<String> iterator() {
        return this.getEntries().keySet().iterator();
    }

    public static Manifest getDefaultManifest() throws ArchiverException {
        try {
            String defManifest = "/org/gradle/internal/impldep/org/codehaus/plexus/archiver/jar/defaultManifest.mf";
            InputStream in = Manifest.class.getResourceAsStream(defManifest);
            if (in == null) {
                throw new ArchiverException("Could not find default manifest: " + defManifest);
            } else {
                Manifest var3;
                try {
                    Manifest defaultManifest = new Manifest(new InputStreamReader(in, "UTF-8"));
                    defaultManifest.getMainAttributes().putValue("Created-By", System.getProperty("java.vm.version") + " (" + System.getProperty("java.vm.vendor") + ")");
                    var3 = defaultManifest;
                    return var3;
                } catch (UnsupportedEncodingException var9) {
                    var3 = new Manifest(new InputStreamReader(in));
                } finally {
                    IOUtil.close(in);
                }

                return var3;
            }
        } catch (ManifestException var11) {
            throw new ArchiverException("Default manifest is invalid !!", var11);
        } catch (IOException var12) {
            throw new ArchiverException("Unable to read default manifest", var12);
        }
    }

    public Manifest() {
        this.setManifestVersion();
    }

    private void setManifestVersion() {
        this.getMainAttributes().put(Name.MANIFEST_VERSION, "1.0");
    }

    public Manifest(Reader r) throws ManifestException, IOException {
        super(getInputStream(r));
        this.setManifestVersion();
    }

    public Manifest(InputStream is) throws IOException {
        super(is);
        this.setManifestVersion();
    }

    public void addConfiguredSection(Manifest.Section section) throws ManifestException {
        String sectionName = section.getName();
        if (sectionName == null) {
            throw new ManifestException("Sections must have a name");
        } else {
            Attributes attributes = this.getOrCreateAttributes(sectionName);
            Iterator i$ = section.attributes.keySet().iterator();

            while(i$.hasNext()) {
                String s = (String)i$.next();
                Manifest.Attribute attribute = section.getAttribute(s);
                attributes.putValue(attribute.getName(), attribute.getValue());
            }

        }
    }

    private Attributes getOrCreateAttributes(String name) {
        Attributes attributes = this.getAttributes(name);
        if (attributes == null) {
            attributes = new Attributes();
            this.getEntries().put(name, attributes);
        }

        return attributes;
    }

    public void addConfiguredAttribute(Manifest.Attribute attribute) throws ManifestException {
        remap(this.getMainAttributes(), attribute);
    }

    public void write(PrintWriter writer) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        super.write(byteArrayOutputStream);
        byte[] arr$ = byteArrayOutputStream.toByteArray();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            byte b = arr$[i$];
            writer.write((char)b);
        }

    }

    public String toString() {
        StringWriter sw = new StringWriter();

        try {
            this.write(new PrintWriter(sw));
        } catch (IOException var3) {
            return null;
        }

        return sw.toString();
    }

    Enumeration<String> getWarnings() {
        Vector<String> warnings = new Vector();
        Enumeration warnEnum = this.mainSection.getWarnings();

        while(warnEnum.hasMoreElements()) {
            warnings.addElement((String) warnEnum.nextElement());
        }

        return warnings.elements();
    }

    public String getManifestVersion() {
        return "1.0";
    }

    public Manifest.ExistingSection getMainSection() {
        return new Manifest.ExistingSection(this.getMainAttributes(), (String)null);
    }

    public Manifest.ExistingSection getSection(String name) {
        Attributes attributes = this.getAttributes(name);
        return attributes != null ? new Manifest.ExistingSection(attributes, name) : null;
    }

    private static InputStream getInputStream(Reader r) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int read;
        while((read = r.read()) != -1) {
            byteArrayOutputStream.write(read);
        }

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    public static String remap(Attributes backingAttributes, Manifest.Attribute attribute) throws ManifestException {
        if (attribute.getKey() != null && attribute.getValue() != null) {
            String attributeKey = attribute.getKey();
            if (attributeKey.equalsIgnoreCase("Class-Path")) {
                String classpathAttribute = backingAttributes.getValue(attributeKey);
                if (classpathAttribute == null) {
                    classpathAttribute = attribute.getValue();
                } else {
                    classpathAttribute = classpathAttribute + " " + attribute.getValue();
                }

                backingAttributes.putValue("Class-Path", classpathAttribute);
            } else {
                backingAttributes.putValue(attribute.getName(), attribute.getValue());
                if (attribute.getKey().equalsIgnoreCase("Name")) {
                    return attribute.getValue();
                }
            }

            return null;
        } else {
            throw new ManifestException("Attributes must have name and value");
        }
    }

    public class ExistingSection implements Iterable<String> {
        private final Attributes backingAttributes;
        private final String sectionName;

        public ExistingSection(Attributes backingAttributes, String sectionName) {
            this.backingAttributes = backingAttributes;
            this.sectionName = sectionName;
        }

        public Iterator<String> iterator() {
            return Manifest.getKeys(this.backingAttributes).iterator();
        }

        public Manifest.ExistingAttribute getAttribute(String attributeName) {
            Name name = new Name(attributeName);
            return this.backingAttributes.containsKey(name) ? Manifest.this.new ExistingAttribute(this.backingAttributes, attributeName) : null;
        }

        public String getName() {
            return this.sectionName;
        }

        public String getAttributeValue(String attributeName) {
            return this.backingAttributes.getValue(attributeName);
        }

        public void removeAttribute(String attributeName) {
            this.backingAttributes.remove(new Name(attributeName));
        }

        public void addConfiguredAttribute(Manifest.Attribute attribute) throws ManifestException {
            this.backingAttributes.putValue(attribute.getName(), attribute.getValue());
        }

        public String addAttributeAndCheck(Manifest.Attribute attribute) throws ManifestException {
            return Manifest.remap(this.backingAttributes, attribute);
        }

        public int hashCode() {
            return this.backingAttributes.hashCode();
        }

        public boolean equals(Object rhs) {
            return rhs instanceof Manifest.ExistingSection && this.backingAttributes.equals(((Manifest.ExistingSection)rhs).backingAttributes);
        }
    }

    public static class Section implements Iterable<String> {
        private Vector<String> warnings = new Vector();
        private String name = null;
        private Hashtable<String, Manifest.Attribute> attributes = new Hashtable();
        private Vector<String> attributeIndex = new Vector();

        public Section() {
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public Iterator<String> iterator() {
            return this.attributes.keySet().iterator();
        }

        public Manifest.Attribute getAttribute(String attributeName) {
            return (Manifest.Attribute)this.attributes.get(attributeName.toLowerCase());
        }

        public void addConfiguredAttribute(Manifest.Attribute attribute) throws ManifestException {
            String check = this.addAttributeAndCheck(attribute);
            if (check != null) {
                throw new ManifestException("Specify the section name using the \"name\" attribute of the <section> element rather than using a \"Name\" manifest attribute");
            }
        }

        public String addAttributeAndCheck(Manifest.Attribute attribute) throws ManifestException {
            if (attribute.getName() != null && attribute.getValue() != null) {
                if (attribute.getKey().equalsIgnoreCase("Name")) {
                    this.warnings.addElement("\"Name\" attributes should not occur in the main section and must be the first element in all other sections: \"" + attribute.getName() + ": " + attribute.getValue() + "\"");
                    return attribute.getValue();
                } else {
                    if (attribute.getKey().startsWith(Manifest.Attribute.getKey("From"))) {
                        this.warnings.addElement("Manifest attributes should not start with \"From\" in \"" + attribute.getName() + ": " + attribute.getValue() + "\"");
                    } else {
                        String attributeKey = attribute.getKey();
                        if (attributeKey.equalsIgnoreCase("Class-Path")) {
                            Manifest.Attribute classpathAttribute = (Manifest.Attribute)this.attributes.get(attributeKey);
                            if (classpathAttribute == null) {
                                this.storeAttribute(attribute);
                            } else {
                                this.warnings.addElement("Multiple Class-Path attributes are supported but violate the Jar specification and may not be correctly processed in all environments");
                                Iterator i$ = attribute.iterator();

                                while(i$.hasNext()) {
                                    String value = (String)i$.next();
                                    classpathAttribute.addValue(value);
                                }
                            }
                        } else {
                            if (this.attributes.containsKey(attributeKey)) {
                                throw new ManifestException("The attribute \"" + attribute.getName() + "\" may not occur more " + "than once in the same section");
                            }

                            this.storeAttribute(attribute);
                        }
                    }

                    return null;
                }
            } else {
                throw new ManifestException("Attributes must have name and value");
            }
        }

        protected void storeAttribute(Manifest.Attribute attribute) {
            if (attribute != null) {
                String attributeKey = attribute.getKey();
                this.attributes.put(attributeKey, attribute);
                if (!this.attributeIndex.contains(attributeKey)) {
                    this.attributeIndex.addElement(attributeKey);
                }

            }
        }

        public Enumeration<String> getWarnings() {
            return this.warnings.elements();
        }

        public int hashCode() {
            int hashCode = 0;
            if (this.name != null) {
                hashCode += this.name.hashCode();
            }

            hashCode += this.attributes.hashCode();
            return hashCode;
        }

        public boolean equals(Object rhs) {
            if (rhs != null && rhs.getClass() == this.getClass()) {
                if (rhs == this) {
                    return true;
                } else {
                    Manifest.Section rhsSection = (Manifest.Section)rhs;
                    return rhsSection.attributes != null && this.attributes.equals(rhsSection.attributes);
                }
            } else {
                return false;
            }
        }
    }

    public class ExistingAttribute extends Manifest.Attribute implements Iterable<String> {
        private final Attributes attributes;

        public ExistingAttribute(Attributes attributes, String name) {
            this.attributes = attributes;
            this.name = name;
        }

        public Iterator<String> iterator() {
            return Manifest.getKeys(this.attributes).iterator();
        }

        public void setName(String name) {
            throw new UnsupportedOperationException("Cant do this");
        }

        public String getKey() {
            return this.name;
        }

        public void setValue(String value) {
            this.attributes.putValue(this.name, value);
        }

        public String getValue() {
            return this.attributes.getValue(this.name);
        }

        public void addValue(String value) {
            String value1 = this.getValue();
            value1 = value1 != null ? " " + value : value;
            this.setValue(value1);
        }

        void write(PrintWriter writer) throws IOException {
            throw new UnsupportedOperationException("Cant do this");
        }
    }

    public static class Attribute extends Manifest.BaseAttribute implements Iterable<String> {
        private Vector<String> values = new Vector();
        private int currentIndex = 0;

        public Attribute() {
        }

        public Attribute(String name, String value) {
            this.name = name;
            this.setValue(value);
        }

        public Iterator<String> iterator() {
            return this.values.iterator();
        }

        public int hashCode() {
            int hashCode = super.hashCode();
            hashCode += this.values.hashCode();
            return hashCode;
        }

        public boolean equals(Object rhs) {
            if (super.equals(rhs)) {
                return false;
            } else if (rhs != null && rhs.getClass() == this.getClass()) {
                if (rhs == this) {
                    return true;
                } else {
                    Manifest.Attribute rhsAttribute = (Manifest.Attribute)rhs;
                    String lhsKey = this.getKey();
                    String rhsKey = rhsAttribute.getKey();
                    if ((lhsKey != null || rhsKey == null) && (lhsKey == null || rhsKey != null) && lhsKey.equals(rhsKey)) {
                        return rhsAttribute.values != null && this.values.equals(rhsAttribute.values);
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getKey() {
            return getKey(this.name);
        }

        private static String getKey(String name) {
            return name == null ? null : name.toLowerCase(Locale.ENGLISH);
        }

        public void setValue(String value) {
            if (this.currentIndex >= this.values.size()) {
                this.values.addElement(value);
                this.currentIndex = this.values.size() - 1;
            } else {
                this.values.setElementAt(value, this.currentIndex);
            }

        }

        public String getValue() {
            if (this.values.size() == 0) {
                return null;
            } else {
                String fullValue = "";

                String value;
                for(Iterator i$ = this.values.iterator(); i$.hasNext(); fullValue = fullValue + value + " ") {
                    value = (String)i$.next();
                }

                return fullValue.trim();
            }
        }

        public void addValue(String value) {
            ++this.currentIndex;
            this.setValue(value);
        }

        void write(PrintWriter writer) throws IOException {
            StringWriter sWriter = new StringWriter();
            PrintWriter bufferWriter = new PrintWriter(sWriter);
            Iterator i$ = this.values.iterator();

            while(i$.hasNext()) {
                String value = (String)i$.next();
                this.writeValue(bufferWriter, value);
            }

            byte[] convertedToUtf8 = sWriter.toString().getBytes("UTF-8");
            writer.print(new String(convertedToUtf8, "UTF-8"));
        }

        private void writeValue(PrintWriter writer, String value) throws IOException {
            String nameValue = this.name + ": " + value;
            StringTokenizer tokenizer = new StringTokenizer(nameValue, "\n\r");

            for(String prefix = ""; tokenizer.hasMoreTokens(); prefix = " ") {
                this.writeLine(writer, prefix + tokenizer.nextToken());
            }

        }

        private void writeLine(PrintWriter writer, String line) throws IOException {
            while(line.getBytes().length > 72) {
                int breakIndex = 70;

                String section;
                for(section = line.substring(0, breakIndex); section.getBytes().length > 70 && breakIndex > 0; section = line.substring(0, breakIndex)) {
                    --breakIndex;
                }

                if (breakIndex == 0) {
                    throw new IOException("Unable to write manifest line " + line);
                }

                writer.print(section + "\r\n");
                line = " " + line.substring(breakIndex);
            }

            writer.print(line + "\r\n");
        }
    }

    public static class BaseAttribute {
        protected String name = null;

        public BaseAttribute() {
        }

        public String getName() {
            return this.name;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (!(o instanceof Manifest.BaseAttribute)) {
                return false;
            } else {
                boolean var10000;
                label34: {
                    Manifest.BaseAttribute that = (Manifest.BaseAttribute)o;
                    if (this.name != null) {
                        if (this.name.equals(that.name)) {
                            break label34;
                        }
                    } else if (that.name == null) {
                        break label34;
                    }

                    var10000 = false;
                    return var10000;
                }

                var10000 = true;
                return var10000;
            }
        }

        public int hashCode() {
            return this.name != null ? this.name.hashCode() : 0;
        }
    }
}
