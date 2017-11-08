package fyusion.vislib;

/* compiled from: Unknown */
public class ImuDataWrapper {
    protected native void jni_loadFromFile(String str);

    public void loadFromFile(String str) {
        jni_loadFromFile(str);
    }
}
