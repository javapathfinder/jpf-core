package java.util.jar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarFile extends ZipFile {
    public static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";

    public JarFile(String name) throws IOException {
        this(new File(name), true, OPEN_READ);
    }

    public JarFile(String name, boolean verify) throws IOException {
        this(new File(name), verify, OPEN_READ);
    }

    public JarFile(File file) throws IOException {
        this(file, true, OPEN_READ);
    }

    public JarFile(File file, boolean verify) throws IOException {
        this(file, verify, OPEN_READ);
    }

    public JarFile(File file, boolean verify, int mode) throws IOException {
        super(file, mode);
        // verify flag ignored for model; host JarFile would verify manifest
    }

    public JarFile(File file, boolean verify, int mode, Runtime.Version version) throws IOException {
        super(file, mode);
    }

    public Manifest getManifest() throws IOException {
        // Pure Java implementation using ZipFile's modeled entry access
        // Avoids SharedSecrets/JUZFA host dependency
        ZipEntry ze = super.getEntry(MANIFEST_NAME);
        if (ze == null) {
            return null;
        }
        InputStream is = super.getInputStream(ze);
        if (is == null) {
            return null;
        }
        try {
            return new Manifest(is);
        } finally {
            is.close();
        }
    }

    public JarEntry getJarEntry(String name) {
        ZipEntry ze = super.getEntry(name);
        if (ze == null) {
            return null;
        }
        // JarEntry has copy constructor ZipEntry
        return new JarEntry(ze);
    }

    @Override
    public ZipEntry getEntry(String name) {
        ZipEntry ze = super.getEntry(name);
        if (ze == null) {
            return null;
        }
        return new JarEntry(ze);
    }

    // JarFile.entries() should return Enumeration<JarEntry> but ZipFile.entries() returns Enumeration<? extends ZipEntry>
    // We add a covariant override that wraps super.entries()
    @SuppressWarnings("unchecked")
    public Enumeration<JarEntry> entriesJar() {
        Enumeration<? extends ZipEntry> superEntries = super.entries();
        return new Enumeration<JarEntry>() {
            @Override
            public boolean hasMoreElements() {
                return superEntries.hasMoreElements();
            }

            @Override
            public JarEntry nextElement() {
                ZipEntry ze = superEntries.nextElement();
                return new JarEntry(ze);
            }
        };
    }

    @Override
    public Enumeration<? extends ZipEntry> entries() {
        // Keep ZipFile signature but return JarEntry instances
        Enumeration<? extends ZipEntry> superEntries = super.entries();
        return new Enumeration<ZipEntry>() {
            @Override
            public boolean hasMoreElements() {
                return superEntries.hasMoreElements();
            }

            @Override
            public ZipEntry nextElement() {
                ZipEntry ze = superEntries.nextElement();
                return new JarEntry(ze);
            }
        };
    }
}
