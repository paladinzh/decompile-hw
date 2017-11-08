package com.fyusion.sdk.camera.b;

import com.fyusion.sdk.camera.CaptureEvent;
import com.fyusion.sdk.camera.CaptureEvent.CaptureStatus;
import com.fyusion.sdk.camera.CaptureEventListener;
import com.fyusion.sdk.camera.FyuseCamera.CameraType;
import com.fyusion.sdk.common.internal.analytics.Fyulytics;

/* compiled from: Unknown */
public class a {
    private CaptureEventListener a = new CaptureEventListener(this) {
        final /* synthetic */ a a;

        {
            this.a = r1;
        }

        public void onCapture(CaptureEvent captureEvent) {
            switch (AnonymousClass3.a[captureEvent.getCaptureStatus().ordinal()]) {
                case 2:
                    a.a(captureEvent.getUid(), "0", null);
                    return;
                case 3:
                case 4:
                    String recordingStatus = captureEvent.getRecordingStatus();
                    if (!"0".equals(recordingStatus)) {
                        a.a(captureEvent.getUid(), recordingStatus, captureEvent.getDescription());
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };

    /* compiled from: Unknown */
    /* renamed from: com.fyusion.sdk.camera.b.a$3 */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] a = new int[CaptureStatus.values().length];

        static {
            try {
                a[CaptureStatus.CAPTURE_IN_PROGRESS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                a[CaptureStatus.CAPTURE_COMPLETED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                a[CaptureStatus.CAPTURE_STOPPED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                a[CaptureStatus.CAPTURE_FAILED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public static void a(CameraType cameraType) {
        Fyulytics.sharedInstance().recordEvent(com.fyusion.sdk.common.internal.analytics.a.a(c(cameraType), false));
    }

    public static void a(CameraType cameraType, int i, int i2) {
        Event a = com.fyusion.sdk.common.internal.analytics.a.a(c(cameraType), true);
        a.c = Fyulytics.makeSizeString(i, i2);
        Fyulytics.sharedInstance().recordEvent(a);
    }

    public static void a(String str, int i, int i2, int i3, int i4, int i5) {
        Event b = com.fyusion.sdk.common.internal.analytics.a.b(i, str);
        b.key = "RECORDING";
        b.c = Fyulytics.makeSizeString(i2, i3);
        b.d = Fyulytics.makeSizeString(i4, i5);
        if (Fyulytics.sharedInstance().startEvent(b)) {
            Fyulytics.sharedInstance().recordEvent(com.fyusion.sdk.common.internal.analytics.a.a(b));
        }
    }

    public static void a(String str, CameraType cameraType, int i, int i2, int i3, int i4) {
        Event a = com.fyusion.sdk.common.internal.analytics.a.a(c(cameraType), str);
        a.key = "RECORDING";
        a.c = Fyulytics.makeSizeString(i, i2);
        a.d = Fyulytics.makeSizeString(i3, i4);
        if (Fyulytics.sharedInstance().startEvent(a)) {
            Fyulytics.sharedInstance().recordEvent(com.fyusion.sdk.common.internal.analytics.a.a(a));
        }
    }

    public static void a(String str, final String str2, final String str3) {
        Fyulytics.sharedInstance().endEvent(Fyulytics.makeTimedEventKey("RECORDING", str), new com.fyusion.sdk.common.internal.analytics.Fyulytics.a<com.fyusion.sdk.common.internal.analytics.a>() {
            public void a(com.fyusion.sdk.common.internal.analytics.a aVar) {
                aVar.key = "RECORDING_END";
                aVar.timestamp = Fyulytics.currentTimestampMs();
                aVar.status = str2;
                aVar.message = str3;
            }
        });
    }

    public static void b(CameraType cameraType) {
        Fyulytics.sharedInstance().recordEvent(com.fyusion.sdk.common.internal.analytics.a.b(c(cameraType), false));
    }

    public static void b(CameraType cameraType, int i, int i2) {
        Event b = com.fyusion.sdk.common.internal.analytics.a.b(c(cameraType), true);
        b.c = Fyulytics.makeSizeString(i, i2);
        Fyulytics.sharedInstance().recordEvent(b);
    }

    public static void b(String str, CameraType cameraType, int i, int i2, int i3, int i4) {
        a(str, c(cameraType), i, i2, i3, i4);
    }

    private static int c(CameraType cameraType) {
        return cameraType != CameraType.BACK_CAMERA ? 0 : 1;
    }

    public CaptureEventListener a() {
        return this.a;
    }
}
