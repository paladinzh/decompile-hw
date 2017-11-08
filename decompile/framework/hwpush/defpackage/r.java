package defpackage;

import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;

/* renamed from: r */
public /* synthetic */ class r {
    public static final /* synthetic */ int[] aj = new int[ChannelType.values().length];

    static {
        try {
            aj[ChannelType.ChannelType_Normal.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            aj[ChannelType.ChannelType_SSL.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            aj[ChannelType.ChannelType_SSL_Resume.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            aj[ChannelType.ChannelType_Secure.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
    }
}
