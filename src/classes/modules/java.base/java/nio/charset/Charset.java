package java.nio.charset;

import java.util.HashMap;
import java.util.Map;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class Charset {
    private static final String DEFAULT_CHARSET_NAME = "UTF-8";
    private static final Map<String, Charset> charsetMap = new HashMap<>();
    private final String name;

    // Static initialization to ensure default charset always exists
    static {
        Charset defaultCharset = new Charset(DEFAULT_CHARSET_NAME);
        charsetMap.put(DEFAULT_CHARSET_NAME.toLowerCase(), defaultCharset);
        charsetMap.put("default", defaultCharset);
    }

    protected Charset(String canonicalName) {
        if (canonicalName == null) {
            throw new IllegalArgumentException("Charset name cannot be null");
        }
        this.name = canonicalName;
    }

    public static Charset defaultCharset() {
        return charsetMap.get(DEFAULT_CHARSET_NAME.toLowerCase());
    }

    public static Charset forName(String charsetName) {
        if (charsetName == null) {
            throw new IllegalArgumentException("Null charset name");
        }

        String normalizedName = charsetName.toLowerCase();

        // Always return the default charset if the requested one isn't found
        // This simplifies the model while preventing null charset issues
        return charsetMap.getOrDefault(normalizedName, defaultCharset());
    }

    public final String name() {
        return name;
    }

    // Simple encode/decode stubs that don't actually do encoding
    // but prevent null pointer issues
    public CharBuffer decode(ByteBuffer bb) {
        if (bb == null) {
            throw new IllegalArgumentException("ByteBuffer cannot be null");
        }
        // Simplified implementation that just copies bytes to chars
        char[] chars = new char[bb.remaining()];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (bb.get() & 0xFF);
        }
        return CharBuffer.wrap(new String(chars));
    }

    public ByteBuffer encode(CharBuffer cb) {
        if (cb == null) {
            throw new IllegalArgumentException("CharBuffer cannot be null");
        }
        // Simplified implementation that just copies chars to bytes
        byte[] bytes = new byte[cb.remaining()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) cb.get();
        }
        return ByteBuffer.wrap(bytes);
    }
}