package fyusion.vislib;

/* compiled from: Unknown */
public class ImuDataWrapperJNI {
    public static final native long IMUData_accelerations__get(long j, IMUData iMUData);

    public static final native void IMUData_accelerations__set(long j, IMUData iMUData, long j2);

    public static final native long IMUData_computeFrameRotations(long j, IMUData iMUData, long j2);

    public static final native long IMUData_gravities__get(long j, IMUData iMUData);

    public static final native void IMUData_gravities__set(long j, IMUData iMUData, long j2);

    public static final native long IMUData_iPhoneToVisionCoordinates(long j, boolean z);

    public static final native boolean IMUData_loadFromFile(long j, IMUData iMUData, String str);

    public static final native long IMUData_magnetic_fields__get(long j, IMUData iMUData);

    public static final native void IMUData_magnetic_fields__set(long j, IMUData iMUData, long j2);

    public static final native long IMUData_rotation_matrices__get(long j, IMUData iMUData);

    public static final native void IMUData_rotation_matrices__set(long j, IMUData iMUData, long j2);

    public static final native long IMUData_rotation_rates__get(long j, IMUData iMUData);

    public static final native void IMUData_rotation_rates__set(long j, IMUData iMUData, long j2);

    public static final native long IMUData_rpys__get(long j, IMUData iMUData);

    public static final native void IMUData_rpys__set(long j, IMUData iMUData, long j2);

    public static final native long IMUData_timestamps__get(long j, IMUData iMUData);

    public static final native void IMUData_timestamps__set(long j, IMUData iMUData, long j2);

    public static final native void delete_IMUData(long j);

    public static final native long new_IMUData();
}
