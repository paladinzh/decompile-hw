package com.android.contacts;

@Deprecated
public final class TypePrecedence {
    private static final int[] TYPE_PRECEDENCE_EMAIL = new int[]{0, 1, 2, 3};
    private static final int[] TYPE_PRECEDENCE_IM = new int[]{0, 1, 2, 3};
    private static final int[] TYPE_PRECEDENCE_ORG = new int[]{0, 1, 2};
    private static final int[] TYPE_PRECEDENCE_PHONES = new int[]{0, 12, 2, 1, 3, 7, 5, 4, 6};
    private static final int[] TYPE_PRECEDENCE_POSTAL = new int[]{0, 1, 2, 3};

    private TypePrecedence() {
    }

    @Deprecated
    public static int getTypePrecedence(String mimetype, int type) {
        int[] typePrecedence = getTypePrecedenceList(mimetype);
        if (typePrecedence == null) {
            return -1;
        }
        for (int i = 0; i < typePrecedence.length; i++) {
            if (typePrecedence[i] == type) {
                return i;
            }
        }
        return typePrecedence.length;
    }

    @Deprecated
    private static int[] getTypePrecedenceList(String mimetype) {
        if (mimetype.equals("vnd.android.cursor.item/phone_v2")) {
            return TYPE_PRECEDENCE_PHONES;
        }
        if (mimetype.equals("vnd.android.cursor.item/email_v2")) {
            return TYPE_PRECEDENCE_EMAIL;
        }
        if (mimetype.equals("vnd.android.cursor.item/postal-address_v2")) {
            return TYPE_PRECEDENCE_POSTAL;
        }
        if (mimetype.equals("vnd.android.cursor.item/im")) {
            return TYPE_PRECEDENCE_IM;
        }
        if (mimetype.equals("vnd.android.cursor.item/video-chat-address")) {
            return TYPE_PRECEDENCE_IM;
        }
        if (mimetype.equals("vnd.android.cursor.item/organization")) {
            return TYPE_PRECEDENCE_ORG;
        }
        return null;
    }
}
