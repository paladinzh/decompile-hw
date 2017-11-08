package com.android.contacts.hap.sprint.calllog;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import com.android.contacts.hap.CommonConstants;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

public class HwCustCallInterceptData {
    private static String SEPERATOR = ",";
    private static final String TAG = "HwCustCallInterceptData";
    private Map<String, CallInterceptDetails> mCacheInterceptPattern = new HashMap();
    private Map<String, CallInterceptDetails> mCallInterceptPattern = new HashMap();

    static class CallInterceptDetails {
        public static final String ALL_ROAMING_STATE = "null";
        public static final String BRANDED_STATE = "1";
        public static final String UNBRANDED_STATE = "2";
        private Map<String, Intent> mAllStateCallInterceptDetails = new HashMap();
        private Map<String, Intent> mBrandedStateCallInterceptDetails = new HashMap();
        private Map<String, Intent> mUnbrandedStateCallInterceptDetails = new HashMap();

        CallInterceptDetails() {
        }

        void addCallIntercepts(String brandedState, String roamingState, String intent) {
            if (brandedState == null || roamingState == null || intent == null) {
                if (CommonConstants.LOG_DEBUG) {
                    Log.d(HwCustCallInterceptData.TAG, "addCallIntercepts() .. returning null, (brandedState :" + brandedState + ") , (roamingState" + roamingState + ") ");
                }
                return;
            }
            brandedState = brandedState.trim();
            roamingState = roamingState.trim();
            intent = intent.trim();
            if (brandedState.length() >= 1 && roamingState.length() >= 1 && intent.length() >= 1) {
                roamingState = roamingState.toLowerCase(Locale.US);
                Intent callInterceptIntent = HwCustCallInterceptData.parseIntentURI(intent);
                if (BRANDED_STATE.equals(brandedState)) {
                    if (CommonConstants.LOG_DEBUG) {
                        Log.d(HwCustCallInterceptData.TAG, "addCallIntercepts() ..phone is branded, with (roamingas " + roamingState + ")");
                    }
                    this.mBrandedStateCallInterceptDetails.put(roamingState, callInterceptIntent);
                } else if (UNBRANDED_STATE.equals(brandedState)) {
                    if (CommonConstants.LOG_DEBUG) {
                        Log.d(HwCustCallInterceptData.TAG, "addCallIntercepts() ..phone is unbranded, with (roamingas " + roamingState + ")");
                    }
                    this.mUnbrandedStateCallInterceptDetails.put(roamingState, callInterceptIntent);
                } else {
                    if (CommonConstants.LOG_DEBUG) {
                        Log.d(HwCustCallInterceptData.TAG, "addCallIntercepts() ..phoneState else condition keeping all unknown values, with (roamingas " + roamingState + ")");
                    }
                    this.mAllStateCallInterceptDetails.put(roamingState, callInterceptIntent);
                }
            }
        }

        public Intent getCallInterceptDetails(String brandedState, String roamingState) {
            Intent intent = null;
            if (BRANDED_STATE.equals(brandedState)) {
                intent = (Intent) this.mBrandedStateCallInterceptDetails.get(roamingState);
                if (intent == null) {
                    intent = (Intent) this.mBrandedStateCallInterceptDetails.get("null");
                }
            } else if (UNBRANDED_STATE.equals(brandedState)) {
                intent = (Intent) this.mUnbrandedStateCallInterceptDetails.get(roamingState);
                if (intent == null) {
                    intent = (Intent) this.mUnbrandedStateCallInterceptDetails.get("null");
                }
            }
            if (intent != null) {
                return intent;
            }
            intent = (Intent) this.mAllStateCallInterceptDetails.get(roamingState);
            if (intent == null) {
                return (Intent) this.mAllStateCallInterceptDetails.get("null");
            }
            return intent;
        }
    }

    public void addRawCallIntercept(String callIntercept) {
        if (callIntercept != null) {
            String[] data = callIntercept.split(SEPERATOR);
            if (data == null || data.length != 4) {
                Log.d(TAG, "received invalid pattern values.. ");
                return;
            }
            String number = PhoneNumberUtils.stripSeparators(data[0]);
            if (number != null) {
                number = number.trim();
                if (number.length() > 0) {
                    CallInterceptDetails details = (CallInterceptDetails) this.mCacheInterceptPattern.get(number);
                    if (details == null) {
                        details = new CallInterceptDetails();
                        this.mCacheInterceptPattern.put(number, details);
                        if (CommonConstants.LOG_DEBUG) {
                            Log.d(TAG, "creating and adding CallInterceptDetails");
                        }
                    }
                    details.addCallIntercepts(data[1], data[2], data[3]);
                } else if (CommonConstants.LOG_DEBUG) {
                    Log.d(TAG, "invlaid number");
                }
            }
        } else if (CommonConstants.LOG_DEBUG) {
            Log.d(TAG, "callIntercept is null ");
        }
    }

    public synchronized void commitCache() {
        this.mCallInterceptPattern.clear();
        if (CommonConstants.LOG_DEBUG) {
            Log.d(TAG, "cleared the mCallInterceptPattern cache ");
        }
        this.mCallInterceptPattern.putAll(this.mCacheInterceptPattern);
        if (CommonConstants.LOG_DEBUG) {
            Log.d(TAG, " putting all mCacheInterceptPattern values to mCallInterceptPattern map...");
        }
        this.mCacheInterceptPattern.clear();
    }

    public synchronized Intent getCallInterceptIntent(String number, String brandedState, String roamingState) {
        Intent callInterceptIntent;
        if (CommonConstants.LOG_DEBUG) {
            Log.d(TAG, " getCallInterceptIntent().(brandedState  :  " + brandedState + " ) , (roamingState  : " + roamingState);
        }
        callInterceptIntent = null;
        if (number != null) {
            CallInterceptDetails details = (CallInterceptDetails) this.mCallInterceptPattern.get(number);
            if (details != null) {
                callInterceptIntent = details.getCallInterceptDetails(brandedState, roamingState);
                if (CommonConstants.LOG_DEBUG) {
                    Log.d(TAG, "getCallInterceptIntent() .. callInterceptIntent got from the map for the number,phoneState,roaming state is : " + callInterceptIntent);
                }
            } else if (CommonConstants.LOG_DEBUG) {
                Log.d(TAG, "getCallInterceptIntent() .. details is null ");
            }
        }
        return callInterceptIntent;
    }

    private static Intent parseIntentURI(String intentString) {
        Intent intent = new Intent();
        Vector<String> intentTokens = new Vector();
        for (String tok : intentString.split(";")) {
            intentTokens.add(tok.trim());
        }
        for (String tok2 : intentTokens) {
            String[] tmpTokenArray = tok2.split("=");
            if (tmpTokenArray.length == 2) {
                String key = tmpTokenArray[0].trim();
                String val = tmpTokenArray[1].trim();
                if (key.toLowerCase(Locale.ENGLISH).compareTo("action") == 0) {
                    intent.setAction(val);
                } else if (key.toLowerCase(Locale.ENGLISH).compareTo("type") == 0) {
                    intent.setType(val);
                } else if (key.toLowerCase(Locale.ENGLISH).compareTo("component") == 0) {
                    tmpTokenArray = val.split("/");
                    intent.setComponent(new ComponentName(tmpTokenArray[0], tmpTokenArray[0] + tmpTokenArray[1]));
                } else if (key.startsWith("S.", 0) || key.startsWith("B.", 0) || key.startsWith("b.", 0) || key.startsWith("c.", 0) || key.startsWith("d.", 0) || key.startsWith("f.", 0) || key.startsWith("i.", 0) || key.startsWith("l.", 0) || key.startsWith("s.", 0)) {
                    intent.putExtra(Uri.decode(key.substring(2, key.length())), val);
                } else {
                    intent.putExtra(key, val);
                }
            }
        }
        String EXTRA_SOURCE = "com.sprint.zone.source";
        if (CommonConstants.LOG_DEBUG) {
            Log.i(TAG, "parseIntentURI() > EXTRA : " + intent.getStringExtra("com.sprint.zone.source"));
        }
        Bundle extras = intent.getExtras();
        if (extras != null && CommonConstants.LOG_DEBUG) {
            Log.i(TAG, "parseIntentURI() > EXTRA data : " + extras.toString());
        }
        return intent;
    }
}
