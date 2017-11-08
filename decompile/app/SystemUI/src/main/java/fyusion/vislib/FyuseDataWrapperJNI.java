package fyusion.vislib;

/* compiled from: Unknown */
public class FyuseDataWrapperJNI {
    public static final native long FyuseData_getFyuse(long j, FyuseData fyuseData);

    public static final native long FyuseData_getIsdData(long j, FyuseData fyuseData);

    public static final native long FyuseData_getSharedPtr(long j);

    public static final native long FyuseData_getStabilizationData(long j, FyuseData fyuseData);

    public static final native boolean FyuseData_loadFromFile(long j, FyuseData fyuseData, String str);

    public static final native boolean FyuseData_populateFyuseData(long j, FyuseData fyuseData, String str);

    public static final native void FyuseData_setFyuse(long j, FyuseData fyuseData, long j2, Fyuse fyuse);

    public static final native boolean FyuseData_writeToFile(long j, FyuseData fyuseData, String str, String str2, int i);

    public static final native void delete_FyuseData(long j, long j2);

    public static final native long new_FyuseData();
}
