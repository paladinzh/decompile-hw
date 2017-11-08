package com.huawei.android.freeshare.client.transfer;

import com.android.gallery3d.util.GalleryLog;
import com.huawei.android.freeshare.client.transfer.FileTransfer.TransferObserver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MissionManger implements TransferObserver {
    private boolean mCouldStartNextMission = true;
    private List<FileTransferListener> mListeners = new ArrayList();
    private FileTransfer mTransfer;
    private BlockingQueue<Mission> mWaitMissions = new LinkedBlockingQueue();

    public void setFileTransfer(FileTransfer transfer) {
        this.mTransfer = transfer;
        this.mTransfer.registerObserver(this);
    }

    public boolean offer(Mission mission) {
        if (this.mTransfer == null) {
            return false;
        }
        GalleryLog.d("freeshare_MissionManager", " start mission called ");
        if (mission.getTargetDevice() == null) {
            GalleryLog.d("freeshare_MissionManager", "target device is null");
            return false;
        } else if (mission.getState() != 2) {
            GalleryLog.e("freeshare_MissionManager", "mission is already started ");
            return false;
        } else {
            boolean res = this.mWaitMissions.offer(mission);
            nextMission();
            return res;
        }
    }

    private boolean nextMission() {
        GalleryLog.d("freeshare_MissionManager", " nextMission");
        boolean z = false;
        synchronized (this) {
            if (this.mCouldStartNextMission) {
                Mission toBeStartMission = (Mission) this.mWaitMissions.poll();
                if (toBeStartMission != null) {
                    z = this.mTransfer.start(toBeStartMission);
                } else {
                    GalleryLog.d("freeshare_MissionManager", "toBeStartMission ==null");
                    z = false;
                }
                if (z) {
                    this.mCouldStartNextMission = false;
                } else {
                    this.mCouldStartNextMission = true;
                }
            }
        }
        return z;
    }

    public void notifyChanged(TransferItem item) {
        GalleryLog.d("freeshare_MissionManager", "notify changed,item=" + item.mUri);
        Mission misson = item.getMission();
        misson.upDateState();
        if (item.isTransferring()) {
            for (FileTransferListener listener : this.mListeners) {
                listener.onProgressUpdate(item.mUri, item.getPorgress());
            }
        } else if (item.isComplete()) {
            boolean success = item.mSuccess;
            if (!success) {
                GalleryLog.d("freeshare_MissionManager", "transfer failed , clean the rest missions which target is the same");
                Iterator<Mission> it = this.mWaitMissions.iterator();
                while (it.hasNext()) {
                    if (((Mission) it.next()).isTheSameTarget(misson)) {
                        it.remove();
                    }
                }
            }
            for (FileTransferListener listener2 : this.mListeners) {
                listener2.onTransferFinish(item.mUri, success);
            }
            if (misson.isComplete()) {
                synchronized (this) {
                    this.mCouldStartNextMission = true;
                    nextMission();
                }
            }
        }
    }

    public int getRestMissionNum() {
        synchronized (this) {
            int numOfWatiMission = this.mWaitMissions.size();
            boolean isTransferring = this.mTransfer.isTransferring();
            GalleryLog.d("freeshare_MissionManager", "numOfWatiMission =" + numOfWatiMission + ",isTransferring=" + isTransferring);
            if (isTransferring) {
                int i = numOfWatiMission + 1;
                return i;
            }
            return numOfWatiMission;
        }
    }

    public boolean isEmpty() {
        return getRestMissionNum() == 0;
    }

    public boolean cancelRestMission() {
        GalleryLog.d("freeshare_MissionManager", "cancelRestMission");
        if (this.mTransfer == null) {
            return false;
        }
        synchronized (this) {
            this.mCouldStartNextMission = false;
            this.mWaitMissions.clear();
        }
        this.mTransfer.cancleTransferringMission();
        synchronized (this) {
            this.mCouldStartNextMission = true;
        }
        return true;
    }

    public void addMissionListener(FileTransferListener listener) {
        this.mListeners.add(listener);
    }
}
