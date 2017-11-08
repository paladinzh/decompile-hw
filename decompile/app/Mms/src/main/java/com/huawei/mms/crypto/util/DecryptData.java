package com.huawei.mms.crypto.util;

public class DecryptData {
    private int encryptType;
    private String messageContent;
    private long messageId;
    private int subId;

    public DecryptData(long messageId, String messageContent, int encryptType, int subId) {
        this.messageId = messageId;
        this.messageContent = messageContent;
        this.encryptType = encryptType;
        this.subId = subId;
    }

    public long getMessageId() {
        return this.messageId;
    }

    public String getMessageContent() {
        return this.messageContent;
    }

    public int getEncryptType() {
        return this.encryptType;
    }

    public void setEncryptType(int type) {
        this.encryptType = type;
    }

    public String toString() {
        return "DecryptData [messageId=" + this.messageId + ", encryptType=" + this.encryptType + ", subId=" + this.subId + "]";
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public int hashCode() {
        return ((((((this.encryptType + 31) * 31) + (this.messageContent == null ? 0 : this.messageContent.hashCode())) * 31) + ((int) (this.messageId ^ (this.messageId >>> 32)))) * 31) + this.subId;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DecryptData other = (DecryptData) obj;
        if (this.encryptType != other.encryptType) {
            return false;
        }
        if (this.messageContent == null) {
            if (other.messageContent != null) {
                return false;
            }
        } else if (!this.messageContent.equals(other.messageContent)) {
            return false;
        }
        return this.messageId == other.messageId && this.subId == other.subId;
    }
}
