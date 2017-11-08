package org.apache.commons.jexl2.parser;

import com.android.systemui.statusbar.policy.HwCustRemoteInputViewImpl;
import fyusion.vislib.BuildConfig;
import java.io.IOException;
import java.io.PrintStream;

public class ParserTokenManager implements ParserConstants {
    static final long[] jjbitVec0 = new long[]{-2, -1, -1, -1};
    static final long[] jjbitVec2 = new long[]{0, 0, -1, -1};
    static final long[] jjbitVec3 = new long[]{-4294967298L, -1, -1, -1};
    static final long[] jjbitVec4 = new long[]{-3298534883329L, -1, -1, -1};
    public static final int[] jjnewLexState = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    static final int[] jjnextStates = new int[]{63, 69, 54, 55, 57, 49, 50, 52, 59, 60, 40, 1, 2, 4, 43, 44, 47, 65, 66, 68, 70, 71, 73, 82, 83, 79, 80, 75, 77, 45, 46, 65, 71, 56, 57, 59, 51, 52, 54, 61, 62, 42, 45, 46, 49, 67, 68, 70, 72, 73, 75, 84, 85, 81, 82, 77, 79, 47, 48};
    public static final String[] jjstrLiteralImages = new String[]{BuildConfig.FLAVOR, null, null, null, null, null, null, null, null, "if", "else", "for", "foreach", "while", "new", "var", "empty", "size", "null", "true", "false", "return", "in", "(", ")", "{", "}", "[", "]", ";", ":", ",", ".", "?", "?:", null, null, null, null, "=~", "!~", null, null, null, null, "=", null, null, null, "+", "-", "*", "~", "&", "|", "^", null, null, null, null, null, null, null};
    static final long[] jjtoSkip = new long[]{510};
    static final long[] jjtoToken = new long[]{8791026472627207681L};
    public static final String[] lexStateNames = new String[]{"REGISTERS", "FOR_EACH_IN", "DEFAULT"};
    protected char curChar;
    int curLexState = 2;
    public PrintStream debugStream = System.out;
    int defaultLexState = 2;
    protected SimpleCharStream input_stream;
    int jjmatchedKind;
    int jjmatchedPos;
    int jjnewStateCnt;
    int jjround;
    private final int[] jjrounds = new int[86];
    private final int[] jjstateSet = new int[172];

    private final int jjStopStringLiteralDfa_2(int pos, long active0) {
        switch (pos) {
            case 0:
                if ((3848704 & active0) != 0) {
                    this.jjmatchedKind = 56;
                    return 37;
                } else if ((9007199254740992L & active0) != 0) {
                    return 6;
                } else {
                    if ((278528 & active0) != 0) {
                        this.jjmatchedKind = 56;
                        return 75;
                    } else if ((18014398509481984L & active0) != 0) {
                        return 11;
                    } else {
                        if ((1099511627776L & active0) != 0) {
                            this.jjmatchedKind = 48;
                            return 19;
                        } else if ((35734127902720L & active0) != 0) {
                            return 15;
                        } else {
                            if ((66560 & active0) == 0) {
                                return -1;
                            }
                            this.jjmatchedKind = 56;
                            return 17;
                        }
                    }
                }
            case 1:
                if ((16384 & active0) != 0) {
                    this.jjmatchedKind = 38;
                    this.jjmatchedPos = 1;
                    return 37;
                } else if ((4176896 & active0) == 0) {
                    return (512 & active0) != 0 ? 37 : -1;
                } else {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 1;
                    return 37;
                }
            case 2:
                if ((55296 & active0) != 0) {
                    return 37;
                }
                if ((4137984 & active0) == 0) {
                    return -1;
                }
                if (this.jjmatchedPos != 2) {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 2;
                }
                return 37;
            case 3:
                if ((3223552 & active0) == 0) {
                    return (918528 & active0) != 0 ? 37 : -1;
                } else {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 3;
                    return 37;
                }
            case 4:
                if ((2101248 & active0) == 0) {
                    return (1122304 & active0) != 0 ? 37 : -1;
                } else {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 4;
                    return 37;
                }
            case 5:
                if ((4096 & active0) == 0) {
                    return (2097152 & active0) != 0 ? 37 : -1;
                } else {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 5;
                    return 37;
                }
            default:
                return -1;
        }
    }

    private final int jjStartNfa_2(int pos, long active0) {
        return jjMoveNfa_2(jjStopStringLiteralDfa_2(pos, active0), pos + 1);
    }

    private int jjStopAtPos(int pos, int kind) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = pos;
        return pos + 1;
    }

    private int jjMoveStringLiteralDfa0_2() {
        switch (this.curChar) {
            case '!':
                return jjMoveStringLiteralDfa1_2(1099511627776L);
            case '&':
                return jjStartNfaWithStates_2(0, 53, 6);
            case '(':
                return jjStopAtPos(0, 23);
            case ')':
                return jjStopAtPos(0, 24);
            case '*':
                return jjStopAtPos(0, 51);
            case '+':
                return jjStopAtPos(0, 49);
            case ',':
                return jjStopAtPos(0, 31);
            case '-':
                return jjStopAtPos(0, 50);
            case '.':
                return jjStopAtPos(0, 32);
            case ':':
                return jjStopAtPos(0, 30);
            case ';':
                return jjStopAtPos(0, 29);
            case '=':
                this.jjmatchedKind = 45;
                return jjMoveStringLiteralDfa1_2(549755813888L);
            case '?':
                this.jjmatchedKind = 33;
                return jjMoveStringLiteralDfa1_2(17179869184L);
            case '[':
                return jjStopAtPos(0, 27);
            case ']':
                return jjStopAtPos(0, 28);
            case '^':
                return jjStopAtPos(0, 55);
            case 'e':
                return jjMoveStringLiteralDfa1_2(66560);
            case 'f':
                return jjMoveStringLiteralDfa1_2(1054720);
            case 'i':
                return jjMoveStringLiteralDfa1_2(512);
            case 'n':
                return jjMoveStringLiteralDfa1_2(278528);
            case 'r':
                return jjMoveStringLiteralDfa1_2(2097152);
            case 's':
                return jjMoveStringLiteralDfa1_2(131072);
            case 't':
                return jjMoveStringLiteralDfa1_2(524288);
            case 'v':
                return jjMoveStringLiteralDfa1_2(32768);
            case 'w':
                return jjMoveStringLiteralDfa1_2(8192);
            case '{':
                return jjStopAtPos(0, 25);
            case '|':
                return jjStartNfaWithStates_2(0, 54, 11);
            case '}':
                return jjStopAtPos(0, 26);
            case '~':
                return jjStopAtPos(0, 52);
            default:
                return jjMoveNfa_2(5, 0);
        }
    }

    private int jjMoveStringLiteralDfa1_2(long active0) {
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case ':':
                    if ((17179869184L & active0) != 0) {
                        return jjStopAtPos(1, 34);
                    }
                    break;
                case 'a':
                    return jjMoveStringLiteralDfa2_2(active0, 1081344);
                case 'e':
                    return jjMoveStringLiteralDfa2_2(active0, 2113536);
                case 'f':
                    if ((512 & active0) != 0) {
                        return jjStartNfaWithStates_2(1, 9, 37);
                    }
                    break;
                case 'h':
                    return jjMoveStringLiteralDfa2_2(active0, 8192);
                case 'i':
                    return jjMoveStringLiteralDfa2_2(active0, 131072);
                case 'l':
                    return jjMoveStringLiteralDfa2_2(active0, 1024);
                case 'm':
                    return jjMoveStringLiteralDfa2_2(active0, 65536);
                case 'o':
                    return jjMoveStringLiteralDfa2_2(active0, 6144);
                case 'r':
                    return jjMoveStringLiteralDfa2_2(active0, 524288);
                case 'u':
                    return jjMoveStringLiteralDfa2_2(active0, 262144);
                case '~':
                    if ((549755813888L & active0) != 0) {
                        return jjStopAtPos(1, 39);
                    }
                    if ((1099511627776L & active0) != 0) {
                        return jjStopAtPos(1, 40);
                    }
                    break;
            }
            return jjStartNfa_2(0, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_2(0, active0);
            return 1;
        }
    }

    private int jjMoveStringLiteralDfa2_2(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_2(0, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'i':
                    return jjMoveStringLiteralDfa3_2(active0, 8192);
                case 'l':
                    return jjMoveStringLiteralDfa3_2(active0, 1310720);
                case 'p':
                    return jjMoveStringLiteralDfa3_2(active0, 65536);
                case 'r':
                    if ((2048 & active0) != 0) {
                        this.jjmatchedKind = 11;
                        this.jjmatchedPos = 2;
                    } else if ((32768 & active0) != 0) {
                        return jjStartNfaWithStates_2(2, 15, 37);
                    }
                    return jjMoveStringLiteralDfa3_2(active0, 4096);
                case 's':
                    return jjMoveStringLiteralDfa3_2(active0, 1024);
                case 't':
                    return jjMoveStringLiteralDfa3_2(active0, 2097152);
                case 'u':
                    return jjMoveStringLiteralDfa3_2(active0, 524288);
                case 'w':
                    if ((16384 & active0) != 0) {
                        return jjStartNfaWithStates_2(2, 14, 37);
                    }
                    break;
                case 'z':
                    return jjMoveStringLiteralDfa3_2(active0, 131072);
            }
            return jjStartNfa_2(1, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_2(1, active0);
            return 2;
        }
    }

    private int jjMoveStringLiteralDfa3_2(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_2(1, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'e':
                    if ((1024 & active0) != 0) {
                        return jjStartNfaWithStates_2(3, 10, 37);
                    }
                    if ((131072 & active0) != 0) {
                        return jjStartNfaWithStates_2(3, 17, 37);
                    }
                    if ((524288 & active0) != 0) {
                        return jjStartNfaWithStates_2(3, 19, 37);
                    }
                    return jjMoveStringLiteralDfa4_2(active0, 4096);
                case 'l':
                    if ((262144 & active0) != 0) {
                        return jjStartNfaWithStates_2(3, 18, 37);
                    }
                    return jjMoveStringLiteralDfa4_2(active0, 8192);
                case 's':
                    return jjMoveStringLiteralDfa4_2(active0, 1048576);
                case 't':
                    return jjMoveStringLiteralDfa4_2(active0, 65536);
                case 'u':
                    return jjMoveStringLiteralDfa4_2(active0, 2097152);
                default:
                    return jjStartNfa_2(2, active0);
            }
        } catch (IOException e) {
            jjStopStringLiteralDfa_2(2, active0);
            return 3;
        }
    }

    private int jjMoveStringLiteralDfa4_2(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_2(2, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'a':
                    return jjMoveStringLiteralDfa5_2(active0, 4096);
                case 'e':
                    if ((8192 & active0) != 0) {
                        return jjStartNfaWithStates_2(4, 13, 37);
                    }
                    if ((1048576 & active0) != 0) {
                        return jjStartNfaWithStates_2(4, 20, 37);
                    }
                    break;
                case 'r':
                    return jjMoveStringLiteralDfa5_2(active0, 2097152);
                case 'y':
                    if ((65536 & active0) != 0) {
                        return jjStartNfaWithStates_2(4, 16, 37);
                    }
                    break;
            }
            return jjStartNfa_2(3, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_2(3, active0);
            return 4;
        }
    }

    private int jjMoveStringLiteralDfa5_2(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_2(3, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'c':
                    return jjMoveStringLiteralDfa6_2(active0, 4096);
                case 'n':
                    if ((2097152 & active0) != 0) {
                        return jjStartNfaWithStates_2(5, 21, 37);
                    }
                    break;
            }
            return jjStartNfa_2(4, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_2(4, active0);
            return 5;
        }
    }

    private int jjMoveStringLiteralDfa6_2(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_2(4, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'h':
                    if ((4096 & active0) != 0) {
                        return jjStartNfaWithStates_2(6, 12, 37);
                    }
                    break;
            }
            return jjStartNfa_2(5, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_2(5, active0);
            return 6;
        }
    }

    private int jjStartNfaWithStates_2(int pos, int kind, int state) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = pos;
        try {
            this.curChar = (char) this.input_stream.readChar();
            return jjMoveNfa_2(state, pos + 1);
        } catch (IOException e) {
            return pos + 1;
        }
    }

    private int jjMoveNfa_2(int startState, int curPos) {
        int startsAt = 0;
        this.jjnewStateCnt = 84;
        int i = 1;
        this.jjstateSet[0] = startState;
        int kind = Integer.MAX_VALUE;
        while (true) {
            int i2 = this.jjround + 1;
            this.jjround = i2;
            if (i2 == Integer.MAX_VALUE) {
                ReInitRounds();
            }
            long l;
            int[] iArr;
            int i3;
            if (this.curChar < '@') {
                l = 1 << this.curChar;
                while (true) {
                    i--;
                    switch (this.jjstateSet[i]) {
                        case 0:
                            if (this.curChar == '#') {
                                if (kind > 1) {
                                    kind = 1;
                                }
                                jjCheckNAddStates(11, 13);
                                break;
                            }
                            break;
                        case 1:
                            if ((-9217 & l) != 0) {
                                if (kind > 1) {
                                    kind = 1;
                                }
                                jjCheckNAddStates(11, 13);
                                break;
                            }
                            break;
                        case 2:
                            if ((9216 & l) != 0 && kind > 1) {
                                kind = 1;
                                break;
                            }
                        case 3:
                            if (this.curChar == '\n' && kind > 1) {
                                kind = 1;
                                break;
                            }
                        case 4:
                            if (this.curChar == '\r') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 3;
                                break;
                            }
                            break;
                        case 5:
                            if ((287948901175001088L & l) != 0) {
                                jjCheckNAddTwoStates(41, 42);
                            } else if (this.curChar == '/') {
                                jjAddStates(0, 1);
                            } else if (this.curChar == '\'') {
                                jjCheckNAddStates(2, 4);
                            } else if (this.curChar == '\"') {
                                jjCheckNAddStates(5, 7);
                            } else if (this.curChar == '$') {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                            } else if (this.curChar != '!') {
                                if (this.curChar != '%') {
                                    if (this.curChar == '<') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 25;
                                    } else if (this.curChar == '>') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 22;
                                    } else if (this.curChar == '=') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 15;
                                    } else if (this.curChar == '&') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 6;
                                    } else if (this.curChar == '#') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 0;
                                    }
                                } else if (kind > 46) {
                                    kind = 46;
                                }
                            } else if (kind > 48) {
                                kind = 48;
                            }
                            if ((287667426198290432L & l) == 0) {
                                if (this.curChar != '0') {
                                    if (this.curChar == '/') {
                                        if (kind > 47) {
                                            kind = 47;
                                            break;
                                        }
                                    } else if (this.curChar == '<') {
                                        if (kind > 43) {
                                            kind = 43;
                                            break;
                                        }
                                    } else if (this.curChar == '>') {
                                        if (kind > 41) {
                                            kind = 41;
                                            break;
                                        }
                                    } else if (this.curChar == '!') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 19;
                                        break;
                                    }
                                }
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddStates(8, 10);
                                break;
                            }
                            if (kind > 60) {
                                kind = 60;
                            }
                            jjCheckNAddTwoStates(39, 40);
                            break;
                            break;
                        case 6:
                            if (this.curChar == '&' && kind > 35) {
                                kind = 35;
                                break;
                            }
                        case 7:
                            if (this.curChar == '&') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 6;
                                break;
                            }
                            break;
                        case 15:
                            if (this.curChar == '=' && kind > 37) {
                                kind = 37;
                                break;
                            }
                        case 16:
                            if (this.curChar == '=') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 15;
                                break;
                            }
                            break;
                        case 17:
                        case 37:
                            if ((287948969894477824L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                        case 19:
                            if (this.curChar == '=' && kind > 38) {
                                kind = 38;
                                break;
                            }
                        case 20:
                            if (this.curChar == '!') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 19;
                                break;
                            }
                            break;
                        case 21:
                            if (this.curChar == '>' && kind > 41) {
                                kind = 41;
                                break;
                            }
                        case 22:
                            if (this.curChar == '=' && kind > 42) {
                                kind = 42;
                                break;
                            }
                        case 23:
                            if (this.curChar == '>') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 22;
                                break;
                            }
                            break;
                        case 24:
                            if (this.curChar == '<' && kind > 43) {
                                kind = 43;
                                break;
                            }
                        case 25:
                            if (this.curChar == '=' && kind > 44) {
                                kind = 44;
                                break;
                            }
                        case 26:
                            if (this.curChar == '<') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 25;
                                break;
                            }
                            break;
                        case 27:
                            if (this.curChar == '%' && kind > 46) {
                                kind = 46;
                                break;
                            }
                        case 31:
                            if (this.curChar == '/' && kind > 47) {
                                kind = 47;
                                break;
                            }
                        case 35:
                            if (this.curChar == '!' && kind > 48) {
                                kind = 48;
                                break;
                            }
                        case 36:
                            if (this.curChar == '$') {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                        case 38:
                            if ((287667426198290432L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(39, 40);
                                break;
                            }
                            break;
                        case 39:
                            if ((287948901175001088L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(39, 40);
                                break;
                            }
                            break;
                        case 41:
                            if ((287948901175001088L & l) != 0) {
                                jjCheckNAddTwoStates(41, 42);
                                break;
                            }
                            break;
                        case 42:
                            if (this.curChar == '.') {
                                jjCheckNAdd(43);
                                break;
                            }
                            break;
                        case 43:
                            if ((287948901175001088L & l) != 0) {
                                if (kind > 61) {
                                    kind = 61;
                                }
                                jjCheckNAddStates(14, 16);
                                break;
                            }
                            break;
                        case 45:
                            if ((43980465111040L & l) != 0) {
                                jjCheckNAdd(46);
                                break;
                            }
                            break;
                        case 46:
                            if ((287948901175001088L & l) != 0) {
                                if (kind > 61) {
                                    kind = 61;
                                }
                                jjCheckNAddTwoStates(46, 47);
                                break;
                            }
                            break;
                        case 48:
                            if (this.curChar == '\"') {
                                jjCheckNAddStates(5, 7);
                                break;
                            }
                            break;
                        case 49:
                            if ((-17179878401L & l) != 0) {
                                jjCheckNAddStates(5, 7);
                                break;
                            }
                            break;
                        case 51:
                            if ((-9217 & l) != 0) {
                                jjCheckNAddStates(5, 7);
                                break;
                            }
                            break;
                        case 52:
                            if (this.curChar == '\"' && kind > 62) {
                                kind = 62;
                                break;
                            }
                        case 53:
                            if (this.curChar == '\'') {
                                jjCheckNAddStates(2, 4);
                                break;
                            }
                            break;
                        case 54:
                            if ((-549755823105L & l) != 0) {
                                jjCheckNAddStates(2, 4);
                                break;
                            }
                            break;
                        case 56:
                            if ((-9217 & l) != 0) {
                                jjCheckNAddStates(2, 4);
                                break;
                            }
                            break;
                        case 57:
                            if (this.curChar == '\'' && kind > 62) {
                                kind = 62;
                                break;
                            }
                        case 58:
                            if (this.curChar == '0') {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddStates(8, 10);
                                break;
                            }
                            break;
                        case 59:
                            if ((71776119061217280L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(59, 40);
                                break;
                            }
                            break;
                        case 61:
                            if ((287948901175001088L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(61, 40);
                                break;
                            }
                            break;
                        case 62:
                            if (this.curChar == '/') {
                                jjAddStates(0, 1);
                                break;
                            }
                            break;
                        case 63:
                            if (this.curChar == '*') {
                                jjCheckNAddTwoStates(64, 65);
                                break;
                            }
                            break;
                        case 64:
                            if ((-4398046511105L & l) != 0) {
                                jjCheckNAddTwoStates(64, 65);
                                break;
                            }
                            break;
                        case 65:
                            if (this.curChar == '*') {
                                jjCheckNAddStates(17, 19);
                                break;
                            }
                            break;
                        case 66:
                            if ((-145135534866433L & l) != 0) {
                                jjCheckNAddTwoStates(67, 65);
                                break;
                            }
                            break;
                        case 67:
                            if ((-4398046511105L & l) != 0) {
                                jjCheckNAddTwoStates(67, 65);
                                break;
                            }
                            break;
                        case 68:
                            if (this.curChar == '/' && kind > 2) {
                                kind = 2;
                                break;
                            }
                        case 69:
                            if (this.curChar == '/') {
                                if (kind > 3) {
                                    kind = 3;
                                }
                                jjCheckNAddStates(20, 22);
                                break;
                            }
                            break;
                        case HwCustRemoteInputViewImpl.NUM_PER_GROUP /*70*/:
                            if ((-9217 & l) != 0) {
                                if (kind > 3) {
                                    kind = 3;
                                }
                                jjCheckNAddStates(20, 22);
                                break;
                            }
                            break;
                        case 71:
                            if ((9216 & l) != 0 && kind > 3) {
                                kind = 3;
                                break;
                            }
                        case 72:
                            if (this.curChar == '\n' && kind > 3) {
                                kind = 3;
                                break;
                            }
                        case 73:
                            if (this.curChar == '\r') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 72;
                                break;
                            }
                            break;
                        case 75:
                            if ((287948969894477824L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                    }
                    if (i != startsAt) {
                    }
                }
            } else if (this.curChar >= '') {
                int hiByte = this.curChar >> 8;
                int i1 = hiByte >> 6;
                long l1 = 1 << (hiByte & 63);
                int i22 = (this.curChar & 255) >> 6;
                long l2 = 1 << (this.curChar & 63);
                do {
                    i--;
                    switch (this.jjstateSet[i]) {
                        case 1:
                            if (jjCanMove_0(hiByte, i1, i22, l1, l2)) {
                                if (kind > 1) {
                                    kind = 1;
                                }
                                jjAddStates(11, 13);
                                continue;
                            } else {
                                continue;
                            }
                        case 49:
                        case 51:
                            if (jjCanMove_1(hiByte, i1, i22, l1, l2)) {
                                jjCheckNAddStates(5, 7);
                                continue;
                            } else {
                                continue;
                            }
                        case 54:
                        case 56:
                            if (jjCanMove_1(hiByte, i1, i22, l1, l2)) {
                                jjCheckNAddStates(2, 4);
                                continue;
                            } else {
                                continue;
                            }
                        case 64:
                            if (jjCanMove_0(hiByte, i1, i22, l1, l2)) {
                                jjCheckNAddTwoStates(64, 65);
                                continue;
                            } else {
                                continue;
                            }
                        case 66:
                        case 67:
                            if (jjCanMove_0(hiByte, i1, i22, l1, l2)) {
                                jjCheckNAddTwoStates(67, 65);
                                continue;
                            } else {
                                continue;
                            }
                        case HwCustRemoteInputViewImpl.NUM_PER_GROUP /*70*/:
                            if (jjCanMove_0(hiByte, i1, i22, l1, l2)) {
                                if (kind > 3) {
                                    kind = 3;
                                }
                                jjAddStates(20, 22);
                                continue;
                            } else {
                                continue;
                            }
                        default:
                            break;
                    }
                } while (i != startsAt);
            } else {
                l = 1 << (this.curChar & 63);
                while (true) {
                    i--;
                    switch (this.jjstateSet[i]) {
                        case 1:
                            if (kind > 1) {
                                kind = 1;
                            }
                            jjAddStates(11, 13);
                            break;
                        case 5:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                            } else if (this.curChar == '|') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 11;
                            }
                            if (this.curChar != 'l') {
                                if (this.curChar != 'g') {
                                    if (this.curChar != 'n') {
                                        if (this.curChar != 'd') {
                                            if (this.curChar != 'm') {
                                                if (this.curChar != 'e') {
                                                    if (this.curChar != 'o') {
                                                        if (this.curChar == 'a') {
                                                            iArr = this.jjstateSet;
                                                            i3 = this.jjnewStateCnt;
                                                            this.jjnewStateCnt = i3 + 1;
                                                            iArr[i3] = 9;
                                                            break;
                                                        }
                                                    }
                                                    iArr = this.jjstateSet;
                                                    i3 = this.jjnewStateCnt;
                                                    this.jjnewStateCnt = i3 + 1;
                                                    iArr[i3] = 13;
                                                    break;
                                                }
                                                iArr = this.jjstateSet;
                                                i3 = this.jjnewStateCnt;
                                                this.jjnewStateCnt = i3 + 1;
                                                iArr[i3] = 17;
                                                break;
                                            }
                                            iArr = this.jjstateSet;
                                            i3 = this.jjnewStateCnt;
                                            this.jjnewStateCnt = i3 + 1;
                                            iArr[i3] = 29;
                                            break;
                                        }
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 33;
                                        break;
                                    }
                                    jjAddStates(27, 28);
                                    break;
                                }
                                jjAddStates(25, 26);
                                break;
                            }
                            jjAddStates(23, 24);
                            break;
                            break;
                        case 8:
                            if (this.curChar == 'd' && kind > 35) {
                                kind = 35;
                                break;
                            }
                        case 9:
                            if (this.curChar == 'n') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 8;
                                break;
                            }
                            break;
                        case 10:
                            if (this.curChar == 'a') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 9;
                                break;
                            }
                            break;
                        case 11:
                            if (this.curChar == '|' && kind > 36) {
                                kind = 36;
                                break;
                            }
                        case 12:
                            if (this.curChar == '|') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 11;
                                break;
                            }
                            break;
                        case 13:
                            if (this.curChar == 'r' && kind > 36) {
                                kind = 36;
                                break;
                            }
                        case 14:
                            if (this.curChar == 'o') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 13;
                                break;
                            }
                            break;
                        case 17:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                            }
                            if (this.curChar == 'q' && kind > 37) {
                                kind = 37;
                                break;
                            }
                        case 18:
                            if (this.curChar == 'e') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 17;
                                break;
                            }
                            break;
                        case 28:
                            if (this.curChar == 'd' && kind > 46) {
                                kind = 46;
                                break;
                            }
                        case 29:
                            if (this.curChar == 'o') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 28;
                                break;
                            }
                            break;
                        case 30:
                            if (this.curChar == 'm') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 29;
                                break;
                            }
                            break;
                        case 32:
                            if (this.curChar == 'v' && kind > 47) {
                                kind = 47;
                                break;
                            }
                        case 33:
                            if (this.curChar == 'i') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 32;
                                break;
                            }
                            break;
                        case 34:
                            if (this.curChar == 'd') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 33;
                                break;
                            }
                            break;
                        case 36:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                        case 37:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                        case 40:
                            if ((18691697676544L & l) != 0 && kind > 60) {
                                kind = 60;
                                break;
                            }
                        case 44:
                            if ((137438953504L & l) != 0) {
                                jjAddStates(29, 30);
                                break;
                            }
                            break;
                        case 47:
                            if ((360777252948L & l) != 0 && kind > 61) {
                                kind = 61;
                                break;
                            }
                        case 49:
                            if ((-268435457 & l) != 0) {
                                jjCheckNAddStates(5, 7);
                                break;
                            }
                            break;
                        case 50:
                            if (this.curChar == '\\') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 51;
                                break;
                            }
                            break;
                        case 51:
                            jjCheckNAddStates(5, 7);
                            break;
                        case 54:
                            if ((-268435457 & l) != 0) {
                                jjCheckNAddStates(2, 4);
                                break;
                            }
                            break;
                        case 55:
                            if (this.curChar == '\\') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 56;
                                break;
                            }
                            break;
                        case 56:
                            jjCheckNAddStates(2, 4);
                            break;
                        case 60:
                            if ((72057594054705152L & l) != 0) {
                                jjCheckNAdd(61);
                                break;
                            }
                            break;
                        case 61:
                            if ((541165879422L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(61, 40);
                                break;
                            }
                            break;
                        case 64:
                            jjCheckNAddTwoStates(64, 65);
                            break;
                        case 66:
                        case 67:
                            jjCheckNAddTwoStates(67, 65);
                            break;
                        case HwCustRemoteInputViewImpl.NUM_PER_GROUP /*70*/:
                            if (kind > 3) {
                                kind = 3;
                            }
                            jjAddStates(20, 22);
                            break;
                        case 74:
                            if (this.curChar == 'n') {
                                jjAddStates(27, 28);
                                break;
                            }
                            break;
                        case 75:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                            }
                            if (this.curChar != 'o') {
                                if (this.curChar == 'e' && kind > 38) {
                                    kind = 38;
                                    break;
                                }
                            }
                            iArr = this.jjstateSet;
                            i3 = this.jjnewStateCnt;
                            this.jjnewStateCnt = i3 + 1;
                            iArr[i3] = 76;
                            break;
                        case 76:
                            if (this.curChar == 't' && kind > 48) {
                                kind = 48;
                                break;
                            }
                        case 77:
                            if (this.curChar == 'o') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 76;
                                break;
                            }
                            break;
                        case 78:
                            if (this.curChar == 'g') {
                                jjAddStates(25, 26);
                                break;
                            }
                            break;
                        case 79:
                            if (this.curChar == 't' && kind > 41) {
                                kind = 41;
                                break;
                            }
                        case 80:
                            if (this.curChar == 'e' && kind > 42) {
                                kind = 42;
                                break;
                            }
                        case 81:
                            if (this.curChar == 'l') {
                                jjAddStates(23, 24);
                                break;
                            }
                            break;
                        case 82:
                            if (this.curChar == 't' && kind > 43) {
                                kind = 43;
                                break;
                            }
                        case 83:
                            if (this.curChar == 'e' && kind > 44) {
                                kind = 44;
                                break;
                            }
                    }
                    if (i != startsAt) {
                    }
                }
            }
            if (kind != Integer.MAX_VALUE) {
                this.jjmatchedKind = kind;
                this.jjmatchedPos = curPos;
                kind = Integer.MAX_VALUE;
            }
            curPos++;
            i = this.jjnewStateCnt;
            this.jjnewStateCnt = startsAt;
            startsAt = 84 - startsAt;
            if (i == startsAt) {
                return curPos;
            }
            try {
                this.curChar = (char) this.input_stream.readChar();
            } catch (IOException e) {
                return curPos;
            }
        }
    }

    private final int jjStopStringLiteralDfa_1(int pos, long active0) {
        switch (pos) {
            case 0:
                if ((8043008 & active0) != 0) {
                    this.jjmatchedKind = 56;
                    return 37;
                } else if ((9007199254740992L & active0) != 0) {
                    return 6;
                } else {
                    if ((278528 & active0) != 0) {
                        this.jjmatchedKind = 56;
                        return 75;
                    } else if ((18014398509481984L & active0) != 0) {
                        return 11;
                    } else {
                        if ((1099511627776L & active0) != 0) {
                            this.jjmatchedKind = 48;
                            return 19;
                        } else if ((35734127902720L & active0) != 0) {
                            return 15;
                        } else {
                            if ((66560 & active0) == 0) {
                                return -1;
                            }
                            this.jjmatchedKind = 56;
                            return 17;
                        }
                    }
                }
            case 1:
                if ((16384 & active0) != 0) {
                    this.jjmatchedKind = 38;
                    this.jjmatchedPos = 1;
                    return 37;
                } else if ((4176896 & active0) == 0) {
                    return (4194816 & active0) != 0 ? 37 : -1;
                } else {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 1;
                    return 37;
                }
            case 2:
                if ((55296 & active0) != 0) {
                    return 37;
                }
                if ((4137984 & active0) == 0) {
                    return -1;
                }
                if (this.jjmatchedPos != 2) {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 2;
                }
                return 37;
            case 3:
                if ((3223552 & active0) == 0) {
                    return (918528 & active0) != 0 ? 37 : -1;
                } else {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 3;
                    return 37;
                }
            case 4:
                if ((2101248 & active0) == 0) {
                    return (1122304 & active0) != 0 ? 37 : -1;
                } else {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 4;
                    return 37;
                }
            case 5:
                if ((4096 & active0) == 0) {
                    return (2097152 & active0) != 0 ? 37 : -1;
                } else {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 5;
                    return 37;
                }
            default:
                return -1;
        }
    }

    private final int jjStartNfa_1(int pos, long active0) {
        return jjMoveNfa_1(jjStopStringLiteralDfa_1(pos, active0), pos + 1);
    }

    private int jjMoveStringLiteralDfa0_1() {
        switch (this.curChar) {
            case '!':
                return jjMoveStringLiteralDfa1_1(1099511627776L);
            case '&':
                return jjStartNfaWithStates_1(0, 53, 6);
            case '(':
                return jjStopAtPos(0, 23);
            case ')':
                return jjStopAtPos(0, 24);
            case '*':
                return jjStopAtPos(0, 51);
            case '+':
                return jjStopAtPos(0, 49);
            case ',':
                return jjStopAtPos(0, 31);
            case '-':
                return jjStopAtPos(0, 50);
            case '.':
                return jjStopAtPos(0, 32);
            case ':':
                return jjStopAtPos(0, 30);
            case ';':
                return jjStopAtPos(0, 29);
            case '=':
                this.jjmatchedKind = 45;
                return jjMoveStringLiteralDfa1_1(549755813888L);
            case '?':
                this.jjmatchedKind = 33;
                return jjMoveStringLiteralDfa1_1(17179869184L);
            case '[':
                return jjStopAtPos(0, 27);
            case ']':
                return jjStopAtPos(0, 28);
            case '^':
                return jjStopAtPos(0, 55);
            case 'e':
                return jjMoveStringLiteralDfa1_1(66560);
            case 'f':
                return jjMoveStringLiteralDfa1_1(1054720);
            case 'i':
                return jjMoveStringLiteralDfa1_1(4194816);
            case 'n':
                return jjMoveStringLiteralDfa1_1(278528);
            case 'r':
                return jjMoveStringLiteralDfa1_1(2097152);
            case 's':
                return jjMoveStringLiteralDfa1_1(131072);
            case 't':
                return jjMoveStringLiteralDfa1_1(524288);
            case 'v':
                return jjMoveStringLiteralDfa1_1(32768);
            case 'w':
                return jjMoveStringLiteralDfa1_1(8192);
            case '{':
                return jjStopAtPos(0, 25);
            case '|':
                return jjStartNfaWithStates_1(0, 54, 11);
            case '}':
                return jjStopAtPos(0, 26);
            case '~':
                return jjStopAtPos(0, 52);
            default:
                return jjMoveNfa_1(5, 0);
        }
    }

    private int jjMoveStringLiteralDfa1_1(long active0) {
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case ':':
                    if ((17179869184L & active0) != 0) {
                        return jjStopAtPos(1, 34);
                    }
                    break;
                case 'a':
                    return jjMoveStringLiteralDfa2_1(active0, 1081344);
                case 'e':
                    return jjMoveStringLiteralDfa2_1(active0, 2113536);
                case 'f':
                    if ((512 & active0) != 0) {
                        return jjStartNfaWithStates_1(1, 9, 37);
                    }
                    break;
                case 'h':
                    return jjMoveStringLiteralDfa2_1(active0, 8192);
                case 'i':
                    return jjMoveStringLiteralDfa2_1(active0, 131072);
                case 'l':
                    return jjMoveStringLiteralDfa2_1(active0, 1024);
                case 'm':
                    return jjMoveStringLiteralDfa2_1(active0, 65536);
                case 'n':
                    if ((4194304 & active0) != 0) {
                        return jjStartNfaWithStates_1(1, 22, 37);
                    }
                    break;
                case 'o':
                    return jjMoveStringLiteralDfa2_1(active0, 6144);
                case 'r':
                    return jjMoveStringLiteralDfa2_1(active0, 524288);
                case 'u':
                    return jjMoveStringLiteralDfa2_1(active0, 262144);
                case '~':
                    if ((549755813888L & active0) != 0) {
                        return jjStopAtPos(1, 39);
                    }
                    if ((1099511627776L & active0) != 0) {
                        return jjStopAtPos(1, 40);
                    }
                    break;
            }
            return jjStartNfa_1(0, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_1(0, active0);
            return 1;
        }
    }

    private int jjMoveStringLiteralDfa2_1(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_1(0, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'i':
                    return jjMoveStringLiteralDfa3_1(active0, 8192);
                case 'l':
                    return jjMoveStringLiteralDfa3_1(active0, 1310720);
                case 'p':
                    return jjMoveStringLiteralDfa3_1(active0, 65536);
                case 'r':
                    if ((2048 & active0) != 0) {
                        this.jjmatchedKind = 11;
                        this.jjmatchedPos = 2;
                    } else if ((32768 & active0) != 0) {
                        return jjStartNfaWithStates_1(2, 15, 37);
                    }
                    return jjMoveStringLiteralDfa3_1(active0, 4096);
                case 's':
                    return jjMoveStringLiteralDfa3_1(active0, 1024);
                case 't':
                    return jjMoveStringLiteralDfa3_1(active0, 2097152);
                case 'u':
                    return jjMoveStringLiteralDfa3_1(active0, 524288);
                case 'w':
                    if ((16384 & active0) != 0) {
                        return jjStartNfaWithStates_1(2, 14, 37);
                    }
                    break;
                case 'z':
                    return jjMoveStringLiteralDfa3_1(active0, 131072);
            }
            return jjStartNfa_1(1, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_1(1, active0);
            return 2;
        }
    }

    private int jjMoveStringLiteralDfa3_1(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_1(1, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'e':
                    if ((1024 & active0) != 0) {
                        return jjStartNfaWithStates_1(3, 10, 37);
                    }
                    if ((131072 & active0) != 0) {
                        return jjStartNfaWithStates_1(3, 17, 37);
                    }
                    if ((524288 & active0) != 0) {
                        return jjStartNfaWithStates_1(3, 19, 37);
                    }
                    return jjMoveStringLiteralDfa4_1(active0, 4096);
                case 'l':
                    if ((262144 & active0) != 0) {
                        return jjStartNfaWithStates_1(3, 18, 37);
                    }
                    return jjMoveStringLiteralDfa4_1(active0, 8192);
                case 's':
                    return jjMoveStringLiteralDfa4_1(active0, 1048576);
                case 't':
                    return jjMoveStringLiteralDfa4_1(active0, 65536);
                case 'u':
                    return jjMoveStringLiteralDfa4_1(active0, 2097152);
                default:
                    return jjStartNfa_1(2, active0);
            }
        } catch (IOException e) {
            jjStopStringLiteralDfa_1(2, active0);
            return 3;
        }
    }

    private int jjMoveStringLiteralDfa4_1(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_1(2, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'a':
                    return jjMoveStringLiteralDfa5_1(active0, 4096);
                case 'e':
                    if ((8192 & active0) != 0) {
                        return jjStartNfaWithStates_1(4, 13, 37);
                    }
                    if ((1048576 & active0) != 0) {
                        return jjStartNfaWithStates_1(4, 20, 37);
                    }
                    break;
                case 'r':
                    return jjMoveStringLiteralDfa5_1(active0, 2097152);
                case 'y':
                    if ((65536 & active0) != 0) {
                        return jjStartNfaWithStates_1(4, 16, 37);
                    }
                    break;
            }
            return jjStartNfa_1(3, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_1(3, active0);
            return 4;
        }
    }

    private int jjMoveStringLiteralDfa5_1(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_1(3, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'c':
                    return jjMoveStringLiteralDfa6_1(active0, 4096);
                case 'n':
                    if ((2097152 & active0) != 0) {
                        return jjStartNfaWithStates_1(5, 21, 37);
                    }
                    break;
            }
            return jjStartNfa_1(4, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_1(4, active0);
            return 5;
        }
    }

    private int jjMoveStringLiteralDfa6_1(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_1(4, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'h':
                    if ((4096 & active0) != 0) {
                        return jjStartNfaWithStates_1(6, 12, 37);
                    }
                    break;
            }
            return jjStartNfa_1(5, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_1(5, active0);
            return 6;
        }
    }

    private int jjStartNfaWithStates_1(int pos, int kind, int state) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = pos;
        try {
            this.curChar = (char) this.input_stream.readChar();
            return jjMoveNfa_1(state, pos + 1);
        } catch (IOException e) {
            return pos + 1;
        }
    }

    private int jjMoveNfa_1(int startState, int curPos) {
        int startsAt = 0;
        this.jjnewStateCnt = 84;
        int i = 1;
        this.jjstateSet[0] = startState;
        int kind = Integer.MAX_VALUE;
        while (true) {
            int i2 = this.jjround + 1;
            this.jjround = i2;
            if (i2 == Integer.MAX_VALUE) {
                ReInitRounds();
            }
            long l;
            int[] iArr;
            int i3;
            if (this.curChar < '@') {
                l = 1 << this.curChar;
                while (true) {
                    i--;
                    switch (this.jjstateSet[i]) {
                        case 0:
                            if (this.curChar == '#') {
                                if (kind > 1) {
                                    kind = 1;
                                }
                                jjCheckNAddStates(11, 13);
                                break;
                            }
                            break;
                        case 1:
                            if ((-9217 & l) != 0) {
                                if (kind > 1) {
                                    kind = 1;
                                }
                                jjCheckNAddStates(11, 13);
                                break;
                            }
                            break;
                        case 2:
                            if ((9216 & l) != 0 && kind > 1) {
                                kind = 1;
                                break;
                            }
                        case 3:
                            if (this.curChar == '\n' && kind > 1) {
                                kind = 1;
                                break;
                            }
                        case 4:
                            if (this.curChar == '\r') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 3;
                                break;
                            }
                            break;
                        case 5:
                            if ((287948901175001088L & l) != 0) {
                                jjCheckNAddTwoStates(41, 42);
                            } else if (this.curChar == '/') {
                                jjAddStates(0, 1);
                            } else if (this.curChar == '\'') {
                                jjCheckNAddStates(2, 4);
                            } else if (this.curChar == '\"') {
                                jjCheckNAddStates(5, 7);
                            } else if (this.curChar == '$') {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                            } else if (this.curChar != '!') {
                                if (this.curChar != '%') {
                                    if (this.curChar == '<') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 25;
                                    } else if (this.curChar == '>') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 22;
                                    } else if (this.curChar == '=') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 15;
                                    } else if (this.curChar == '&') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 6;
                                    } else if (this.curChar == '#') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 0;
                                    }
                                } else if (kind > 46) {
                                    kind = 46;
                                }
                            } else if (kind > 48) {
                                kind = 48;
                            }
                            if ((287667426198290432L & l) == 0) {
                                if (this.curChar != '0') {
                                    if (this.curChar == '/') {
                                        if (kind > 47) {
                                            kind = 47;
                                            break;
                                        }
                                    } else if (this.curChar == '<') {
                                        if (kind > 43) {
                                            kind = 43;
                                            break;
                                        }
                                    } else if (this.curChar == '>') {
                                        if (kind > 41) {
                                            kind = 41;
                                            break;
                                        }
                                    } else if (this.curChar == '!') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 19;
                                        break;
                                    }
                                }
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddStates(8, 10);
                                break;
                            }
                            if (kind > 60) {
                                kind = 60;
                            }
                            jjCheckNAddTwoStates(39, 40);
                            break;
                            break;
                        case 6:
                            if (this.curChar == '&' && kind > 35) {
                                kind = 35;
                                break;
                            }
                        case 7:
                            if (this.curChar == '&') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 6;
                                break;
                            }
                            break;
                        case 15:
                            if (this.curChar == '=' && kind > 37) {
                                kind = 37;
                                break;
                            }
                        case 16:
                            if (this.curChar == '=') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 15;
                                break;
                            }
                            break;
                        case 17:
                        case 37:
                            if ((287948969894477824L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                        case 19:
                            if (this.curChar == '=' && kind > 38) {
                                kind = 38;
                                break;
                            }
                        case 20:
                            if (this.curChar == '!') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 19;
                                break;
                            }
                            break;
                        case 21:
                            if (this.curChar == '>' && kind > 41) {
                                kind = 41;
                                break;
                            }
                        case 22:
                            if (this.curChar == '=' && kind > 42) {
                                kind = 42;
                                break;
                            }
                        case 23:
                            if (this.curChar == '>') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 22;
                                break;
                            }
                            break;
                        case 24:
                            if (this.curChar == '<' && kind > 43) {
                                kind = 43;
                                break;
                            }
                        case 25:
                            if (this.curChar == '=' && kind > 44) {
                                kind = 44;
                                break;
                            }
                        case 26:
                            if (this.curChar == '<') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 25;
                                break;
                            }
                            break;
                        case 27:
                            if (this.curChar == '%' && kind > 46) {
                                kind = 46;
                                break;
                            }
                        case 31:
                            if (this.curChar == '/' && kind > 47) {
                                kind = 47;
                                break;
                            }
                        case 35:
                            if (this.curChar == '!' && kind > 48) {
                                kind = 48;
                                break;
                            }
                        case 36:
                            if (this.curChar == '$') {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                        case 38:
                            if ((287667426198290432L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(39, 40);
                                break;
                            }
                            break;
                        case 39:
                            if ((287948901175001088L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(39, 40);
                                break;
                            }
                            break;
                        case 41:
                            if ((287948901175001088L & l) != 0) {
                                jjCheckNAddTwoStates(41, 42);
                                break;
                            }
                            break;
                        case 42:
                            if (this.curChar == '.') {
                                jjCheckNAdd(43);
                                break;
                            }
                            break;
                        case 43:
                            if ((287948901175001088L & l) != 0) {
                                if (kind > 61) {
                                    kind = 61;
                                }
                                jjCheckNAddStates(14, 16);
                                break;
                            }
                            break;
                        case 45:
                            if ((43980465111040L & l) != 0) {
                                jjCheckNAdd(46);
                                break;
                            }
                            break;
                        case 46:
                            if ((287948901175001088L & l) != 0) {
                                if (kind > 61) {
                                    kind = 61;
                                }
                                jjCheckNAddTwoStates(46, 47);
                                break;
                            }
                            break;
                        case 48:
                            if (this.curChar == '\"') {
                                jjCheckNAddStates(5, 7);
                                break;
                            }
                            break;
                        case 49:
                            if ((-17179878401L & l) != 0) {
                                jjCheckNAddStates(5, 7);
                                break;
                            }
                            break;
                        case 51:
                            if ((-9217 & l) != 0) {
                                jjCheckNAddStates(5, 7);
                                break;
                            }
                            break;
                        case 52:
                            if (this.curChar == '\"' && kind > 62) {
                                kind = 62;
                                break;
                            }
                        case 53:
                            if (this.curChar == '\'') {
                                jjCheckNAddStates(2, 4);
                                break;
                            }
                            break;
                        case 54:
                            if ((-549755823105L & l) != 0) {
                                jjCheckNAddStates(2, 4);
                                break;
                            }
                            break;
                        case 56:
                            if ((-9217 & l) != 0) {
                                jjCheckNAddStates(2, 4);
                                break;
                            }
                            break;
                        case 57:
                            if (this.curChar == '\'' && kind > 62) {
                                kind = 62;
                                break;
                            }
                        case 58:
                            if (this.curChar == '0') {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddStates(8, 10);
                                break;
                            }
                            break;
                        case 59:
                            if ((71776119061217280L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(59, 40);
                                break;
                            }
                            break;
                        case 61:
                            if ((287948901175001088L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(61, 40);
                                break;
                            }
                            break;
                        case 62:
                            if (this.curChar == '/') {
                                jjAddStates(0, 1);
                                break;
                            }
                            break;
                        case 63:
                            if (this.curChar == '*') {
                                jjCheckNAddTwoStates(64, 65);
                                break;
                            }
                            break;
                        case 64:
                            if ((-4398046511105L & l) != 0) {
                                jjCheckNAddTwoStates(64, 65);
                                break;
                            }
                            break;
                        case 65:
                            if (this.curChar == '*') {
                                jjCheckNAddStates(17, 19);
                                break;
                            }
                            break;
                        case 66:
                            if ((-145135534866433L & l) != 0) {
                                jjCheckNAddTwoStates(67, 65);
                                break;
                            }
                            break;
                        case 67:
                            if ((-4398046511105L & l) != 0) {
                                jjCheckNAddTwoStates(67, 65);
                                break;
                            }
                            break;
                        case 68:
                            if (this.curChar == '/' && kind > 2) {
                                kind = 2;
                                break;
                            }
                        case 69:
                            if (this.curChar == '/') {
                                if (kind > 3) {
                                    kind = 3;
                                }
                                jjCheckNAddStates(20, 22);
                                break;
                            }
                            break;
                        case HwCustRemoteInputViewImpl.NUM_PER_GROUP /*70*/:
                            if ((-9217 & l) != 0) {
                                if (kind > 3) {
                                    kind = 3;
                                }
                                jjCheckNAddStates(20, 22);
                                break;
                            }
                            break;
                        case 71:
                            if ((9216 & l) != 0 && kind > 3) {
                                kind = 3;
                                break;
                            }
                        case 72:
                            if (this.curChar == '\n' && kind > 3) {
                                kind = 3;
                                break;
                            }
                        case 73:
                            if (this.curChar == '\r') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 72;
                                break;
                            }
                            break;
                        case 75:
                            if ((287948969894477824L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                    }
                    if (i != startsAt) {
                    }
                }
            } else if (this.curChar >= '') {
                int hiByte = this.curChar >> 8;
                int i1 = hiByte >> 6;
                long l1 = 1 << (hiByte & 63);
                int i22 = (this.curChar & 255) >> 6;
                long l2 = 1 << (this.curChar & 63);
                do {
                    i--;
                    switch (this.jjstateSet[i]) {
                        case 1:
                            if (jjCanMove_0(hiByte, i1, i22, l1, l2)) {
                                if (kind > 1) {
                                    kind = 1;
                                }
                                jjAddStates(11, 13);
                                continue;
                            } else {
                                continue;
                            }
                        case 49:
                        case 51:
                            if (jjCanMove_1(hiByte, i1, i22, l1, l2)) {
                                jjCheckNAddStates(5, 7);
                                continue;
                            } else {
                                continue;
                            }
                        case 54:
                        case 56:
                            if (jjCanMove_1(hiByte, i1, i22, l1, l2)) {
                                jjCheckNAddStates(2, 4);
                                continue;
                            } else {
                                continue;
                            }
                        case 64:
                            if (jjCanMove_0(hiByte, i1, i22, l1, l2)) {
                                jjCheckNAddTwoStates(64, 65);
                                continue;
                            } else {
                                continue;
                            }
                        case 66:
                        case 67:
                            if (jjCanMove_0(hiByte, i1, i22, l1, l2)) {
                                jjCheckNAddTwoStates(67, 65);
                                continue;
                            } else {
                                continue;
                            }
                        case HwCustRemoteInputViewImpl.NUM_PER_GROUP /*70*/:
                            if (jjCanMove_0(hiByte, i1, i22, l1, l2)) {
                                if (kind > 3) {
                                    kind = 3;
                                }
                                jjAddStates(20, 22);
                                continue;
                            } else {
                                continue;
                            }
                        default:
                            break;
                    }
                } while (i != startsAt);
            } else {
                l = 1 << (this.curChar & 63);
                while (true) {
                    i--;
                    switch (this.jjstateSet[i]) {
                        case 1:
                            if (kind > 1) {
                                kind = 1;
                            }
                            jjAddStates(11, 13);
                            break;
                        case 5:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                            } else if (this.curChar == '|') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 11;
                            }
                            if (this.curChar != 'l') {
                                if (this.curChar != 'g') {
                                    if (this.curChar != 'n') {
                                        if (this.curChar != 'd') {
                                            if (this.curChar != 'm') {
                                                if (this.curChar != 'e') {
                                                    if (this.curChar != 'o') {
                                                        if (this.curChar == 'a') {
                                                            iArr = this.jjstateSet;
                                                            i3 = this.jjnewStateCnt;
                                                            this.jjnewStateCnt = i3 + 1;
                                                            iArr[i3] = 9;
                                                            break;
                                                        }
                                                    }
                                                    iArr = this.jjstateSet;
                                                    i3 = this.jjnewStateCnt;
                                                    this.jjnewStateCnt = i3 + 1;
                                                    iArr[i3] = 13;
                                                    break;
                                                }
                                                iArr = this.jjstateSet;
                                                i3 = this.jjnewStateCnt;
                                                this.jjnewStateCnt = i3 + 1;
                                                iArr[i3] = 17;
                                                break;
                                            }
                                            iArr = this.jjstateSet;
                                            i3 = this.jjnewStateCnt;
                                            this.jjnewStateCnt = i3 + 1;
                                            iArr[i3] = 29;
                                            break;
                                        }
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 33;
                                        break;
                                    }
                                    jjAddStates(27, 28);
                                    break;
                                }
                                jjAddStates(25, 26);
                                break;
                            }
                            jjAddStates(23, 24);
                            break;
                            break;
                        case 8:
                            if (this.curChar == 'd' && kind > 35) {
                                kind = 35;
                                break;
                            }
                        case 9:
                            if (this.curChar == 'n') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 8;
                                break;
                            }
                            break;
                        case 10:
                            if (this.curChar == 'a') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 9;
                                break;
                            }
                            break;
                        case 11:
                            if (this.curChar == '|' && kind > 36) {
                                kind = 36;
                                break;
                            }
                        case 12:
                            if (this.curChar == '|') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 11;
                                break;
                            }
                            break;
                        case 13:
                            if (this.curChar == 'r' && kind > 36) {
                                kind = 36;
                                break;
                            }
                        case 14:
                            if (this.curChar == 'o') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 13;
                                break;
                            }
                            break;
                        case 17:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                            }
                            if (this.curChar == 'q' && kind > 37) {
                                kind = 37;
                                break;
                            }
                        case 18:
                            if (this.curChar == 'e') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 17;
                                break;
                            }
                            break;
                        case 28:
                            if (this.curChar == 'd' && kind > 46) {
                                kind = 46;
                                break;
                            }
                        case 29:
                            if (this.curChar == 'o') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 28;
                                break;
                            }
                            break;
                        case 30:
                            if (this.curChar == 'm') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 29;
                                break;
                            }
                            break;
                        case 32:
                            if (this.curChar == 'v' && kind > 47) {
                                kind = 47;
                                break;
                            }
                        case 33:
                            if (this.curChar == 'i') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 32;
                                break;
                            }
                            break;
                        case 34:
                            if (this.curChar == 'd') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 33;
                                break;
                            }
                            break;
                        case 36:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                        case 37:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                        case 40:
                            if ((18691697676544L & l) != 0 && kind > 60) {
                                kind = 60;
                                break;
                            }
                        case 44:
                            if ((137438953504L & l) != 0) {
                                jjAddStates(29, 30);
                                break;
                            }
                            break;
                        case 47:
                            if ((360777252948L & l) != 0 && kind > 61) {
                                kind = 61;
                                break;
                            }
                        case 49:
                            if ((-268435457 & l) != 0) {
                                jjCheckNAddStates(5, 7);
                                break;
                            }
                            break;
                        case 50:
                            if (this.curChar == '\\') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 51;
                                break;
                            }
                            break;
                        case 51:
                            jjCheckNAddStates(5, 7);
                            break;
                        case 54:
                            if ((-268435457 & l) != 0) {
                                jjCheckNAddStates(2, 4);
                                break;
                            }
                            break;
                        case 55:
                            if (this.curChar == '\\') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 56;
                                break;
                            }
                            break;
                        case 56:
                            jjCheckNAddStates(2, 4);
                            break;
                        case 60:
                            if ((72057594054705152L & l) != 0) {
                                jjCheckNAdd(61);
                                break;
                            }
                            break;
                        case 61:
                            if ((541165879422L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(61, 40);
                                break;
                            }
                            break;
                        case 64:
                            jjCheckNAddTwoStates(64, 65);
                            break;
                        case 66:
                        case 67:
                            jjCheckNAddTwoStates(67, 65);
                            break;
                        case HwCustRemoteInputViewImpl.NUM_PER_GROUP /*70*/:
                            if (kind > 3) {
                                kind = 3;
                            }
                            jjAddStates(20, 22);
                            break;
                        case 74:
                            if (this.curChar == 'n') {
                                jjAddStates(27, 28);
                                break;
                            }
                            break;
                        case 75:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                            }
                            if (this.curChar != 'o') {
                                if (this.curChar == 'e' && kind > 38) {
                                    kind = 38;
                                    break;
                                }
                            }
                            iArr = this.jjstateSet;
                            i3 = this.jjnewStateCnt;
                            this.jjnewStateCnt = i3 + 1;
                            iArr[i3] = 76;
                            break;
                        case 76:
                            if (this.curChar == 't' && kind > 48) {
                                kind = 48;
                                break;
                            }
                        case 77:
                            if (this.curChar == 'o') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 76;
                                break;
                            }
                            break;
                        case 78:
                            if (this.curChar == 'g') {
                                jjAddStates(25, 26);
                                break;
                            }
                            break;
                        case 79:
                            if (this.curChar == 't' && kind > 41) {
                                kind = 41;
                                break;
                            }
                        case 80:
                            if (this.curChar == 'e' && kind > 42) {
                                kind = 42;
                                break;
                            }
                        case 81:
                            if (this.curChar == 'l') {
                                jjAddStates(23, 24);
                                break;
                            }
                            break;
                        case 82:
                            if (this.curChar == 't' && kind > 43) {
                                kind = 43;
                                break;
                            }
                        case 83:
                            if (this.curChar == 'e' && kind > 44) {
                                kind = 44;
                                break;
                            }
                    }
                    if (i != startsAt) {
                    }
                }
            }
            if (kind != Integer.MAX_VALUE) {
                this.jjmatchedKind = kind;
                this.jjmatchedPos = curPos;
                kind = Integer.MAX_VALUE;
            }
            curPos++;
            i = this.jjnewStateCnt;
            this.jjnewStateCnt = startsAt;
            startsAt = 84 - startsAt;
            if (i == startsAt) {
                return curPos;
            }
            try {
                this.curChar = (char) this.input_stream.readChar();
            } catch (IOException e) {
                return curPos;
            }
        }
    }

    private final int jjStopStringLiteralDfa_0(int pos, long active0) {
        switch (pos) {
            case 0:
                if ((3848704 & active0) != 0) {
                    this.jjmatchedKind = 56;
                    return 37;
                } else if ((9007199254740992L & active0) != 0) {
                    return 6;
                } else {
                    if ((278528 & active0) != 0) {
                        this.jjmatchedKind = 56;
                        return 77;
                    } else if ((18014398509481984L & active0) != 0) {
                        return 11;
                    } else {
                        if ((1099511627776L & active0) != 0) {
                            this.jjmatchedKind = 48;
                            return 19;
                        } else if ((35734127902720L & active0) != 0) {
                            return 15;
                        } else {
                            if ((66560 & active0) == 0) {
                                return -1;
                            }
                            this.jjmatchedKind = 56;
                            return 17;
                        }
                    }
                }
            case 1:
                if ((16384 & active0) != 0) {
                    this.jjmatchedKind = 38;
                    this.jjmatchedPos = 1;
                    return 37;
                } else if ((4176896 & active0) == 0) {
                    return (512 & active0) != 0 ? 37 : -1;
                } else {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 1;
                    return 37;
                }
            case 2:
                if ((55296 & active0) != 0) {
                    return 37;
                }
                if ((4137984 & active0) == 0) {
                    return -1;
                }
                if (this.jjmatchedPos != 2) {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 2;
                }
                return 37;
            case 3:
                if ((3223552 & active0) == 0) {
                    return (918528 & active0) != 0 ? 37 : -1;
                } else {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 3;
                    return 37;
                }
            case 4:
                if ((2101248 & active0) == 0) {
                    return (1122304 & active0) != 0 ? 37 : -1;
                } else {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 4;
                    return 37;
                }
            case 5:
                if ((4096 & active0) == 0) {
                    return (2097152 & active0) != 0 ? 37 : -1;
                } else {
                    this.jjmatchedKind = 56;
                    this.jjmatchedPos = 5;
                    return 37;
                }
            default:
                return -1;
        }
    }

    private final int jjStartNfa_0(int pos, long active0) {
        return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
    }

    private int jjMoveStringLiteralDfa0_0() {
        switch (this.curChar) {
            case '!':
                return jjMoveStringLiteralDfa1_0(1099511627776L);
            case '&':
                return jjStartNfaWithStates_0(0, 53, 6);
            case '(':
                return jjStopAtPos(0, 23);
            case ')':
                return jjStopAtPos(0, 24);
            case '*':
                return jjStopAtPos(0, 51);
            case '+':
                return jjStopAtPos(0, 49);
            case ',':
                return jjStopAtPos(0, 31);
            case '-':
                return jjStopAtPos(0, 50);
            case '.':
                return jjStopAtPos(0, 32);
            case ':':
                return jjStopAtPos(0, 30);
            case ';':
                return jjStopAtPos(0, 29);
            case '=':
                this.jjmatchedKind = 45;
                return jjMoveStringLiteralDfa1_0(549755813888L);
            case '?':
                this.jjmatchedKind = 33;
                return jjMoveStringLiteralDfa1_0(17179869184L);
            case '[':
                return jjStopAtPos(0, 27);
            case ']':
                return jjStopAtPos(0, 28);
            case '^':
                return jjStopAtPos(0, 55);
            case 'e':
                return jjMoveStringLiteralDfa1_0(66560);
            case 'f':
                return jjMoveStringLiteralDfa1_0(1054720);
            case 'i':
                return jjMoveStringLiteralDfa1_0(512);
            case 'n':
                return jjMoveStringLiteralDfa1_0(278528);
            case 'r':
                return jjMoveStringLiteralDfa1_0(2097152);
            case 's':
                return jjMoveStringLiteralDfa1_0(131072);
            case 't':
                return jjMoveStringLiteralDfa1_0(524288);
            case 'v':
                return jjMoveStringLiteralDfa1_0(32768);
            case 'w':
                return jjMoveStringLiteralDfa1_0(8192);
            case '{':
                return jjStopAtPos(0, 25);
            case '|':
                return jjStartNfaWithStates_0(0, 54, 11);
            case '}':
                return jjStopAtPos(0, 26);
            case '~':
                return jjStopAtPos(0, 52);
            default:
                return jjMoveNfa_0(5, 0);
        }
    }

    private int jjMoveStringLiteralDfa1_0(long active0) {
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case ':':
                    if ((17179869184L & active0) != 0) {
                        return jjStopAtPos(1, 34);
                    }
                    break;
                case 'a':
                    return jjMoveStringLiteralDfa2_0(active0, 1081344);
                case 'e':
                    return jjMoveStringLiteralDfa2_0(active0, 2113536);
                case 'f':
                    if ((512 & active0) != 0) {
                        return jjStartNfaWithStates_0(1, 9, 37);
                    }
                    break;
                case 'h':
                    return jjMoveStringLiteralDfa2_0(active0, 8192);
                case 'i':
                    return jjMoveStringLiteralDfa2_0(active0, 131072);
                case 'l':
                    return jjMoveStringLiteralDfa2_0(active0, 1024);
                case 'm':
                    return jjMoveStringLiteralDfa2_0(active0, 65536);
                case 'o':
                    return jjMoveStringLiteralDfa2_0(active0, 6144);
                case 'r':
                    return jjMoveStringLiteralDfa2_0(active0, 524288);
                case 'u':
                    return jjMoveStringLiteralDfa2_0(active0, 262144);
                case '~':
                    if ((549755813888L & active0) != 0) {
                        return jjStopAtPos(1, 39);
                    }
                    if ((1099511627776L & active0) != 0) {
                        return jjStopAtPos(1, 40);
                    }
                    break;
            }
            return jjStartNfa_0(0, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_0(0, active0);
            return 1;
        }
    }

    private int jjMoveStringLiteralDfa2_0(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_0(0, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'i':
                    return jjMoveStringLiteralDfa3_0(active0, 8192);
                case 'l':
                    return jjMoveStringLiteralDfa3_0(active0, 1310720);
                case 'p':
                    return jjMoveStringLiteralDfa3_0(active0, 65536);
                case 'r':
                    if ((2048 & active0) != 0) {
                        this.jjmatchedKind = 11;
                        this.jjmatchedPos = 2;
                    } else if ((32768 & active0) != 0) {
                        return jjStartNfaWithStates_0(2, 15, 37);
                    }
                    return jjMoveStringLiteralDfa3_0(active0, 4096);
                case 's':
                    return jjMoveStringLiteralDfa3_0(active0, 1024);
                case 't':
                    return jjMoveStringLiteralDfa3_0(active0, 2097152);
                case 'u':
                    return jjMoveStringLiteralDfa3_0(active0, 524288);
                case 'w':
                    if ((16384 & active0) != 0) {
                        return jjStartNfaWithStates_0(2, 14, 37);
                    }
                    break;
                case 'z':
                    return jjMoveStringLiteralDfa3_0(active0, 131072);
            }
            return jjStartNfa_0(1, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_0(1, active0);
            return 2;
        }
    }

    private int jjMoveStringLiteralDfa3_0(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_0(1, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'e':
                    if ((1024 & active0) != 0) {
                        return jjStartNfaWithStates_0(3, 10, 37);
                    }
                    if ((131072 & active0) != 0) {
                        return jjStartNfaWithStates_0(3, 17, 37);
                    }
                    if ((524288 & active0) != 0) {
                        return jjStartNfaWithStates_0(3, 19, 37);
                    }
                    return jjMoveStringLiteralDfa4_0(active0, 4096);
                case 'l':
                    if ((262144 & active0) != 0) {
                        return jjStartNfaWithStates_0(3, 18, 37);
                    }
                    return jjMoveStringLiteralDfa4_0(active0, 8192);
                case 's':
                    return jjMoveStringLiteralDfa4_0(active0, 1048576);
                case 't':
                    return jjMoveStringLiteralDfa4_0(active0, 65536);
                case 'u':
                    return jjMoveStringLiteralDfa4_0(active0, 2097152);
                default:
                    return jjStartNfa_0(2, active0);
            }
        } catch (IOException e) {
            jjStopStringLiteralDfa_0(2, active0);
            return 3;
        }
    }

    private int jjMoveStringLiteralDfa4_0(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_0(2, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'a':
                    return jjMoveStringLiteralDfa5_0(active0, 4096);
                case 'e':
                    if ((8192 & active0) != 0) {
                        return jjStartNfaWithStates_0(4, 13, 37);
                    }
                    if ((1048576 & active0) != 0) {
                        return jjStartNfaWithStates_0(4, 20, 37);
                    }
                    break;
                case 'r':
                    return jjMoveStringLiteralDfa5_0(active0, 2097152);
                case 'y':
                    if ((65536 & active0) != 0) {
                        return jjStartNfaWithStates_0(4, 16, 37);
                    }
                    break;
            }
            return jjStartNfa_0(3, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_0(3, active0);
            return 4;
        }
    }

    private int jjMoveStringLiteralDfa5_0(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_0(3, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'c':
                    return jjMoveStringLiteralDfa6_0(active0, 4096);
                case 'n':
                    if ((2097152 & active0) != 0) {
                        return jjStartNfaWithStates_0(5, 21, 37);
                    }
                    break;
            }
            return jjStartNfa_0(4, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_0(4, active0);
            return 5;
        }
    }

    private int jjMoveStringLiteralDfa6_0(long old0, long active0) {
        active0 &= old0;
        if (active0 == 0) {
            return jjStartNfa_0(4, old0);
        }
        try {
            this.curChar = (char) this.input_stream.readChar();
            switch (this.curChar) {
                case 'h':
                    if ((4096 & active0) != 0) {
                        return jjStartNfaWithStates_0(6, 12, 37);
                    }
                    break;
            }
            return jjStartNfa_0(5, active0);
        } catch (IOException e) {
            jjStopStringLiteralDfa_0(5, active0);
            return 6;
        }
    }

    private int jjStartNfaWithStates_0(int pos, int kind, int state) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos = pos;
        try {
            this.curChar = (char) this.input_stream.readChar();
            return jjMoveNfa_0(state, pos + 1);
        } catch (IOException e) {
            return pos + 1;
        }
    }

    private int jjMoveNfa_0(int startState, int curPos) {
        int startsAt = 0;
        this.jjnewStateCnt = 86;
        int i = 1;
        this.jjstateSet[0] = startState;
        int kind = Integer.MAX_VALUE;
        while (true) {
            int i2 = this.jjround + 1;
            this.jjround = i2;
            if (i2 == Integer.MAX_VALUE) {
                ReInitRounds();
            }
            long l;
            int[] iArr;
            int i3;
            if (this.curChar < '@') {
                l = 1 << this.curChar;
                while (true) {
                    i--;
                    switch (this.jjstateSet[i]) {
                        case 0:
                            if (this.curChar == '#') {
                                if (kind > 1) {
                                    kind = 1;
                                }
                                jjCheckNAddStates(11, 13);
                                break;
                            }
                            break;
                        case 1:
                            if ((-9217 & l) != 0) {
                                if (kind > 1) {
                                    kind = 1;
                                }
                                jjCheckNAddStates(11, 13);
                                break;
                            }
                            break;
                        case 2:
                            if ((9216 & l) != 0 && kind > 1) {
                                kind = 1;
                                break;
                            }
                        case 3:
                            if (this.curChar == '\n' && kind > 1) {
                                kind = 1;
                                break;
                            }
                        case 4:
                            if (this.curChar == '\r') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 3;
                                break;
                            }
                            break;
                        case 5:
                            if ((287948901175001088L & l) != 0) {
                                jjCheckNAddTwoStates(43, 44);
                            } else if (this.curChar == '/') {
                                jjAddStates(31, 32);
                            } else if (this.curChar == '\'') {
                                jjCheckNAddStates(33, 35);
                            } else if (this.curChar == '\"') {
                                jjCheckNAddStates(36, 38);
                            } else if (this.curChar == '#') {
                                jjCheckNAdd(39);
                            } else if (this.curChar == '$') {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                            } else if (this.curChar != '!') {
                                if (this.curChar != '%') {
                                    if (this.curChar == '<') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 25;
                                    } else if (this.curChar == '>') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 22;
                                    } else if (this.curChar == '=') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 15;
                                    } else if (this.curChar == '&') {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 6;
                                    }
                                } else if (kind > 46) {
                                    kind = 46;
                                }
                            } else if (kind > 48) {
                                kind = 48;
                            }
                            if ((287667426198290432L & l) == 0) {
                                if (this.curChar != '0') {
                                    if (this.curChar == '/') {
                                        if (kind > 47) {
                                            kind = 47;
                                            break;
                                        }
                                    } else if (this.curChar == '<') {
                                        if (kind > 43) {
                                            kind = 43;
                                            break;
                                        }
                                    } else if (this.curChar == '>') {
                                        if (kind > 41) {
                                            kind = 41;
                                            break;
                                        }
                                    } else if (this.curChar != '!') {
                                        if (this.curChar == '#') {
                                            iArr = this.jjstateSet;
                                            i3 = this.jjnewStateCnt;
                                            this.jjnewStateCnt = i3 + 1;
                                            iArr[i3] = 0;
                                            break;
                                        }
                                    } else {
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 19;
                                        break;
                                    }
                                }
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddStates(39, 41);
                                break;
                            }
                            if (kind > 60) {
                                kind = 60;
                            }
                            jjCheckNAddTwoStates(41, 42);
                            break;
                            break;
                        case 6:
                            if (this.curChar == '&' && kind > 35) {
                                kind = 35;
                                break;
                            }
                        case 7:
                            if (this.curChar == '&') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 6;
                                break;
                            }
                            break;
                        case 15:
                            if (this.curChar == '=' && kind > 37) {
                                kind = 37;
                                break;
                            }
                        case 16:
                            if (this.curChar == '=') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 15;
                                break;
                            }
                            break;
                        case 17:
                        case 37:
                            if ((287948969894477824L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                        case 19:
                            if (this.curChar == '=' && kind > 38) {
                                kind = 38;
                                break;
                            }
                        case 20:
                            if (this.curChar == '!') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 19;
                                break;
                            }
                            break;
                        case 21:
                            if (this.curChar == '>' && kind > 41) {
                                kind = 41;
                                break;
                            }
                        case 22:
                            if (this.curChar == '=' && kind > 42) {
                                kind = 42;
                                break;
                            }
                        case 23:
                            if (this.curChar == '>') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 22;
                                break;
                            }
                            break;
                        case 24:
                            if (this.curChar == '<' && kind > 43) {
                                kind = 43;
                                break;
                            }
                        case 25:
                            if (this.curChar == '=' && kind > 44) {
                                kind = 44;
                                break;
                            }
                        case 26:
                            if (this.curChar == '<') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 25;
                                break;
                            }
                            break;
                        case 27:
                            if (this.curChar == '%' && kind > 46) {
                                kind = 46;
                                break;
                            }
                        case 31:
                            if (this.curChar == '/' && kind > 47) {
                                kind = 47;
                                break;
                            }
                        case 35:
                            if (this.curChar == '!' && kind > 48) {
                                kind = 48;
                                break;
                            }
                        case 36:
                            if (this.curChar == '$') {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                        case 38:
                            if (this.curChar == '#') {
                                jjCheckNAdd(39);
                                break;
                            }
                            break;
                        case 39:
                            if ((287948901175001088L & l) != 0) {
                                if (kind > 59) {
                                    kind = 59;
                                }
                                jjCheckNAdd(39);
                                break;
                            }
                            break;
                        case 40:
                            if ((287667426198290432L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(41, 42);
                                break;
                            }
                            break;
                        case 41:
                            if ((287948901175001088L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(41, 42);
                                break;
                            }
                            break;
                        case 43:
                            if ((287948901175001088L & l) != 0) {
                                jjCheckNAddTwoStates(43, 44);
                                break;
                            }
                            break;
                        case 44:
                            if (this.curChar == '.') {
                                jjCheckNAdd(45);
                                break;
                            }
                            break;
                        case 45:
                            if ((287948901175001088L & l) != 0) {
                                if (kind > 61) {
                                    kind = 61;
                                }
                                jjCheckNAddStates(42, 44);
                                break;
                            }
                            break;
                        case 47:
                            if ((43980465111040L & l) != 0) {
                                jjCheckNAdd(48);
                                break;
                            }
                            break;
                        case 48:
                            if ((287948901175001088L & l) != 0) {
                                if (kind > 61) {
                                    kind = 61;
                                }
                                jjCheckNAddTwoStates(48, 49);
                                break;
                            }
                            break;
                        case 50:
                            if (this.curChar == '\"') {
                                jjCheckNAddStates(36, 38);
                                break;
                            }
                            break;
                        case 51:
                            if ((-17179878401L & l) != 0) {
                                jjCheckNAddStates(36, 38);
                                break;
                            }
                            break;
                        case 53:
                            if ((-9217 & l) != 0) {
                                jjCheckNAddStates(36, 38);
                                break;
                            }
                            break;
                        case 54:
                            if (this.curChar == '\"' && kind > 62) {
                                kind = 62;
                                break;
                            }
                        case 55:
                            if (this.curChar == '\'') {
                                jjCheckNAddStates(33, 35);
                                break;
                            }
                            break;
                        case 56:
                            if ((-549755823105L & l) != 0) {
                                jjCheckNAddStates(33, 35);
                                break;
                            }
                            break;
                        case 58:
                            if ((-9217 & l) != 0) {
                                jjCheckNAddStates(33, 35);
                                break;
                            }
                            break;
                        case 59:
                            if (this.curChar == '\'' && kind > 62) {
                                kind = 62;
                                break;
                            }
                        case 60:
                            if (this.curChar == '0') {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddStates(39, 41);
                                break;
                            }
                            break;
                        case 61:
                            if ((71776119061217280L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(61, 42);
                                break;
                            }
                            break;
                        case 63:
                            if ((287948901175001088L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(63, 42);
                                break;
                            }
                            break;
                        case 64:
                            if (this.curChar == '/') {
                                jjAddStates(31, 32);
                                break;
                            }
                            break;
                        case 65:
                            if (this.curChar == '*') {
                                jjCheckNAddTwoStates(66, 67);
                                break;
                            }
                            break;
                        case 66:
                            if ((-4398046511105L & l) != 0) {
                                jjCheckNAddTwoStates(66, 67);
                                break;
                            }
                            break;
                        case 67:
                            if (this.curChar == '*') {
                                jjCheckNAddStates(45, 47);
                                break;
                            }
                            break;
                        case 68:
                            if ((-145135534866433L & l) != 0) {
                                jjCheckNAddTwoStates(69, 67);
                                break;
                            }
                            break;
                        case 69:
                            if ((-4398046511105L & l) != 0) {
                                jjCheckNAddTwoStates(69, 67);
                                break;
                            }
                            break;
                        case HwCustRemoteInputViewImpl.NUM_PER_GROUP /*70*/:
                            if (this.curChar == '/' && kind > 2) {
                                kind = 2;
                                break;
                            }
                        case 71:
                            if (this.curChar == '/') {
                                if (kind > 3) {
                                    kind = 3;
                                }
                                jjCheckNAddStates(48, 50);
                                break;
                            }
                            break;
                        case 72:
                            if ((-9217 & l) != 0) {
                                if (kind > 3) {
                                    kind = 3;
                                }
                                jjCheckNAddStates(48, 50);
                                break;
                            }
                            break;
                        case 73:
                            if ((9216 & l) != 0 && kind > 3) {
                                kind = 3;
                                break;
                            }
                        case 74:
                            if (this.curChar == '\n' && kind > 3) {
                                kind = 3;
                                break;
                            }
                        case 75:
                            if (this.curChar == '\r') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 74;
                                break;
                            }
                            break;
                        case 77:
                            if ((287948969894477824L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                    }
                    if (i != startsAt) {
                    }
                }
            } else if (this.curChar >= '') {
                int hiByte = this.curChar >> 8;
                int i1 = hiByte >> 6;
                long l1 = 1 << (hiByte & 63);
                int i22 = (this.curChar & 255) >> 6;
                long l2 = 1 << (this.curChar & 63);
                do {
                    i--;
                    switch (this.jjstateSet[i]) {
                        case 1:
                            if (jjCanMove_0(hiByte, i1, i22, l1, l2)) {
                                if (kind > 1) {
                                    kind = 1;
                                }
                                jjAddStates(11, 13);
                                continue;
                            } else {
                                continue;
                            }
                        case 51:
                        case 53:
                            if (jjCanMove_1(hiByte, i1, i22, l1, l2)) {
                                jjCheckNAddStates(36, 38);
                                continue;
                            } else {
                                continue;
                            }
                        case 56:
                        case 58:
                            if (jjCanMove_1(hiByte, i1, i22, l1, l2)) {
                                jjCheckNAddStates(33, 35);
                                continue;
                            } else {
                                continue;
                            }
                        case 66:
                            if (jjCanMove_0(hiByte, i1, i22, l1, l2)) {
                                jjCheckNAddTwoStates(66, 67);
                                continue;
                            } else {
                                continue;
                            }
                        case 68:
                        case 69:
                            if (jjCanMove_0(hiByte, i1, i22, l1, l2)) {
                                jjCheckNAddTwoStates(69, 67);
                                continue;
                            } else {
                                continue;
                            }
                        case 72:
                            if (jjCanMove_0(hiByte, i1, i22, l1, l2)) {
                                if (kind > 3) {
                                    kind = 3;
                                }
                                jjAddStates(48, 50);
                                continue;
                            } else {
                                continue;
                            }
                        default:
                            break;
                    }
                } while (i != startsAt);
            } else {
                l = 1 << (this.curChar & 63);
                while (true) {
                    i--;
                    switch (this.jjstateSet[i]) {
                        case 1:
                            if (kind > 1) {
                                kind = 1;
                            }
                            jjAddStates(11, 13);
                            break;
                        case 5:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                            } else if (this.curChar == '|') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 11;
                            }
                            if (this.curChar != 'l') {
                                if (this.curChar != 'g') {
                                    if (this.curChar != 'n') {
                                        if (this.curChar != 'd') {
                                            if (this.curChar != 'm') {
                                                if (this.curChar != 'e') {
                                                    if (this.curChar != 'o') {
                                                        if (this.curChar == 'a') {
                                                            iArr = this.jjstateSet;
                                                            i3 = this.jjnewStateCnt;
                                                            this.jjnewStateCnt = i3 + 1;
                                                            iArr[i3] = 9;
                                                            break;
                                                        }
                                                    }
                                                    iArr = this.jjstateSet;
                                                    i3 = this.jjnewStateCnt;
                                                    this.jjnewStateCnt = i3 + 1;
                                                    iArr[i3] = 13;
                                                    break;
                                                }
                                                iArr = this.jjstateSet;
                                                i3 = this.jjnewStateCnt;
                                                this.jjnewStateCnt = i3 + 1;
                                                iArr[i3] = 17;
                                                break;
                                            }
                                            iArr = this.jjstateSet;
                                            i3 = this.jjnewStateCnt;
                                            this.jjnewStateCnt = i3 + 1;
                                            iArr[i3] = 29;
                                            break;
                                        }
                                        iArr = this.jjstateSet;
                                        i3 = this.jjnewStateCnt;
                                        this.jjnewStateCnt = i3 + 1;
                                        iArr[i3] = 33;
                                        break;
                                    }
                                    jjAddStates(55, 56);
                                    break;
                                }
                                jjAddStates(53, 54);
                                break;
                            }
                            jjAddStates(51, 52);
                            break;
                            break;
                        case 8:
                            if (this.curChar == 'd' && kind > 35) {
                                kind = 35;
                                break;
                            }
                        case 9:
                            if (this.curChar == 'n') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 8;
                                break;
                            }
                            break;
                        case 10:
                            if (this.curChar == 'a') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 9;
                                break;
                            }
                            break;
                        case 11:
                            if (this.curChar == '|' && kind > 36) {
                                kind = 36;
                                break;
                            }
                        case 12:
                            if (this.curChar == '|') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 11;
                                break;
                            }
                            break;
                        case 13:
                            if (this.curChar == 'r' && kind > 36) {
                                kind = 36;
                                break;
                            }
                        case 14:
                            if (this.curChar == 'o') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 13;
                                break;
                            }
                            break;
                        case 17:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                            }
                            if (this.curChar == 'q' && kind > 37) {
                                kind = 37;
                                break;
                            }
                        case 18:
                            if (this.curChar == 'e') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 17;
                                break;
                            }
                            break;
                        case 28:
                            if (this.curChar == 'd' && kind > 46) {
                                kind = 46;
                                break;
                            }
                        case 29:
                            if (this.curChar == 'o') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 28;
                                break;
                            }
                            break;
                        case 30:
                            if (this.curChar == 'm') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 29;
                                break;
                            }
                            break;
                        case 32:
                            if (this.curChar == 'v' && kind > 47) {
                                kind = 47;
                                break;
                            }
                        case 33:
                            if (this.curChar == 'i') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 32;
                                break;
                            }
                            break;
                        case 34:
                            if (this.curChar == 'd') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 33;
                                break;
                            }
                            break;
                        case 36:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                        case 37:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                                break;
                            }
                            break;
                        case 42:
                            if ((18691697676544L & l) != 0 && kind > 60) {
                                kind = 60;
                                break;
                            }
                        case 46:
                            if ((137438953504L & l) != 0) {
                                jjAddStates(57, 58);
                                break;
                            }
                            break;
                        case 49:
                            if ((360777252948L & l) != 0 && kind > 61) {
                                kind = 61;
                                break;
                            }
                        case 51:
                            if ((-268435457 & l) != 0) {
                                jjCheckNAddStates(36, 38);
                                break;
                            }
                            break;
                        case 52:
                            if (this.curChar == '\\') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 53;
                                break;
                            }
                            break;
                        case 53:
                            jjCheckNAddStates(36, 38);
                            break;
                        case 56:
                            if ((-268435457 & l) != 0) {
                                jjCheckNAddStates(33, 35);
                                break;
                            }
                            break;
                        case 57:
                            if (this.curChar == '\\') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 58;
                                break;
                            }
                            break;
                        case 58:
                            jjCheckNAddStates(33, 35);
                            break;
                        case 62:
                            if ((72057594054705152L & l) != 0) {
                                jjCheckNAdd(63);
                                break;
                            }
                            break;
                        case 63:
                            if ((541165879422L & l) != 0) {
                                if (kind > 60) {
                                    kind = 60;
                                }
                                jjCheckNAddTwoStates(63, 42);
                                break;
                            }
                            break;
                        case 66:
                            jjCheckNAddTwoStates(66, 67);
                            break;
                        case 68:
                        case 69:
                            jjCheckNAddTwoStates(69, 67);
                            break;
                        case 72:
                            if (kind > 3) {
                                kind = 3;
                            }
                            jjAddStates(48, 50);
                            break;
                        case 76:
                            if (this.curChar == 'n') {
                                jjAddStates(55, 56);
                                break;
                            }
                            break;
                        case 77:
                            if ((576460745995190271L & l) != 0) {
                                if (kind > 56) {
                                    kind = 56;
                                }
                                jjCheckNAdd(37);
                            }
                            if (this.curChar != 'o') {
                                if (this.curChar == 'e' && kind > 38) {
                                    kind = 38;
                                    break;
                                }
                            }
                            iArr = this.jjstateSet;
                            i3 = this.jjnewStateCnt;
                            this.jjnewStateCnt = i3 + 1;
                            iArr[i3] = 78;
                            break;
                        case 78:
                            if (this.curChar == 't' && kind > 48) {
                                kind = 48;
                                break;
                            }
                        case 79:
                            if (this.curChar == 'o') {
                                iArr = this.jjstateSet;
                                i3 = this.jjnewStateCnt;
                                this.jjnewStateCnt = i3 + 1;
                                iArr[i3] = 78;
                                break;
                            }
                            break;
                        case 80:
                            if (this.curChar == 'g') {
                                jjAddStates(53, 54);
                                break;
                            }
                            break;
                        case 81:
                            if (this.curChar == 't' && kind > 41) {
                                kind = 41;
                                break;
                            }
                        case 82:
                            if (this.curChar == 'e' && kind > 42) {
                                kind = 42;
                                break;
                            }
                        case 83:
                            if (this.curChar == 'l') {
                                jjAddStates(51, 52);
                                break;
                            }
                            break;
                        case 84:
                            if (this.curChar == 't' && kind > 43) {
                                kind = 43;
                                break;
                            }
                        case 85:
                            if (this.curChar == 'e' && kind > 44) {
                                kind = 44;
                                break;
                            }
                    }
                    if (i != startsAt) {
                    }
                }
            }
            if (kind != Integer.MAX_VALUE) {
                this.jjmatchedKind = kind;
                this.jjmatchedPos = curPos;
                kind = Integer.MAX_VALUE;
            }
            curPos++;
            i = this.jjnewStateCnt;
            this.jjnewStateCnt = startsAt;
            startsAt = 86 - startsAt;
            if (i == startsAt) {
                return curPos;
            }
            try {
                this.curChar = (char) this.input_stream.readChar();
            } catch (IOException e) {
                return curPos;
            }
        }
    }

    private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2) {
        boolean z = true;
        switch (hiByte) {
            case 0:
                if ((jjbitVec2[i2] & l2) == 0) {
                    z = false;
                }
                return z;
            default:
                return (jjbitVec0[i1] & l1) != 0;
        }
    }

    private static final boolean jjCanMove_1(int hiByte, int i1, int i2, long l1, long l2) {
        boolean z = true;
        switch (hiByte) {
            case 0:
                if ((jjbitVec2[i2] & l2) == 0) {
                    z = false;
                }
                return z;
            case 32:
                if ((jjbitVec4[i2] & l2) == 0) {
                    z = false;
                }
                return z;
            default:
                return (jjbitVec3[i1] & l1) != 0;
        }
    }

    public ParserTokenManager(SimpleCharStream stream) {
        this.input_stream = stream;
    }

    public void ReInit(SimpleCharStream stream) {
        this.jjnewStateCnt = 0;
        this.jjmatchedPos = 0;
        this.curLexState = this.defaultLexState;
        this.input_stream = stream;
        ReInitRounds();
    }

    private void ReInitRounds() {
        this.jjround = -2147483647;
        int i = 86;
        while (true) {
            int i2 = i - 1;
            if (i > 0) {
                this.jjrounds[i2] = Integer.MIN_VALUE;
                i = i2;
            } else {
                return;
            }
        }
    }

    protected Token jjFillToken() {
        String im = jjstrLiteralImages[this.jjmatchedKind];
        String curTokenImage = im != null ? im : this.input_stream.GetImage();
        int beginLine = this.input_stream.getBeginLine();
        int beginColumn = this.input_stream.getBeginColumn();
        int endLine = this.input_stream.getEndLine();
        int endColumn = this.input_stream.getEndColumn();
        Token t = Token.newToken(this.jjmatchedKind, curTokenImage);
        t.beginLine = beginLine;
        t.endLine = endLine;
        t.beginColumn = beginColumn;
        t.endColumn = endColumn;
        return t;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Token getNextToken() {
        int curPos = 0;
        while (true) {
            try {
                this.curChar = (char) this.input_stream.BeginToken();
                switch (this.curLexState) {
                    case 0:
                        try {
                            this.input_stream.backup(0);
                            while (this.curChar <= ' ') {
                                if (((1 << this.curChar) & 4294981120L) != 0) {
                                    this.curChar = (char) this.input_stream.BeginToken();
                                } else {
                                    this.jjmatchedKind = Integer.MAX_VALUE;
                                    this.jjmatchedPos = 0;
                                    curPos = jjMoveStringLiteralDfa0_0();
                                }
                            }
                            this.jjmatchedKind = Integer.MAX_VALUE;
                            this.jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_0();
                        } catch (IOException e) {
                            break;
                        }
                    case 1:
                        try {
                            this.input_stream.backup(0);
                            while (this.curChar <= ' ') {
                                if (((1 << this.curChar) & 4294981120L) != 0) {
                                    this.curChar = (char) this.input_stream.BeginToken();
                                } else {
                                    this.jjmatchedKind = Integer.MAX_VALUE;
                                    this.jjmatchedPos = 0;
                                    curPos = jjMoveStringLiteralDfa0_1();
                                }
                            }
                            this.jjmatchedKind = Integer.MAX_VALUE;
                            this.jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_1();
                        } catch (IOException e2) {
                            break;
                        }
                    case 2:
                        try {
                            this.input_stream.backup(0);
                            while (this.curChar <= ' ') {
                                if (((1 << this.curChar) & 4294981120L) != 0) {
                                    this.curChar = (char) this.input_stream.BeginToken();
                                } else {
                                    this.jjmatchedKind = Integer.MAX_VALUE;
                                    this.jjmatchedPos = 0;
                                    curPos = jjMoveStringLiteralDfa0_2();
                                }
                            }
                            this.jjmatchedKind = Integer.MAX_VALUE;
                            this.jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_2();
                        } catch (IOException e3) {
                            break;
                        }
                    default:
                        if (this.jjmatchedKind != Integer.MAX_VALUE) {
                            if (this.jjmatchedPos + 1 < curPos) {
                                this.input_stream.backup((curPos - this.jjmatchedPos) - 1);
                            }
                            if ((jjtoToken[this.jjmatchedKind >> 6] & (1 << (this.jjmatchedKind & 63))) == 0) {
                                if (jjnewLexState[this.jjmatchedKind] == -1) {
                                    break;
                                }
                                this.curLexState = jjnewLexState[this.jjmatchedKind];
                                break;
                            }
                            Token matchedToken = jjFillToken();
                            if (jjnewLexState[this.jjmatchedKind] != -1) {
                                this.curLexState = jjnewLexState[this.jjmatchedKind];
                            }
                            return matchedToken;
                        }
                        int error_line = this.input_stream.getEndLine();
                        int error_column = this.input_stream.getEndColumn();
                        String error_after = null;
                        boolean EOFSeen = false;
                        try {
                            this.input_stream.readChar();
                            this.input_stream.backup(1);
                        } catch (IOException e4) {
                            EOFSeen = true;
                            if (curPos > 1) {
                                error_after = this.input_stream.GetImage();
                            } else {
                                error_after = BuildConfig.FLAVOR;
                            }
                            if (this.curChar == '\n' || this.curChar == '\r') {
                                error_line++;
                                error_column = 0;
                            } else {
                                error_column++;
                            }
                        }
                        if (!EOFSeen) {
                            this.input_stream.backup(1);
                            error_after = curPos > 1 ? this.input_stream.GetImage() : BuildConfig.FLAVOR;
                        }
                        throw new TokenMgrError(EOFSeen, this.curLexState, error_line, error_column, error_after, this.curChar, 0);
                }
            } catch (IOException e5) {
                this.jjmatchedKind = 0;
                return jjFillToken();
            }
        }
    }

    private void jjCheckNAdd(int state) {
        if (this.jjrounds[state] != this.jjround) {
            int[] iArr = this.jjstateSet;
            int i = this.jjnewStateCnt;
            this.jjnewStateCnt = i + 1;
            iArr[i] = state;
            this.jjrounds[state] = this.jjround;
        }
    }

    private void jjAddStates(int start, int end) {
        while (true) {
            int[] iArr = this.jjstateSet;
            int i = this.jjnewStateCnt;
            this.jjnewStateCnt = i + 1;
            iArr[i] = jjnextStates[start];
            int start2 = start + 1;
            if (start != end) {
                start = start2;
            } else {
                return;
            }
        }
    }

    private void jjCheckNAddTwoStates(int state1, int state2) {
        jjCheckNAdd(state1);
        jjCheckNAdd(state2);
    }

    private void jjCheckNAddStates(int start, int end) {
        while (true) {
            jjCheckNAdd(jjnextStates[start]);
            int start2 = start + 1;
            if (start != end) {
                start = start2;
            } else {
                return;
            }
        }
    }
}
