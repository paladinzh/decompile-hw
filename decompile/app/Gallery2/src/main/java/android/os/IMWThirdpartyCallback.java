package android.os;

import com.huawei.gallery.proguard.Keep;

@Keep
public interface IMWThirdpartyCallback {
    void onModeChanged(boolean z);

    void onSizeChanged();

    void onZoneChanged();
}
