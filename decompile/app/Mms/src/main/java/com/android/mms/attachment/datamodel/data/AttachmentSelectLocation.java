package com.android.mms.attachment.datamodel.data;

public class AttachmentSelectLocation extends AttachmentSelectData {
    private String mLatitude;
    private String mLocationSub;
    private String mLocationTitle;
    private String mLongitude;

    public AttachmentSelectLocation(int attachmentType, String locationTitle, String locationSub, String latitude, String longitude) {
        super(attachmentType);
        this.mLocationTitle = locationTitle;
        this.mLocationSub = locationSub;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
    }

    public String getLocationTitle() {
        return this.mLocationTitle;
    }

    public String getLocationSub() {
        return this.mLocationSub;
    }

    public String getLatitude() {
        return this.mLatitude;
    }

    public String getLongitude() {
        return this.mLongitude;
    }
}
