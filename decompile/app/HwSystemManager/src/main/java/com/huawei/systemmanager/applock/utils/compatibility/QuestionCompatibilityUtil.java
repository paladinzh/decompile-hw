package com.huawei.systemmanager.applock.utils.compatibility;

import android.content.Context;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import java.util.List;

public class QuestionCompatibilityUtil {
    public static final int CUSTOMIZED_QUESTION_INDEX = -1;
    public static final int NOT_EXiST_IDX = 5;
    public static final int SET_CUSTOMIZED_QUESTION_INDEX = 100;
    public static final int VALID_MAX_QUESTION_IDX = 10;

    public static boolean validIndex(int index) {
        return -1 <= index && 10 >= index && 5 != index;
    }

    public static List<QuestionItem> getProtectionQuestionList(Context ctx, String tempCustomQuestion) {
        int index = AppLockPwdUtils.getProtectionQuestionIndex(ctx);
        List<QuestionItem> questionItems = Lists.newArrayList();
        if (TextUtils.isEmpty(tempCustomQuestion)) {
            String customQuestion = AppLockPwdUtils.getCustomProtectionQuestion(ctx);
            if (!TextUtils.isEmpty(customQuestion)) {
                questionItems.add(new QuestionItem(customQuestion, -1));
            }
        } else {
            questionItems.add(new QuestionItem(tempCustomQuestion, -1));
        }
        switch (index) {
            case 0:
                questionItems.add(new QuestionItem(ctx.getString(R.string.TextField_SettingsPasswordProtectionQuestion_Q1), 1));
                break;
            case 1:
                questionItems.add(new QuestionItem(ctx.getString(R.string.TextField_SettingsPasswordProtectionQuestion_Q2), 2));
                break;
            case 2:
                questionItems.add(new QuestionItem(ctx.getString(R.string.TextField_SettingsPasswordProtectionQuestion_Q3), 3));
                break;
            case 3:
                questionItems.add(new QuestionItem(ctx.getString(R.string.TextField_SettingsPasswordProtectionQuestion_Q4), 4));
                break;
            case 4:
                questionItems.add(new QuestionItem(ctx.getString(R.string.TextField_SettingsPasswordProtectionQuestion_Q5), 5));
                break;
        }
        questionItems.add(new QuestionItem(ctx.getString(R.string.applock_protect_question_Q6), 6));
        questionItems.add(new QuestionItem(ctx.getString(R.string.applock_protect_question_Q7), 7));
        questionItems.add(new QuestionItem(ctx.getString(R.string.applock_protect_question_Q8), 8));
        questionItems.add(new QuestionItem(ctx.getString(R.string.applock_protect_question_Q9), 9));
        questionItems.add(new QuestionItem(ctx.getString(R.string.applock_protect_question_Q10), 10));
        questionItems.add(new QuestionItem(ctx.getString(R.string.applock_protect_question_Custom), 100));
        return questionItems;
    }

    public static String getVerifyQuestion(Context ctx) {
        int index = AppLockPwdUtils.getProtectionQuestionIndex(ctx);
        if (-1 == index) {
            return AppLockPwdUtils.getCustomProtectionQuestion(ctx);
        }
        switch (index) {
            case 0:
                return ctx.getString(R.string.TextField_SettingsPasswordProtectionQuestion_Q1);
            case 1:
                return ctx.getString(R.string.TextField_SettingsPasswordProtectionQuestion_Q2);
            case 2:
                return ctx.getString(R.string.TextField_SettingsPasswordProtectionQuestion_Q3);
            case 3:
                return ctx.getString(R.string.TextField_SettingsPasswordProtectionQuestion_Q4);
            case 4:
                return ctx.getString(R.string.TextField_SettingsPasswordProtectionQuestion_Q5);
            case 6:
                return ctx.getString(R.string.applock_protect_question_Q6);
            case 7:
                return ctx.getString(R.string.applock_protect_question_Q7);
            case 8:
                return ctx.getString(R.string.applock_protect_question_Q8);
            case 9:
                return ctx.getString(R.string.applock_protect_question_Q9);
            case 10:
                return ctx.getString(R.string.applock_protect_question_Q10);
            default:
                return "";
        }
    }
}
