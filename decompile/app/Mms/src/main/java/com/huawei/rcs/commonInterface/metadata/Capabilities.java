package com.huawei.rcs.commonInterface.metadata;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Capabilities implements Parcelable {
    public static final Creator<Capabilities> CREATOR = new Creator<Capabilities>() {
        public Capabilities createFromParcel(Parcel source) {
            boolean z;
            boolean z2 = true;
            Capabilities capability = new Capabilities();
            capability.setVoiceCall(source.readInt() == 0);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setCSCall(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setWithPresence(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setFileTransfer(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setIM(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setImageSharing(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setLocationSharing(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setMultimediaTechnology(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setNAB(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setVoIP(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setSMS(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setSocailPresence(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setVideoCall(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setVideoSharing(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setIsSupFTViaHTTP(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setIsSuptPreCall(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setIsSuptShMap(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setIsSuptShSketch(z);
            if (source.readInt() == 0) {
                z = true;
            } else {
                z = false;
            }
            capability.setIsSuptPostCall(z);
            if (source.readInt() != 0) {
                z2 = false;
            }
            capability.setIsOnLine(z2);
            return capability;
        }

        public Capabilities[] newArray(int size) {
            return new Capabilities[size];
        }
    };
    private boolean mCSCall = false;
    private boolean mFileTransfer = false;
    private boolean mIM = false;
    private boolean mImageSharing = false;
    private boolean mIsOnLine = false;
    private boolean mIsSuptFtViaHttp = false;
    private boolean mIsSuptPostCall = false;
    private boolean mIsSuptPreCall = false;
    private boolean mIsSuptShMap = false;
    private boolean mIsSuptShSketch = false;
    private boolean mLocationSharing = false;
    private boolean mMultimediaTechnology = false;
    private boolean mNAB = false;
    private boolean mSMS = false;
    private boolean mSocailPresence = false;
    private String mStringSupported = "VoiceCall,CSCall,WithPresence,FileTransfer,IM,ImageSharing,LocationSharing,MultimediaTechnology,NAB,VoIP,SMS,SocailPresence,VideoCall,VideoSharing";
    private boolean mVideoCall = false;
    private boolean mVideoSharing = false;
    private boolean mVoIP = false;
    private boolean mVoiceCall = false;
    private boolean mWithPresence = false;

    public void setVoiceCall(boolean voiceCall) {
        this.mVoiceCall = voiceCall;
    }

    public void setCSCall(boolean CSCall) {
        this.mCSCall = CSCall;
    }

    public void setWithPresence(boolean withPresence) {
        this.mWithPresence = withPresence;
    }

    public void setFileTransfer(boolean fileTransfer) {
        this.mFileTransfer = fileTransfer;
    }

    public void setIM(boolean IM) {
        this.mIM = IM;
    }

    public void setImageSharing(boolean imageSharing) {
        this.mImageSharing = imageSharing;
    }

    public void setLocationSharing(boolean locationSharing) {
        this.mLocationSharing = locationSharing;
    }

    public void setMultimediaTechnology(boolean multimediaTechnology) {
        this.mMultimediaTechnology = multimediaTechnology;
    }

    public void setNAB(boolean NAB) {
        this.mNAB = NAB;
    }

    public void setVoIP(boolean voIP) {
        this.mVoIP = voIP;
    }

    public void setSMS(boolean SMS) {
        this.mSMS = SMS;
    }

    public void setSocailPresence(boolean socailPresence) {
        this.mSocailPresence = socailPresence;
    }

    public void setVideoCall(boolean videoCall) {
        this.mVideoCall = videoCall;
    }

    public void setVideoSharing(boolean videoSharing) {
        this.mVideoSharing = videoSharing;
    }

    public void setIsSupFTViaHTTP(boolean isSuptFtViaHttp) {
        this.mIsSuptFtViaHttp = isSuptFtViaHttp;
    }

    public void setIsSuptPreCall(boolean isSuptPreCall) {
        this.mIsSuptPreCall = isSuptPreCall;
    }

    public void setIsSuptShMap(boolean isSuptShMap) {
        this.mIsSuptShMap = isSuptShMap;
    }

    public void setIsSuptShSketch(boolean isSuptShSketch) {
        this.mIsSuptShSketch = isSuptShSketch;
    }

    public void setIsSuptPostCall(boolean isSuptPostCall) {
        this.mIsSuptPostCall = isSuptPostCall;
    }

    public void setIsOnLine(boolean isOnLine) {
        this.mIsOnLine = isOnLine;
    }

    public boolean isFileTransferSupported() {
        return this.mFileTransfer;
    }

    public boolean isLocationSharingSupported() {
        return this.mLocationSharing;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 0;
        if (this.mVoiceCall) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mCSCall) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mWithPresence) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mFileTransfer) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mIM) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mImageSharing) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mLocationSharing) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mMultimediaTechnology) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mNAB) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mVoIP) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mSMS) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mSocailPresence) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mVideoCall) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mVideoSharing) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mIsSuptFtViaHttp) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mIsSuptPreCall) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mIsSuptShMap) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mIsSuptShSketch) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.mIsSuptPostCall) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (!this.mIsOnLine) {
            i2 = 1;
        }
        dest.writeInt(i2);
    }
}
