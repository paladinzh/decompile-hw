package com.android.settings.applications;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.app.procstats.ProcessState;
import com.android.internal.app.procstats.ProcessStats;
import com.android.internal.app.procstats.ProcessStats.PackageState;
import com.android.internal.app.procstats.ProcessStats.ProcessDataCollection;
import com.android.internal.app.procstats.ServiceState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class ProcStatsEntry implements Parcelable {
    public static final Creator<ProcStatsEntry> CREATOR = new Creator<ProcStatsEntry>() {
        public ProcStatsEntry createFromParcel(Parcel in) {
            return new ProcStatsEntry(in);
        }

        public ProcStatsEntry[] newArray(int size) {
            return new ProcStatsEntry[size];
        }
    };
    private static boolean DEBUG = false;
    final long mAvgBgMem;
    final long mAvgRunMem;
    String mBestTargetPackage;
    final long mBgDuration;
    final double mBgWeight;
    public CharSequence mLabel;
    final long mMaxBgMem;
    final long mMaxRunMem;
    final String mName;
    final String mPackage;
    final ArrayList<String> mPackages = new ArrayList();
    final long mRunDuration;
    final double mRunWeight;
    ArrayMap<String, ArrayList<Service>> mServices = new ArrayMap(1);
    final int mUid;

    public static final class Service implements Parcelable {
        public static final Creator<Service> CREATOR = new Creator<Service>() {
            public Service createFromParcel(Parcel in) {
                return new Service(in);
            }

            public Service[] newArray(int size) {
                return new Service[size];
            }
        };
        final long mDuration;
        final String mName;
        final String mPackage;
        final String mProcess;

        public Service(ServiceState service) {
            this.mPackage = service.getPackage();
            this.mName = service.getName();
            this.mProcess = service.getProcessName();
            this.mDuration = service.dumpTime(null, null, 0, -1, 0, 0);
        }

        public Service(Parcel in) {
            this.mPackage = in.readString();
            this.mName = in.readString();
            this.mProcess = in.readString();
            this.mDuration = in.readLong();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mPackage);
            dest.writeString(this.mName);
            dest.writeString(this.mProcess);
            dest.writeLong(this.mDuration);
        }
    }

    public ProcStatsEntry(ProcessState proc, String packageName, ProcessDataCollection tmpBgTotals, ProcessDataCollection tmpRunTotals, boolean useUss) {
        proc.computeProcessData(tmpBgTotals, 0);
        proc.computeProcessData(tmpRunTotals, 0);
        this.mPackage = proc.getPackage();
        this.mUid = proc.getUid();
        this.mName = proc.getName();
        this.mPackages.add(packageName);
        this.mBgDuration = tmpBgTotals.totalTime;
        this.mAvgBgMem = useUss ? tmpBgTotals.avgUss : tmpBgTotals.avgPss;
        this.mMaxBgMem = useUss ? tmpBgTotals.maxUss : tmpBgTotals.maxPss;
        this.mBgWeight = ((double) this.mAvgBgMem) * ((double) this.mBgDuration);
        this.mRunDuration = tmpRunTotals.totalTime;
        this.mAvgRunMem = useUss ? tmpRunTotals.avgUss : tmpRunTotals.avgPss;
        this.mMaxRunMem = useUss ? tmpRunTotals.maxUss : tmpRunTotals.maxPss;
        this.mRunWeight = ((double) this.mAvgRunMem) * ((double) this.mRunDuration);
        if (DEBUG) {
            Log.d("ProcStatsEntry", "New proc entry " + proc.getName() + ": dur=" + this.mBgDuration + " avgpss=" + this.mAvgBgMem + " weight=" + this.mBgWeight);
        }
    }

    public ProcStatsEntry(String pkgName, int uid, String procName, long duration, long mem, long memDuration) {
        this.mPackage = pkgName;
        this.mUid = uid;
        this.mName = procName;
        this.mRunDuration = duration;
        this.mBgDuration = duration;
        this.mMaxRunMem = mem;
        this.mAvgRunMem = mem;
        this.mMaxBgMem = mem;
        this.mAvgBgMem = mem;
        double d = ((double) memDuration) * ((double) mem);
        this.mRunWeight = d;
        this.mBgWeight = d;
        if (DEBUG) {
            Log.d("ProcStatsEntry", "New proc entry " + procName + ": dur=" + this.mBgDuration + " avgpss=" + this.mAvgBgMem + " weight=" + this.mBgWeight);
        }
    }

    public ProcStatsEntry(Parcel in) {
        this.mPackage = in.readString();
        this.mUid = in.readInt();
        this.mName = in.readString();
        in.readStringList(this.mPackages);
        this.mBgDuration = in.readLong();
        this.mAvgBgMem = in.readLong();
        this.mMaxBgMem = in.readLong();
        this.mBgWeight = in.readDouble();
        this.mRunDuration = in.readLong();
        this.mAvgRunMem = in.readLong();
        this.mMaxRunMem = in.readLong();
        this.mRunWeight = in.readDouble();
        this.mBestTargetPackage = in.readString();
        int N = in.readInt();
        if (N > 0) {
            this.mServices.ensureCapacity(N);
            for (int i = 0; i < N; i++) {
                String key = in.readString();
                ArrayList<Service> value = new ArrayList();
                in.readTypedList(value, Service.CREATOR);
                this.mServices.append(key, value);
            }
        }
    }

    public void addPackage(String packageName) {
        this.mPackages.add(packageName);
    }

    public void evaluateTargetPackage(PackageManager pm, ProcessStats stats, ProcessDataCollection bgTotals, ProcessDataCollection runTotals, Comparator<ProcStatsEntry> compare, boolean useUss) {
        this.mBestTargetPackage = null;
        if (this.mPackages.size() == 1) {
            if (DEBUG) {
                Log.d("ProcStatsEntry", "Eval pkg of " + this.mName + ": single pkg " + ((String) this.mPackages.get(0)));
            }
            this.mBestTargetPackage = (String) this.mPackages.get(0);
            return;
        }
        int ipkg;
        for (ipkg = 0; ipkg < this.mPackages.size(); ipkg++) {
            if ("android".equals(this.mPackages.get(ipkg))) {
                this.mBestTargetPackage = (String) this.mPackages.get(ipkg);
                return;
            }
        }
        ArrayList<ProcStatsEntry> subProcs = new ArrayList();
        for (ipkg = 0; ipkg < this.mPackages.size(); ipkg++) {
            SparseArray<PackageState> vpkgs = (SparseArray) stats.mPackages.get((String) this.mPackages.get(ipkg), this.mUid);
            for (int ivers = 0; ivers < vpkgs.size(); ivers++) {
                PackageState pkgState = (PackageState) vpkgs.valueAt(ivers);
                if (DEBUG) {
                    Log.d("ProcStatsEntry", "Eval pkg of " + this.mName + ", pkg " + pkgState + ":");
                }
                if (pkgState == null) {
                    Log.w("ProcStatsEntry", "No package state found for " + ((String) this.mPackages.get(ipkg)) + "/" + this.mUid + " in process " + this.mName);
                } else {
                    ProcessState pkgProc = (ProcessState) pkgState.mProcesses.get(this.mName);
                    if (pkgProc == null) {
                        Log.w("ProcStatsEntry", "No process " + this.mName + " found in package state " + ((String) this.mPackages.get(ipkg)) + "/" + this.mUid);
                    } else {
                        subProcs.add(new ProcStatsEntry(pkgProc, pkgState.mPackageName, bgTotals, runTotals, useUss));
                    }
                }
            }
        }
        if (subProcs.size() > 1) {
            Collections.sort(subProcs, compare);
            if (((ProcStatsEntry) subProcs.get(0)).mRunWeight > ((ProcStatsEntry) subProcs.get(1)).mRunWeight * 3.0d) {
                if (DEBUG) {
                    Log.d("ProcStatsEntry", "Eval pkg of " + this.mName + ": best pkg " + ((ProcStatsEntry) subProcs.get(0)).mPackage + " weight " + ((ProcStatsEntry) subProcs.get(0)).mRunWeight + " better than " + ((ProcStatsEntry) subProcs.get(1)).mPackage + " weight " + ((ProcStatsEntry) subProcs.get(1)).mRunWeight);
                }
                this.mBestTargetPackage = ((ProcStatsEntry) subProcs.get(0)).mPackage;
                return;
            }
            double maxWeight = ((ProcStatsEntry) subProcs.get(0)).mRunWeight;
            long bestRunTime = -1;
            boolean bestPersistent = false;
            for (int i = 0; i < subProcs.size(); i++) {
                ProcStatsEntry subProc = (ProcStatsEntry) subProcs.get(i);
                if (subProc.mRunWeight >= maxWeight / 2.0d) {
                    try {
                        ApplicationInfo ai = pm.getApplicationInfo(subProc.mPackage, 0);
                        if (ai.icon == 0) {
                            if (DEBUG) {
                                Log.d("ProcStatsEntry", "Eval pkg of " + this.mName + ": pkg " + subProc.mPackage + " has no icon");
                            }
                        } else if ((ai.flags & 8) != 0) {
                            thisRunTime = subProc.mRunDuration;
                            if (!bestPersistent || thisRunTime > bestRunTime) {
                                if (DEBUG) {
                                    Log.d("ProcStatsEntry", "Eval pkg of " + this.mName + ": pkg " + subProc.mPackage + " new best pers run time " + thisRunTime);
                                }
                                bestRunTime = thisRunTime;
                                bestPersistent = true;
                            } else if (DEBUG) {
                                Log.d("ProcStatsEntry", "Eval pkg of " + this.mName + ": pkg " + subProc.mPackage + " pers run time " + thisRunTime + " not as good as last " + bestRunTime);
                            }
                        } else if (!bestPersistent) {
                            ArrayList subProcServices = null;
                            int NSP = this.mServices.size();
                            for (int isp = 0; isp < NSP; isp++) {
                                ArrayList<Service> subServices = (ArrayList) this.mServices.valueAt(isp);
                                if (((Service) subServices.get(0)).mPackage.equals(subProc.mPackage)) {
                                    subProcServices = subServices;
                                    break;
                                }
                            }
                            thisRunTime = 0;
                            if (subProcServices != null) {
                                int iss = 0;
                                int NSS = subProcServices.size();
                                while (iss < NSS) {
                                    Service service = (Service) subProcServices.get(iss);
                                    if (service.mDuration > 0) {
                                        if (DEBUG) {
                                            Log.d("ProcStatsEntry", "Eval pkg of " + this.mName + ": pkg " + subProc.mPackage + " service " + service.mName + " run time is " + service.mDuration);
                                        }
                                        thisRunTime = service.mDuration;
                                    } else {
                                        iss++;
                                    }
                                }
                            }
                            if (thisRunTime > bestRunTime) {
                                if (DEBUG) {
                                    Log.d("ProcStatsEntry", "Eval pkg of " + this.mName + ": pkg " + subProc.mPackage + " new best run time " + thisRunTime);
                                }
                                this.mBestTargetPackage = subProc.mPackage;
                                bestRunTime = thisRunTime;
                            } else if (DEBUG) {
                                Log.d("ProcStatsEntry", "Eval pkg of " + this.mName + ": pkg " + subProc.mPackage + " run time " + thisRunTime + " not as good as last " + bestRunTime);
                            }
                        } else if (DEBUG) {
                            Log.d("ProcStatsEntry", "Eval pkg of " + this.mName + ": pkg " + subProc.mPackage + " is not persistent");
                        }
                    } catch (NameNotFoundException e) {
                        if (DEBUG) {
                            Log.d("ProcStatsEntry", "Eval pkg of " + this.mName + ": pkg " + subProc.mPackage + " failed finding app info");
                        }
                    }
                } else if (DEBUG) {
                    Log.d("ProcStatsEntry", "Eval pkg of " + this.mName + ": pkg " + subProc.mPackage + " weight " + subProc.mRunWeight + " too small");
                }
            }
        } else if (subProcs.size() == 1) {
            this.mBestTargetPackage = ((ProcStatsEntry) subProcs.get(0)).mPackage;
        }
    }

    public void addService(ServiceState svc) {
        ArrayList<Service> services = (ArrayList) this.mServices.get(svc.getPackage());
        if (services == null) {
            services = new ArrayList();
            this.mServices.put(svc.getPackage(), services);
        }
        services.add(new Service(svc));
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackage);
        dest.writeInt(this.mUid);
        dest.writeString(this.mName);
        dest.writeStringList(this.mPackages);
        dest.writeLong(this.mBgDuration);
        dest.writeLong(this.mAvgBgMem);
        dest.writeLong(this.mMaxBgMem);
        dest.writeDouble(this.mBgWeight);
        dest.writeLong(this.mRunDuration);
        dest.writeLong(this.mAvgRunMem);
        dest.writeLong(this.mMaxRunMem);
        dest.writeDouble(this.mRunWeight);
        dest.writeString(this.mBestTargetPackage);
        int N = this.mServices.size();
        dest.writeInt(N);
        for (int i = 0; i < N; i++) {
            dest.writeString((String) this.mServices.keyAt(i));
            dest.writeTypedList((List) this.mServices.valueAt(i));
        }
    }
}
