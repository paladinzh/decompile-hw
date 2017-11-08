package com.huawei.gallery.media;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.text.TextUtils;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.gallery.phonestatus.PhoneState;
import com.huawei.gallery.service.AsyncService;
import com.huawei.gallery.story.utils.DBSCANClustering;
import com.huawei.gallery.story.utils.LocationUtils;
import com.huawei.gallery.story.utils.LocationUtils.AddressInfo;
import com.huawei.gallery.story.utils.LocationUtils.LatlngData;
import com.huawei.gallery.story.utils.LocationUtils.SamplingFileInfo;
import com.huawei.gallery.story.utils.SimilarPhotoScanner;
import com.huawei.gallery.story.utils.SimilarPhotoScanner.SimilarScanFinishListener;
import com.huawei.gallery.story.utils.StoryAlbumDateUtils;
import com.huawei.gallery.story.utils.StoryAlbumDateUtils.DateTaken;
import com.huawei.gallery.story.utils.StoryAlbumFileDownLoader;
import com.huawei.gallery.story.utils.StoryAlbumFileDownLoader.FileDownloadListener;
import com.huawei.gallery.story.utils.StoryAlbumUtils;
import com.huawei.gallery.util.MyPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class StoryAlbumService extends AsyncService {
    private static final MyPrinter LOG = new MyPrinter("Clustering_StoryAlbumService");
    private static Comparator sComparator = new Comparator<DateTaken>() {
        public int compare(DateTaken t1, DateTaken t2) {
            if (t1.min > t2.min) {
                return 1;
            }
            if (t1.min == t2.min) {
                return 0;
            }
            return -1;
        }
    };
    private FileDownloadListener lcdDownloadListener = new FileDownloadListener() {
        public void onProgress(double percentage) {
        }

        public void onDownloadFinished() {
            StoryAlbumService.LOG.d("!--- end download lcd files for album " + StoryAlbumService.this.mStoryAlbum.storyId);
            StoryAlbumService.this.sendMessage(6, 2000, null);
        }
    };
    private volatile boolean mForceStop = false;
    private int mJobCode = 0;
    private ArrayList<StoryAlbum> mNewStoryAlbums = new ArrayList();
    private List<StoryAlbum> mOldStoryAlbums = new ArrayList();
    private boolean mPendingCoverUpdate = false;
    private int mPendingMessageId = -1;
    private boolean mRestartGeoService = false;
    private long mServiceBeginTime = 0;
    private SimilarPhotoScanner mSimilarPhotoScanner = null;
    private StoryAlbum mStoryAlbum = null;
    private List<StoryAlbumFile> mTodoClusterFiles = new ArrayList();
    private List<FileInfo> mUnReadyLcdFiles = new ArrayList();
    private SimilarScanFinishListener similarScanFinishListener = new SimilarScanFinishListener() {
        public void onFinished(ArrayList<Integer> similarId) {
            Intent intent = new Intent();
            intent.putIntegerArrayListExtra("similarIds", similarId);
            StoryAlbumService.this.sendMessage(10, 0, intent);
            StoryAlbumService.LOG.d("!--- end scan similar");
        }
    };

    private void stopStoryAlbumService(int r1) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.gallery.media.StoryAlbumService.stopStoryAlbumService(int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.gallery.media.StoryAlbumService.stopStoryAlbumService(int):void");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            LOG.d("null intent, return sticky");
            return 2;
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            LOG.d("empty action, return sticky");
            return 2;
        } else if (action.equals("start_cluster")) {
            return processClusterAction(intent);
        } else {
            if (action.equals("update_cover")) {
                return processUpdateCoverAction(intent);
            }
            return 2;
        }
    }

    private int processClusterAction(Intent intent) {
        if ((this.mJobCode & 1) != 0) {
            LOG.d("has job todo, return sticky");
            return 2;
        }
        this.mJobCode |= 1;
        LOG.d("--schedule-- [onStartCommand] start story service");
        this.mForceStop = false;
        sendMessage(0, 0, intent);
        return 3;
    }

    private int processUpdateCoverAction(Intent intent) {
        if ((this.mJobCode & 2) != 0) {
            LOG.d("has update job todo, return sticky");
            this.mPendingCoverUpdate = true;
            return 2;
        }
        LOG.d("--schedule-- [onStartCommand] start update cover");
        this.mJobCode |= 2;
        sendMessage(11, 0, intent);
        return 3;
    }

    public void onDestroy() {
        LOG.d("--schedule-- StoryService onDestroy");
        this.mForceStop = true;
        super.onDestroy();
    }

    private void sendMessage(int startId, long time, Intent intent) {
        Message msg = this.mServiceHandler.obtainMessage();
        decorateMsg(msg, intent, startId);
        if (time == 0) {
            this.mServiceHandler.sendMessage(msg);
        } else {
            this.mServiceHandler.sendMessageDelayed(msg, time);
        }
    }

    public boolean handleMessage(Message msg) {
        if (isPermissionIgnoreMessage(msg.what) || getRunningPermission()) {
            switch (msg.what) {
                case 0:
                    LOG.d("begin cluster ... ");
                    this.mServiceBeginTime = System.currentTimeMillis();
                    this.mRestartGeoService = false;
                    sendMessage(1, 0, null);
                    break;
                case 1:
                    LOG.d("!--- begin recover album error");
                    recoverStoryAlbumError();
                    sendMessage(2, 0, null);
                    break;
                case 2:
                    if (updateAlbumInfo()) {
                        return true;
                    }
                    break;
                case 3:
                    if (generateTimeClusters()) {
                        return true;
                    }
                    break;
                case 4:
                    if (assignClusterCodeState()) {
                        return true;
                    }
                    break;
                case 5:
                    checkDataIntegrity();
                    break;
                case 6:
                    LOG.d("!--- begin scan similar");
                    if (this.mSimilarPhotoScanner == null) {
                        this.mSimilarPhotoScanner = new SimilarPhotoScanner(this);
                    }
                    this.mSimilarPhotoScanner.scanSimilarPhotos(this.mStoryAlbum.storyId, this.similarScanFinishListener);
                    break;
                case 7:
                    if (generateNewAlbum()) {
                        return true;
                    }
                    break;
                case 8:
                    LOG.d("!--- begin down lcd files for album " + this.mStoryAlbum.storyId);
                    if (!StoryAlbumFileDownLoader.downloadUnReadyFiles(this.mStoryAlbum.storyId, this.mUnReadyLcdFiles, 1)) {
                        LOG.d("download fail");
                        if (!this.mOldStoryAlbums.contains(this.mStoryAlbum)) {
                            LOG.d("insert unintegrity album info");
                            this.mStoryAlbum.insertStoryAlbum(getContentResolver());
                        }
                        sendMessage(4, 0, null);
                        break;
                    }
                    StoryAlbumFileDownLoader.addDownloadListener(this.mStoryAlbum.storyId, this.lcdDownloadListener);
                    break;
                case 9:
                    LOG.d(String.format("Clustering service cost: %d", new Object[]{Long.valueOf(System.currentTimeMillis() - this.mServiceBeginTime)}));
                    LOG.d("end cluster ... ");
                    if (this.mRestartGeoService && !GalleryUtils.forbidWithNetwork()) {
                        startService(new Intent().setClass(this, GeoService.class));
                    }
                    stopStoryAlbumService(1);
                    break;
                case 10:
                    LOG.d("!--- begin remove similar");
                    removeSimilarPhotos((Intent) msg.obj);
                    break;
                case 11:
                    updateStoryAlbumCover();
                    if (!this.mPendingCoverUpdate) {
                        stopStoryAlbumService(2);
                        break;
                    }
                    this.mPendingCoverUpdate = false;
                    sendMessage(11, 0, null);
                    break;
            }
            return true;
        }
        stopStoryAlbumService(1);
        return true;
    }

    private void checkDataIntegrity() {
        LOG.d("!--- begin check data intergrity");
        this.mUnReadyLcdFiles = StoryAlbumUtils.queryStoryAlbumUnReadyFileInfo(this.mStoryAlbum.storyId, 1, getContentResolver());
        if (this.mUnReadyLcdFiles.size() > 0) {
            sendMessage(8, 0, null);
        } else {
            sendMessage(6, 0, null);
        }
    }

    private boolean generateNewAlbum() {
        LOG.d("!--- begin generate new album");
        if (removeInvalidStoryAlbum(this.mStoryAlbum)) {
            sendMessage(4, 0, null);
            return true;
        }
        updateStoryAlbumsDateTakenRange(this.mStoryAlbum);
        this.mStoryAlbum.date = StoryAlbumDateUtils.getDateString(this.mStoryAlbum.minDateTaken, this.mStoryAlbum.maxDateTaken, "yyyy.MM.dd");
        String address = genStoryAlbumAddress(this.mStoryAlbum);
        if (!TextUtils.isEmpty(address)) {
            this.mStoryAlbum.name = StoryAlbumUtils.genStoryAlbumName(this.mStoryAlbum.minDateTaken, this.mStoryAlbum.maxDateTaken, address, this);
        }
        this.mStoryAlbum.coverId = StoryAlbumUtils.getStoryAlbumCoverId(this.mStoryAlbum.storyId, getContentResolver());
        if (StoryAlbumUtils.queryStoryAlbumInfo(this.mStoryAlbum.storyId, getContentResolver()) != null) {
            this.mStoryAlbum.updateStoryAlbum(getContentResolver());
        } else {
            this.mStoryAlbum.insertStoryAlbum(getContentResolver());
        }
        LOG.d("new cluster album: " + this.mStoryAlbum.storyId + " cluster date=" + this.mStoryAlbum.date + " cluster name=" + " " + this.mStoryAlbum.name + "  minDateTaken=" + this.mStoryAlbum.minDateTaken + " maxDateTaken=" + this.mStoryAlbum.maxDateTaken);
        LOG.d("!--- end generate new album");
        sendMessage(4, 0, null);
        return false;
    }

    private boolean assignClusterCodeState() {
        LOG.d("!--- begin assign cluster code");
        if (this.mNewStoryAlbums.size() == 0) {
            LOG.d("!--- new album size 0");
            if (this.mPendingMessageId != -1) {
                sendMessage(this.mPendingMessageId, 0, null);
                this.mPendingMessageId = -1;
                return true;
            }
            sendMessage(9, 0, null);
            return true;
        }
        this.mStoryAlbum = (StoryAlbum) this.mNewStoryAlbums.get(this.mNewStoryAlbums.size() - 1);
        this.mNewStoryAlbums.remove(this.mStoryAlbum);
        assignFilesToCluster(this.mTodoClusterFiles, this.mStoryAlbum);
        sendMessage(5, 2000, null);
        return false;
    }

    private boolean generateTimeClusters() {
        LOG.d("!--- begin generate new time clusters");
        this.mTodoClusterFiles = StoryAlbumUtils.queryTodoStoryAlbumFiles(getContentResolver());
        if (this.mTodoClusterFiles.size() == 0) {
            LOG.d("no todo data");
            sendMessage(9, 0, null);
            return true;
        }
        this.mNewStoryAlbums = genNewStoryAlbums(clusteringImagesByDateTaken(this.mTodoClusterFiles), this.mOldStoryAlbums);
        if (this.mNewStoryAlbums.size() == 0) {
            LOG.d("no new cluster data");
            sendMessage(9, 0, null);
            return true;
        }
        sendMessage(4, 0, null);
        return false;
    }

    private boolean updateAlbumInfo() {
        LOG.d("!--- begin update old albums");
        this.mOldStoryAlbums = StoryAlbumUtils.queryStoryAlbums(getContentResolver(), false);
        LOG.d("old story album size is " + this.mOldStoryAlbums.size());
        if (this.mOldStoryAlbums.size() == 0) {
            sendMessage(3, 0, null);
            return true;
        }
        List<StoryAlbum> unReadyAlbums = new ArrayList();
        for (StoryAlbum album : this.mOldStoryAlbums) {
            if (TextUtils.isEmpty(album.date)) {
                unReadyAlbums.add(album);
            } else {
                updateStoryAlbumInfo(album);
            }
        }
        LOG.d("unready album size is " + unReadyAlbums.size());
        if (unReadyAlbums.size() == 0) {
            sendMessage(3, 0, null);
            return true;
        }
        this.mNewStoryAlbums.clear();
        this.mNewStoryAlbums.addAll(unReadyAlbums);
        this.mStoryAlbum = (StoryAlbum) this.mNewStoryAlbums.get(0);
        this.mNewStoryAlbums.remove(this.mStoryAlbum);
        this.mPendingMessageId = 3;
        sendMessage(5, 0, null);
        return false;
    }

    private void recoverStoryAlbumError() {
        for (String clusterCode : StoryAlbumUtils.queryClusterCodeInGalleryMedia(getContentResolver())) {
            StoryAlbum album = StoryAlbumUtils.queryStoryAlbumInfo(clusterCode, getContentResolver());
            if (album == null) {
                StoryAlbumUtils.clearStoryAlbumFiles(clusterCode, getContentResolver());
            } else {
                checkStoryAbumDuplicatedItemError(album);
            }
        }
        for (StoryAlbum album2 : StoryAlbumUtils.queryStoryAlbums(getContentResolver(), false)) {
            if (StoryAlbumUtils.queryStoryAlbumFileCount(album2.storyId, getContentResolver()) == 0) {
                StoryAlbum.removeStoryAlbum(album2.storyId, getContentResolver());
            }
        }
    }

    private void checkStoryAbumDuplicatedItemError(StoryAlbum album) {
        int count = StoryAlbumUtils.queryStoryAlbumItemCount(album.storyId, getContentResolver());
        if (count > 1) {
            LOG.d("album " + album.storyId + " has " + count + " duplicated item clear ...");
            StoryAlbum.removeStoryAlbum(album.storyId, getContentResolver());
            album.insertStoryAlbum(getContentResolver());
        }
    }

    private void removeSimilarPhotos(Intent intent) {
        if (intent != null) {
            ArrayList<Integer> idList = intent.getIntegerArrayListExtra("similarIds");
            if (idList != null && idList.size() > 0) {
                StoryAlbumFile.setStoryAlbumFileDuplicated(idList, getContentResolver());
            }
        }
        StoryAlbumUtils.removeSimilarTimeFiles(this.mStoryAlbum.storyId, getContentResolver());
        sendMessage(7, 2000, null);
    }

    private void updateStoryAlbumInfo(StoryAlbum storyAlbum) {
        if (TextUtils.isEmpty(storyAlbum.name)) {
            String address = genStoryAlbumAddress(storyAlbum);
            if (!TextUtils.isEmpty(address)) {
                storyAlbum.name = StoryAlbumUtils.genStoryAlbumName(storyAlbum.minDateTaken, storyAlbum.maxDateTaken, address, this);
            }
        }
        if (!StoryAlbumUtils.isStoryAlbumFilesAvailable(storyAlbum.coverId, getContentResolver())) {
            storyAlbum.coverId = StoryAlbumUtils.updateStoryAlbumCoverId(storyAlbum.storyId, getContentResolver());
        }
        storyAlbum.updateStoryAlbum(getContentResolver());
        LOG.d("update album: " + storyAlbum.storyId + " name=" + " " + storyAlbum.name + "  min=" + storyAlbum.minDateTaken + " max=" + storyAlbum.maxDateTaken + " coverid=" + storyAlbum.coverId);
    }

    private void updateStoryAlbumsDateTakenRange(StoryAlbum album) {
        DateTaken dateTakn = StoryAlbumUtils.queryStoryAlbumDateTaken(album.storyId, getContentResolver());
        if (dateTakn != null && dateTakn.min <= dateTakn.max) {
            long j;
            album.minDateTaken = dateTakn.min != 0 ? dateTakn.min : album.minDateTaken;
            if (dateTakn.max != 0) {
                j = dateTakn.max;
            } else {
                j = album.maxDateTaken;
            }
            album.maxDateTaken = j;
        }
    }

    private String genStoryAlbumAddress(StoryAlbum album) {
        Map<LatlngData, Uri> latlngMap = StoryAlbumUtils.queryStoryAlbumLocationData(album.storyId, getContentResolver());
        LOG.d("album code=" + album.storyId + "  latlngMap size=" + latlngMap.size());
        if (latlngMap.size() == 0) {
            return "";
        }
        Map<AddressInfo, List<Uri>> addressInfo = LocationUtils.getAddressInfoByLatLngData(latlngMap, getContentResolver());
        if (addressInfo == null) {
            LOG.d("need restart geo");
            this.mRestartGeoService = true;
            return "";
        }
        Map<AddressInfo, Integer> addressCount = new HashMap();
        int totalAddress = 0;
        for (Entry<AddressInfo, List<Uri>> entry : addressInfo.entrySet()) {
            List<Uri> uris = (List) entry.getValue();
            addressCount.put((AddressInfo) entry.getKey(), Integer.valueOf(uris.size()));
            totalAddress += uris.size();
        }
        if (((double) totalAddress) < ((double) latlngMap.size()) * 0.5d) {
            return "";
        }
        LOG.d("show address for album " + album.storyId);
        AddressInfo polularAddress = LocationUtils.getPopularAddress(addressCount);
        String albumAddress = "";
        if (polularAddress == null) {
            LOG.d("no 90% same addrress");
            for (AddressInfo address : addressCount.keySet()) {
                if (!TextUtils.isEmpty(address.admin_area)) {
                    albumAddress = albumAddress.concat(address.admin_area + " ");
                }
            }
        } else if (!TextUtils.isEmpty(polularAddress.sub_locality)) {
            albumAddress = polularAddress.sub_locality;
        } else if (!TextUtils.isEmpty(polularAddress.locality)) {
            albumAddress = polularAddress.locality;
        } else if (!TextUtils.isEmpty(polularAddress.admin_area)) {
            albumAddress = polularAddress.admin_area;
        }
        return albumAddress;
    }

    private List<DateTaken> clusteringImagesByDateTaken(List<StoryAlbumFile> todoFiles) {
        LOG.d("total " + todoFiles.size() + " should process.");
        List<SamplingFileInfo> imageInfoList = new ArrayList();
        for (StoryAlbumFile file : todoFiles) {
            if ("image/jpeg".equalsIgnoreCase(file.mimeType)) {
                imageInfoList.add(new SamplingFileInfo(file.dateTaken, new LatlngData(file.latitude, file.longitude)));
            }
        }
        LOG.d("total " + imageInfoList.size() + " jpeg to do cluster ...");
        return updateClusterByRules(clusteringPoints(new DBSCANClustering(43200000, 10), imageInfoList));
    }

    private List<List<Long>> clusteringPoints(DBSCANClustering clusterMethod, List<SamplingFileInfo> points) {
        List<List<Long>> clusters = new ArrayList();
        int beginPos = 0;
        int endPos = 1;
        while (endPos <= points.size() - 1) {
            while (Math.abs(((SamplingFileInfo) points.get(endPos)).dateTaken - ((SamplingFileInfo) points.get(endPos - 1)).dateTaken) < 57600000) {
                endPos++;
                if (endPos > points.size() - 1) {
                    break;
                }
            }
            List<SamplingFileInfo> partPoints = points.subList(beginPos, endPos);
            beginPos = endPos;
            endPos++;
            if (partPoints.size() >= 10) {
                List<SamplingFileInfo> samplingPoints = LocationUtils.samplingSimilarLatLngPoints(partPoints);
                if (samplingPoints.size() >= 10) {
                    Long[] dateTaken = new Long[samplingPoints.size()];
                    for (int i = 0; i < samplingPoints.size(); i++) {
                        dateTaken[i] = Long.valueOf(((SamplingFileInfo) samplingPoints.get(i)).dateTaken);
                    }
                    clusters.addAll(clusterMethod.getCluster(dateTaken));
                }
            }
        }
        return clusters;
    }

    private List<DateTaken> updateClusterByRules(List<List<Long>> clusterList) {
        List<DateTaken> timeRanges = new ArrayList();
        for (List<Long> dateTakenList : clusterList) {
            Collections.sort(dateTakenList);
            timeRanges.add(new DateTaken(((Long) dateTakenList.get(0)).longValue(), ((Long) dateTakenList.get(dateTakenList.size() - 1)).longValue()));
        }
        Collections.sort(timeRanges, sComparator);
        int i = 0;
        while (i < timeRanges.size() - 1) {
            long mergeRadius = 57600000;
            if (StoryAlbumDateUtils.inHolidayRange(((DateTaken) timeRanges.get(i)).max) || StoryAlbumDateUtils.inHolidayRange(((DateTaken) timeRanges.get(i + 1)).min)) {
                mergeRadius = 86400000;
            }
            if (((DateTaken) timeRanges.get(i + 1)).min - ((DateTaken) timeRanges.get(i)).max < mergeRadius) {
                LOG.d("[" + ((DateTaken) timeRanges.get(i)).min + ", " + ((DateTaken) timeRanges.get(i)).max + "] & [" + ((DateTaken) timeRanges.get(i + 1)).min + ", " + ((DateTaken) timeRanges.get(i + 1)).max + "] merged");
                ((DateTaken) timeRanges.get(i)).min = Math.min(((DateTaken) timeRanges.get(i)).min, ((DateTaken) timeRanges.get(i + 1)).min);
                ((DateTaken) timeRanges.get(i)).max = Math.max(((DateTaken) timeRanges.get(i)).max, ((DateTaken) timeRanges.get(i + 1)).max);
                timeRanges.remove(i + 1);
                i--;
            }
            i++;
        }
        long now = System.currentTimeMillis();
        LOG.d(" time now is " + now);
        Iterator<DateTaken> iterator = timeRanges.iterator();
        while (iterator.hasNext()) {
            DateTaken dateTaken = (DateTaken) iterator.next();
            if (now - dateTaken.max < 57600000) {
                LOG.d("[" + dateTaken.min + ", " + dateTaken.max + "] will be remove, not 16 hr away");
                iterator.remove();
            }
        }
        return timeRanges;
    }

    private ArrayList<StoryAlbum> genNewStoryAlbums(List<DateTaken> clusterDateTakens, List<StoryAlbum> clusterAlbums) {
        ArrayList<String> newClusterCode = genNewStoryAlbumClusterCodes(clusterDateTakens.size(), clusterAlbums);
        int index = 0;
        ArrayList<StoryAlbum> newAlbums = new ArrayList();
        for (DateTaken dateTaken : clusterDateTakens) {
            StoryAlbum album = new StoryAlbum((String) newClusterCode.get(index), dateTaken.min, dateTaken.max);
            newAlbums.add(album);
            index++;
            LOG.d("new album " + album.storyId + " [" + album.minDateTaken + ", " + album.maxDateTaken + "]");
        }
        return newAlbums;
    }

    private ArrayList<String> genNewStoryAlbumClusterCodes(int clusterSize, List<StoryAlbum> oldAlbums) {
        ArrayList<String> clusterCodes = new ArrayList();
        for (StoryAlbum album : oldAlbums) {
            clusterCodes.add(album.storyId);
        }
        ArrayList<String> newClusterCode = new ArrayList();
        int i = -1;
        while (newClusterCode.size() < clusterSize) {
            i++;
            if (!clusterCodes.contains(String.valueOf(i))) {
                newClusterCode.add(String.valueOf(i));
            }
        }
        return newClusterCode;
    }

    private void assignFilesToCluster(List<StoryAlbumFile> clusterFileList, StoryAlbum album) {
        if (clusterFileList != null && clusterFileList.size() != 0) {
            Iterator<StoryAlbumFile> iterator = clusterFileList.iterator();
            while (iterator.hasNext()) {
                StoryAlbumFile file = (StoryAlbumFile) iterator.next();
                long extendTime = 0;
                if (file.mimeType.equalsIgnoreCase("video/mp4")) {
                    extendTime = 43200000;
                }
                long min = Math.max(0, album.minDateTaken - extendTime);
                long max = Math.min(Long.MAX_VALUE, album.maxDateTaken + extendTime);
                if (file.dateTaken >= min && file.dateTaken <= max) {
                    setStoryAlbumFileInfo(file, album.storyId, "done");
                    iterator.remove();
                } else if (file.dateTaken > max) {
                    iterator.remove();
                } else if (file.dateTaken < min) {
                    return;
                }
            }
        }
    }

    private boolean removeInvalidStoryAlbum(StoryAlbum album) {
        int clusterFileCount = StoryAlbumUtils.queryStoryAlbumFileCount(album.storyId, getContentResolver());
        if (clusterFileCount >= 10) {
            return false;
        }
        LOG.d("cluster code " + album.storyId + " has " + clusterFileCount + " files, remove ... ");
        StoryAlbumUtils.clearStoryAlbumFiles(album.storyId, getContentResolver());
        return true;
    }

    private void setStoryAlbumFileInfo(StoryAlbumFile file, String storyId, String clusterState) {
        if (TextUtils.isEmpty(storyId)) {
            storyId = file.storyId;
        }
        file.storyId = storyId;
        if (TextUtils.isEmpty(clusterState)) {
            clusterState = file.storyClusterState;
        }
        file.storyClusterState = clusterState;
        file.updateStoryAlbumFile(getContentResolver());
    }

    private void updateStoryAlbumCover() {
        for (StoryAlbum album : StoryAlbumUtils.queryStoryAlbums(getContentResolver(), true)) {
            checkStoryAbumDuplicatedItemError(album);
            if (!StoryAlbumUtils.isStoryAlbumFilesAvailable(album.coverId, getContentResolver())) {
                LOG.d("cover not available, reset for album " + album.storyId);
                StoryAlbumUtils.updateStoryAlbumCoverId(album.storyId, getContentResolver());
            }
        }
    }

    private boolean getRunningPermission() {
        if ((this.mJobCode & 1) == 0 || this.mForceStop || !PhoneState.isChargeIn(this) || !PhoneState.isBatteryLevelOK(this)) {
            return false;
        }
        return PhoneState.isScreenOff(this);
    }

    private boolean isPermissionIgnoreMessage(int msgId) {
        return msgId == 11;
    }

    public static void startStoryService(Context context, int jobCode) {
        if (GalleryUtils.IS_STORY_ENABLE && context != null) {
            Intent intent = new Intent();
            intent.setClass(context, StoryAlbumService.class);
            switch (jobCode) {
                case 1:
                    intent.setAction("start_cluster");
                    break;
                case 2:
                    intent.setAction("update_cover");
                    break;
            }
            context.startService(intent);
        }
    }

    public static void stopStoryService(Context context) {
        if (context != null) {
            Intent intent = new Intent();
            intent.setClass(context, StoryAlbumService.class);
            context.stopService(intent);
        }
    }

    protected String getServiceTag() {
        return "story album service thread";
    }

    protected void decorateMsg(Message message, Intent intent, int startId) {
        message.what = startId;
        message.obj = intent;
    }
}
