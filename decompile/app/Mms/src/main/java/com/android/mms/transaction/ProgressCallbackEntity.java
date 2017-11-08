package com.android.mms.transaction;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import cn.com.xy.sms.sdk.net.NetUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import org.apache.http.entity.ByteArrayEntity;

public class ProgressCallbackEntity extends ByteArrayEntity {
    private final byte[] mContent;
    private final Context mContext;
    private final long mToken;

    public ProgressCallbackEntity(Context context, long token, byte[] b) {
        super(b);
        this.mContext = context;
        this.mContent = Arrays.copyOf(b, b.length);
        this.mToken = token;
    }

    public void writeTo(OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        boolean completed = false;
        try {
            broadcastProgressIfNeeded(-1);
            int pos = 0;
            int totalLen = this.mContent.length;
            while (pos < totalLen) {
                int len = totalLen - pos;
                if (len > 4096) {
                    len = 4096;
                }
                outstream.write(this.mContent, pos, len);
                outstream.flush();
                pos += len;
                broadcastProgressIfNeeded((pos * 100) / totalLen);
            }
            broadcastProgressIfNeeded(100);
            completed = true;
        } finally {
            if (!completed) {
                broadcastProgressIfNeeded(-2);
            }
        }
    }

    private void broadcastProgressIfNeeded(int progress) {
        if (this.mToken > 0) {
            Intent intent = new Intent("com.android.mms.PROGRESS_STATUS");
            intent.putExtra("progress", progress);
            intent.putExtra(NetUtil.REQ_QUERY_TOEKN, this.mToken);
            LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(intent);
        }
    }
}
