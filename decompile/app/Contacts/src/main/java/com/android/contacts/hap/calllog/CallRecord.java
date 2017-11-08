package com.android.contacts.hap.calllog;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import java.io.File;
import java.util.ArrayList;

public class CallRecord {
    private ArrayList<CallRecordItem> mArrayList = new ArrayList();
    private Context mContext;
    private String mPhoneNumber;

    public static class CallRecordItem {
        public String mAbsolutePath;
        public long mCreateDate;
        public String mPhoneNumber;

        public String toString() {
            return "path=" + this.mAbsolutePath + "/number=" + this.mPhoneNumber + "/date=" + this.mCreateDate;
        }
    }

    public CallRecord(Context context, String phoneNum) {
        this.mPhoneNumber = phoneNum;
        this.mContext = context;
        setup();
    }

    private void setup() {
        String[] allRecordPathCandidates = CallRecordUtils.getRecordStoragePaths(this.mContext);
        if (allRecordPathCandidates != null && allRecordPathCandidates.length != 0) {
            for (String recordStoreDir : allRecordPathCandidates) {
                String[] recordFileList = new File(recordStoreDir).list();
                if (recordFileList != null) {
                    for (String recordName : recordFileList) {
                        File localFile = new File(recordStoreDir, recordName);
                        if (!localFile.isDirectory() && localFile.exists() && (recordName.endsWith(".3gpp") || recordName.endsWith(".amr") || recordName.endsWith(".mp3"))) {
                            long createDate;
                            CallRecordItem localCallRecordItem;
                            if (recordName.startsWith("unknown")) {
                                createDate = CallRecordUtils.getCallRecordCreatedDate(recordName);
                                if (createDate != -1) {
                                    localCallRecordItem = new CallRecordItem();
                                    localCallRecordItem.mAbsolutePath = localFile.getAbsolutePath();
                                    localCallRecordItem.mCreateDate = createDate;
                                    localCallRecordItem.mPhoneNumber = "unknown";
                                    this.mArrayList.add(localCallRecordItem);
                                }
                            } else {
                                String phoneNumber = CallRecordUtils.getRecordPhoneNumber(recordName);
                                if (!(phoneNumber == null || phoneNumber.equals("common-number") || !PhoneNumberUtils.compare(this.mPhoneNumber, phoneNumber))) {
                                    createDate = CallRecordUtils.getCallRecordCreatedDate(recordName);
                                    if (createDate != -1) {
                                        localCallRecordItem = new CallRecordItem();
                                        localCallRecordItem.mAbsolutePath = localFile.getAbsolutePath();
                                        localCallRecordItem.mCreateDate = createDate;
                                        localCallRecordItem.mPhoneNumber = phoneNumber;
                                        this.mArrayList.add(localCallRecordItem);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public CallRecordItem[] getCallRecordItems(long callBeginTime, long callEndTime, String phoneNumber) {
        if (this.mArrayList == null) {
            return null;
        }
        ArrayList<CallRecordItem> callRecordItems = new ArrayList();
        for (CallRecordItem item : this.mArrayList) {
            if (item.mCreateDate >= callBeginTime - 1000 && item.mCreateDate <= callEndTime + 1000) {
                callRecordItems.add(item);
            }
        }
        return (CallRecordItem[]) callRecordItems.toArray(new CallRecordItem[callRecordItems.size()]);
    }
}
