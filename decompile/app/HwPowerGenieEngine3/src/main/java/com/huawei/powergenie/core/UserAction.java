package com.huawei.powergenie.core;

public final class UserAction extends PowerAction {
    private String mStateName;
    private int mStateType = -1;
    private int mSubState = -1;
    private int mUserState = 0;

    public UserAction(int userState, long ts, String name, int subState, int stateType) {
        super(userState, ts);
        this.mUserState = userState;
        this.mStateName = name;
        this.mSubState = subState;
        this.mStateType = stateType;
    }

    public String getStateName() {
        return this.mStateName;
    }

    public String getSubStateName() {
        if (this.mSubState == 0) {
            return "IDLE";
        }
        if (this.mSubState == 1) {
            return "ACTIVE";
        }
        return "UNKNOWN";
    }

    public int getUserState() {
        return this.mUserState;
    }

    public int getUserStateType() {
        return this.mStateType;
    }

    public int getType() {
        if (super.getType() >= 0) {
            return super.getType();
        }
        return 2;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" state=").append(this.mStateName);
        builder.append(" substate=").append(getSubStateName());
        builder.append(" statetype=").append(getUserStateType());
        return builder.toString();
    }
}
