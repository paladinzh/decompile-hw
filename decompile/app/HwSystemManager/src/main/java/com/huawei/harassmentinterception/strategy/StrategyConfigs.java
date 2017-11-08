package com.huawei.harassmentinterception.strategy;

public class StrategyConfigs {
    public static final int DEFAULT_FLAG = 0;
    public static final int DISABLED = 0;
    public static final int ENABLED = 1;

    public enum StrategyId {
        INVALID(0),
        PASS_ALL(1),
        PASS_WHITELIST(2),
        PASS_CONTACT(4),
        BLOCK_UNKNOWN(8),
        BLOCK_BLACKLIST(16),
        BLOCK_STRANGER(32),
        BLOCK_INTELLIGENT(64),
        BLOCK_KEYWORDS(128),
        USER_DEFINE(4096),
        BLOCK_ALL(8192);
        
        private final int value;

        private StrategyId(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public String toString() {
            return String.valueOf(this.value);
        }
    }
}
