package com.huawei.harassmentinterception.common;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.huawei.harassmentinterception.common.Tables.TbCalls;
import com.huawei.harassmentinterception.common.Tables.TbMessages;
import com.huawei.harassmentinterception.util.CommonHelper;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.ListItem;
import com.huawei.systemmanager.comparator.AlpComparator;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.numberlocation.NumberLocationInfo;
import com.huawei.systemmanager.util.phonematch.PhoneMatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonObject {

    public static class ContactQueryColumn {
        private String mColumnName;
        private String mColumnPhone;
        private String mId;

        public ContactQueryColumn(String columnId, String columnName, String columnPhone) {
            this.mId = columnId;
            this.mColumnName = columnName;
            this.mColumnPhone = columnPhone;
        }

        public String getColumnName() {
            return this.mColumnName;
        }

        public String getColumnPhone() {
            return this.mColumnPhone;
        }

        public String getQuerySelection() {
            return "1=1 group by " + this.mColumnPhone;
        }

        public String[] getQuerySelectionArgs() {
            return new String[0];
        }

        public String[] getSqlQueryProjection() {
            return new String[]{this.mId, this.mColumnName, this.mColumnPhone};
        }

        public ContentValues getSqlUpdateValues(ContactInfo contact) {
            ContentValues updateValue = new ContentValues();
            updateValue.put(this.mColumnName, contact.getName());
            return updateValue;
        }

        public String getSqlUpdateSelection() {
            return this.mColumnPhone + "=?";
        }

        public String[] getSqlUpdateAgrs(ContactInfo contact) {
            return new String[]{contact.getPhone()};
        }

        public int getIdColIndexFromCursor(Cursor cursor) {
            return cursor.getColumnIndex(this.mId);
        }

        public int getNameColIndexFromCursor(Cursor cursor) {
            return cursor.getColumnIndex(this.mColumnName);
        }

        public int getPhoneColIndexFromCursor(Cursor cursor) {
            return cursor.getColumnIndex(this.mColumnPhone);
        }
    }

    public static class BlacklistContactQueryColumn extends ContactQueryColumn {
        public BlacklistContactQueryColumn(String columnId, String columnName, String columnPhone) {
            super(columnId, columnName, columnPhone);
        }

        public String getQuerySelection() {
            return "type=?";
        }

        public String[] getQuerySelectionArgs() {
            return new String[]{String.valueOf(0)};
        }
    }

    public static class ContactInfo {
        private int mId;
        private boolean mIsSelected;
        private NumberLocationInfo mLocation;
        private String mMatchedNumber;
        private String mName;
        private String mPhone;

        public ContactInfo(String phone, String name) {
            this.mId = -1;
            this.mPhone = phone;
            this.mName = name;
            this.mIsSelected = false;
        }

        public ContactInfo(String phone, String name, NumberLocationInfo location) {
            this.mId = -1;
            this.mPhone = phone;
            this.mName = name;
            this.mIsSelected = false;
            this.mLocation = location;
        }

        public ContactInfo(int nId, String phone, String name) {
            this.mId = nId;
            this.mPhone = phone;
            this.mName = name;
            this.mIsSelected = false;
        }

        public ContactInfo(int nId, String phone, String name, NumberLocationInfo location) {
            this.mId = nId;
            this.mPhone = phone;
            this.mName = name;
            this.mIsSelected = false;
            this.mLocation = location;
        }

        public String getGeoLocation() {
            if (this.mLocation == null) {
                return "";
            }
            return this.mLocation.getGeoLocation();
        }

        public int getId() {
            return this.mId;
        }

        public String getPhone() {
            return this.mPhone;
        }

        public String getName() {
            return this.mName;
        }

        public void setName(String name) {
            this.mName = name;
        }

        public String getMatchedNumber() {
            if (TextUtils.isEmpty(this.mMatchedNumber)) {
                this.mMatchedNumber = PhoneMatch.getPhoneNumberMatchInfo(this.mPhone).getPhoneNumber();
            }
            return this.mMatchedNumber;
        }

        public String getContactInfo(Context context) {
            String strContactInfo = "";
            if (!TextUtils.isEmpty(this.mName)) {
                strContactInfo = strContactInfo + this.mName + " ";
            }
            if (TextUtils.isEmpty(this.mPhone)) {
                return strContactInfo + context.getResources().getString(R.string.harassment_unknown_phonenumber);
            }
            return strContactInfo + "‭" + this.mPhone + "‬";
        }

        public boolean isSelected() {
            return this.mIsSelected;
        }

        public void setSelected(boolean isSelected) {
            this.mIsSelected = isSelected;
        }

        public String[] getIdAsSqlSeclectionArgs() {
            return new String[]{String.valueOf(getId())};
        }

        public boolean isSameId(ContactInfo info) {
            return this.mId == info.getId();
        }

        public NumberLocationInfo getNumberLocationInfo() {
            return this.mLocation;
        }
    }

    public static class BlacklistInfo extends ContactInfo implements ListItem {
        public static final AlpComparator<BlacklistInfo> HARASSMENT_ALP_COMPARATOR = new AlpComparator<BlacklistInfo>() {
            public String getStringKey(BlacklistInfo t) {
                String name = t.getName();
                String num = t.getPhone();
                if (TextUtils.isEmpty(name)) {
                    return num;
                }
                return t.getName();
            }
        };
        private int mCallCount;
        private int mMsgCount;
        private int mOption;
        private int mType;

        public BlacklistInfo(int nId, String phone, String name, int callCount, int msgCount, int option, int type) {
            super(nId, phone, name);
            this.mCallCount = callCount;
            this.mMsgCount = msgCount;
            this.mOption = option;
            this.mType = type;
        }

        public BlacklistInfo(int nId, String phone, String name, int callCount, int msgCount, int option, int type, NumberLocationInfo location) {
            super(nId, phone, name, location);
            this.mCallCount = callCount;
            this.mMsgCount = msgCount;
            this.mOption = option;
            this.mType = type;
        }

        public int getCallCount() {
            return this.mCallCount;
        }

        public void setCallCount(int callCount) {
            this.mCallCount = callCount;
        }

        public int getMsgCount() {
            return this.mMsgCount;
        }

        public void setMsgCount(int msgCount) {
            this.mMsgCount = msgCount;
        }

        public int getOption() {
            return this.mOption;
        }

        public void setOption(int option) {
            this.mOption = option;
        }

        public int getType() {
            return this.mType;
        }

        public boolean isBlockMsg() {
            return (this.mOption & 1) != 0;
        }

        public boolean isBlockCall() {
            return (this.mOption & 2) != 0;
        }

        public boolean isBlackListHeader() {
            return this.mType != 0;
        }

        public boolean isMatchHeader(String number) {
            if (1 != this.mType) {
                return false;
            }
            return CommonHelper.trimCountryCode(number).startsWith(CommonHelper.trimCountryCode(getPhone()));
        }

        public boolean isMatchOption(int nCheckOption) {
            return (this.mOption & nCheckOption) != 0;
        }

        public String getPhoneText() {
            if (this.mType == 0) {
                return getPhone();
            }
            return getPhone() + "*";
        }

        public String getOptionText(Context context) {
            String strStat = "";
            Resources res = context.getResources();
            switch (this.mOption) {
                case 1:
                    return String.format(res.getString(R.string.harassmentInterceptionRecordsCount_msg), new Object[]{Integer.valueOf(this.mMsgCount)});
                case 2:
                    return String.format(res.getString(R.string.harassmentInterceptionRecordsCount_call), new Object[]{Integer.valueOf(this.mCallCount)});
                case 3:
                    return String.format(res.getString(R.string.harassmentInterceptionRecordsCount_all), new Object[]{Integer.valueOf(this.mCallCount), Integer.valueOf(this.mMsgCount)});
                default:
                    return strStat;
            }
        }

        public String getContactInfo(Context context) {
            String strContactInfo = "";
            if (!TextUtils.isEmpty(getName())) {
                strContactInfo = strContactInfo + getName() + " ";
            }
            if (TextUtils.isEmpty(getPhone())) {
                return strContactInfo + context.getResources().getString(R.string.harassment_unknown_phonenumber);
            }
            return strContactInfo + "‭" + getPhoneText() + "‬";
        }

        public String getTitle(Context ctx) {
            return getPhone();
        }
    }

    public static class CallInfo extends ContactInfo {
        private BlockReason blockReason;
        private long mDate;
        public int mSubId = 0;

        public CallInfo(String phone, String name, long date) {
            super(phone, name);
            this.mDate = date;
        }

        public CallInfo(int nId, String phone, String name, long date, NumberLocationInfo location) {
            super(nId, phone, name, location);
            this.mDate = date;
        }

        public ContentValues getAsContentValues() {
            ContentValues values = new ContentValues();
            values.put("phone", getPhone());
            values.put("name", getName());
            values.put("date", Long.valueOf(getDate()));
            values.put("sub_id", Integer.valueOf(this.mSubId));
            if (this.blockReason != null) {
                values.put("block_reason", Integer.valueOf(this.blockReason.getReason()));
                values.put(TbCalls.BLOCK_TYPE, Integer.valueOf(this.blockReason.getType()));
                values.put(TbCalls.MARK_COUNT, Integer.valueOf(this.blockReason.getMarkCount()));
            }
            return values;
        }

        public long getDate() {
            return this.mDate;
        }

        public void setDate(long date) {
            this.mDate = date;
        }

        public void setSubId(int subId) {
            this.mSubId = subId;
        }

        public int getSubId() {
            return this.mSubId;
        }

        public void setBlockReason(BlockReason reason) {
            this.blockReason = reason;
        }

        public BlockReason getReason() {
            return this.blockReason;
        }
    }

    public static class CallLogInfo extends ContactInfo {
        private long date;

        public CallLogInfo(String name, String number, long date) {
            super(-1, number, name);
            this.date = date;
        }

        public CallLogInfo(String name, String number, long date, NumberLocationInfo location) {
            super(-1, number, name, location);
            this.date = date;
        }

        public long getDate() {
            return this.date;
        }

        public void setDate(long date) {
            this.date = date;
        }
    }

    public static class ImIntentWrapper {
        private Intent mImIntent;
        private String mmsgPeerNum;
        private int mmsgType;
        private long mmsgid;
        private String phoneNum;

        public ImIntentWrapper(long msgid, int msgType, String phone, Intent ImIntent, String phoneNum) {
            this.mmsgid = msgid;
            this.mImIntent = ImIntent;
            this.mmsgType = msgType;
            this.mmsgPeerNum = phone;
            this.phoneNum = phoneNum;
        }

        public void setImMsgId(long msgid) {
            this.mmsgid = msgid;
        }

        public String getMmsgPeerNum() {
            return this.mmsgPeerNum;
        }

        public void setMmsgPeerNum(String mmsgPeerNum) {
            this.mmsgPeerNum = mmsgPeerNum;
        }

        public void setImIntent(Intent ImIntent) {
            this.mImIntent = ImIntent;
        }

        public void setImMsgType(int msgType) {
            this.mmsgType = msgType;
        }

        public long getImMsgId() {
            return this.mmsgid;
        }

        public Intent getImIntent() {
            return this.mImIntent;
        }

        public int getImMsgType() {
            return this.mmsgType;
        }

        public String getPhoneNum() {
            return this.phoneNum;
        }
    }

    public static class InCommingCall {
        private boolean mIsLocal;
        private int mMarkCount = 0;
        private String mMarkName;
        private int mMarkType;
        String number;
        int presentation;
        BlockReason reason;
        int subId = 0;

        public InCommingCall(String number, int presentation, int subId) {
            this.number = number;
            this.presentation = presentation;
            this.subId = subId;
        }

        public String getNumber() {
            return this.number;
        }

        public int getPersentation() {
            return this.presentation;
        }

        public int getSubId() {
            return this.subId;
        }

        public int getMarkType() {
            return this.mMarkType;
        }

        public String getMarkName() {
            return this.mMarkName;
        }

        public int getMarkCount() {
            return this.mMarkCount;
        }

        public boolean getIsLocal() {
            return this.mIsLocal;
        }

        public BlockReason getBlockReason() {
            return this.reason;
        }

        public void setReason(BlockReason reason) {
            this.reason = reason;
        }

        public void setMarkType(int markType) {
            this.mMarkType = markType;
        }

        public void setMarkName(String markName) {
            this.mMarkName = markName;
        }

        public void setMarkCount(int markCount) {
            this.mMarkCount = markCount;
        }

        public void setIsLocal(boolean isLocal) {
            this.mIsLocal = isLocal;
        }
    }

    public static class InterceptionRuleInfo {
        private String mName;
        private int mState;

        public InterceptionRuleInfo(String name, int state) {
            this.mName = name;
            this.mState = state;
        }

        public String getName() {
            return this.mName;
        }

        public void setName(String name) {
            this.mName = name;
        }

        public int getState() {
            return this.mState;
        }

        public void setState(int state) {
            this.mState = state;
        }

        public String[] getSqlSelectionArgs() {
            return new String[]{getName()};
        }

        public ContentValues getAsContentValues() {
            ContentValues values = new ContentValues();
            values.put("name", getName());
            values.put("state", Integer.valueOf(getState()));
            return values;
        }
    }

    public static class KeywordsInfo {
        public static final AlpComparator<KeywordsInfo> KEYWORD_ALP_COMPARATOR = new AlpComparator<KeywordsInfo>() {
            public String getStringKey(KeywordsInfo t) {
                return t.getValue();
            }
        };
        private int mId;
        private boolean mIsChecked = false;
        private String mKeywords;

        public KeywordsInfo(int id, String keywords) {
            this.mKeywords = keywords;
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }

        public String getValue() {
            return this.mKeywords;
        }

        public boolean isChecked() {
            return this.mIsChecked;
        }

        public void setChecked(boolean isChecked) {
            this.mIsChecked = isChecked;
        }

        public void reverseCheckStatus() {
            this.mIsChecked = !this.mIsChecked;
        }

        public boolean isMatch(String msgInfo) {
            if (TextUtils.isEmpty(msgInfo)) {
                return false;
            }
            return msgInfo.contains(this.mKeywords);
        }
    }

    public static class SmsMsgInfo extends ContactInfo {
        private static final String TAG = "SmsMsgInfo";
        private int mBlockReason;
        private String mBody;
        private long mDate;
        private int mSubId;

        public SmsMsgInfo(String phone, String name, String body, long date, int subId) {
            super(phone, name);
            this.mBody = body;
            this.mDate = date;
            this.mSubId = subId;
        }

        public SmsMsgInfo(int nId, String phone, String name, String body, long date, int subId, NumberLocationInfo location) {
            super(nId, phone, name, location);
            this.mBody = body;
            this.mDate = date;
            this.mSubId = subId;
        }

        public ContentValues getAsContentValues() {
            ContentValues values = new ContentValues();
            String formateNumber = PhoneNumberUtils.stripSeparators(getPhone());
            if (TextUtils.isEmpty(formateNumber)) {
                formateNumber = getPhone();
                HwLog.i(TAG, "phone number is not digtal format,use origin number");
            }
            values.put("phone", formateNumber);
            values.put("name", getName());
            values.put(TbMessages.BODY, getBody());
            values.put("date", Long.valueOf(getDate()));
            values.put("sub_id", Integer.valueOf(getSubId()));
            values.put("block_reason", Integer.valueOf(getBlockReason()));
            return values;
        }

        public ContentValues getAsSysSmsContentValues(String sysSmsDBSubIdKey) {
            ContentValues values = new ContentValues();
            values.put("address", getPhone());
            values.put(TbMessages.BODY, getBody());
            values.put("date", Long.valueOf(getDate()));
            int nSubId = getSubId();
            if (!(-1 == nSubId || TextUtils.isEmpty(sysSmsDBSubIdKey))) {
                values.put(sysSmsDBSubIdKey, Integer.valueOf(nSubId));
            }
            return values;
        }

        public String getBody() {
            return this.mBody;
        }

        public long getDate() {
            return this.mDate;
        }

        public int getSubId() {
            return this.mSubId;
        }

        public void setBlockReason(int reason) {
            this.mBlockReason = reason;
        }

        public int getBlockReason() {
            return this.mBlockReason;
        }
    }

    public static class MessageInfo extends SmsMsgInfo {
        private static final String TAG = "MessageInfo";
        public static final int TYPE_MMS = 1;
        public static final int TYPE_SMS = 0;
        private long mExpDate;
        private int mMsgType = 0;
        private byte[] mPdu;
        private long mSize;

        public MessageInfo(int nId, String phone, String name, String body, long size, long date, long expdate, int subId, byte[] pdu, NumberLocationInfo location, int msgType) {
            super(nId, phone, name, body, date, subId, location);
            this.mSize = size;
            this.mExpDate = expdate;
            if (pdu != null) {
                this.mPdu = Arrays.copyOf(pdu, pdu.length);
            }
            this.mMsgType = msgType;
        }

        public MessageInfo(String phone, String name, String body, long size, long date, long expdate, int subId, byte[] pdu, int msgType) {
            super(phone, name, body, date, subId);
            this.mSize = size;
            this.mExpDate = expdate;
            if (pdu != null) {
                this.mPdu = Arrays.copyOf(pdu, pdu.length);
            }
            this.mMsgType = msgType;
        }

        public int getMsgType() {
            return this.mMsgType;
        }

        public ContentValues getAsContentValues() {
            ContentValues values = new ContentValues();
            values.put("phone", PhoneNumberUtils.stripSeparators(getPhone()));
            values.put("name", getName());
            values.put(TbMessages.BODY, getBody());
            values.put("size", Long.valueOf(getSize()));
            values.put("date", Long.valueOf(getDate()));
            values.put(TbMessages.EXPDATE, Long.valueOf(getExpDate()));
            values.put("sub_id", Integer.valueOf(getSubId()));
            values.put(TbMessages.PDU, getPdu());
            values.put("type", Integer.valueOf(getMsgType()));
            values.put("block_reason", Integer.valueOf(getBlockReason()));
            return values;
        }

        public long getSize() {
            return this.mSize;
        }

        public long getExpDate() {
            return this.mExpDate;
        }

        public byte[] getPdu() {
            if (this.mPdu != null) {
                return Arrays.copyOf(this.mPdu, this.mPdu.length);
            }
            return new byte[0];
        }

        public String toString() {
            return "[size]= " + getSize() + "[date]= " + getDate() + "[exp_date]= " + getExpDate() + "[type]= " + getMsgType();
        }

        public static SmsMsgInfo translateFromMessageInfo(MessageInfo messageInfo) {
            return new SmsMsgInfo(messageInfo.getId(), messageInfo.getPhone(), messageInfo.getName(), messageInfo.getBody(), messageInfo.getDate(), messageInfo.getSubId(), messageInfo.getNumberLocationInfo());
        }

        public static List<SmsMsgInfo> translateFromMessageInfoList(List<MessageInfo> messageInfoList) {
            List<SmsMsgInfo> smsMsgInfos = new ArrayList();
            for (MessageInfo messageInfo : messageInfoList) {
                if (translateFromMessageInfo(messageInfo) != null) {
                    smsMsgInfos.add(translateFromMessageInfo(messageInfo));
                }
            }
            return smsMsgInfos;
        }

        public String getBodyEx(Context context) {
            int type = getMsgType();
            String origin_body = getBody();
            switch (type) {
                case 0:
                    if (!TextUtils.isEmpty(origin_body)) {
                        return origin_body;
                    }
                    HwLog.w(TAG, "SMS has no body.");
                    return "";
                case 1:
                    return context.getResources().getString(R.string.harassment_mms_list_prefix_message);
                default:
                    HwLog.w(TAG, "it will never happen");
                    return "";
            }
        }
    }

    public static class MsgIntentWrapper {
        private MessageInfo mMsgInfo;
        private Intent mMsgIntent;

        public MsgIntentWrapper(MessageInfo msgInfo, Intent msgIntent) {
            this.mMsgInfo = msgInfo;
            this.mMsgIntent = msgIntent;
        }

        public MessageInfo getMsgInfo() {
            return this.mMsgInfo;
        }

        public Intent getMsgIntent() {
            return this.mMsgIntent;
        }
    }

    public static class NumberMarkInfo {
        private static final Map<String, Integer> NUMBER_CSP_TAG_TYPE_TO_HSMID = new HashMap<String, Integer>() {
            {
                put("crank", Integer.valueOf(50));
                put("fraud", Integer.valueOf(54));
                put("express", Integer.valueOf(55));
                put("promote sales", Integer.valueOf(53));
                put("house agent", Integer.valueOf(51));
                put("others", Integer.valueOf(TagType.TAG_TYPE_SELF_TAG));
            }
        };
        private static final String TAG = "NumberMarkInfo";
        private String mClassify;
        private String mDescription;
        private boolean mIsLocal;
        private boolean mIsTimeout;
        public int mMarkCount = 0;
        public String mMarkName;
        public String mMarkNumber;
        public int mMarkType;
        private String mSaveTimeStamp;
        private String mSupplier;

        public static class TagType {
            public static final int TAG_TYPE_ADVERTISE_SALES = 53;
            public static final int TAG_TYPE_ESTATE_AGENT = 51;
            public static final int TAG_TYPE_EXPRESS = 55;
            public static final int TAG_TYPE_HARASS_PHONE = 50;
            public static final int TAG_TYPE_INSURANCE_FINANCING = 52;
            public static final int TAG_TYPE_SELF_TAG = 10055;
            public static final int TAG_TYPE_TAXI = 56;
            public static final int TAG_TYPE_TELEPHONE_FRAND = 54;
        }

        public NumberMarkInfo(int markType, int markCount, String markContent, String phone) {
            this.mMarkType = markType;
            this.mMarkCount = markCount;
            this.mMarkNumber = phone;
            this.mMarkName = markContent;
        }

        public int getmMarkCount() {
            return this.mMarkCount;
        }

        public int getmMarkType() {
            return this.mMarkType;
        }

        public String getmMarkName() {
            return this.mMarkName;
        }

        public String getmMarkNumber() {
            return this.mMarkNumber;
        }

        public String getClassify() {
            return this.mClassify;
        }

        public boolean getIsLocal() {
            return this.mIsLocal;
        }

        public String getSupplier() {
            return this.mSupplier;
        }

        public String getDescription() {
            return this.mDescription;
        }

        public String getSaveTimeStamp() {
            return this.mSaveTimeStamp;
        }

        public boolean getIsTimeout() {
            return this.mIsTimeout;
        }

        public void setIsLocal(boolean isLocal) {
            this.mIsLocal = isLocal;
        }

        public void setClassify(String classify) {
            this.mClassify = classify;
        }

        public void setSupplier(String supplier) {
            this.mSupplier = supplier;
        }

        public void setDescription(String description) {
            this.mDescription = description;
        }

        public void setSaveTimeStamp(String saveTimeStamp) {
            this.mSaveTimeStamp = saveTimeStamp;
        }

        public void setIsTimeout(boolean isTimeout) {
            this.mIsTimeout = isTimeout;
        }

        public String toString() {
            return "type = " + this.mMarkType + " name = " + this.mMarkName + " count=" + this.mMarkCount + " phone= " + this.mMarkNumber;
        }

        public static int getTypeFromClassify(String classify) {
            if (NUMBER_CSP_TAG_TYPE_TO_HSMID.containsKey(classify)) {
                return ((Integer) NUMBER_CSP_TAG_TYPE_TO_HSMID.get(classify)).intValue();
            }
            HwLog.i(TAG, "this classify is out of scope");
            return TagType.TAG_TYPE_SELF_TAG;
        }
    }

    public static class ParcelableBlacklistItem implements Parcelable {
        public static final Creator<ParcelableBlacklistItem> CREATOR = new Creator<ParcelableBlacklistItem>() {
            public ParcelableBlacklistItem[] newArray(int size) {
                return new ParcelableBlacklistItem[size];
            }

            public ParcelableBlacklistItem createFromParcel(Parcel source) {
                return new ParcelableBlacklistItem(source);
            }
        };
        private String mName;
        private String mPhone;

        public ParcelableBlacklistItem(Parcel source) {
            this.mPhone = source.readString();
            this.mName = source.readString();
        }

        public ParcelableBlacklistItem(String phone, String name) {
            this.mPhone = phone;
            this.mName = name;
        }

        public ParcelableBlacklistItem() {
            this.mPhone = "";
            this.mName = "";
        }

        public String getPhone() {
            return this.mPhone;
        }

        public void setPhone(String phone) {
            this.mPhone = phone;
        }

        public String getName() {
            return this.mName;
        }

        public void setName(String name) {
            this.mName = name;
        }

        public void writeToParcel(Parcel dest, int flag) {
            dest.writeString(getPhone());
            dest.writeString(getName());
        }

        public int describeContents() {
            return 0;
        }
    }

    public static class SmsInfo extends ContactInfo {
        private static final String TAG = "SmsInfo";
        public static final int TYPE_MMS = 1;
        public static final int TYPE_SMS = 0;
        private long date;
        private String messages;
        private int type = 0;

        public SmsInfo(String name, String number, String messages, long date) {
            super(-1, number, name);
            this.messages = messages;
            this.date = date;
        }

        public SmsInfo(String name, String number, String messages, long date, int type) {
            super(-1, number, name);
            this.messages = messages;
            this.date = date;
            this.type = type;
        }

        public String getMessages() {
            return this.messages;
        }

        public void setMessages(String messages) {
            this.messages = messages;
        }

        public long getDate() {
            return this.date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public int getType() {
            return this.type;
        }

        public String getMessageBodyEx(Context context) {
            int type = getType();
            String origin_body = getMessages();
            switch (type) {
                case 0:
                    if (!TextUtils.isEmpty(origin_body)) {
                        return origin_body;
                    }
                    HwLog.w(TAG, "SMS has no body.");
                    return "";
                case 1:
                    if (TextUtils.isEmpty(origin_body)) {
                        String nosubjectString = context.getResources().getString(R.string.harassment_mms_list_subject_message);
                        return context.getResources().getString(R.string.harassment_mms_list_body_message, new Object[]{nosubjectString});
                    }
                    return context.getResources().getString(R.string.harassment_mms_list_body_message, new Object[]{origin_body});
                default:
                    return "";
            }
        }
    }

    public static class SmsIntentWrapper {
        private SmsMsgInfo mSmsInfo;
        private Intent mSmsIntent;

        public SmsIntentWrapper(SmsMsgInfo smsInfo, Intent smsIntent) {
            this.mSmsInfo = smsInfo;
            this.mSmsIntent = smsIntent;
        }

        public SmsMsgInfo getSmsMsgInfo() {
            return this.mSmsInfo;
        }

        public Intent getSmsIntent() {
            return this.mSmsIntent;
        }
    }

    public static class WhitelistInfo extends ContactInfo {
        public static final AlpComparator<WhitelistInfo> WHITELIST_ALP_COMPARATOR = new AlpComparator<WhitelistInfo>() {
            public String getStringKey(WhitelistInfo t) {
                String name = t.getName();
                String num = t.getPhone();
                if (TextUtils.isEmpty(name)) {
                    return num;
                }
                return t.getName();
            }
        };

        public WhitelistInfo(int nId, String phone, String name) {
            super(nId, phone, name);
        }

        public WhitelistInfo(int nId, String phone, String name, NumberLocationInfo location) {
            super(nId, phone, name, location);
        }

        public WhitelistInfo(ParcelableBlacklistItem item) {
            super(0, item.getPhone(), item.getName());
        }
    }
}
