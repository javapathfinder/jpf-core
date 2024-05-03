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

import sun.nio.cs.*;

import java.io.ObjectStreamField;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
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

	private static final char REPL = '\ufffd';
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

		if (length == 0) {
			this.value = "".value;
			this.coder = "".coder;
		} else if (cset == UTF_8.INSTANCE) {
			if (COMPACT_STRINGS && !StringCoding.hasNegatives(x, offset, length)) {
				this.value = Arrays.copyOfRange(x, offset, offset + length);
				this.coder = LATIN1;
			} else {
				int sl = offset + length;
				int dp = 0;
				byte[] dst = null;
				if (COMPACT_STRINGS) {
					dst = new byte[length];
					while (offset < sl) {
						int b1 = x[offset];
						if (b1 >= 0) {
							dst[dp++] = (byte)b1;
							offset++;
							continue;
						}
						if ((b1 & 0xfe) == 0xc2 && offset + 1 < sl) { // b1 either 0xc2 or 0xc3
							int b2 = x[offset + 1];
							if (!isNotContinuation(b2)) {
								dst[dp++] = (byte)decode2(b1, b2);
								offset += 2;
								continue;
							}
						}
						// anything not a latin1, including the repl
						// we have to go with the utf16
						break;
					}
					if (offset == sl) {
						if (dp != dst.length) {
							dst = Arrays.copyOf(dst, dp);
						}
						this.value = dst;
						this.coder = LATIN1;
						return;
					}
				}
				if (dp == 0 || dst == null) {
					dst = new byte[length << 1];
				} else {
					byte[] buf = new byte[length << 1];
					StringLatin1.inflate(dst, 0, buf, 0, dp);
					dst = buf;
				}
				dp = decodeUTF8_UTF16(x, offset, sl, dst, dp, true);
				if (dp != length) {
					dst = Arrays.copyOf(dst, dp << 1);
				}
				this.value = dst;
				this.coder = UTF16;
			}
		} else if (cset == ISO_8859_1.INSTANCE) {
			if (COMPACT_STRINGS) {
				this.value = Arrays.copyOfRange(x, offset, offset + length);
				this.coder = LATIN1;
			} else {
				this.value = StringLatin1.inflate(x, offset, length);
				this.coder = UTF16;
			}
		} else if (cset == US_ASCII.INSTANCE) {
			if (COMPACT_STRINGS && !StringCoding.hasNegatives(x, offset, length)) {
				this.value = Arrays.copyOfRange(x, offset, offset + length);
				this.coder = LATIN1;
			} else {
				byte[] dst = new byte[length << 1];
				int dp = 0;
				while (dp < length) {
					int b = x[offset++];
					StringUTF16.putChar(dst, dp++, (b >= 0) ? (char) b : REPL);
				}
				this.value = dst;
				this.coder = UTF16;
			}
		} else {
			// (1)We never cache the "external" cs, the only benefit of creating
			// an additional StringDe/Encoder object to wrap it is to share the
			// de/encode() method. These SD/E objects are short-lived, the young-gen
			// gc should be able to take care of them well. But the best approach
			// is still not to generate them if not really necessary.
			// (2)The defensive copy of the input byte/char[] has a big performance
			// impact, as well as the outgoing result byte/char[]. Need to do the
			// optimization check of (sm==null && classLoader0==null) for both.
			CharsetDecoder cd = cset.newDecoder();
			// ArrayDecoder fastpaths
			if (cd instanceof ArrayDecoder ad) {
				// ascii
				if (ad.isASCIICompatible() && !StringCoding.hasNegatives(x, offset, length)) {
					if (COMPACT_STRINGS) {
						this.value = Arrays.copyOfRange(x, offset, offset + length);
						this.coder = LATIN1;
						return;
					}
					this.value = StringLatin1.inflate(x, offset, length);
					this.coder = UTF16;
					return;
				}

				// fastpath for always Latin1 decodable single byte
				if (COMPACT_STRINGS && ad.isLatin1Decodable()) {
					byte[] dst = new byte[length];
					ad.decodeToLatin1(x, offset, length, dst);
					this.value = dst;
					this.coder = LATIN1;
					return;
				}

				int en = scale(length, cd.maxCharsPerByte());
				cd.onMalformedInput(CodingErrorAction.REPLACE)
						.onUnmappableCharacter(CodingErrorAction.REPLACE);
				char[] ca = new char[en];
				int clen = ad.decode(x, offset, length, ca);
				if (COMPACT_STRINGS) {
					byte[] bs = StringUTF16.compress(ca, 0, clen);
					if (bs != null) {
						value = bs;
						coder = LATIN1;
						return;
					}
				}
				coder = UTF16;
				value = StringUTF16.toBytes(ca, 0, clen);
				return;
			}

			// decode using CharsetDecoder
			int en = scale(length, cd.maxCharsPerByte());
			cd.onMalformedInput(CodingErrorAction.REPLACE)
					.onUnmappableCharacter(CodingErrorAction.REPLACE);
			char[] ca = new char[en];
			if (cset.getClass().getClassLoader() != null &&
					System.getSecurityManager() != null) {
				x = Arrays.copyOfRange(x, offset, offset + length);
				offset = 0;
			}

			int caLen;
			try {
				caLen = decodeWithDecoder(cd, ca, x, offset, length);
			} catch (CharacterCodingException xx) {
				// Substitution is enabled, so this shouldn't happen
				throw new Error(xx);
			}
			if (COMPACT_STRINGS) {
				byte[] bs = StringUTF16.compress(ca, 0, caLen);
				if (bs != null) {
					value = bs;
					coder = LATIN1;
					return;
				}
			}
			coder = UTF16;
			value = StringUTF16.toBytes(ca, 0, caLen);
		}
	}

	private static int decodeWithDecoder(CharsetDecoder cd, char[] dst, byte[] src, int offset, int length)
			throws CharacterCodingException {
		ByteBuffer bb = ByteBuffer.wrap(src);
		CharSequence charSequence = new String(dst);
		CharBuffer cb = CharBuffer.wrap(charSequence);
		CoderResult cr = cd.decode(bb, cb, true);
		if (!cr.isUnderflow())
			cr.throwException();
		cr = cd.flush(cb);
		if (!cr.isUnderflow())
			cr.throwException();
		return cb.position();
	}

	private static int decodeUTF8_UTF16(byte[] src, int sp, int sl, byte[] dst, int dp, boolean doReplace) {
		while (sp < sl) {
			int b1 = src[sp++];
			if (b1 >= 0) {
				StringUTF16.putChar(dst, dp++, (char) b1);
			} else if ((b1 >> 5) == -2 && (b1 & 0x1e) != 0) {
				if (sp < sl) {
					int b2 = src[sp++];
					if (isNotContinuation(b2)) {
						if (!doReplace) {
							throwMalformed(sp - 1, 1);
						}
						StringUTF16.putChar(dst, dp++, REPL);
						sp--;
					} else {
						StringUTF16.putChar(dst, dp++, decode2(b1, b2));
					}
					continue;
				}
				if (!doReplace) {
					throwMalformed(sp, 1);  // underflow()
				}
				StringUTF16.putChar(dst, dp++, REPL);
				break;
			} else if ((b1 >> 4) == -2) {
				if (sp + 1 < sl) {
					int b2 = src[sp++];
					int b3 = src[sp++];
					if (isMalformed3(b1, b2, b3)) {
						if (!doReplace) {
							throwMalformed(sp - 3, 3);
						}
						StringUTF16.putChar(dst, dp++, REPL);
						sp -= 3;
						sp += malformed3(src, sp);
					} else {
						char c = decode3(b1, b2, b3);
						if (Character.isSurrogate(c)) {
							if (!doReplace) {
								throwMalformed(sp - 3, 3);
							}
							StringUTF16.putChar(dst, dp++, REPL);
						} else {
							StringUTF16.putChar(dst, dp++, c);
						}
					}
					continue;
				}
				if (sp < sl && isMalformed3_2(b1, src[sp])) {
					if (!doReplace) {
						throwMalformed(sp - 1, 2);
					}
					StringUTF16.putChar(dst, dp++, REPL);
					continue;
				}
				if (!doReplace) {
					throwMalformed(sp, 1);
				}
				StringUTF16.putChar(dst, dp++, REPL);
				break;
			} else if ((b1 >> 3) == -2) {
				if (sp + 2 < sl) {
					int b2 = src[sp++];
					int b3 = src[sp++];
					int b4 = src[sp++];
					int uc = decode4(b1, b2, b3, b4);
					if (isMalformed4(b2, b3, b4) ||
							!Character.isSupplementaryCodePoint(uc)) { // shortest form check
						if (!doReplace) {
							throwMalformed(sp - 4, 4);
						}
						StringUTF16.putChar(dst, dp++, REPL);
						sp -= 4;
						sp += malformed4(src, sp);
					} else {
						StringUTF16.putChar(dst, dp++, Character.highSurrogate(uc));
						StringUTF16.putChar(dst, dp++, Character.lowSurrogate(uc));
					}
					continue;
				}
				b1 &= 0xff;
				if (b1 > 0xf4 || sp < sl && isMalformed4_2(b1, src[sp] & 0xff)) {
					if (!doReplace) {
						throwMalformed(sp - 1, 1);  // or 2
					}
					StringUTF16.putChar(dst, dp++, REPL);
					continue;
				}
				if (!doReplace) {
					throwMalformed(sp - 1, 1);
				}
				sp++;
				StringUTF16.putChar(dst, dp++, REPL);
				if (sp < sl && isMalformed4_3(src[sp])) {
					continue;
				}
				break;
			} else {
				if (!doReplace) {
					throwMalformed(sp - 1, 1);
				}
				StringUTF16.putChar(dst, dp++, REPL);
			}
		}
		return dp;
	}

	private static boolean isMalformed4_2(int b1, int b2) {
		return (b1 == 0xf0 && (b2 < 0x90 || b2 > 0xbf)) ||
				(b1 == 0xf4 && (b2 & 0xf0) != 0x80) ||
				(b2 & 0xc0) != 0x80;
	}

	private static boolean isMalformed4_3(int b3) {
		return (b3 & 0xc0) != 0x80;
	}

	private static boolean isMalformed4(int b2, int b3, int b4) {
		return (b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80 ||
				(b4 & 0xc0) != 0x80;
	}

	private static int decode4(int b1, int b2, int b3, int b4) {
		return ((b1 << 18) ^
				(b2 << 12) ^
				(b3 <<  6) ^
				(b4 ^
						(((byte) 0xF0 << 18) ^
								((byte) 0x80 << 12) ^
								((byte) 0x80 <<  6) ^
								((byte) 0x80 <<  0))));
	}

	private static boolean isMalformed3_2(int b1, int b2) {
		return (b1 == (byte)0xe0 && (b2 & 0xe0) == 0x80) ||
				(b2 & 0xc0) != 0x80;
	}

	private static char decode3(int b1, int b2, int b3) {
		return (char)((b1 << 12) ^
				(b2 <<  6) ^
				(b3 ^
						(((byte) 0xE0 << 12) ^
								((byte) 0x80 <<  6) ^
								((byte) 0x80 <<  0))));
	}

	private static boolean isMalformed3(int b1, int b2, int b3) {
		return (b1 == (byte)0xe0 && (b2 & 0xe0) == 0x80) ||
				(b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80;
	}

	private static int malformed4(byte[] src, int sp) {
		// we don't care the speed here
		int b1 = src[sp++] & 0xff;
		int b2 = src[sp++] & 0xff;
		if (b1 > 0xf4 ||
				(b1 == 0xf0 && (b2 < 0x90 || b2 > 0xbf)) ||
				(b1 == 0xf4 && (b2 & 0xf0) != 0x80) ||
				isNotContinuation(b2))
			return 1;
		if (isNotContinuation(src[sp]))
			return 2;
		return 3;
	}

	private static int malformed3(byte[] src, int sp) {
		int b1 = src[sp++];
		int b2 = src[sp];    // no need to lookup b3
		return ((b1 == (byte)0xe0 && (b2 & 0xe0) == 0x80) ||
				isNotContinuation(b2)) ? 1 : 2;
	}

	private static void throwMalformed(int off, int nb) {
		String msg = "malformed input off : " + off + ", length : " + nb;
		throw new IllegalArgumentException(msg, new MalformedInputException(nb));
	}


	private static char decode2(int b1, int b2) {
		return (char)(((b1 << 6) ^ b2) ^
				(((byte) 0xC0 << 6) ^
						((byte) 0x80 << 0)));
	}

	private static boolean isNotContinuation(int b) {
		return (b & 0xc0) != 0x80;
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
	public void getBytes(byte[] dst, int srcPos, int dstBegin, byte coder, int length){
		getBytes(srcPos,(srcPos+length),dst,dstBegin);
	}

	native public byte[] getBytes(String charsetName)
			throws UnsupportedEncodingException;

	public byte[] getBytes(Charset x){
		if (x == null) throw new NullPointerException();
		return encode(x, coder(), value);
	}

	private static byte[] encode(Charset cs, byte coder, byte[] val) {
		if (cs == UTF_8.INSTANCE) {
			return encodeUTF8(coder, val, true);
		}
		if (cs == ISO_8859_1.INSTANCE) {
			return encode8859_1(coder, val);
		}
		if (cs == US_ASCII.INSTANCE) {
			return encodeASCII(coder, val);
		}
		return encodeWithEncoder(cs, coder, val, true);
	}

	private static byte[] encodeUTF8(byte coder, byte[] val, boolean doReplace) {
		if (coder == UTF16)
			return encodeUTF8_UTF16(val, doReplace);

		if (!StringCoding.hasNegatives(val, 0, val.length))
			return Arrays.copyOf(val, val.length);

		int dp = 0;
		byte[] dst = new byte[val.length << 1];
		for (byte c : val) {
			if (c < 0) {
				dst[dp++] = (byte) (0xc0 | ((c & 0xff) >> 6));
				dst[dp++] = (byte) (0x80 | (c & 0x3f));
			} else {
				dst[dp++] = c;
			}
		}
		if (dp == dst.length)
			return dst;
		return Arrays.copyOf(dst, dp);
	}

	private static byte[] encode8859_1(byte coder, byte[] val) {
		return encode8859_1(coder, val, true);
	}

	private static byte[] encode8859_1(byte coder, byte[] val, boolean doReplace) {
		if (coder == LATIN1) {
			return Arrays.copyOf(val, val.length);
		}
		int len = val.length >> 1;
		byte[] dst = new byte[len];
		int dp = 0;
		int sp = 0;
		int sl = len;
		while (sp < sl) {
			int ret = StringCoding.implEncodeISOArray(val, sp, dst, dp, len);
			sp = sp + ret;
			dp = dp + ret;
			if (ret != len) {
				if (!doReplace) {
					throwUnmappable(sp);
				}
				char c = StringUTF16.getChar(val, sp++);
				if (Character.isHighSurrogate(c) && sp < sl &&
						Character.isLowSurrogate(StringUTF16.getChar(val, sp))) {
					sp++;
				}
				dst[dp++] = '?';
				len = sl - sp;
			}
		}
		if (dp == dst.length) {
			return dst;
		}
		return Arrays.copyOf(dst, dp);
	}

	private static byte[] encodeASCII(byte coder, byte[] val) {
		if (coder == LATIN1) {
			byte[] dst = Arrays.copyOf(val, val.length);
			for (int i = 0; i < dst.length; i++) {
				if (dst[i] < 0) {
					dst[i] = '?';
				}
			}
			return dst;
		}
		int len = val.length >> 1;
		byte[] dst = new byte[len];
		int dp = 0;
		for (int i = 0; i < len; i++) {
			char c = StringUTF16.getChar(val, i);
			if (c < 0x80) {
				dst[dp++] = (byte)c;
				continue;
			}
			if (Character.isHighSurrogate(c) && i + 1 < len &&
					Character.isLowSurrogate(StringUTF16.getChar(val, i + 1))) {
				i++;
			}
			dst[dp++] = '?';
		}
		if (len == dp) {
			return dst;
		}
		return Arrays.copyOf(dst, dp);
	}

	private static byte[] encodeWithEncoder(Charset cs, byte coder, byte[] val, boolean doReplace) {
		CharsetEncoder ce = cs.newEncoder();
		int len = val.length >> coder;  // assume LATIN1=0/UTF16=1;
		int en = scale(len, ce.maxBytesPerChar());
		// fastpath with ArrayEncoder implies `doReplace`.
		if (doReplace && ce instanceof ArrayEncoder ae) {
			// fastpath for ascii compatible
			if (coder == LATIN1 &&
					ae.isASCIICompatible() &&
					!StringCoding.hasNegatives(val, 0, val.length)) {
				return Arrays.copyOf(val, val.length);
			}
			byte[] ba = new byte[en];
			if (len == 0) {
				return ba;
			}

			int blen = (coder == LATIN1) ? ae.encodeFromLatin1(val, 0, len, ba)
					: ae.encodeFromUTF16(val, 0, len, ba);
			if (blen != -1) {
				return safeTrim(ba, blen, true);
			}
		}

		byte[] ba = new byte[en];
		if (len == 0) {
			return ba;
		}
		if (doReplace) {
			ce.onMalformedInput(CodingErrorAction.REPLACE)
					.onUnmappableCharacter(CodingErrorAction.REPLACE);
		}
		char[] ca = (coder == LATIN1 ) ? StringLatin1.toChars(val)
				: StringUTF16.toChars(val);
		ByteBuffer bb = ByteBuffer.wrap(ba);
		CharSequence charSequence = new String(ca);
		CharBuffer cb = CharBuffer.wrap(charSequence);
		try {
			CoderResult cr = ce.encode(cb, bb, true);
			if (!cr.isUnderflow())
				cr.throwException();
			cr = ce.flush(bb);
			if (!cr.isUnderflow())
				cr.throwException();
		} catch (CharacterCodingException x) {
			if (!doReplace) {
				throw new IllegalArgumentException(x);
			} else {
				throw new Error(x);
			}
		}
		return safeTrim(ba, bb.position(), cs.getClass().getClassLoader() == null);
	}

	private static byte[] safeTrim(byte[] ba, int len, boolean isTrusted) {
		if (len == ba.length && (isTrusted || System.getSecurityManager() == null)) {
			return ba;
		} else {
			return Arrays.copyOf(ba, len);
		}
	}

	private static int scale(int len, float expansionFactor) {
		// We need to perform double, not float, arithmetic; otherwise
		// we lose low order bits when len is larger than 2**24.
		return (int)(len * (double)expansionFactor);
	}

	private static byte[] encodeUTF8_UTF16(byte[] val, boolean doReplace) {
		int dp = 0;
		int sp = 0;
		int sl = val.length >> 1;
		byte[] dst = new byte[sl * 3];
		while (sp < sl) {
			// ascii fast loop;
			char c = StringUTF16.getChar(val, sp);
			if (c >= '\u0080') {
				break;
			}
			dst[dp++] = (byte)c;
			sp++;
		}
		while (sp < sl) {
			char c = StringUTF16.getChar(val, sp++);
			if (c < 0x80) {
				dst[dp++] = (byte)c;
			} else if (c < 0x800) {
				dst[dp++] = (byte)(0xc0 | (c >> 6));
				dst[dp++] = (byte)(0x80 | (c & 0x3f));
			} else if (Character.isSurrogate(c)) {
				int uc = -1;
				char c2;
				if (Character.isHighSurrogate(c) && sp < sl &&
						Character.isLowSurrogate(c2 = StringUTF16.getChar(val, sp))) {
					uc = Character.toCodePoint(c, c2);
				}
				if (uc < 0) {
					if (doReplace) {
						dst[dp++] = '?';
					} else {
						throwUnmappable(sp - 1);
					}
				} else {
					dst[dp++] = (byte)(0xf0 | ((uc >> 18)));
					dst[dp++] = (byte)(0x80 | ((uc >> 12) & 0x3f));
					dst[dp++] = (byte)(0x80 | ((uc >>  6) & 0x3f));
					dst[dp++] = (byte)(0x80 | (uc & 0x3f));
					sp++;  // 2 chars
				}
			} else {
				// 3 bytes, 16 bits
				dst[dp++] = (byte)(0xe0 | ((c >> 12)));
				dst[dp++] = (byte)(0x80 | ((c >>  6) & 0x3f));
				dst[dp++] = (byte)(0x80 | (c & 0x3f));
			}
		}
		if (dp == dst.length) {
			return dst;
		}
		return Arrays.copyOf(dst, dp);
	}

	private static void throwUnmappable(int off) {
		String msg = "malformed input off : " + off + ", length : 1";
		throw new IllegalArgumentException(msg, new UnmappableCharacterException(1));
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


  // This private method is NOT part of the String API.
  // It is meant to be used ONLY in FunctionObjectFactory
  // for invokedynamic's String concatenation.
  //
  // Any further modifications of this function MUST NOT use any
  // string concatenation operations in it, otherwise it can
  // trap into infinite recursion.
  private static String generateStringByConcatenatingArgs(Object[] args) {
    // We use StringBuilder just like what OpenJDK does before JDK 9.
    // More importantly, unlike StringBuffer, StringBuilder
    // doesn't involves any synchronization operations, which prevent JPF
    // from generating unnecessary state transitions.
    StringBuilder builder = new StringBuilder();
    for (Object arg : args) {
      builder.append(arg);
    }
    return builder.toString();
  }

  /**
   * StringIndexOutOfBoundsException  if {@code index} is
   * negative or greater than or equal to {@code length}.
   *
   * Added to support SAXParserTest
   */
  static void checkIndex(int index, int length) {
    if (index < 0 || index >= length) {
      throw new StringIndexOutOfBoundsException("index " + index + ",length " + length);
    }
  }

}

