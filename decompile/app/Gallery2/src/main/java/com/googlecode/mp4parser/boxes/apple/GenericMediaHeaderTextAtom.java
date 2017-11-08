package com.googlecode.mp4parser.boxes.apple;

import com.android.gallery3d.settings.HicloudAccountReceiver;
import com.googlecode.mp4parser.AbstractBox;
import com.huawei.watermark.ui.WMEditor;
import java.nio.ByteBuffer;

public class GenericMediaHeaderTextAtom extends AbstractBox {
    int unknown_1 = HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT;
    int unknown_2;
    int unknown_3;
    int unknown_4;
    int unknown_5 = HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT;
    int unknown_6;
    int unknown_7;
    int unknown_8;
    int unknown_9 = 1073741824;

    public GenericMediaHeaderTextAtom() {
        super(WMEditor.TYPETEXT);
    }

    protected long getContentSize() {
        return 36;
    }

    protected void getContent(ByteBuffer byteBuffer) {
        byteBuffer.putInt(this.unknown_1);
        byteBuffer.putInt(this.unknown_2);
        byteBuffer.putInt(this.unknown_3);
        byteBuffer.putInt(this.unknown_4);
        byteBuffer.putInt(this.unknown_5);
        byteBuffer.putInt(this.unknown_6);
        byteBuffer.putInt(this.unknown_7);
        byteBuffer.putInt(this.unknown_8);
        byteBuffer.putInt(this.unknown_9);
    }

    protected void _parseDetails(ByteBuffer content) {
        this.unknown_1 = content.getInt();
        this.unknown_2 = content.getInt();
        this.unknown_3 = content.getInt();
        this.unknown_4 = content.getInt();
        this.unknown_5 = content.getInt();
        this.unknown_6 = content.getInt();
        this.unknown_7 = content.getInt();
        this.unknown_8 = content.getInt();
        this.unknown_9 = content.getInt();
    }
}
