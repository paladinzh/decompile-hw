package com.huawei.systemmanager.applock.utils.compatibility;

import android.content.Context;
import com.google.common.hash.Hashing;
import com.huawei.systemmanager.applock.utils.DatabaseSharePrefUtil;
import com.huawei.systemmanager.util.HwLog;
import java.io.IOException;
import java.security.SecureRandom;

public class AppLockPwdUtils {
    private static final String PASSWORD_KEY = "encrypt_password_sha256";
    private static final String PASSWORD_PROTECTION_ANSWER_KEY = "encrypt_password_protection_answer_sha256";
    private static final String PASSWORD_PROTECTION_ANSWER_SALT_KEY = "encrypt_password_protection_answer_sha256_salt";
    private static final String PASSWORD_PROTECTION_CUSTOMIZED_QUESTION_KEY = "password_protection_question";
    private static final String PASSWORD_PROTECTION_QUESTION_IDX_KEY = "encrypt_password_protection_question";
    private static final String PASSWORD_SALT_KEY = "encrypt_password_sha256_salt";
    private static final String TAG = "AppLockPwdUtils";

    public static boolean isPasswordSet(Context context) {
        if (DatabaseSharePrefUtil.getPref(context, PASSWORD_KEY, "", false).isEmpty()) {
            return false;
        }
        return true;
    }

    public static boolean verifyPassword(Context context, String input) {
        return verifyEncryptDataInner(context, PASSWORD_KEY, PASSWORD_SALT_KEY, input);
    }

    public static void setPassword(Context context, String input) {
        setEncryptDataInner(context, PASSWORD_KEY, PASSWORD_SALT_KEY, input);
    }

    public static boolean isPasswordProtectionSet(Context context) {
        if (DatabaseSharePrefUtil.getPref(context, PASSWORD_PROTECTION_ANSWER_KEY, "", false).isEmpty()) {
            return false;
        }
        return true;
    }

    public static boolean verifyPasswordProtection(Context context, String input) {
        return verifyEncryptDataInner(context, PASSWORD_PROTECTION_ANSWER_KEY, PASSWORD_PROTECTION_ANSWER_SALT_KEY, input);
    }

    public static void setQuestionAndAnswer(Context context, int questionIdx, String answer) {
        if (QuestionCompatibilityUtil.validIndex(questionIdx)) {
            DatabaseSharePrefUtil.setPref(context, PASSWORD_PROTECTION_QUESTION_IDX_KEY, questionIdx, false);
            setEncryptDataInner(context, PASSWORD_PROTECTION_ANSWER_KEY, PASSWORD_PROTECTION_ANSWER_SALT_KEY, answer);
            return;
        }
        HwLog.e(TAG, "setQuestionAndAnswer invalid index: " + questionIdx);
    }

    public static int getProtectionQuestionIndex(Context context) {
        return DatabaseSharePrefUtil.getPref(context, PASSWORD_PROTECTION_QUESTION_IDX_KEY, -1, false);
    }

    public static void setCustomProtectionQuestion(Context ctx, String question) {
        DatabaseSharePrefUtil.setPref(ctx, PASSWORD_PROTECTION_CUSTOMIZED_QUESTION_KEY, question, false);
    }

    public static String getCustomProtectionQuestion(Context ctx) {
        return DatabaseSharePrefUtil.getPref(ctx, PASSWORD_PROTECTION_CUSTOMIZED_QUESTION_KEY, "", false);
    }

    private static boolean compareEncryptString(String plainText, String encryptValue) {
        return encryptStringSha256(plainText).equals(encryptValue);
    }

    private static String encryptStringSha256(String plainText) {
        try {
            return Hashing.sha256().newHasher().putBytes(plainText.getBytes("UTF-8")).hash().toString();
        } catch (IOException e) {
            return "";
        }
    }

    private static void setEncryptDataInner(Context context, String encryptKey, String saltKey, String input) {
        String saltValue = String.valueOf(new SecureRandom().nextLong());
        DatabaseSharePrefUtil.setPref(context, saltKey, saltValue, false);
        DatabaseSharePrefUtil.setPref(context, encryptKey, encryptStringSha256(input + saltValue), false);
    }

    private static boolean verifyEncryptDataInner(Context context, String encryptKey, String saltKey, String input) {
        return compareEncryptString(input + DatabaseSharePrefUtil.getPref(context, saltKey, "", false), DatabaseSharePrefUtil.getPref(context, encryptKey, "", false));
    }
}
