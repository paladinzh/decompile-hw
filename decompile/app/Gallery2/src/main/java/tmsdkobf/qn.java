package tmsdkobf;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import tmsdk.common.creator.BaseManagerC;

/* compiled from: Unknown */
final class qn extends BaseManagerC {
    private Handler mHandler;

    qn() {
    }

    public boolean a(Runnable runnable, long j) {
        if (runnable == null) {
            return false;
        }
        Message obtainMessage = this.mHandler.obtainMessage();
        obtainMessage.obj = runnable;
        return this.mHandler.sendMessageDelayed(obtainMessage, j);
    }

    public int getSingletonType() {
        return 1;
    }

    public void onCreate(Context context) {
        this.mHandler = new Handler(this, context.getMainLooper()) {
            final /* synthetic */ qn JM;

            public void handleMessage(Message message) {
                Runnable runnable = (Runnable) message.obj;
                if (runnable != null) {
                    jq.ct().c(runnable, "DefaultPhoneRunableTask").start();
                }
            }
        };
    }
}
