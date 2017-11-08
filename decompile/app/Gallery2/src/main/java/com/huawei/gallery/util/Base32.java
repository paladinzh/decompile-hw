package com.huawei.gallery.util;

import com.android.gallery3d.gadget.XmlUtils;
import java.nio.charset.Charset;

public class Base32 {
    private static final int[] base32Lookup = new int[]{255, 255, 26, 27, 28, 29, 30, 31, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 255, 255, 255, 255, 255, 255, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 255, 255, 255, 255, 255};

    public static String encode(String input) {
        byte[] bytes = input.getBytes(Charset.forName(XmlUtils.INPUT_ENCODING));
        int i = 0;
        int index = 0;
        StringBuffer base32 = new StringBuffer(((bytes.length + 7) * 8) / 5);
        while (i < bytes.length) {
            int digit;
            int currByte = bytes[i] >= (byte) 0 ? bytes[i] : bytes[i] + 256;
            int nextPosition = i + 1;
            if (index > 3) {
                int nextByte;
                if (nextPosition >= bytes.length) {
                    nextByte = 0;
                } else if (bytes[nextPosition] >= (byte) 0) {
                    nextByte = bytes[nextPosition];
                } else {
                    nextByte = bytes[nextPosition] + 256;
                }
                digit = currByte & (255 >> index);
                index = (index + 5) % 8;
                digit = (digit << index) | (nextByte >> (8 - index));
                i++;
            } else {
                digit = (currByte >> (8 - (index + 5))) & 31;
                index = (index + 5) % 8;
                if (index == 0) {
                    i++;
                }
            }
            base32.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".charAt(digit));
        }
        return base32.toString();
    }

    public static String decode(String base32) {
        int length = base32.length();
        byte[] bytes = new byte[((length * 5) / 8)];
        int index = 0;
        int offset = 0;
        for (int i = 0; i < length; i++) {
            int lookup = base32.charAt(i) - 48;
            if (lookup >= 0 && lookup < base32Lookup.length) {
                int digit = base32Lookup[lookup];
                if (digit != 255) {
                    if (index > 3) {
                        index = (index + 5) % 8;
                        bytes[offset] = (byte) (bytes[offset] | (digit >>> index));
                        offset++;
                        if (offset >= bytes.length) {
                            break;
                        }
                        bytes[offset] = (byte) (bytes[offset] | (digit << (8 - index)));
                    } else {
                        index = (index + 5) % 8;
                        if (index == 0) {
                            bytes[offset] = (byte) (bytes[offset] | digit);
                            offset++;
                            if (offset >= bytes.length) {
                                break;
                            }
                        } else {
                            bytes[offset] = (byte) (bytes[offset] | (digit << (8 - index)));
                        }
                    }
                } else {
                    continue;
                }
            }
        }
        return new String(bytes, Charset.forName(XmlUtils.INPUT_ENCODING));
    }
}
