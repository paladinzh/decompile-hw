package com.huawei.systemmanager.applock.utils.compatibility;

public class QuestionItem {
    private String mQuestion;
    private int mQuestionIndex;

    public QuestionItem(String question, int index) {
        this.mQuestion = question;
        this.mQuestionIndex = index;
    }

    public int questionIndex() {
        return this.mQuestionIndex;
    }

    public String toString() {
        return this.mQuestion;
    }
}
