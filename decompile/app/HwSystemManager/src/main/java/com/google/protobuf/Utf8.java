package com.google.protobuf;

import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;

final class Utf8 {
    public static final int COMPLETE = 0;
    public static final int MALFORMED = -1;

    private Utf8() {
    }

    public static boolean isValidUtf8(byte[] bArr) {
        return isValidUtf8(bArr, 0, bArr.length);
    }

    public static boolean isValidUtf8(byte[] bArr, int i, int i2) {
        return partialIsValidUtf8(bArr, i, i2) == 0;
    }

    public static int partialIsValidUtf8(int i, byte[] bArr, int i2, int i3) {
        byte b = (byte) 0;
        if (i != 0) {
            if (i2 >= i3) {
                return i;
            }
            int i4 = (byte) i;
            int i5;
            if (i4 < (byte) -32) {
                if (i4 >= (byte) -62) {
                    i5 = i2 + 1;
                    if (bArr[i2] <= (byte) -65) {
                        i2 = i5;
                    }
                }
                return -1;
            } else if (i4 >= (byte) -16) {
                int i6;
                int i7;
                byte b2 = (byte) ((i >> 8) ^ -1);
                if (b2 != (byte) 0) {
                    b = (byte) (i >> 16);
                    i6 = b2;
                    r1 = i2;
                } else {
                    i7 = i2 + 1;
                    b2 = bArr[i2];
                    if (i7 >= i3) {
                        return incompleteStateFor(i4, b2);
                    }
                    byte b3 = b2;
                    r1 = i7;
                }
                if (b == (byte) 0) {
                    i7 = r1 + 1;
                    i5 = bArr[r1];
                    if (i7 >= i3) {
                        return incompleteStateFor(i4, i6, i5);
                    }
                    r1 = i7;
                }
                if (i6 <= -65 && (((i4 << 28) + (i6 + Events.E_ADDVIEW_SET_ALL)) >> 30) == 0 && r0 <= (byte) -65) {
                    i2 = r1 + 1;
                    if (bArr[r1] <= (byte) -65) {
                    }
                }
                return -1;
            } else {
                b = (byte) ((i >> 8) ^ -1);
                if (b != (byte) 0) {
                    r1 = i2;
                } else {
                    r1 = i2 + 1;
                    b = bArr[i2];
                    if (r1 >= i3) {
                        return incompleteStateFor(i4, b);
                    }
                }
                if (b <= (byte) -65) {
                    if (i4 != (byte) -32 || b >= (byte) -96) {
                        if (i4 != (byte) -19 || b < (byte) -96) {
                            i2 = r1 + 1;
                            if (bArr[r1] <= (byte) -65) {
                            }
                        }
                    }
                }
                return -1;
            }
        }
        return partialIsValidUtf8(bArr, i2, i3);
    }

    public static int partialIsValidUtf8(byte[] bArr, int i, int i2) {
        while (i < i2 && bArr[i] >= (byte) 0) {
            i++;
        }
        if (i < i2) {
            return partialIsValidUtf8NonAscii(bArr, i, i2);
        }
        return 0;
    }

    private static int partialIsValidUtf8NonAscii(byte[] bArr, int i, int i2) {
        while (i < i2) {
            int i3 = i + 1;
            byte b = bArr[i];
            if (b >= (byte) 0) {
                i = i3;
            } else {
                int i4;
                if (b >= (byte) -32) {
                    int i5;
                    byte b2;
                    if (b >= (byte) -16) {
                        if (i3 >= i2 - 2) {
                            return incompleteStateFor(bArr, i3, i2);
                        }
                        i5 = i3 + 1;
                        b2 = bArr[i3];
                        if (b2 <= (byte) -65 && (((b << 28) + (b2 + Events.E_ADDVIEW_SET_ALL)) >> 30) == 0) {
                            i3 = i5 + 1;
                            if (bArr[i5] <= (byte) -65) {
                                i4 = i3 + 1;
                                if (bArr[i3] <= (byte) -65) {
                                }
                            }
                        }
                        return -1;
                    } else if (i3 >= i2 - 1) {
                        return incompleteStateFor(bArr, i3, i2);
                    } else {
                        i5 = i3 + 1;
                        b2 = bArr[i3];
                        if (b2 <= (byte) -65) {
                            if (b != (byte) -32 || b2 >= (byte) -96) {
                                if (b != (byte) -19 || b2 < (byte) -96) {
                                    i4 = i5 + 1;
                                    if (bArr[i5] <= (byte) -65) {
                                    }
                                }
                            }
                        }
                        return -1;
                    }
                } else if (i3 >= i2) {
                    return b;
                } else {
                    if (b >= (byte) -62) {
                        i4 = i3 + 1;
                        if (bArr[i3] > (byte) -65) {
                        }
                    }
                    return -1;
                }
                i = i4;
            }
        }
        return 0;
    }

    private static int incompleteStateFor(int i) {
        return i <= -12 ? i : -1;
    }

    private static int incompleteStateFor(int i, int i2) {
        return (i <= -12 && i2 <= -65) ? (i2 << 8) ^ i : -1;
    }

    private static int incompleteStateFor(int i, int i2, int i3) {
        return (i <= -12 && i2 <= -65 && i3 <= -65) ? ((i2 << 8) ^ i) ^ (i3 << 16) : -1;
    }

    private static int incompleteStateFor(byte[] bArr, int i, int i2) {
        int i3 = bArr[i - 1];
        switch (i2 - i) {
            case 0:
                return incompleteStateFor(i3);
            case 1:
                return incompleteStateFor(i3, bArr[i]);
            case 2:
                return incompleteStateFor(i3, bArr[i], bArr[i + 1]);
            default:
                throw new AssertionError();
        }
    }
}
