package com.google.protobuf;

import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import java.util.Collections;
import java.util.List;

public class UninitializedMessageException extends RuntimeException {
    private static final long serialVersionUID = -7466929953374883507L;
    private final List<String> missingFields;

    public UninitializedMessageException(MessageLite messageLite) {
        super("Message was missing required fields.  (Lite runtime could not determine which fields were missing).");
        this.missingFields = null;
    }

    public UninitializedMessageException(List<String> list) {
        super(buildDescription(list));
        this.missingFields = list;
    }

    public List<String> getMissingFields() {
        return Collections.unmodifiableList(this.missingFields);
    }

    public InvalidProtocolBufferException asInvalidProtocolBufferException() {
        return new InvalidProtocolBufferException(getMessage());
    }

    private static String buildDescription(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder("Message missing required fields: ");
        Object obj = 1;
        for (String str : list) {
            if (obj == null) {
                stringBuilder.append(SqlMarker.COMMA_SEPARATE);
            } else {
                obj = null;
            }
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }
}
