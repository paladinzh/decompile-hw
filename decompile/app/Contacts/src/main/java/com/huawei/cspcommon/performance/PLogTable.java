package com.huawei.cspcommon.performance;

public class PLogTable {

    static class Row {
        String mDescriptionStr;
        int[] mEnding;
        int[] mExcludeStarting;
        int[] mExcluding;
        int[] mFollowStarting;
        int[] mIncluding;
        int[] mStarting;

        public Row(String descriptionStr, int[] starting, int[] excludeStarting, int[] followStarting, int[] including, int[] excluding, int[] ending) {
            this.mDescriptionStr = descriptionStr;
            this.mStarting = starting;
            this.mExcludeStarting = excludeStarting;
            this.mFollowStarting = followStarting;
            this.mIncluding = including;
            this.mExcluding = excluding;
            this.mEnding = ending;
        }

        boolean isStarting(int tagId) {
            if (this.mStarting != null) {
                for (int i : this.mStarting) {
                    if (i == tagId) {
                        return true;
                    }
                }
            }
            return false;
        }

        boolean isExcludeStarting(int tagId) {
            if (this.mExcludeStarting != null) {
                for (int i : this.mExcludeStarting) {
                    if (i == tagId) {
                        return true;
                    }
                }
            }
            return false;
        }

        boolean isFollowStarting(int tagId) {
            if (this.mFollowStarting != null) {
                for (int i : this.mFollowStarting) {
                    if (i == tagId) {
                        return true;
                    }
                }
            }
            return false;
        }

        boolean isExcluding(int tagId) {
            if (this.mExcluding != null) {
                for (int i : this.mExcluding) {
                    if (i == tagId) {
                        return true;
                    }
                }
            }
            return false;
        }

        boolean isEnding(int tagId) {
            if (this.mEnding != null) {
                for (int i : this.mEnding) {
                    if (i == tagId) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static boolean isStarting(int tagId, int sceneId) {
        Row row = (Row) PConstants.sMappingTable.get(sceneId);
        return row == null ? false : row.isStarting(tagId);
    }

    public static boolean isExcludeStarting(int tagId, int sceneId) {
        Row row = (Row) PConstants.sMappingTable.get(sceneId);
        return row == null ? false : row.isExcludeStarting(tagId);
    }

    public static boolean isFollowStarting(int tagId, int sceneId) {
        Row row = (Row) PConstants.sMappingTable.get(sceneId);
        return row == null ? false : row.isFollowStarting(tagId);
    }

    public static boolean isExcluding(int tagId, int sceneId) {
        Row row = (Row) PConstants.sMappingTable.get(sceneId);
        return row == null ? false : row.isExcluding(tagId);
    }

    public static boolean isEnding(int tagId, int sceneId) {
        Row row = (Row) PConstants.sMappingTable.get(sceneId);
        return row == null ? false : row.isEnding(tagId);
    }

    public static int[] getIncluding(int sceneId) {
        Row row = (Row) PConstants.sMappingTable.get(sceneId);
        if (row == null) {
            return null;
        }
        return row.mIncluding;
    }

    public static int[] getExcludeStarting(int sceneId) {
        Row row = (Row) PConstants.sMappingTable.get(sceneId);
        if (row == null) {
            return null;
        }
        return row.mExcludeStarting;
    }

    public static String getDescription(int sceneId) {
        Row row = (Row) PConstants.sMappingTable.get(sceneId);
        if (row == null) {
            return null;
        }
        return row.mDescriptionStr;
    }

    public static int getSize() {
        return PConstants.sMappingTable.size();
    }
}
