package com.google.android.gms.internal;

import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.metadata.internal.h;
import java.util.Arrays;

/* compiled from: Unknown */
public class fu extends h<DriveId> {
    public static final fu EI = new fu();

    private fu() {
        super("driveId", Arrays.asList(new String[]{"sqlId", "resourceId"}), 4100000);
    }
}
