package libcore.io;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public final class Base64 {
    private static final byte[] BASE_64_ALPHABET = initializeBase64Alphabet();
    private static final byte END_OF_INPUT = (byte) -3;
    private static final int FIRST_OUTPUT_BYTE_MASK = 16515072;
    private static final int FOURTH_OUTPUT_BYTE_MASK = 63;
    private static final byte PAD_AS_BYTE = (byte) -1;
    private static final int SECOND_OUTPUT_BYTE_MASK = 258048;
    private static final int THIRD_OUTPUT_BYTE_MASK = 4032;
    private static final byte WHITESPACE_AS_BYTE = (byte) -2;

    private static class InvalidBase64ByteException extends Exception {
        private InvalidBase64ByteException() {
        }
    }

    private static byte[] initializeBase64Alphabet() {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes(StandardCharsets.US_ASCII);
    }

    private Base64() {
    }

    public static String encode(byte[] in) {
        int len = in.length;
        byte[] output = new byte[computeEncodingOutputLen(len)];
        int i = 0;
        int outputIndex = 0;
        while (i < len) {
            int outputIndex2;
            int byteTripletAsInt = in[i] & 255;
            if (i + 1 < len) {
                byteTripletAsInt = (byteTripletAsInt << 8) | (in[i + 1] & 255);
                if (i + 2 < len) {
                    byteTripletAsInt = (byteTripletAsInt << 8) | (in[i + 2] & 255);
                } else {
                    byteTripletAsInt <<= 2;
                }
            } else {
                byteTripletAsInt <<= 4;
            }
            if (i + 2 < len) {
                outputIndex2 = outputIndex + 1;
                output[outputIndex] = BASE_64_ALPHABET[(FIRST_OUTPUT_BYTE_MASK & byteTripletAsInt) >>> 18];
                outputIndex = outputIndex2;
            }
            if (i + 1 < len) {
                outputIndex2 = outputIndex + 1;
                output[outputIndex] = BASE_64_ALPHABET[(SECOND_OUTPUT_BYTE_MASK & byteTripletAsInt) >>> 12];
            } else {
                outputIndex2 = outputIndex;
            }
            outputIndex = outputIndex2 + 1;
            output[outputIndex2] = BASE_64_ALPHABET[(byteTripletAsInt & THIRD_OUTPUT_BYTE_MASK) >>> 6];
            outputIndex2 = outputIndex + 1;
            output[outputIndex] = BASE_64_ALPHABET[byteTripletAsInt & 63];
            i += 3;
            outputIndex = outputIndex2;
        }
        int inLengthMod3 = len % 3;
        if (inLengthMod3 > 0) {
            outputIndex2 = outputIndex + 1;
            output[outputIndex] = (byte) 61;
            if (inLengthMod3 == 1) {
                outputIndex = outputIndex2 + 1;
                output[outputIndex2] = (byte) 61;
                outputIndex2 = outputIndex;
            }
        }
        return new String(output, StandardCharsets.US_ASCII);
    }

    private static int computeEncodingOutputLen(int inLength) {
        int inLengthMod3 = inLength % 3;
        int outputLen = (inLength / 3) * 4;
        if (inLengthMod3 == 2) {
            return outputLen + 4;
        }
        if (inLengthMod3 == 1) {
            return outputLen + 4;
        }
        return outputLen;
    }

    public static byte[] decode(byte[] in) {
        return decode(in, in.length);
    }

    public static byte[] decode(byte[] in, int len) {
        byte[] bArr = null;
        int inLength = Math.min(in.length, len);
        ByteArrayOutputStream output = new ByteArrayOutputStream(((inLength / 4) * 3) + 3);
        int[] pos = new int[1];
        while (pos[0] < inLength) {
            try {
                int byteTripletAsInt = 0;
                for (int j = 0; j < 4; j++) {
                    byte c = getNextByte(in, pos, inLength);
                    if (c == END_OF_INPUT || c == (byte) -1) {
                        switch (j) {
                            case 0:
                            case 1:
                                if (c == END_OF_INPUT) {
                                    bArr = output.toByteArray();
                                }
                                return bArr;
                            case 2:
                                if (c == END_OF_INPUT) {
                                    return checkNoTrailingAndReturn(output, in, pos[0], inLength);
                                }
                                pos[0] = pos[0] + 1;
                                c = getNextByte(in, pos, inLength);
                                if (c == END_OF_INPUT) {
                                    return checkNoTrailingAndReturn(output, in, pos[0], inLength);
                                }
                                if (c != (byte) -1) {
                                    return null;
                                }
                                output.write(byteTripletAsInt >> 4);
                                return checkNoTrailingAndReturn(output, in, pos[0], inLength);
                            case 3:
                                if (c == (byte) -1) {
                                    byteTripletAsInt >>= 2;
                                    output.write(byteTripletAsInt >> 8);
                                    output.write(byteTripletAsInt & 255);
                                }
                                return checkNoTrailingAndReturn(output, in, pos[0], inLength);
                            default:
                                break;
                        }
                    }
                    byteTripletAsInt = (byteTripletAsInt << 6) + (c & 255);
                    pos[0] = pos[0] + 1;
                }
                output.write(byteTripletAsInt >> 16);
                output.write((byteTripletAsInt >> 8) & 255);
                output.write(byteTripletAsInt & 255);
            } catch (InvalidBase64ByteException e) {
                return null;
            }
        }
        return checkNoTrailingAndReturn(output, in, pos[0], inLength);
    }

    private static byte getNextByte(byte[] in, int[] pos, int inLength) throws InvalidBase64ByteException {
        while (pos[0] < inLength) {
            byte c = base64AlphabetToNumericalValue(in[pos[0]]);
            if (c != WHITESPACE_AS_BYTE) {
                return c;
            }
            pos[0] = pos[0] + 1;
        }
        return END_OF_INPUT;
    }

    private static byte[] checkNoTrailingAndReturn(ByteArrayOutputStream output, byte[] in, int i, int inLength) throws InvalidBase64ByteException {
        while (i < inLength) {
            byte c = base64AlphabetToNumericalValue(in[i]);
            if (c != WHITESPACE_AS_BYTE && c != (byte) -1) {
                return null;
            }
            i++;
        }
        return output.toByteArray();
    }

    private static byte base64AlphabetToNumericalValue(byte c) throws InvalidBase64ByteException {
        if ((byte) 65 <= c && c <= (byte) 90) {
            return (byte) (c - 65);
        }
        if ((byte) 97 <= c && c <= (byte) 122) {
            return (byte) ((c - 97) + 26);
        }
        if ((byte) 48 <= c && c <= (byte) 57) {
            return (byte) ((c - 48) + 52);
        }
        if (c == (byte) 43) {
            return (byte) 62;
        }
        if (c == (byte) 47) {
            return (byte) 63;
        }
        if (c == (byte) 61) {
            return (byte) -1;
        }
        if (c == (byte) 32 || c == (byte) 9 || c == (byte) 13 || c == (byte) 10) {
            return WHITESPACE_AS_BYTE;
        }
        throw new InvalidBase64ByteException();
    }
}
