package com.huawei.systemmanager.comm.grule.rules;

import android.content.Context;
import com.huawei.systemmanager.comm.grule.GRuleException;

public class GPostRuleBase<T> implements IRule<T> {
    private int mMatchPost = 3;
    private int mMismatchPost = 3;
    private IRule<T> mRule;

    public interface PostOp {
        public static final int POST_CONTINUE = 2;
        public static final int POST_INVALID = 3;
        public static final int POST_RETURN_ALLOW = 0;
        public static final int POST_RETURN_MONITOR = 1;
    }

    public interface PostStringKey {
        public static final String VAL_POST_ALLOW = "allow";
        public static final String VAL_POST_CONTINUE = "continue";
        public static final String VAL_POST_MONITOR = "monitor";
    }

    public interface RuleAttrKey {
        public static final String ATTR_MATCH_POST = "matchPost";
        public static final String ATTR_MISMATCH_POST = "mismatchPost";
        public static final String ATTR_NAME = "name";
    }

    public GPostRuleBase(IRule<T> rule) {
        this.mRule = rule;
    }

    public boolean match(Context context, T matchKey) {
        if (this.mRule != null) {
            return this.mRule.match(context, matchKey);
        }
        return false;
    }

    public int getPost(boolean isMatch) {
        if (isMatch) {
            return this.mMatchPost;
        }
        return this.mMismatchPost;
    }

    public void checkPostValid() {
        if (3 == this.mMatchPost || 3 == this.mMismatchPost) {
            throw new GRuleException("Invalid post process enum value!");
        }
    }

    public void setMatchPost(int postValue) {
        this.mMatchPost = postValue;
    }

    public void setMismatchPost(int postValue) {
        this.mMismatchPost = postValue;
    }

    public String toString() {
        return "Rule " + this.mRule.getClass().getSimpleName() + " post: match[" + this.mMatchPost + "], mismatch[" + this.mMismatchPost + "]";
    }
}
