package org.apache.commons.jexl2.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.jexl2.JexlInfo;

public class Parser extends JexlParser implements ParserTreeConstants, ParserConstants {
    private static int[] jj_la1_0;
    private static int[] jj_la1_1;
    public boolean ALLOW_REGISTERS = false;
    private final JJCalls[] jj_2_rtns = new JJCalls[20];
    private int jj_endpos;
    private List<int[]> jj_expentries = new ArrayList();
    private int[] jj_expentry;
    private int jj_gc = 0;
    private int jj_gen;
    SimpleCharStream jj_input_stream;
    private int jj_kind = -1;
    private int jj_la;
    private final int[] jj_la1 = new int[47];
    private Token jj_lastpos;
    private int[] jj_lasttokens = new int[100];
    private final LookaheadSuccess jj_ls = new LookaheadSuccess();
    public Token jj_nt;
    private int jj_ntk;
    private boolean jj_rescan = false;
    private Token jj_scanpos;
    protected JJTParserState jjtree = new JJTParserState();
    public Token token;
    public ParserTokenManager token_source;

    static final class JJCalls {
        int arg;
        Token first;
        int gen;
        JJCalls next;

        JJCalls() {
        }
    }

    private static final class LookaheadSuccess extends Error {
        private LookaheadSuccess() {
        }
    }

    public ASTJexlScript parse(Reader reader, JexlInfo info) throws ParseException {
        if (this.ALLOW_REGISTERS) {
            this.token_source.defaultLexState = 0;
        }
        ReInit(reader);
        ASTJexlScript tree = JexlScript();
        tree.value = info;
        return tree;
    }

    public final ASTJexlScript JexlScript() throws ParseException {
        Node jjtn000 = new ASTJexlScript(0);
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        while (true) {
            try {
                int i;
                if (this.jj_ntk != -1) {
                    i = this.jj_ntk;
                } else {
                    i = jj_ntk();
                }
                switch (i) {
                    case 9:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                    case 21:
                    case 23:
                    case 25:
                    case 27:
                    case 29:
                    case 48:
                    case 50:
                    case 52:
                    case 56:
                    case 59:
                    case 60:
                    case 61:
                    case 62:
                        Statement();
                    default:
                        this.jj_la1[0] = this.jj_gen;
                        jj_consume_token(0);
                        this.jjtree.closeNodeScope(jjtn000, true);
                        jjtreeCloseNodeScope(jjtn000);
                        return jjtn000;
                }
            } catch (Throwable th) {
                if (1 != null) {
                    this.jjtree.closeNodeScope(jjtn000, true);
                    jjtreeCloseNodeScope(jjtn000);
                }
            }
        }
    }

    public final void Statement() throws ParseException {
        switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
            case 29:
                jj_consume_token(29);
                return;
            default:
                this.jj_la1[1] = this.jj_gen;
                if (jj_2_1(3)) {
                    Block();
                    return;
                }
                int i;
                if (this.jj_ntk != -1) {
                    i = this.jj_ntk;
                } else {
                    i = jj_ntk();
                }
                switch (i) {
                    case 9:
                        IfStatement();
                        return;
                    case 11:
                    case 12:
                        ForeachStatement();
                        return;
                    case 13:
                        WhileStatement();
                        return;
                    case 14:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                    case 23:
                    case 25:
                    case 27:
                    case 48:
                    case 50:
                    case 52:
                    case 56:
                    case 59:
                    case 60:
                    case 61:
                    case 62:
                        ExpressionStatement();
                        return;
                    case 15:
                        Var();
                        return;
                    case 21:
                        ReturnStatement();
                        return;
                    default:
                        this.jj_la1[2] = this.jj_gen;
                        jj_consume_token(-1);
                        throw new ParseException();
                }
        }
    }

    public final void Block() throws ParseException {
        Node jjtn000 = new ASTBlock(2);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            jj_consume_token(25);
            while (true) {
                int i;
                if (this.jj_ntk != -1) {
                    i = this.jj_ntk;
                } else {
                    i = jj_ntk();
                }
                switch (i) {
                    case 9:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                    case 21:
                    case 23:
                    case 25:
                    case 27:
                    case 29:
                    case 48:
                    case 50:
                    case 52:
                    case 56:
                    case 59:
                    case 60:
                    case 61:
                    case 62:
                        Statement();
                    default:
                        this.jj_la1[3] = this.jj_gen;
                        jj_consume_token(26);
                        this.jjtree.closeNodeScope(jjtn000, true);
                        jjtreeCloseNodeScope(jjtn000);
                        return;
                }
            }
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void ExpressionStatement() throws ParseException {
        Expression();
        while (true) {
            int i;
            if (this.jj_ntk != -1) {
                i = this.jj_ntk;
            } else {
                i = jj_ntk();
            }
            switch (i) {
                case 14:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 23:
                case 25:
                case 27:
                case 48:
                case 50:
                case 52:
                case 56:
                case 59:
                case 60:
                case 61:
                case 62:
                    Node jjtn001 = new ASTAmbiguous(3);
                    boolean jjtc001 = true;
                    this.jjtree.openNodeScope(jjtn001);
                    jjtreeOpenNodeScope(jjtn001);
                    try {
                        Expression();
                        this.jjtree.closeNodeScope(jjtn001, true);
                        jjtreeCloseNodeScope(jjtn001);
                    } catch (Throwable th) {
                        if (jjtc001) {
                            this.jjtree.closeNodeScope(jjtn001, true);
                            jjtreeCloseNodeScope(jjtn001);
                        }
                    }
                default:
                    this.jj_la1[4] = this.jj_gen;
                    if (jj_2_2(2)) {
                        jj_consume_token(29);
                        return;
                    }
                    return;
            }
        }
    }

    public final void IfStatement() throws ParseException {
        Node jjtn000 = new ASTIfStatement(4);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            jj_consume_token(9);
            jj_consume_token(23);
            Expression();
            jj_consume_token(24);
            Statement();
            switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                case 10:
                    jj_consume_token(10);
                    Statement();
                    break;
                default:
                    this.jj_la1[5] = this.jj_gen;
                    break;
            }
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void WhileStatement() throws ParseException {
        Node jjtn000 = new ASTWhileStatement(5);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            jj_consume_token(13);
            jj_consume_token(23);
            Expression();
            jj_consume_token(24);
            Statement();
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void ForeachStatement() throws ParseException {
        Node jjtn000 = new ASTForeachStatement(6);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            int i;
            if (this.jj_ntk != -1) {
                i = this.jj_ntk;
            } else {
                i = jj_ntk();
            }
            switch (i) {
                case 11:
                    jj_consume_token(11);
                    jj_consume_token(23);
                    LValueVar();
                    jj_consume_token(30);
                    Expression();
                    jj_consume_token(24);
                    Statement();
                    break;
                case 12:
                    jj_consume_token(12);
                    jj_consume_token(23);
                    LValueVar();
                    jj_consume_token(22);
                    Expression();
                    jj_consume_token(24);
                    Statement();
                    break;
                default:
                    this.jj_la1[6] = this.jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void ReturnStatement() throws ParseException {
        Node jjtn000 = new ASTReturnStatement(7);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            jj_consume_token(21);
            Expression();
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void Expression() throws ParseException {
        int i;
        ConditionalExpression();
        if (this.jj_ntk != -1) {
            i = this.jj_ntk;
        } else {
            i = jj_ntk();
        }
        switch (i) {
            case 45:
                jj_consume_token(45);
                Node jjtn001 = new ASTAssignment(8);
                boolean jjtc001 = true;
                this.jjtree.openNodeScope(jjtn001);
                jjtreeOpenNodeScope(jjtn001);
                try {
                    Expression();
                    this.jjtree.closeNodeScope(jjtn001, 2);
                    jjtreeCloseNodeScope(jjtn001);
                    return;
                } catch (Throwable th) {
                    if (jjtc001) {
                        this.jjtree.closeNodeScope(jjtn001, 2);
                        jjtreeCloseNodeScope(jjtn001);
                    }
                }
            default:
                this.jj_la1[7] = this.jj_gen;
                return;
        }
    }

    public final void Var() throws ParseException {
        int i;
        jj_consume_token(15);
        DeclareVar();
        if (this.jj_ntk != -1) {
            i = this.jj_ntk;
        } else {
            i = jj_ntk();
        }
        switch (i) {
            case 45:
                jj_consume_token(45);
                Node jjtn001 = new ASTAssignment(8);
                boolean jjtc001 = true;
                this.jjtree.openNodeScope(jjtn001);
                jjtreeOpenNodeScope(jjtn001);
                try {
                    Expression();
                    this.jjtree.closeNodeScope(jjtn001, 2);
                    jjtreeCloseNodeScope(jjtn001);
                    return;
                } catch (Throwable th) {
                    if (jjtc001) {
                        this.jjtree.closeNodeScope(jjtn001, 2);
                        jjtreeCloseNodeScope(jjtn001);
                    }
                }
            default:
                this.jj_la1[8] = this.jj_gen;
                return;
        }
    }

    public final void DeclareVar() throws ParseException {
        Node jjtn000 = new ASTVar(9);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            Token t = jj_consume_token(56);
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
            jjtreeCloseNodeScope(jjtn000);
            declareVariable(jjtn000, t.image);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void LValueVar() throws ParseException {
        Node jjtn000 = new ASTReference(10);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            int i;
            if (this.jj_ntk != -1) {
                i = this.jj_ntk;
            } else {
                i = jj_ntk();
            }
            switch (i) {
                case 15:
                    jj_consume_token(15);
                    DeclareVar();
                    DotReference();
                    break;
                case 56:
                case 59:
                    Identifier(true);
                    DotReference();
                    break;
                default:
                    this.jj_la1[9] = this.jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void ConditionalExpression() throws ParseException {
        int i;
        ConditionalOrExpression();
        if (this.jj_ntk != -1) {
            i = this.jj_ntk;
        } else {
            i = jj_ntk();
        }
        switch (i) {
            case 33:
            case 34:
                if (this.jj_ntk != -1) {
                    i = this.jj_ntk;
                } else {
                    i = jj_ntk();
                }
                switch (i) {
                    case 33:
                        jj_consume_token(33);
                        Expression();
                        jj_consume_token(30);
                        Node jjtn001 = new ASTTernaryNode(11);
                        boolean jjtc001 = true;
                        this.jjtree.openNodeScope(jjtn001);
                        jjtreeOpenNodeScope(jjtn001);
                        try {
                            Expression();
                            this.jjtree.closeNodeScope(jjtn001, 3);
                            jjtreeCloseNodeScope(jjtn001);
                            return;
                        } catch (Throwable th) {
                            if (jjtc001) {
                                this.jjtree.closeNodeScope(jjtn001, 3);
                                jjtreeCloseNodeScope(jjtn001);
                            }
                        }
                    case 34:
                        jj_consume_token(34);
                        Node jjtn002 = new ASTTernaryNode(11);
                        boolean jjtc002 = true;
                        this.jjtree.openNodeScope(jjtn002);
                        jjtreeOpenNodeScope(jjtn002);
                        try {
                            Expression();
                            this.jjtree.closeNodeScope(jjtn002, 2);
                            jjtreeCloseNodeScope(jjtn002);
                            return;
                        } catch (Throwable th2) {
                            if (jjtc002) {
                                this.jjtree.closeNodeScope(jjtn002, 2);
                                jjtreeCloseNodeScope(jjtn002);
                            }
                        }
                    default:
                        this.jj_la1[10] = this.jj_gen;
                        jj_consume_token(-1);
                        throw new ParseException();
                }
            default:
                this.jj_la1[11] = this.jj_gen;
                return;
        }
    }

    public final void ConditionalOrExpression() throws ParseException {
        ConditionalAndExpression();
        while (true) {
            switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                case 36:
                    jj_consume_token(36);
                    Node jjtn001 = new ASTOrNode(12);
                    boolean jjtc001 = true;
                    this.jjtree.openNodeScope(jjtn001);
                    jjtreeOpenNodeScope(jjtn001);
                    try {
                        ConditionalAndExpression();
                        this.jjtree.closeNodeScope(jjtn001, 2);
                        jjtreeCloseNodeScope(jjtn001);
                    } catch (Throwable th) {
                        if (jjtc001) {
                            this.jjtree.closeNodeScope(jjtn001, 2);
                            jjtreeCloseNodeScope(jjtn001);
                        }
                    }
                default:
                    this.jj_la1[12] = this.jj_gen;
                    return;
            }
        }
    }

    public final void ConditionalAndExpression() throws ParseException {
        InclusiveOrExpression();
        while (true) {
            switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                case 35:
                    jj_consume_token(35);
                    Node jjtn001 = new ASTAndNode(13);
                    boolean jjtc001 = true;
                    this.jjtree.openNodeScope(jjtn001);
                    jjtreeOpenNodeScope(jjtn001);
                    try {
                        InclusiveOrExpression();
                        this.jjtree.closeNodeScope(jjtn001, 2);
                        jjtreeCloseNodeScope(jjtn001);
                    } catch (Throwable th) {
                        if (jjtc001) {
                            this.jjtree.closeNodeScope(jjtn001, 2);
                            jjtreeCloseNodeScope(jjtn001);
                        }
                    }
                default:
                    this.jj_la1[13] = this.jj_gen;
                    return;
            }
        }
    }

    public final void InclusiveOrExpression() throws ParseException {
        ExclusiveOrExpression();
        while (true) {
            switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                case 54:
                    jj_consume_token(54);
                    Node jjtn001 = new ASTBitwiseOrNode(14);
                    boolean jjtc001 = true;
                    this.jjtree.openNodeScope(jjtn001);
                    jjtreeOpenNodeScope(jjtn001);
                    try {
                        ExclusiveOrExpression();
                        this.jjtree.closeNodeScope(jjtn001, 2);
                        jjtreeCloseNodeScope(jjtn001);
                    } catch (Throwable th) {
                        if (jjtc001) {
                            this.jjtree.closeNodeScope(jjtn001, 2);
                            jjtreeCloseNodeScope(jjtn001);
                        }
                    }
                default:
                    this.jj_la1[14] = this.jj_gen;
                    return;
            }
        }
    }

    public final void ExclusiveOrExpression() throws ParseException {
        AndExpression();
        while (true) {
            switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                case 55:
                    jj_consume_token(55);
                    Node jjtn001 = new ASTBitwiseXorNode(15);
                    boolean jjtc001 = true;
                    this.jjtree.openNodeScope(jjtn001);
                    jjtreeOpenNodeScope(jjtn001);
                    try {
                        AndExpression();
                        this.jjtree.closeNodeScope(jjtn001, 2);
                        jjtreeCloseNodeScope(jjtn001);
                    } catch (Throwable th) {
                        if (jjtc001) {
                            this.jjtree.closeNodeScope(jjtn001, 2);
                            jjtreeCloseNodeScope(jjtn001);
                        }
                    }
                default:
                    this.jj_la1[15] = this.jj_gen;
                    return;
            }
        }
    }

    public final void AndExpression() throws ParseException {
        EqualityExpression();
        while (true) {
            switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                case 53:
                    jj_consume_token(53);
                    Node jjtn001 = new ASTBitwiseAndNode(16);
                    boolean jjtc001 = true;
                    this.jjtree.openNodeScope(jjtn001);
                    jjtreeOpenNodeScope(jjtn001);
                    try {
                        EqualityExpression();
                        this.jjtree.closeNodeScope(jjtn001, 2);
                        jjtreeCloseNodeScope(jjtn001);
                    } catch (Throwable th) {
                        if (jjtc001) {
                            this.jjtree.closeNodeScope(jjtn001, 2);
                            jjtreeCloseNodeScope(jjtn001);
                        }
                    }
                default:
                    this.jj_la1[16] = this.jj_gen;
                    return;
            }
        }
    }

    public final void EqualityExpression() throws ParseException {
        int i;
        RelationalExpression();
        if (this.jj_ntk != -1) {
            i = this.jj_ntk;
        } else {
            i = jj_ntk();
        }
        switch (i) {
            case 37:
            case 38:
                if (this.jj_ntk != -1) {
                    i = this.jj_ntk;
                } else {
                    i = jj_ntk();
                }
                switch (i) {
                    case 37:
                        jj_consume_token(37);
                        Node jjtn001 = new ASTEQNode(17);
                        boolean jjtc001 = true;
                        this.jjtree.openNodeScope(jjtn001);
                        jjtreeOpenNodeScope(jjtn001);
                        try {
                            RelationalExpression();
                            this.jjtree.closeNodeScope(jjtn001, 2);
                            jjtreeCloseNodeScope(jjtn001);
                            return;
                        } catch (Throwable th) {
                            if (jjtc001) {
                                this.jjtree.closeNodeScope(jjtn001, 2);
                                jjtreeCloseNodeScope(jjtn001);
                            }
                        }
                    case 38:
                        jj_consume_token(38);
                        Node jjtn002 = new ASTNENode(18);
                        boolean jjtc002 = true;
                        this.jjtree.openNodeScope(jjtn002);
                        jjtreeOpenNodeScope(jjtn002);
                        try {
                            RelationalExpression();
                            this.jjtree.closeNodeScope(jjtn002, 2);
                            jjtreeCloseNodeScope(jjtn002);
                            return;
                        } catch (Throwable th2) {
                            if (jjtc002) {
                                this.jjtree.closeNodeScope(jjtn002, 2);
                                jjtreeCloseNodeScope(jjtn002);
                            }
                        }
                    default:
                        this.jj_la1[17] = this.jj_gen;
                        jj_consume_token(-1);
                        throw new ParseException();
                }
            default:
                this.jj_la1[18] = this.jj_gen;
                return;
        }
    }

    public final void RelationalExpression() throws ParseException {
        int i;
        JexlNode aSTERNode;
        AdditiveExpression();
        if (this.jj_ntk != -1) {
            i = this.jj_ntk;
        } else {
            i = jj_ntk();
        }
        switch (i) {
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
                if (this.jj_ntk != -1) {
                    i = this.jj_ntk;
                } else {
                    i = jj_ntk();
                }
                switch (i) {
                    case 39:
                        jj_consume_token(39);
                        aSTERNode = new ASTERNode(23);
                        boolean jjtc005 = true;
                        this.jjtree.openNodeScope(aSTERNode);
                        jjtreeOpenNodeScope(aSTERNode);
                        try {
                            AdditiveExpression();
                            this.jjtree.closeNodeScope((Node) aSTERNode, 2);
                            jjtreeCloseNodeScope(aSTERNode);
                            return;
                        } catch (Throwable th) {
                            if (jjtc005) {
                                this.jjtree.closeNodeScope((Node) aSTERNode, 2);
                                jjtreeCloseNodeScope(aSTERNode);
                            }
                        }
                    case 40:
                        jj_consume_token(40);
                        JexlNode jjtn006 = new ASTNRNode(24);
                        boolean jjtc006 = true;
                        this.jjtree.openNodeScope(jjtn006);
                        jjtreeOpenNodeScope(jjtn006);
                        try {
                            AdditiveExpression();
                            this.jjtree.closeNodeScope((Node) jjtn006, 2);
                            jjtreeCloseNodeScope(jjtn006);
                            return;
                        } catch (Throwable th2) {
                            if (jjtc006) {
                                this.jjtree.closeNodeScope((Node) jjtn006, 2);
                                jjtreeCloseNodeScope(jjtn006);
                            }
                        }
                    case 41:
                        jj_consume_token(41);
                        aSTERNode = new ASTGTNode(20);
                        boolean jjtc002 = true;
                        this.jjtree.openNodeScope(aSTERNode);
                        jjtreeOpenNodeScope(aSTERNode);
                        try {
                            AdditiveExpression();
                            this.jjtree.closeNodeScope((Node) aSTERNode, 2);
                            jjtreeCloseNodeScope(aSTERNode);
                            return;
                        } catch (Throwable th3) {
                            if (jjtc002) {
                                this.jjtree.closeNodeScope((Node) aSTERNode, 2);
                                jjtreeCloseNodeScope(aSTERNode);
                            }
                        }
                    case 42:
                        jj_consume_token(42);
                        aSTERNode = new ASTGENode(22);
                        boolean jjtc004 = true;
                        this.jjtree.openNodeScope(aSTERNode);
                        jjtreeOpenNodeScope(aSTERNode);
                        try {
                            AdditiveExpression();
                            this.jjtree.closeNodeScope((Node) aSTERNode, 2);
                            jjtreeCloseNodeScope(aSTERNode);
                            return;
                        } catch (Throwable th4) {
                            if (jjtc004) {
                                this.jjtree.closeNodeScope((Node) aSTERNode, 2);
                                jjtreeCloseNodeScope(aSTERNode);
                            }
                        }
                    case 43:
                        jj_consume_token(43);
                        Node jjtn001 = new ASTLTNode(19);
                        boolean jjtc001 = true;
                        this.jjtree.openNodeScope(jjtn001);
                        jjtreeOpenNodeScope(jjtn001);
                        try {
                            AdditiveExpression();
                            this.jjtree.closeNodeScope(jjtn001, 2);
                            jjtreeCloseNodeScope(jjtn001);
                            return;
                        } catch (Throwable th5) {
                            if (jjtc001) {
                                this.jjtree.closeNodeScope(jjtn001, 2);
                                jjtreeCloseNodeScope(jjtn001);
                            }
                        }
                    case 44:
                        jj_consume_token(44);
                        aSTERNode = new ASTLENode(21);
                        boolean jjtc003 = true;
                        this.jjtree.openNodeScope(aSTERNode);
                        jjtreeOpenNodeScope(aSTERNode);
                        try {
                            AdditiveExpression();
                            this.jjtree.closeNodeScope((Node) aSTERNode, 2);
                            jjtreeCloseNodeScope(aSTERNode);
                            return;
                        } catch (Throwable th6) {
                            if (jjtc003) {
                                this.jjtree.closeNodeScope((Node) aSTERNode, 2);
                                jjtreeCloseNodeScope(aSTERNode);
                            }
                        }
                    default:
                        this.jj_la1[19] = this.jj_gen;
                        jj_consume_token(-1);
                        throw new ParseException();
                }
            default:
                this.jj_la1[20] = this.jj_gen;
                return;
        }
    }

    public final void AdditiveExpression() throws ParseException {
        boolean z = false;
        Node jjtn000 = new ASTAdditiveNode(25);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            MultiplicativeExpression();
            while (true) {
                switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                    case 49:
                    case 50:
                        AdditiveOperator();
                        MultiplicativeExpression();
                    default:
                        this.jj_la1[21] = this.jj_gen;
                        JJTParserState jJTParserState = this.jjtree;
                        if (this.jjtree.nodeArity() > 1) {
                            z = true;
                        }
                        jJTParserState.closeNodeScope(jjtn000, z);
                        jjtreeCloseNodeScope(jjtn000);
                        return;
                }
            }
        } catch (Throwable th) {
            if (jjtc000) {
                JJTParserState jJTParserState2 = this.jjtree;
                if (this.jjtree.nodeArity() > 1) {
                    z = true;
                }
                jJTParserState2.closeNodeScope(jjtn000, z);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void AdditiveOperator() throws ParseException {
        Node jjtn000 = new ASTAdditiveOperator(26);
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            int i;
            if (this.jj_ntk != -1) {
                i = this.jj_ntk;
            } else {
                i = jj_ntk();
            }
            switch (i) {
                case 49:
                    jj_consume_token(49);
                    this.jjtree.closeNodeScope(jjtn000, true);
                    jjtreeCloseNodeScope(jjtn000);
                    jjtn000.image = "+";
                    break;
                case 50:
                    jj_consume_token(50);
                    this.jjtree.closeNodeScope(jjtn000, true);
                    jjtreeCloseNodeScope(jjtn000);
                    jjtn000.image = "-";
                    break;
                default:
                    this.jj_la1[22] = this.jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
            if (null != null) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        } catch (Throwable th) {
            if (true) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void MultiplicativeExpression() throws ParseException {
        UnaryExpression();
        while (true) {
            switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                case 46:
                case 47:
                case 51:
                    int i;
                    if (this.jj_ntk != -1) {
                        i = this.jj_ntk;
                    } else {
                        i = jj_ntk();
                    }
                    switch (i) {
                        case 46:
                            jj_consume_token(46);
                            Node jjtn003 = new ASTModNode(29);
                            boolean jjtc003 = true;
                            this.jjtree.openNodeScope(jjtn003);
                            jjtreeOpenNodeScope(jjtn003);
                            try {
                                UnaryExpression();
                                this.jjtree.closeNodeScope(jjtn003, 2);
                                jjtreeCloseNodeScope(jjtn003);
                                break;
                            } catch (Throwable th) {
                                if (jjtc003) {
                                    this.jjtree.closeNodeScope(jjtn003, 2);
                                    jjtreeCloseNodeScope(jjtn003);
                                }
                            }
                        case 47:
                            jj_consume_token(47);
                            Node jjtn002 = new ASTDivNode(28);
                            boolean jjtc002 = true;
                            this.jjtree.openNodeScope(jjtn002);
                            jjtreeOpenNodeScope(jjtn002);
                            try {
                                UnaryExpression();
                                this.jjtree.closeNodeScope(jjtn002, 2);
                                jjtreeCloseNodeScope(jjtn002);
                                break;
                            } catch (Throwable th2) {
                                if (jjtc002) {
                                    this.jjtree.closeNodeScope(jjtn002, 2);
                                    jjtreeCloseNodeScope(jjtn002);
                                }
                            }
                        case 51:
                            jj_consume_token(51);
                            Node jjtn001 = new ASTMulNode(27);
                            boolean jjtc001 = true;
                            this.jjtree.openNodeScope(jjtn001);
                            jjtreeOpenNodeScope(jjtn001);
                            try {
                                UnaryExpression();
                                this.jjtree.closeNodeScope(jjtn001, 2);
                                jjtreeCloseNodeScope(jjtn001);
                                break;
                            } catch (Throwable th3) {
                                if (jjtc001) {
                                    this.jjtree.closeNodeScope(jjtn001, 2);
                                    jjtreeCloseNodeScope(jjtn001);
                                }
                            }
                        default:
                            this.jj_la1[24] = this.jj_gen;
                            jj_consume_token(-1);
                            throw new ParseException();
                    }
                default:
                    this.jj_la1[23] = this.jj_gen;
                    return;
            }
        }
    }

    public final void UnaryExpression() throws ParseException {
        int i;
        if (this.jj_ntk != -1) {
            i = this.jj_ntk;
        } else {
            i = jj_ntk();
        }
        switch (i) {
            case 14:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 23:
            case 25:
            case 27:
            case 56:
            case 59:
            case 60:
            case 61:
            case 62:
                PrimaryExpression();
                return;
            case 48:
                jj_consume_token(48);
                Node jjtn003 = new ASTNotNode(32);
                boolean jjtc003 = true;
                this.jjtree.openNodeScope(jjtn003);
                jjtreeOpenNodeScope(jjtn003);
                try {
                    UnaryExpression();
                    this.jjtree.closeNodeScope(jjtn003, 1);
                    jjtreeCloseNodeScope(jjtn003);
                    return;
                } catch (Throwable th) {
                    if (jjtc003) {
                        this.jjtree.closeNodeScope(jjtn003, 1);
                        jjtreeCloseNodeScope(jjtn003);
                    }
                }
            case 50:
                jj_consume_token(50);
                Node jjtn001 = new ASTUnaryMinusNode(30);
                boolean jjtc001 = true;
                this.jjtree.openNodeScope(jjtn001);
                jjtreeOpenNodeScope(jjtn001);
                try {
                    UnaryExpression();
                    this.jjtree.closeNodeScope(jjtn001, 1);
                    jjtreeCloseNodeScope(jjtn001);
                    return;
                } catch (Throwable th2) {
                    if (jjtc001) {
                        this.jjtree.closeNodeScope(jjtn001, 1);
                        jjtreeCloseNodeScope(jjtn001);
                    }
                }
            case 52:
                jj_consume_token(52);
                Node jjtn002 = new ASTBitwiseComplNode(31);
                boolean jjtc002 = true;
                this.jjtree.openNodeScope(jjtn002);
                jjtreeOpenNodeScope(jjtn002);
                try {
                    UnaryExpression();
                    this.jjtree.closeNodeScope(jjtn002, 1);
                    jjtreeCloseNodeScope(jjtn002);
                    return;
                } catch (Throwable th3) {
                    if (jjtc002) {
                        this.jjtree.closeNodeScope(jjtn002, 1);
                        jjtreeCloseNodeScope(jjtn002);
                    }
                }
            default:
                this.jj_la1[25] = this.jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
    }

    public final void Identifier(boolean top) throws ParseException {
        Node jjtn000 = new ASTIdentifier(33);
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            int i;
            if (this.jj_ntk != -1) {
                i = this.jj_ntk;
            } else {
                i = jj_ntk();
            }
            Token t;
            switch (i) {
                case 56:
                    String checkVariable;
                    t = jj_consume_token(56);
                    this.jjtree.closeNodeScope(jjtn000, true);
                    jjtreeCloseNodeScope(jjtn000);
                    if (top) {
                        checkVariable = checkVariable(jjtn000, t.image);
                    } else {
                        checkVariable = t.image;
                    }
                    jjtn000.image = checkVariable;
                    break;
                case 59:
                    t = jj_consume_token(59);
                    this.jjtree.closeNodeScope(jjtn000, true);
                    jjtreeCloseNodeScope(jjtn000);
                    jjtn000.image = t.image;
                    jjtn000.setRegister(t.image);
                    break;
                default:
                    this.jj_la1[26] = this.jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
            if (null != null) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        } catch (Throwable th) {
            if (true) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void StringIdentifier() throws ParseException {
        Node jjtn000 = new ASTIdentifier(33);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            Token t = jj_consume_token(62);
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
            jjtreeCloseNodeScope(jjtn000);
            jjtn000.image = StringParser.buildString(t.image, true);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void Literal() throws ParseException {
        int i;
        if (this.jj_ntk != -1) {
            i = this.jj_ntk;
        } else {
            i = jj_ntk();
        }
        switch (i) {
            case 18:
                NullLiteral();
                return;
            case 19:
            case 20:
                BooleanLiteral();
                return;
            case 60:
                IntegerLiteral();
                return;
            case 61:
                FloatLiteral();
                return;
            case 62:
                StringLiteral();
                return;
            default:
                this.jj_la1[27] = this.jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
    }

    public final void NullLiteral() throws ParseException {
        Node jjtn000 = new ASTNullLiteral(34);
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            jj_consume_token(18);
        } finally {
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        }
    }

    public final void BooleanLiteral() throws ParseException {
        int i;
        if (this.jj_ntk != -1) {
            i = this.jj_ntk;
        } else {
            i = jj_ntk();
        }
        switch (i) {
            case 19:
                Node jjtn001 = new ASTTrueNode(35);
                this.jjtree.openNodeScope(jjtn001);
                jjtreeOpenNodeScope(jjtn001);
                try {
                    jj_consume_token(19);
                    return;
                } finally {
                    this.jjtree.closeNodeScope(jjtn001, true);
                    jjtreeCloseNodeScope(jjtn001);
                }
            case 20:
                Node jjtn002 = new ASTFalseNode(36);
                this.jjtree.openNodeScope(jjtn002);
                jjtreeOpenNodeScope(jjtn002);
                try {
                    jj_consume_token(20);
                    return;
                } finally {
                    this.jjtree.closeNodeScope(jjtn002, true);
                    jjtreeCloseNodeScope(jjtn002);
                }
            default:
                this.jj_la1[28] = this.jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
    }

    public final void IntegerLiteral() throws ParseException {
        Node jjtn000 = new ASTNumberLiteral(37);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            Token t = jj_consume_token(60);
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
            jjtreeCloseNodeScope(jjtn000);
            jjtn000.image = t.image;
            jjtn000.setNatural(t.image);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void FloatLiteral() throws ParseException {
        Node jjtn000 = new ASTNumberLiteral(37);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            Token t = jj_consume_token(61);
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
            jjtreeCloseNodeScope(jjtn000);
            jjtn000.image = t.image;
            jjtn000.setReal(t.image);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void StringLiteral() throws ParseException {
        Node jjtn000 = new ASTStringLiteral(38);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            Token t = jj_consume_token(62);
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
            jjtreeCloseNodeScope(jjtn000);
            jjtn000.image = StringParser.buildString(t.image, true);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void ArrayLiteral() throws ParseException {
        Node jjtn000 = new ASTArrayLiteral(39);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            int i;
            jj_consume_token(27);
            if (this.jj_ntk != -1) {
                i = this.jj_ntk;
            } else {
                i = jj_ntk();
            }
            switch (i) {
                case 14:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 23:
                case 25:
                case 27:
                case 48:
                case 50:
                case 52:
                case 56:
                case 59:
                case 60:
                case 61:
                case 62:
                    Expression();
                    while (true) {
                        switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                            case 31:
                                jj_consume_token(31);
                                Expression();
                            default:
                                this.jj_la1[29] = this.jj_gen;
                                break;
                        }
                    }
                default:
                    this.jj_la1[30] = this.jj_gen;
                    break;
            }
            jj_consume_token(28);
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void MapLiteral() throws ParseException {
        Node jjtn000 = new ASTMapLiteral(40);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            int i;
            jj_consume_token(25);
            if (this.jj_ntk != -1) {
                i = this.jj_ntk;
            } else {
                i = jj_ntk();
            }
            switch (i) {
                case 14:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 23:
                case 25:
                case 27:
                case 48:
                case 50:
                case 52:
                case 56:
                case 59:
                case 60:
                case 61:
                case 62:
                    MapEntry();
                    while (true) {
                        switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                            case 31:
                                jj_consume_token(31);
                                MapEntry();
                            default:
                                this.jj_la1[31] = this.jj_gen;
                                break;
                        }
                    }
                case 30:
                    jj_consume_token(30);
                    break;
                default:
                    this.jj_la1[32] = this.jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
            jj_consume_token(26);
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void MapEntry() throws ParseException {
        Node jjtn000 = new ASTMapEntry(41);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            Expression();
            jj_consume_token(30);
            Expression();
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void EmptyFunction() throws ParseException {
        Node jjtn000 = new ASTEmptyFunction(42);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            if (jj_2_3(3)) {
                jj_consume_token(16);
                jj_consume_token(23);
                Expression();
                jj_consume_token(24);
            } else {
                switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                    case 16:
                        jj_consume_token(16);
                        Reference();
                        break;
                    default:
                        this.jj_la1[33] = this.jj_gen;
                        jj_consume_token(-1);
                        throw new ParseException();
                }
            }
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void SizeFunction() throws ParseException {
        Node jjtn000 = new ASTSizeFunction(43);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            jj_consume_token(17);
            jj_consume_token(23);
            Expression();
            jj_consume_token(24);
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void Function() throws ParseException {
        Node jjtn000 = new ASTFunctionNode(44);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            int i;
            Identifier();
            jj_consume_token(30);
            Identifier();
            jj_consume_token(23);
            if (this.jj_ntk != -1) {
                i = this.jj_ntk;
            } else {
                i = jj_ntk();
            }
            switch (i) {
                case 14:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 23:
                case 25:
                case 27:
                case 48:
                case 50:
                case 52:
                case 56:
                case 59:
                case 60:
                case 61:
                case 62:
                    Expression();
                    while (true) {
                        switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                            case 31:
                                jj_consume_token(31);
                                Expression();
                            default:
                                this.jj_la1[34] = this.jj_gen;
                                break;
                        }
                    }
                default:
                    this.jj_la1[35] = this.jj_gen;
                    break;
            }
            jj_consume_token(24);
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void Method() throws ParseException {
        Node jjtn000 = new ASTMethodNode(45);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            int i;
            Identifier();
            jj_consume_token(23);
            if (this.jj_ntk != -1) {
                i = this.jj_ntk;
            } else {
                i = jj_ntk();
            }
            switch (i) {
                case 14:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 23:
                case 25:
                case 27:
                case 48:
                case 50:
                case 52:
                case 56:
                case 59:
                case 60:
                case 61:
                case 62:
                    Expression();
                    while (true) {
                        switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                            case 31:
                                jj_consume_token(31);
                                Expression();
                            default:
                                this.jj_la1[36] = this.jj_gen;
                                break;
                        }
                    }
                default:
                    this.jj_la1[37] = this.jj_gen;
                    break;
            }
            jj_consume_token(24);
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void AnyMethod() throws ParseException {
        if (jj_2_4(Integer.MAX_VALUE)) {
            SizeMethod();
        } else if (jj_2_5(Integer.MAX_VALUE)) {
            Method();
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
    }

    public final void SizeMethod() throws ParseException {
        Node jjtn000 = new ASTSizeMethod(46);
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            jj_consume_token(17);
            jj_consume_token(23);
            jj_consume_token(24);
        } finally {
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        }
    }

    public final void Constructor() throws ParseException {
        Node jjtn000 = new ASTConstructorNode(47);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            int i;
            jj_consume_token(14);
            jj_consume_token(23);
            if (this.jj_ntk != -1) {
                i = this.jj_ntk;
            } else {
                i = jj_ntk();
            }
            switch (i) {
                case 14:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 23:
                case 25:
                case 27:
                case 48:
                case 50:
                case 52:
                case 56:
                case 59:
                case 60:
                case 61:
                case 62:
                    Expression();
                    while (true) {
                        switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                            case 31:
                                jj_consume_token(31);
                                Expression();
                            default:
                                this.jj_la1[38] = this.jj_gen;
                                break;
                        }
                    }
                default:
                    this.jj_la1[39] = this.jj_gen;
                    break;
            }
            jj_consume_token(24);
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void PrimaryExpression() throws ParseException {
        if (jj_2_6(2)) {
            Reference();
        } else if (jj_2_7(Integer.MAX_VALUE)) {
            EmptyFunction();
        } else if (jj_2_8(Integer.MAX_VALUE)) {
            SizeFunction();
        } else if (jj_2_9(Integer.MAX_VALUE)) {
            Constructor();
        } else if (jj_2_10(Integer.MAX_VALUE)) {
            MapLiteral();
        } else if (jj_2_11(Integer.MAX_VALUE)) {
            ArrayLiteral();
        } else {
            switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                case 18:
                case 19:
                case 20:
                case 60:
                case 61:
                case 62:
                    Literal();
                    return;
                default:
                    this.jj_la1[40] = this.jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
        }
    }

    public final void ArrayAccess() throws org.apache.commons.jexl2.parser.ParseException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:37)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:61)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r7 = this;
        r6 = 1;
        r2 = new org.apache.commons.jexl2.parser.ASTArrayAccess;
        r3 = 48;
        r2.<init>(r3);
        r0 = 1;
        r3 = r7.jjtree;
        r3.openNodeScope(r2);
        r7.jjtreeOpenNodeScope(r2);
        r7.Identifier();	 Catch:{ Throwable -> 0x0041 }
    L_0x0014:
        r3 = 27;	 Catch:{ Throwable -> 0x0041 }
        r7.jj_consume_token(r3);	 Catch:{ Throwable -> 0x0041 }
        r7.Expression();	 Catch:{ Throwable -> 0x0041 }
        r3 = 28;	 Catch:{ Throwable -> 0x0041 }
        r7.jj_consume_token(r3);	 Catch:{ Throwable -> 0x0041 }
        r3 = r7.jj_ntk;	 Catch:{ Throwable -> 0x0041 }
        r4 = -1;	 Catch:{ Throwable -> 0x0041 }
        if (r3 == r4) goto L_0x003c;	 Catch:{ Throwable -> 0x0041 }
    L_0x0026:
        r3 = r7.jj_ntk;	 Catch:{ Throwable -> 0x0041 }
    L_0x0028:
        switch(r3) {
            case 27: goto L_0x0014;
            default: goto L_0x002b;
        };	 Catch:{ Throwable -> 0x0041 }
    L_0x002b:
        r3 = r7.jj_la1;	 Catch:{ Throwable -> 0x0041 }
        r4 = 41;	 Catch:{ Throwable -> 0x0041 }
        r5 = r7.jj_gen;	 Catch:{ Throwable -> 0x0041 }
        r3[r4] = r5;	 Catch:{ Throwable -> 0x0041 }
        r3 = r7.jjtree;
        r3.closeNodeScope(r2, r6);
        r7.jjtreeCloseNodeScope(r2);
        return;
    L_0x003c:
        r3 = r7.jj_ntk();	 Catch:{ Throwable -> 0x0041 }
        goto L_0x0028;
    L_0x0041:
        r1 = move-exception;
        r3 = r7.jjtree;	 Catch:{ all -> 0x0053 }
        r3.clearNodeScope(r2);	 Catch:{ all -> 0x0053 }
        r0 = 0;	 Catch:{ all -> 0x0053 }
        r3 = r1 instanceof java.lang.RuntimeException;	 Catch:{ all -> 0x0053 }
        if (r3 != 0) goto L_0x0057;	 Catch:{ all -> 0x0053 }
    L_0x004c:
        r3 = r1 instanceof org.apache.commons.jexl2.parser.ParseException;	 Catch:{ all -> 0x0053 }
        if (r3 != 0) goto L_0x005a;	 Catch:{ all -> 0x0053 }
    L_0x0050:
        r1 = (java.lang.Error) r1;	 Catch:{ all -> 0x0053 }
        throw r1;	 Catch:{ all -> 0x0053 }
    L_0x0053:
        r3 = move-exception;
        if (r0 != 0) goto L_0x005d;
    L_0x0056:
        throw r3;
    L_0x0057:
        r1 = (java.lang.RuntimeException) r1;	 Catch:{ all -> 0x0053 }
        throw r1;	 Catch:{ all -> 0x0053 }
    L_0x005a:
        r1 = (org.apache.commons.jexl2.parser.ParseException) r1;	 Catch:{ all -> 0x0053 }
        throw r1;	 Catch:{ all -> 0x0053 }
    L_0x005d:
        r4 = r7.jjtree;
        r4.closeNodeScope(r2, r6);
        r7.jjtreeCloseNodeScope(r2);
        goto L_0x0056;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.jexl2.parser.Parser.ArrayAccess():void");
    }

    public final void DotReference() throws ParseException {
        while (true) {
            switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                case 32:
                    jj_consume_token(32);
                    if (jj_2_13(Integer.MAX_VALUE)) {
                        ArrayAccess();
                    } else {
                        int i;
                        if (this.jj_ntk != -1) {
                            i = this.jj_ntk;
                        } else {
                            i = jj_ntk();
                        }
                        switch (i) {
                            case 17:
                            case 56:
                            case 59:
                            case 60:
                            case 62:
                                if (jj_2_12(2)) {
                                    AnyMethod();
                                    break;
                                }
                                if (this.jj_ntk != -1) {
                                    i = this.jj_ntk;
                                } else {
                                    i = jj_ntk();
                                }
                                switch (i) {
                                    case 56:
                                    case 59:
                                        Identifier();
                                        break;
                                    case 60:
                                        IntegerLiteral();
                                        break;
                                    case 62:
                                        StringIdentifier();
                                        break;
                                    default:
                                        this.jj_la1[43] = this.jj_gen;
                                        jj_consume_token(-1);
                                        throw new ParseException();
                                }
                            default:
                                this.jj_la1[44] = this.jj_gen;
                                jj_consume_token(-1);
                                throw new ParseException();
                        }
                    }
                default:
                    this.jj_la1[42] = this.jj_gen;
                    return;
            }
        }
    }

    public final void Reference() throws ParseException {
        Node jjtn000 = new ASTReference(10);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            if (jj_2_14(Integer.MAX_VALUE)) {
                Constructor();
            } else if (jj_2_15(Integer.MAX_VALUE)) {
                ArrayAccess();
            } else if (jj_2_16(Integer.MAX_VALUE)) {
                Function();
            } else if (jj_2_17(Integer.MAX_VALUE)) {
                Method();
            } else if (jj_2_18(Integer.MAX_VALUE)) {
                MapLiteral();
            } else if (jj_2_19(Integer.MAX_VALUE)) {
                ArrayLiteral();
            } else if (jj_2_20(Integer.MAX_VALUE)) {
                ReferenceExpression();
            } else {
                switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                    case 56:
                    case 59:
                        Identifier(true);
                        break;
                    case 62:
                        StringLiteral();
                        break;
                    default:
                        this.jj_la1[45] = this.jj_gen;
                        jj_consume_token(-1);
                        throw new ParseException();
                }
            }
            DotReference();
            this.jjtree.closeNodeScope(jjtn000, true);
            jjtreeCloseNodeScope(jjtn000);
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    public final void ReferenceExpression() throws ParseException {
        Node jjtn000 = new ASTReferenceExpression(49);
        boolean jjtc000 = true;
        this.jjtree.openNodeScope(jjtn000);
        jjtreeOpenNodeScope(jjtn000);
        try {
            jj_consume_token(23);
            Expression();
            jj_consume_token(24);
            while (true) {
                switch (this.jj_ntk != -1 ? this.jj_ntk : jj_ntk()) {
                    case 27:
                        jj_consume_token(27);
                        Expression();
                        jj_consume_token(28);
                    default:
                        this.jj_la1[46] = this.jj_gen;
                        this.jjtree.closeNodeScope(jjtn000, true);
                        jjtreeCloseNodeScope(jjtn000);
                        return;
                }
            }
        } catch (Throwable th) {
            if (jjtc000) {
                this.jjtree.closeNodeScope(jjtn000, true);
                jjtreeCloseNodeScope(jjtn000);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_1(int xla) {
        boolean z = true;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (jj_3_1()) {
                z = false;
            }
            jj_save(0, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(0, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_2(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_2()) {
                z = true;
            }
            jj_save(1, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(1, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_3(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_3()) {
                z = true;
            }
            jj_save(2, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(2, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_4(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_4()) {
                z = true;
            }
            jj_save(3, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(3, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_5(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_5()) {
                z = true;
            }
            jj_save(4, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(4, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_6(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_6()) {
                z = true;
            }
            jj_save(5, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(5, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_7(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_7()) {
                z = true;
            }
            jj_save(6, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(6, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_8(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_8()) {
                z = true;
            }
            jj_save(7, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(7, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_9(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_9()) {
                z = true;
            }
            jj_save(8, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(8, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_10(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_10()) {
                z = true;
            }
            jj_save(9, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(9, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_11(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_11()) {
                z = true;
            }
            jj_save(10, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(10, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_12(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_12()) {
                z = true;
            }
            jj_save(11, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(11, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_13(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_13()) {
                z = true;
            }
            jj_save(12, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(12, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_14(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_14()) {
                z = true;
            }
            jj_save(13, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(13, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_15(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_15()) {
                z = true;
            }
            jj_save(14, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(14, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_16(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_16()) {
                z = true;
            }
            jj_save(15, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(15, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_17(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_17()) {
                z = true;
            }
            jj_save(16, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(16, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_18(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_18()) {
                z = true;
            }
            jj_save(17, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(17, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_19(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_19()) {
                z = true;
            }
            jj_save(18, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(18, xla);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean jj_2_20(int xla) {
        boolean z = false;
        this.jj_la = xla;
        Token token = this.token;
        this.jj_scanpos = token;
        this.jj_lastpos = token;
        try {
            if (!jj_3_20()) {
                z = true;
            }
            jj_save(19, xla);
            return z;
        } catch (LookaheadSuccess e) {
            return true;
        } catch (Throwable th) {
            jj_save(19, xla);
        }
    }

    private boolean jj_3R_94() {
        if (jj_3R_102()) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_103());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3R_113() {
        return jj_scan_token(50);
    }

    private boolean jj_3R_104() {
        Token xsp = this.jj_scanpos;
        if (jj_3R_112()) {
            this.jj_scanpos = xsp;
            if (jj_3R_113()) {
                return true;
            }
        }
        return false;
    }

    private boolean jj_3R_112() {
        return jj_scan_token(49);
    }

    private boolean jj_3R_90() {
        if (jj_3R_94()) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_95());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3R_101() {
        return jj_scan_token(40) || jj_3R_90();
    }

    private boolean jj_3R_100() {
        return jj_scan_token(39) || jj_3R_90();
    }

    private boolean jj_3R_99() {
        return jj_scan_token(42) || jj_3R_90();
    }

    private boolean jj_3R_98() {
        return jj_scan_token(44) || jj_3R_90();
    }

    private boolean jj_3R_97() {
        return jj_scan_token(41) || jj_3R_90();
    }

    private boolean jj_3R_91() {
        Token xsp = this.jj_scanpos;
        if (jj_3R_96()) {
            this.jj_scanpos = xsp;
            if (jj_3R_97()) {
                this.jj_scanpos = xsp;
                if (jj_3R_98()) {
                    this.jj_scanpos = xsp;
                    if (jj_3R_99()) {
                        this.jj_scanpos = xsp;
                        if (jj_3R_100()) {
                            this.jj_scanpos = xsp;
                            if (jj_3R_101()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean jj_3R_96() {
        return jj_scan_token(43) || jj_3R_90();
    }

    private boolean jj_3R_88() {
        if (jj_3R_90()) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (jj_3R_91()) {
            this.jj_scanpos = xsp;
        }
        return false;
    }

    private boolean jj_3R_93() {
        return jj_scan_token(38) || jj_3R_88();
    }

    private boolean jj_3R_89() {
        Token xsp = this.jj_scanpos;
        if (jj_3R_92()) {
            this.jj_scanpos = xsp;
            if (jj_3R_93()) {
                return true;
            }
        }
        return false;
    }

    private boolean jj_3R_92() {
        return jj_scan_token(37) || jj_3R_88();
    }

    private boolean jj_3R_86() {
        if (jj_3R_88()) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (jj_3R_89()) {
            this.jj_scanpos = xsp;
        }
        return false;
    }

    private boolean jj_3R_87() {
        return jj_scan_token(53) || jj_3R_86();
    }

    private boolean jj_3R_84() {
        if (jj_3R_86()) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_87());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3R_85() {
        return jj_scan_token(55) || jj_3R_84();
    }

    private boolean jj_3R_82() {
        if (jj_3R_84()) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_85());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3R_83() {
        return jj_scan_token(54) || jj_3R_82();
    }

    private boolean jj_3R_75() {
        if (jj_3R_82()) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_83());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3R_77() {
        return jj_scan_token(35) || jj_3R_75();
    }

    private boolean jj_3R_62() {
        if (jj_3R_75()) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_77());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3R_66() {
        return jj_scan_token(36) || jj_3R_62();
    }

    private boolean jj_3R_44() {
        if (jj_3R_62()) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_66());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3R_68() {
        return jj_scan_token(34) || jj_3R_20();
    }

    private boolean jj_3R_67() {
        return jj_scan_token(33) || jj_3R_20() || jj_scan_token(30) || jj_3R_20();
    }

    private boolean jj_3R_55() {
        Token xsp = this.jj_scanpos;
        if (jj_3R_67()) {
            this.jj_scanpos = xsp;
            if (jj_3R_68()) {
                return true;
            }
        }
        return false;
    }

    private boolean jj_3R_27() {
        if (jj_3R_44()) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (jj_3R_55()) {
            this.jj_scanpos = xsp;
        }
        return false;
    }

    private boolean jj_3R_42() {
        return jj_scan_token(45) || jj_3R_20();
    }

    private boolean jj_3R_142() {
        return jj_scan_token(27) || jj_3R_20() || jj_scan_token(28);
    }

    private boolean jj_3_2() {
        return jj_scan_token(29);
    }

    private boolean jj_3R_81() {
        return jj_scan_token(56);
    }

    private boolean jj_3R_74() {
        return jj_scan_token(15) || jj_3R_81();
    }

    private boolean jj_3R_20() {
        if (jj_3R_27()) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (jj_3R_42()) {
            this.jj_scanpos = xsp;
        }
        return false;
    }

    private boolean jj_3R_73() {
        return jj_scan_token(21) || jj_3R_20();
    }

    private boolean jj_3R_51() {
        if (jj_scan_token(23) || jj_3R_20() || jj_scan_token(24)) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_142());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3_20() {
        return jj_scan_token(23) || jj_3R_20();
    }

    private boolean jj_3R_79() {
        return jj_scan_token(12) || jj_scan_token(23);
    }

    private boolean jj_3R_70() {
        Token xsp = this.jj_scanpos;
        if (jj_3R_78()) {
            this.jj_scanpos = xsp;
            if (jj_3R_79()) {
                return true;
            }
        }
        return false;
    }

    private boolean jj_3R_78() {
        return jj_scan_token(11) || jj_scan_token(23);
    }

    private boolean jj_3_19() {
        return jj_scan_token(27);
    }

    private boolean jj_3_18() {
        return jj_scan_token(25);
    }

    private boolean jj_3R_80() {
        return jj_3R_20();
    }

    private boolean jj_3R_38() {
        return jj_3R_21();
    }

    private boolean jj_3_17() {
        return jj_3R_21() || jj_scan_token(23);
    }

    private boolean jj_3R_71() {
        return jj_scan_token(13) || jj_scan_token(23);
    }

    private boolean jj_3R_37() {
        return jj_3R_52();
    }

    private boolean jj_3_16() {
        return jj_3R_21() || jj_scan_token(30) || jj_3R_21() || jj_scan_token(23);
    }

    private boolean jj_3R_36() {
        return jj_3R_51();
    }

    private boolean jj_3_15() {
        return jj_3R_21() || jj_scan_token(27);
    }

    private boolean jj_3R_35() {
        return jj_3R_50();
    }

    private boolean jj_3_14() {
        return jj_scan_token(14);
    }

    private boolean jj_3R_69() {
        return jj_scan_token(9) || jj_scan_token(23);
    }

    private boolean jj_3R_34() {
        return jj_3R_49();
    }

    private boolean jj_3R_26() {
        return jj_3R_43();
    }

    private boolean jj_3R_33() {
        return jj_3R_48();
    }

    private boolean jj_3R_32() {
        return jj_3R_47();
    }

    private boolean jj_3R_72() {
        if (jj_3R_20()) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_80());
        this.jj_scanpos = xsp;
        xsp = this.jj_scanpos;
        if (jj_3_2()) {
            this.jj_scanpos = xsp;
        }
        return false;
    }

    private boolean jj_3R_31() {
        return jj_3R_46();
    }

    private boolean jj_3R_149() {
        return jj_3R_150();
    }

    private boolean jj_3R_30() {
        return jj_3R_45();
    }

    private boolean jj_3R_148() {
        return jj_3R_131();
    }

    private boolean jj_3R_22() {
        Token xsp = this.jj_scanpos;
        if (jj_3R_30()) {
            this.jj_scanpos = xsp;
            if (jj_3R_31()) {
                this.jj_scanpos = xsp;
                if (jj_3R_32()) {
                    this.jj_scanpos = xsp;
                    if (jj_3R_33()) {
                        this.jj_scanpos = xsp;
                        if (jj_3R_34()) {
                            this.jj_scanpos = xsp;
                            if (jj_3R_35()) {
                                this.jj_scanpos = xsp;
                                if (jj_3R_36()) {
                                    this.jj_scanpos = xsp;
                                    if (jj_3R_37()) {
                                        this.jj_scanpos = xsp;
                                        if (jj_3R_38()) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return jj_3R_39();
    }

    private boolean jj_3R_147() {
        return jj_3R_21();
    }

    private boolean jj_3R_19() {
        if (jj_scan_token(25)) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_26());
        this.jj_scanpos = xsp;
        return jj_scan_token(26);
    }

    private boolean jj_3_13() {
        return jj_3R_21() || jj_scan_token(27);
    }

    private boolean jj_3R_61() {
        return jj_3R_74();
    }

    private boolean jj_3R_60() {
        return jj_3R_73();
    }

    private boolean jj_3R_59() {
        return jj_3R_72();
    }

    private boolean jj_3R_58() {
        return jj_3R_71();
    }

    private boolean jj_3R_63() {
        return jj_scan_token(27) || jj_3R_20() || jj_scan_token(28);
    }

    private boolean jj_3_12() {
        return jj_3R_25();
    }

    private boolean jj_3R_24() {
        return jj_3R_20();
    }

    private boolean jj_3R_57() {
        return jj_3R_70();
    }

    private boolean jj_3R_144() {
        Token xsp = this.jj_scanpos;
        if (jj_3_12()) {
            this.jj_scanpos = xsp;
            if (jj_3R_147()) {
                this.jj_scanpos = xsp;
                if (jj_3R_148()) {
                    this.jj_scanpos = xsp;
                    if (jj_3R_149()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean jj_3R_56() {
        return jj_3R_69();
    }

    private boolean jj_3_1() {
        return jj_3R_19();
    }

    private boolean jj_3R_23() {
        return jj_3R_20();
    }

    private boolean jj_3R_145() {
        return jj_scan_token(31) || jj_3R_20();
    }

    private boolean jj_3R_143() {
        return jj_3R_46();
    }

    private boolean jj_3R_43() {
        Token xsp = this.jj_scanpos;
        if (jj_scan_token(29)) {
            this.jj_scanpos = xsp;
            if (jj_3_1()) {
                this.jj_scanpos = xsp;
                if (jj_3R_56()) {
                    this.jj_scanpos = xsp;
                    if (jj_3R_57()) {
                        this.jj_scanpos = xsp;
                        if (jj_3R_58()) {
                            this.jj_scanpos = xsp;
                            if (jj_3R_59()) {
                                this.jj_scanpos = xsp;
                                if (jj_3R_60()) {
                                    this.jj_scanpos = xsp;
                                    if (jj_3R_61()) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean jj_3R_53() {
        if (jj_scan_token(32)) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (jj_3R_143()) {
            this.jj_scanpos = xsp;
            if (jj_3R_144()) {
                return true;
            }
        }
        return false;
    }

    private boolean jj_3R_39() {
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_53());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3_11() {
        if (jj_scan_token(27)) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (jj_3R_24()) {
            this.jj_scanpos = xsp;
            if (jj_scan_token(28)) {
                return true;
            }
        }
        return false;
    }

    private boolean jj_3R_135() {
        return jj_scan_token(31) || jj_3R_20();
    }

    private boolean jj_3R_46() {
        if (jj_3R_21() || jj_3R_63()) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_63());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3_10() {
        if (jj_scan_token(25)) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (jj_3R_23()) {
            this.jj_scanpos = xsp;
        }
        return jj_scan_token(30);
    }

    private boolean jj_3_9() {
        return jj_scan_token(14) || jj_scan_token(23);
    }

    private boolean jj_3_8() {
        return jj_scan_token(17);
    }

    private boolean jj_3_7() {
        return jj_scan_token(16);
    }

    private boolean jj_3R_140() {
        if (jj_3R_20()) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_145());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3R_146() {
        return jj_scan_token(31) || jj_3R_20();
    }

    private boolean jj_3R_120() {
        return jj_3R_123();
    }

    private boolean jj_3R_119() {
        return jj_3R_50();
    }

    private boolean jj_3R_118() {
        return jj_3R_49();
    }

    private boolean jj_3R_117() {
        return jj_3R_45();
    }

    private boolean jj_3R_130() {
        if (jj_3R_20()) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_135());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3R_116() {
        return jj_3R_122();
    }

    private boolean jj_3R_115() {
        return jj_3R_121();
    }

    private boolean jj_3_6() {
        return jj_3R_22();
    }

    private boolean jj_3R_114() {
        Token xsp = this.jj_scanpos;
        if (jj_3_6()) {
            this.jj_scanpos = xsp;
            if (jj_3R_115()) {
                this.jj_scanpos = xsp;
                if (jj_3R_116()) {
                    this.jj_scanpos = xsp;
                    if (jj_3R_117()) {
                        this.jj_scanpos = xsp;
                        if (jj_3R_118()) {
                            this.jj_scanpos = xsp;
                            if (jj_3R_119()) {
                                this.jj_scanpos = xsp;
                                if (jj_3R_120()) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean jj_3R_141() {
        if (jj_3R_20()) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_146());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3_5() {
        return jj_3R_21() || jj_scan_token(23);
    }

    private boolean jj_3R_45() {
        if (jj_scan_token(14) || jj_scan_token(23)) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (jj_3R_130()) {
            this.jj_scanpos = xsp;
        }
        return jj_scan_token(24);
    }

    private boolean jj_3_4() {
        return jj_scan_token(17);
    }

    private boolean jj_3R_54() {
        return jj_scan_token(17) || jj_scan_token(23) || jj_scan_token(24);
    }

    private boolean jj_3R_41() {
        return jj_3R_48();
    }

    private boolean jj_3R_40() {
        return jj_3R_54();
    }

    private boolean jj_3R_25() {
        Token xsp = this.jj_scanpos;
        if (jj_3R_40()) {
            this.jj_scanpos = xsp;
            if (jj_3R_41()) {
                return true;
            }
        }
        return false;
    }

    private boolean jj_3R_48() {
        if (jj_3R_21() || jj_scan_token(23)) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (jj_3R_141()) {
            this.jj_scanpos = xsp;
        }
        return jj_scan_token(24);
    }

    private boolean jj_3R_47() {
        if (jj_3R_21() || jj_scan_token(30) || jj_3R_21() || jj_scan_token(23)) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (jj_3R_140()) {
            this.jj_scanpos = xsp;
        }
        return jj_scan_token(24);
    }

    private boolean jj_3R_122() {
        return jj_scan_token(17) || jj_scan_token(23) || jj_3R_20() || jj_scan_token(24);
    }

    private boolean jj_3R_124() {
        return jj_scan_token(16) || jj_3R_22();
    }

    private boolean jj_3_3() {
        return jj_scan_token(16) || jj_scan_token(23) || jj_3R_20() || jj_scan_token(24);
    }

    private boolean jj_3R_121() {
        Token xsp = this.jj_scanpos;
        if (jj_3_3()) {
            this.jj_scanpos = xsp;
            if (jj_3R_124()) {
                return true;
            }
        }
        return false;
    }

    private boolean jj_3R_137() {
        return jj_scan_token(31) || jj_3R_20();
    }

    private boolean jj_3R_136() {
        return jj_scan_token(31) || jj_3R_76();
    }

    private boolean jj_3R_76() {
        return jj_3R_20() || jj_scan_token(30) || jj_3R_20();
    }

    private boolean jj_3R_64() {
        if (jj_3R_76()) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_136());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3R_65() {
        if (jj_3R_20()) {
            return true;
        }
        Token xsp;
        do {
            xsp = this.jj_scanpos;
        } while (!jj_3R_137());
        this.jj_scanpos = xsp;
        return false;
    }

    private boolean jj_3R_49() {
        if (jj_scan_token(25)) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (jj_3R_64()) {
            this.jj_scanpos = xsp;
            if (jj_scan_token(30)) {
                return true;
            }
        }
        return jj_scan_token(26);
    }

    private boolean jj_3R_50() {
        if (jj_scan_token(27)) {
            return true;
        }
        Token xsp = this.jj_scanpos;
        if (jj_3R_65()) {
            this.jj_scanpos = xsp;
        }
        return jj_scan_token(28);
    }

    private boolean jj_3R_52() {
        return jj_scan_token(62);
    }

    private boolean jj_3R_132() {
        return jj_scan_token(61);
    }

    private boolean jj_3R_131() {
        return jj_scan_token(60);
    }

    private boolean jj_3R_139() {
        return jj_scan_token(20);
    }

    private boolean jj_3R_133() {
        Token xsp = this.jj_scanpos;
        if (jj_3R_138()) {
            this.jj_scanpos = xsp;
            if (jj_3R_139()) {
                return true;
            }
        }
        return false;
    }

    private boolean jj_3R_138() {
        return jj_scan_token(19);
    }

    private boolean jj_3R_134() {
        return jj_scan_token(18);
    }

    private boolean jj_3R_129() {
        return jj_3R_134();
    }

    private boolean jj_3R_128() {
        return jj_3R_52();
    }

    private boolean jj_3R_127() {
        return jj_3R_133();
    }

    private boolean jj_3R_126() {
        return jj_3R_132();
    }

    private boolean jj_3R_123() {
        Token xsp = this.jj_scanpos;
        if (jj_3R_125()) {
            this.jj_scanpos = xsp;
            if (jj_3R_126()) {
                this.jj_scanpos = xsp;
                if (jj_3R_127()) {
                    this.jj_scanpos = xsp;
                    if (jj_3R_128()) {
                        this.jj_scanpos = xsp;
                        if (jj_3R_129()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean jj_3R_125() {
        return jj_3R_131();
    }

    private boolean jj_3R_150() {
        return jj_scan_token(62);
    }

    private boolean jj_3R_29() {
        return jj_scan_token(59);
    }

    private boolean jj_3R_21() {
        Token xsp = this.jj_scanpos;
        if (jj_3R_28()) {
            this.jj_scanpos = xsp;
            if (jj_3R_29()) {
                return true;
            }
        }
        return false;
    }

    private boolean jj_3R_28() {
        return jj_scan_token(56);
    }

    private boolean jj_3R_95() {
        return jj_3R_104() || jj_3R_94();
    }

    private boolean jj_3R_108() {
        return jj_3R_114();
    }

    private boolean jj_3R_107() {
        return jj_scan_token(48) || jj_3R_102();
    }

    private boolean jj_3R_106() {
        return jj_scan_token(52) || jj_3R_102();
    }

    private boolean jj_3R_102() {
        Token xsp = this.jj_scanpos;
        if (jj_3R_105()) {
            this.jj_scanpos = xsp;
            if (jj_3R_106()) {
                this.jj_scanpos = xsp;
                if (jj_3R_107()) {
                    this.jj_scanpos = xsp;
                    if (jj_3R_108()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean jj_3R_105() {
        return jj_scan_token(50) || jj_3R_102();
    }

    private boolean jj_3R_111() {
        return jj_scan_token(46) || jj_3R_102();
    }

    private boolean jj_3R_110() {
        return jj_scan_token(47) || jj_3R_102();
    }

    private boolean jj_3R_103() {
        Token xsp = this.jj_scanpos;
        if (jj_3R_109()) {
            this.jj_scanpos = xsp;
            if (jj_3R_110()) {
                this.jj_scanpos = xsp;
                if (jj_3R_111()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean jj_3R_109() {
        return jj_scan_token(51) || jj_3R_102();
    }

    static {
        jj_la1_init_0();
        jj_la1_init_1();
    }

    private static void jj_la1_init_0() {
        jj_la1_0 = new int[]{717224448, 536870912, 180353536, 717224448, 178208768, 1024, 6144, 0, 0, 32768, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 178208768, 0, 1835008, 1572864, Integer.MIN_VALUE, 178208768, Integer.MIN_VALUE, 1251950592, 65536, Integer.MIN_VALUE, 178208768, Integer.MIN_VALUE, 178208768, Integer.MIN_VALUE, 178208768, 1835008, 134217728, 0, 0, 131072, 0, 134217728};
    }

    private static void jj_la1_init_1() {
        jj_la1_1 = new int[]{2031419392, 0, 2031419392, 2031419392, 2031419392, 0, 0, 8192, 8192, 150994944, 6, 6, 16, 8, 4194304, 8388608, 2097152, 96, 96, 8064, 8064, 393216, 393216, 573440, 573440, 2031419392, 150994944, 1879048192, 0, 0, 2031419392, 0, 2031419392, 0, 0, 2031419392, 0, 2031419392, 0, 2031419392, 1879048192, 0, 1, 1493172224, 1493172224, 1224736768, 0};
    }

    public Parser(Reader stream) {
        int i;
        this.jj_input_stream = new SimpleCharStream(stream, 1, 1);
        this.token_source = new ParserTokenManager(this.jj_input_stream);
        this.token = new Token();
        this.jj_ntk = -1;
        this.jj_gen = 0;
        for (i = 0; i < 47; i++) {
            this.jj_la1[i] = -1;
        }
        for (i = 0; i < this.jj_2_rtns.length; i++) {
            this.jj_2_rtns[i] = new JJCalls();
        }
    }

    public void ReInit(Reader stream) {
        int i;
        this.jj_input_stream.ReInit(stream, 1, 1);
        this.token_source.ReInit(this.jj_input_stream);
        this.token = new Token();
        this.jj_ntk = -1;
        this.jjtree.reset();
        this.jj_gen = 0;
        for (i = 0; i < 47; i++) {
            this.jj_la1[i] = -1;
        }
        for (i = 0; i < this.jj_2_rtns.length; i++) {
            this.jj_2_rtns[i] = new JJCalls();
        }
    }

    private Token jj_consume_token(int kind) throws ParseException {
        Token oldToken = this.token;
        if (oldToken.next == null) {
            Token token = this.token;
            Token nextToken = this.token_source.getNextToken();
            token.next = nextToken;
            this.token = nextToken;
        } else {
            this.token = this.token.next;
        }
        this.jj_ntk = -1;
        if (this.token.kind != kind) {
            this.token = oldToken;
            this.jj_kind = kind;
            throw generateParseException();
        }
        this.jj_gen++;
        int i = this.jj_gc + 1;
        this.jj_gc = i;
        if (i > 100) {
            this.jj_gc = 0;
            for (JJCalls c : this.jj_2_rtns) {
                for (JJCalls c2 = this.jj_2_rtns[i]; c2 != null; c2 = c2.next) {
                    if (c2.gen < this.jj_gen) {
                        c2.first = null;
                    }
                }
            }
        }
        return this.token;
    }

    private boolean jj_scan_token(int kind) {
        if (this.jj_scanpos != this.jj_lastpos) {
            this.jj_scanpos = this.jj_scanpos.next;
        } else {
            this.jj_la--;
            Token token;
            if (this.jj_scanpos.next != null) {
                token = this.jj_scanpos.next;
                this.jj_scanpos = token;
                this.jj_lastpos = token;
            } else {
                token = this.jj_scanpos;
                Token nextToken = this.token_source.getNextToken();
                token.next = nextToken;
                this.jj_scanpos = nextToken;
                this.jj_lastpos = nextToken;
            }
        }
        if (this.jj_rescan) {
            int i = 0;
            Token tok = this.token;
            while (tok != null && tok != this.jj_scanpos) {
                i++;
                tok = tok.next;
            }
            if (tok != null) {
                jj_add_error_token(kind, i);
            }
        }
        if (this.jj_scanpos.kind != kind) {
            return true;
        }
        if (this.jj_la != 0 || this.jj_scanpos != this.jj_lastpos) {
            return false;
        }
        throw this.jj_ls;
    }

    public final Token getToken(int index) {
        int i = 0;
        Token t = this.token;
        while (i < index) {
            Token t2;
            if (t.next == null) {
                t2 = this.token_source.getNextToken();
                t.next = t2;
            } else {
                t2 = t.next;
            }
            i++;
            t = t2;
        }
        return t;
    }

    private int jj_ntk() {
        Token token = this.token.next;
        this.jj_nt = token;
        if (token != null) {
            int i = this.jj_nt.kind;
            this.jj_ntk = i;
            return i;
        }
        token = this.token;
        Token nextToken = this.token_source.getNextToken();
        token.next = nextToken;
        i = nextToken.kind;
        this.jj_ntk = i;
        return i;
    }

    private void jj_add_error_token(int kind, int pos) {
        if (pos < 100) {
            int[] iArr;
            if (pos == this.jj_endpos + 1) {
                iArr = this.jj_lasttokens;
                int i = this.jj_endpos;
                this.jj_endpos = i + 1;
                iArr[i] = kind;
            } else if (this.jj_endpos != 0) {
                int i2;
                this.jj_expentry = new int[this.jj_endpos];
                for (i2 = 0; i2 < this.jj_endpos; i2++) {
                    this.jj_expentry[i2] = this.jj_lasttokens[i2];
                }
                loop1:
                for (int[] oldentry : this.jj_expentries) {
                    if (oldentry.length == this.jj_expentry.length) {
                        i2 = 0;
                        while (i2 < this.jj_expentry.length) {
                            if (oldentry[i2] == this.jj_expentry[i2]) {
                                i2++;
                            }
                        }
                        this.jj_expentries.add(this.jj_expentry);
                        break loop1;
                    }
                }
                if (pos != 0) {
                    iArr = this.jj_lasttokens;
                    this.jj_endpos = pos;
                    iArr[pos - 1] = kind;
                }
            }
        }
    }

    public ParseException generateParseException() {
        int i;
        this.jj_expentries.clear();
        boolean[] la1tokens = new boolean[63];
        if (this.jj_kind >= 0) {
            la1tokens[this.jj_kind] = true;
            this.jj_kind = -1;
        }
        for (i = 0; i < 47; i++) {
            if (this.jj_la1[i] == this.jj_gen) {
                for (int j = 0; j < 32; j++) {
                    if ((jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                    if ((jj_la1_1[i] & (1 << j)) != 0) {
                        la1tokens[j + 32] = true;
                    }
                }
            }
        }
        for (i = 0; i < 63; i++) {
            if (la1tokens[i]) {
                this.jj_expentry = new int[1];
                this.jj_expentry[0] = i;
                this.jj_expentries.add(this.jj_expentry);
            }
        }
        this.jj_endpos = 0;
        jj_rescan_token();
        jj_add_error_token(0, 0);
        int[][] exptokseq = new int[this.jj_expentries.size()][];
        for (i = 0; i < this.jj_expentries.size(); i++) {
            exptokseq[i] = (int[]) this.jj_expentries.get(i);
        }
        return new ParseException(this.token, exptokseq, tokenImage);
    }

    private void jj_rescan_token() {
        this.jj_rescan = true;
        for (int i = 0; i < 20; i++) {
            try {
                JJCalls p = this.jj_2_rtns[i];
                do {
                    if (p.gen > this.jj_gen) {
                        this.jj_la = p.arg;
                        Token token = p.first;
                        this.jj_scanpos = token;
                        this.jj_lastpos = token;
                        switch (i) {
                            case 0:
                                jj_3_1();
                                break;
                            case 1:
                                jj_3_2();
                                break;
                            case 2:
                                jj_3_3();
                                break;
                            case 3:
                                jj_3_4();
                                break;
                            case 4:
                                jj_3_5();
                                break;
                            case 5:
                                jj_3_6();
                                break;
                            case 6:
                                jj_3_7();
                                break;
                            case 7:
                                jj_3_8();
                                break;
                            case 8:
                                jj_3_9();
                                break;
                            case 9:
                                jj_3_10();
                                break;
                            case 10:
                                jj_3_11();
                                break;
                            case 11:
                                jj_3_12();
                                break;
                            case 12:
                                jj_3_13();
                                break;
                            case 13:
                                jj_3_14();
                                break;
                            case 14:
                                jj_3_15();
                                break;
                            case 15:
                                jj_3_16();
                                break;
                            case 16:
                                jj_3_17();
                                break;
                            case 17:
                                jj_3_18();
                                break;
                            case 18:
                                jj_3_19();
                                break;
                            case 19:
                                jj_3_20();
                                break;
                            default:
                                break;
                        }
                    }
                    p = p.next;
                } while (p != null);
            } catch (LookaheadSuccess e) {
            }
        }
        this.jj_rescan = false;
    }

    private void jj_save(int index, int xla) {
        JJCalls p = this.jj_2_rtns[index];
        while (p.gen > this.jj_gen) {
            if (p.next == null) {
                JJCalls p2 = new JJCalls();
                p.next = p2;
                p = p2;
                break;
            }
            p = p.next;
        }
        p.gen = (this.jj_gen + xla) - this.jj_la;
        p.first = this.token;
        p.arg = xla;
    }
}
