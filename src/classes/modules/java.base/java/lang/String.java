/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package java.lang;

import java.io.ObjectStreamField;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MJI adapter for java.lang.String, based on jdk 1.7.0_07 source.
 * Adapted by frank.
 * 
 * We have to model java.lang.String since it changed its implementation between
 * Java 1.6 and Java 1.7, and JPF is initializing string objects internally without
 * doing roundtrips (for performance reasons), i.e. we need to be able to rely
 * on fields of the String implementation.
 * 
 * The silver lining is that most methods are now native and don't appear in
 * traces anymore. Spending a lot of JPF cycles (instruction.execute()) on
 * String is just not worth it in terms of potential properties.
 */

public final class String
implements java.io.Serializable, Comparable<String>, CharSequence {

	/** The value is used for character storage. */
	private final byte value[];

	/**
	 * The identifier of the encoding used to encode the bytes in
	 * The supported values in this implementation are:
	 * LATIN1
	 * UTF16
	 */
	private final byte coder;

	static final byte LATIN1 = 0;
	static final byte UTF16 = 1;

	/** Cache the hash code for the string */
	private int hash; // Default to 0

	private static final long serialVersionUID = -6849794470754667710L;

	static final boolean COMPACT_STRINGS;
	static {
		COMPACT_STRINGS = true;
	}

	private static final ObjectStreamField[] serialPersistentFields =
			new ObjectStreamField[0];

	public String() {
		this.value = "".value;
		this.coder = "".coder;
	}

	public String(String original) {
		this.value = original.value;
		this.coder = original.coder;
		this.hash = original.hash;
	}

	public String(char value[]) {
		this(value, 0, value.length, null);
	}

	public String(char value[], boolean share) {
		this(value, 0, value.length, null);
	}
	
	public String(char value[], int offset, int count) {
		String proxy=init(value,offset,count);
		this.value=proxy.value;
		this.coder=proxy.coder;
	}

	private native String init(char[] value, int offset, int count);

	public String(int[] codePoints, int offset, int count) {
		String proxy=init(codePoints,offset,count);
		this.value=proxy.value;
		this.coder=proxy.coder;
	}

	private native String init(int[] codePoints, int offset, int count);

	@Deprecated
	public String(byte ascii[], int hibyte, int offset, int count) {
		String proxy=init(ascii,hibyte,offset,count);
		this.value=proxy.value;
		this.coder=proxy.coder;
	}

	private native String init(byte ascii[], int hibyte, int offset, int count);

	@Deprecated
	public String(byte ascii[], int hibyte) {
		this(ascii, hibyte, 0, ascii.length);
	}



	public String(byte bytes[], int offset, int length, String charsetName){
		String proxy=init(bytes,offset,length,charsetName);
		this.value=proxy.value;
		this.coder=proxy.coder;
	}

	private native String init(byte bytes[], int offset, int length, String charsetName);


	public String(byte x[], int offset, int length, Charset cset) {
		// no Charset model
		if (cset == null){
			throw new NullPointerException("cset");
		}
		if (length < 0){
			throw new StringIndexOutOfBoundsException(length);
		}
		if (offset < 0){
			throw new StringIndexOutOfBoundsException(offset);
		}
		if (offset > x.length - length){
			throw new StringIndexOutOfBoundsException(offset + length);
		}

		StringCoding.Result result =  StringCoding.decode(cset, x, offset, length);
		this.value = result.value;
		this.coder = result.coder;
	}

	public String(byte bytes[], String charsetName)
			throws UnsupportedEncodingException {
		this(bytes, 0, bytes.length, charsetName);
	}

	public String(byte bytes[], Charset charset) {
		this(bytes, 0, bytes.length, charset);
	}

	public String(byte bytes[], int offset, int length) {
		String proxy=init(bytes,offset,length);
		this.value=proxy.value;
		this.coder=proxy.coder;
		this.hash=proxy.hash;
	}


	private native String init(byte bytes[], int offset, int length);

	public String(byte bytes[]) {
		this(bytes, 0, bytes.length);
	}


	public String(StringBuffer x) {
		this(x.toString());
	}


	public String(StringBuilder x) {
		this.value = Arrays.copyOf(x.getValue(), x.length());
		this.coder = x.coder;
	}

	String(byte[] value, byte coder) {
		this.value = value;
		this.coder = coder;
	}

	String(char[] value, int off, int len, Void sig) {
		if (len == 0) {
			this.value = "".value;
			this.coder = "".coder;
			return;
		}
		if (COMPACT_STRINGS) {
			byte[] val = StringUTF16.compress(value, off, len);
			if (val != null) {
				this.value = val;
				this.coder = LATIN1;
				return;
			}
		}
		this.coder = UTF16;
		this.value = StringUTF16.toBytes(value, off, len);
	}

	@Deprecated
	String(int offset, int count, char[] value) {
		this(value, offset, count);
	}

	@Override
	public int length() {
		return value.length >> coder();
	}

	public boolean isEmpty() {
		return value.length == 0;
	}
	@Override
	public char charAt(int index) {
		if ((index < 0) || (index >= value.length)) {
			throw new StringIndexOutOfBoundsException(index);
		}
		return this.isLatin1() ? StringLatin1.charAt(this.value, index) : StringUTF16.charAt(this.value, index);
	}

	native public int codePointAt(int index);
	native public int codePointBefore(int index);
	native public int codePointCount(int beginIndex, int endIndex);
	native public int offsetByCodePoints(int index, int codePointOffset);
	native public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin);
	native void getChars(char dst[], int dstBegin);

	@Deprecated
	native public void getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin);
	native public byte[] getBytes(String charsetName)
			throws UnsupportedEncodingException;

	public byte[] getBytes(Charset x){
		if (x == null) throw new NullPointerException();
		return StringCoding.encode(x, coder(), value);
	}

	/**
	 * If two coders are different and target is big enough,
	 * invoker guarantees that the target is in UTF16
	 */
	void getBytes(byte dst[], int dstBegin, byte coder) {
		if (coder() == coder) {
			System.arraycopy(value, 0, dst, dstBegin << coder, value.length);
		} else {
			assert this.coder == LATIN1 && coder == UTF16;
			StringLatin1.inflate(value, 0, dst, dstBegin, value.length);
		}
	}

	native public byte[] getBytes();
	@Override
	native public boolean equals(Object anObject);
	public boolean contentEquals(StringBuffer stringBuffer){
		// No StringBuffer model.
		synchronized (stringBuffer) {
			return contentEquals((CharSequence) stringBuffer);
		}
	}

	private boolean nonSyncContentEquals(AbstractStringBuilder abstractStringBuilder) {
		int len = length();
		if (len != abstractStringBuilder.length()) {
			return false;
		}
		byte v1[] = value;
		byte v2[] = abstractStringBuilder.getValue();
		if (coder() == abstractStringBuilder.getCoder()) {
			int n = v1.length;
			for (int i = 0; i < n; i++) {
				if (v1[i] != v2[i]) {
					return false;
				}
			}
		} else {
			return isLatin1() && StringUTF16.contentEquals(v1, v2, len);
		}
		return true;
	}

  native static boolean equals0 (char[] a, char[] b, int len);
  
  /**
   * we can't turn this into a native method at top level since it would require a bunch
   * of round trips
   */
	public boolean contentEquals (CharSequence charSequence){
		if (value.length != charSequence.length()){
			return false;
		}

		// cs is a String
		if (charSequence instanceof String) {
			return equals(charSequence);
		}

		// cs is a StringBuffer, or StringBuilder
		if (charSequence instanceof AbstractStringBuilder) {
			if (charSequence instanceof StringBuffer) {
				synchronized (charSequence) {
					return nonSyncContentEquals((AbstractStringBuilder) charSequence);
				}
			} else {
				return nonSyncContentEquals((AbstractStringBuilder) charSequence);
			}
		}

		// generic CharSequence - expensive
		int n = charSequence.length();
		if (n != length()) {
			return false;
		}

		byte[] val = this.value;
		if (isLatin1()) {
			for (int i = 0; i < n; i++) {
				if ((val[i] & 0xff) != charSequence.charAt(i)) {
					return false;
				}
			}
		} else {
			return StringUTF16.contentEquals(val, charSequence, n);
		}

		return true;
	}

	native public boolean equalsIgnoreCase(String anotherString);
	@Override
	native public int compareTo(String anotherString);

	public static final Comparator<String> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();
	private static class CaseInsensitiveComparator implements Comparator<String>, java.io.Serializable {
		// use serialVersionUID from JDK 1.2.2 for interoperability
		private static final long serialVersionUID = 8575799808933029326L;

		@Override
		public int compare(String s1, String s2) {
			return MJIcompare(s1,s2);
		}
	}

	native private static int MJIcompare(String s1,String s2);
	public int compareToIgnoreCase(String str) {
		return CASE_INSENSITIVE_ORDER.compare(this, str);
	}

	native public boolean regionMatches(int toffset, String other, int ooffset, int len);
	native public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len);
	native public boolean startsWith(String prefix, int toffset);
	public boolean startsWith(String prefix) {
		return startsWith(prefix, 0);
	}

	public boolean endsWith(String suffix) {
		return startsWith(suffix, value.length - suffix.value.length);
	}

	@Override
	native public int hashCode();
	public int indexOf(int ch) {
		return indexOf(ch, 0);
	}
	native public int indexOf(int ch, int fromIndex);
	native public int lastIndexOf(int ch);
	native public int lastIndexOf(int ch, int fromIndex);
	native public int indexOf(String str);
	native public int indexOf(String str, int fromIndex);

	public int lastIndexOf(String str) {
		return lastIndexOf(str, value.length);
	}
	native public int lastIndexOf(String str, int fromIndex);
	native public String substring(int beginIndex);
	native public String substring(int beginIndex, int endIndex);
	@Override
	public CharSequence subSequence(int beginIndex, int endIndex) {
		return this.substring(beginIndex, endIndex);
	}
	native public String concat(String str);
	native public String replace(char oldChar, char newChar);
	native public boolean matches(String regex);
	public boolean contains(CharSequence charSequence) {
		// No CharSequence model
		return indexOf(charSequence.toString()) > -1;
	}
	native public String replaceFirst(String regex, String replacement);
	native public String replaceAll(String regex, String replacement);
	public String replace(CharSequence target, CharSequence other) {
		// No CharSequence model
		int PATTERN= 0x10;
		Matcher pattern=Pattern.compile(target.toString(), PATTERN).matcher(this);
		return pattern.replaceAll(Matcher.quoteReplacement(other.toString()));
	}
	native public String[] split(String regex, int limit);
	native public String[] split(String regex);
	native public String toLowerCase(Locale locale);
	native public String toLowerCase();
	native public String toUpperCase(Locale locale);
	native public String toUpperCase();
	native public String trim();
	@Override
	public String toString() {
		return this;
	}
	native public char[] toCharArray();
	native public static String format(String format, Object... args);
	native public static String format(Locale l, String format, Object... args);
	public static String valueOf(Object x){
		// can't translate arbitrary object
		return (x == null) ? "null" : x.toString();
	}
	public static String valueOf(char values[]) {
		return new String(values);
	}
	public static String valueOf(char values[], int offset, int count) {
		return new String(values, offset, count);
	}
	public static String copyValueOf(char values[], int offset, int count) {
		return new String(values, offset, count);
	}
	public static String copyValueOf(char values[]) {
		return new String(values);
	}
	public static String valueOf(boolean bool) {
		return bool ? "true" : "false";
	}
	public static String valueOf(char character) {
		char data[] = {character};
		return new String(data);
	}
	native public static String valueOf(int i);
	native public static String valueOf(long l);
	native public static String valueOf(float f);
	native public static String valueOf(double d);
	public native String intern();

	byte coder() {
		return COMPACT_STRINGS ? coder : UTF16;
	}

	private boolean isLatin1() {
		return COMPACT_STRINGS && coder == LATIN1;
	}

  /*
   * methods to be compatible with Harmony/Android, which now has modified
   * versions of the old (offset based) String
   * 
   * NOTE - if the changes get too large we have to create Android specific
   * models and peers
   */
  
  // used internally by Android's java.lang.AbstractStringBuffer
  void _getChars(int start, int end, char[] buffer, int index) {
    System.arraycopy(value, start, buffer, index, end - start);
  }

  native public static void checkBoundsOffCount(int offset, int count, int length);
}
