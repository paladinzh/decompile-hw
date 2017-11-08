package com.android.contacts.statistical;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.RawContacts;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimUtility;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.statistical.StatisticalHelper.ReportSimState;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ContactReport {
    private static ContactReport instance;
    private Context mContext;
    private Timer timer;

    public static class AccountDetails {
        public String mAccountDataset;
        public String mAccountName;
        public String mAccountType;
        public int mContactsUsed;

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof AccountDetails)) {
                return false;
            }
            AccountDetails ad = (AccountDetails) o;
            if (this.mAccountName == null || this.mAccountType == null) {
                return false;
            }
            if (this.mAccountName.equals(ad.mAccountName)) {
                z = this.mAccountType.equals(ad.mAccountType);
            }
            return z;
        }

        public int hashCode() {
            return super.hashCode();
        }
    }

    private class MemoryUsageStatusBackgroundLoader implements Runnable {
        MemoryUsageStatusBackgroundLoader() {
        }

        public void run() {
            if (ContactReport.this.mContext != null) {
                List<AccountWithDataSet> lAccounts = AccountTypeManager.getInstance(ContactReport.this.mContext).getAccounts(false);
                if (lAccounts != null) {
                    if (HwLog.HWDBG) {
                        HwLog.d("ContactReport", "Account count = " + lAccounts.size());
                    }
                    AccountTypeManager lAccManager = AccountTypeManager.getInstance(ContactReport.this.mContext);
                    int countAll = 0;
                    int simCount = 0;
                    int phoneCount = 0;
                    int weiXinCount = 0;
                    int exchangeCount = 0;
                    for (AccountWithDataSet accountWithDataSet : lAccounts) {
                        if (HwLog.HWDBG) {
                            HwLog.d("ContactReport", "accountWithDataSet.type: " + accountWithDataSet.type);
                        }
                        AccountType lAccountType = lAccManager.getAccountType(accountWithDataSet.type, accountWithDataSet.dataSet);
                        if ((lAccountType != null && !lAccountType.isExtension()) || accountWithDataSet.hasData(ContactReport.this.mContext)) {
                            if ("com.android.huawei.sim".equalsIgnoreCase(accountWithDataSet.type)) {
                                int sim1Count = ContactReport.this.getSimContactCount(accountWithDataSet.type);
                                countAll += sim1Count;
                                simCount += sim1Count;
                                if (HwLog.HWDBG) {
                                    HwLog.d("ContactReport", accountWithDataSet.type + ":" + countAll);
                                }
                            } else if ("com.android.huawei.secondsim".equalsIgnoreCase(accountWithDataSet.type)) {
                                int sim2Count = ContactReport.this.getSimContactCount(accountWithDataSet.type);
                                countAll += sim2Count;
                                simCount += sim2Count;
                                if (HwLog.HWDBG) {
                                    HwLog.d("ContactReport", accountWithDataSet.type + ":" + countAll);
                                }
                            } else {
                                AccountDetails lAccountDetails = new AccountDetails();
                                lAccountDetails.mAccountType = accountWithDataSet.type;
                                lAccountDetails.mAccountName = accountWithDataSet.name;
                                if ("phone".equalsIgnoreCase(lAccountDetails.mAccountName)) {
                                    lAccountDetails.mAccountName = "Phone";
                                }
                                lAccountDetails.mAccountDataset = accountWithDataSet.dataSet;
                                int used = ContactReport.this.getContactCountBasedOnAccountType(lAccountDetails);
                                lAccountDetails.mContactsUsed = used;
                                if ("phone".equalsIgnoreCase(lAccountDetails.mAccountName)) {
                                    phoneCount = used;
                                } else if ("com.tencent.mm.account".equals(lAccountDetails.mAccountType)) {
                                    weiXinCount = used;
                                } else if (HwCustCommonConstants.EAS_ACCOUNT_TYPE.equals(lAccountDetails.mAccountType)) {
                                    exchangeCount += used;
                                }
                                countAll += lAccountDetails.mContactsUsed;
                            }
                        }
                    }
                    StatisticalHelper.report(ContactReport.this.mContext, 1001, String.format("{A:%d,P:%d,S:%d,W:%d,E:%d,STAR:%d}", new Object[]{Integer.valueOf(countAll), Integer.valueOf(phoneCount), Integer.valueOf(simCount), Integer.valueOf(weiXinCount), Integer.valueOf(exchangeCount), Integer.valueOf(ContactReport.this.getStaredCounts(ContactReport.this.mContext))}));
                }
            }
        }
    }

    private ContactReport(Context context) {
        if (context != null) {
            this.mContext = context.getApplicationContext();
        }
    }

    public static synchronized ContactReport getInstance(Context context) {
        ContactReport contactReport;
        synchronized (ContactReport.class) {
            if (instance == null) {
                instance = new ContactReport(context);
            }
            contactReport = instance;
        }
        return contactReport;
    }

    public void reportContactCount() {
        if (this.timer == null) {
            this.timer = new Timer();
            this.timer.schedule(new TimerTask() {
                public void run() {
                    if (ContactReport.this.mContext != null) {
                        ContactsThreadPool.getInstance().execute(new MemoryUsageStatusBackgroundLoader());
                        ContactsThreadPool.getInstance().execute(new ReportSimState(ContactReport.this.mContext));
                    }
                }
            }, 3600000, 604800000);
        }
    }

    private int getStaredCounts(Context ctx) {
        if (ctx == null) {
            HwLog.w("ContactReport", "getStaredCounts Context is null");
            return 0;
        }
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"_id"}, "starred=?", new String[]{CallInterceptDetails.BRANDED_STATE}, null);
            if (cursor != null) {
                int count = cursor.getCount();
                if (cursor != null) {
                    cursor.close();
                }
                return count;
            }
            if (cursor != null) {
                cursor.close();
            }
            return 0;
        } catch (Exception e) {
            HwLog.e("ContactReport", "somethings wrong with databases", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int getContactCountBasedOnAccountType(AccountDetails aAccountDetails) {
        if (this.mContext == null) {
            return 0;
        }
        String and = " AND ";
        StringBuffer lWhereClause = new StringBuffer();
        lWhereClause.append("account_name=?");
        lWhereClause.append(and + "account_type" + "='" + aAccountDetails.mAccountType + "'");
        lWhereClause.append(and + "data_set");
        if (aAccountDetails.mAccountDataset != null) {
            lWhereClause.append("='" + aAccountDetails.mAccountDataset + "'");
        } else {
            lWhereClause.append(" IS NULL");
        }
        lWhereClause.append(and + "deleted" + "= 0");
        if (PhoneCapabilityTester.isOnlySyncMyContactsEnabled(this.mContext)) {
            lWhereClause.append(and + "contact_id IN default_directory");
        }
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = this.mContext.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"contact_id"}, lWhereClause.toString(), new String[]{aAccountDetails.mAccountName}, null);
            if (cursor != null) {
                count = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
            return count;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int getSimContactCount(String accountType) {
        int lSlotId = SimFactoryManager.getSlotIdBasedOnAccountType(accountType);
        if (!SimUtility.isSimReady(lSlotId)) {
            return 0;
        }
        int lTotalSpace = SimFactoryManager.getSimConfig(accountType).getSimCapacity();
        int lFreespace = SimFactoryManager.getSimConfig(accountType).getAvailableFreeSpace();
        if (lTotalSpace == 0 && lFreespace == 0) {
            return SimFactoryManager.getTotalSIMContactsPresent(lSlotId);
        }
        return lTotalSpace - lFreespace;
    }
}
