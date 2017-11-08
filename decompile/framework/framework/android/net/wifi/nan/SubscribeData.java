package android.net.wifi.nan;

import android.net.wifi.nan.TlvBufferUtils.TlvIterable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public class SubscribeData implements Parcelable {
    public static final Creator<SubscribeData> CREATOR = new Creator<SubscribeData>() {
        public SubscribeData[] newArray(int size) {
            return new SubscribeData[size];
        }

        public SubscribeData createFromParcel(Parcel in) {
            String serviceName = in.readString();
            int ssiLength = in.readInt();
            byte[] ssi = new byte[ssiLength];
            if (ssiLength != 0) {
                in.readByteArray(ssi);
            }
            int txFilterLength = in.readInt();
            byte[] txFilter = new byte[txFilterLength];
            if (txFilterLength != 0) {
                in.readByteArray(txFilter);
            }
            int rxFilterLength = in.readInt();
            byte[] rxFilter = new byte[rxFilterLength];
            if (rxFilterLength != 0) {
                in.readByteArray(rxFilter);
            }
            return new SubscribeData(serviceName, ssi, ssiLength, txFilter, txFilterLength, rxFilter, rxFilterLength);
        }
    };
    public final byte[] mRxFilter;
    public final int mRxFilterLength;
    public final String mServiceName;
    public final byte[] mServiceSpecificInfo;
    public final int mServiceSpecificInfoLength;
    public final byte[] mTxFilter;
    public final int mTxFilterLength;

    public static final class Builder {
        private byte[] mRxFilter = new byte[0];
        private int mRxFilterLength;
        private String mServiceName;
        private byte[] mServiceSpecificInfo = new byte[0];
        private int mServiceSpecificInfoLength;
        private byte[] mTxFilter = new byte[0];
        private int mTxFilterLength;

        public Builder setServiceName(String serviceName) {
            this.mServiceName = serviceName;
            return this;
        }

        public Builder setServiceSpecificInfo(byte[] serviceSpecificInfo, int serviceSpecificInfoLength) {
            this.mServiceSpecificInfoLength = serviceSpecificInfoLength;
            this.mServiceSpecificInfo = serviceSpecificInfo;
            return this;
        }

        public Builder setServiceSpecificInfo(String serviceSpecificInfoStr) {
            this.mServiceSpecificInfoLength = serviceSpecificInfoStr.length();
            this.mServiceSpecificInfo = serviceSpecificInfoStr.getBytes();
            return this;
        }

        public Builder setTxFilter(byte[] txFilter, int txFilterLength) {
            this.mTxFilter = txFilter;
            this.mTxFilterLength = txFilterLength;
            return this;
        }

        public Builder setRxFilter(byte[] rxFilter, int rxFilterLength) {
            this.mRxFilter = rxFilter;
            this.mRxFilterLength = rxFilterLength;
            return this;
        }

        public SubscribeData build() {
            return new SubscribeData(this.mServiceName, this.mServiceSpecificInfo, this.mServiceSpecificInfoLength, this.mTxFilter, this.mTxFilterLength, this.mRxFilter, this.mRxFilterLength);
        }
    }

    private SubscribeData(String serviceName, byte[] serviceSpecificInfo, int serviceSpecificInfoLength, byte[] txFilter, int txFilterLength, byte[] rxFilter, int rxFilterLength) {
        this.mServiceName = serviceName;
        this.mServiceSpecificInfoLength = serviceSpecificInfoLength;
        this.mServiceSpecificInfo = serviceSpecificInfo;
        this.mTxFilterLength = txFilterLength;
        this.mTxFilter = txFilter;
        this.mRxFilterLength = rxFilterLength;
        this.mRxFilter = rxFilter;
    }

    public String toString() {
        return "SubscribeData [mServiceName='" + this.mServiceName + "', mServiceSpecificInfo='" + new String(this.mServiceSpecificInfo, 0, this.mServiceSpecificInfoLength) + "', mTxFilter=" + new TlvIterable(0, 1, this.mTxFilter, this.mTxFilterLength).toString() + ", mRxFilter=" + new TlvIterable(0, 1, this.mRxFilter, this.mRxFilterLength).toString() + "']";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mServiceName);
        dest.writeInt(this.mServiceSpecificInfoLength);
        if (this.mServiceSpecificInfoLength != 0) {
            dest.writeByteArray(this.mServiceSpecificInfo, 0, this.mServiceSpecificInfoLength);
        }
        dest.writeInt(this.mTxFilterLength);
        if (this.mTxFilterLength != 0) {
            dest.writeByteArray(this.mTxFilter, 0, this.mTxFilterLength);
        }
        dest.writeInt(this.mRxFilterLength);
        if (this.mRxFilterLength != 0) {
            dest.writeByteArray(this.mRxFilter, 0, this.mRxFilterLength);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubscribeData)) {
            return false;
        }
        SubscribeData lhs = (SubscribeData) o;
        if (!this.mServiceName.equals(lhs.mServiceName) || this.mServiceSpecificInfoLength != lhs.mServiceSpecificInfoLength || this.mTxFilterLength != lhs.mTxFilterLength || this.mRxFilterLength != lhs.mRxFilterLength) {
            return false;
        }
        int i;
        if (this.mServiceSpecificInfo != null && lhs.mServiceSpecificInfo != null) {
            for (i = 0; i < this.mServiceSpecificInfoLength; i++) {
                if (this.mServiceSpecificInfo[i] != lhs.mServiceSpecificInfo[i]) {
                    return false;
                }
            }
        } else if (this.mServiceSpecificInfoLength != 0) {
            return false;
        }
        if (this.mTxFilter != null && lhs.mTxFilter != null) {
            for (i = 0; i < this.mTxFilterLength; i++) {
                if (this.mTxFilter[i] != lhs.mTxFilter[i]) {
                    return false;
                }
            }
        } else if (this.mTxFilterLength != 0) {
            return false;
        }
        if (this.mRxFilter != null && lhs.mRxFilter != null) {
            for (i = 0; i < this.mRxFilterLength; i++) {
                if (this.mRxFilter[i] != lhs.mRxFilter[i]) {
                    return false;
                }
            }
        } else if (this.mRxFilterLength != 0) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return ((((((((((((this.mServiceName.hashCode() + 527) * 31) + this.mServiceSpecificInfoLength) * 31) + Arrays.hashCode(this.mServiceSpecificInfo)) * 31) + this.mTxFilterLength) * 31) + Arrays.hashCode(this.mTxFilter)) * 31) + this.mRxFilterLength) * 31) + Arrays.hashCode(this.mRxFilter);
    }
}
