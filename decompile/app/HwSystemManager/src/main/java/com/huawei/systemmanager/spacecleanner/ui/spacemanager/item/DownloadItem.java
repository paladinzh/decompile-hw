package com.huawei.systemmanager.spacecleanner.ui.spacemanager.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwAppCustomDataTrash;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwCustomDataItemTrash;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwAppCustomMgr;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustAppGroup;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustom.HwCustomTypeDataGroup;
import com.huawei.systemmanager.spacecleanner.engine.trash.ApkFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.Convertor;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ExpandeItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.deepscan.DownloadAppGroupItem;
import com.huawei.systemmanager.spacecleanner.ui.deepscan.DownloadAppTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.deepscan.DownloadTypeGroupItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListDownloadActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;
import com.huawei.systemmanager.util.HwLog;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class DownloadItem extends TrashDeepItem {
    public static final Comparator<TrashItemGroup> FILE_APP_NAME_COMPARATOR = new Comparator<TrashItemGroup>() {
        private Collator collator = Collator.getInstance();

        public int compare(TrashItemGroup firstOne, TrashItemGroup SecondOne) {
            return this.collator.getCollationKey(firstOne.getName()).compareTo(this.collator.getCollationKey(SecondOne.getName()));
        }
    };
    public static final Comparator<ITrashItem> FILE_SIZE_COMPARATOR = new Comparator<ITrashItem>() {
        public int compare(ITrashItem firstOne, ITrashItem SecondOne) {
            long firstSize = firstOne.getTrashSize();
            long secondSize = SecondOne.getTrashSize();
            if (firstSize > secondSize) {
                return -1;
            }
            if (firstSize < secondSize) {
                return 1;
            }
            return 0;
        }
    };
    public static final Comparator<TrashItemGroup> FILE_TYPE_COMPARATOR = new Comparator<TrashItemGroup>() {
        public int compare(TrashItemGroup firstOne, TrashItemGroup SecondOne) {
            int firstOneTypeWeight = ((Integer) DownloadItem.mTypeWeight.get(Integer.valueOf(getFileType(firstOne)))).intValue();
            int secondOneTypeWeight = ((Integer) DownloadItem.mTypeWeight.get(Integer.valueOf(getFileType(SecondOne)))).intValue();
            if (firstOneTypeWeight < secondOneTypeWeight) {
                return -1;
            }
            if (firstOneTypeWeight > secondOneTypeWeight) {
                return 1;
            }
            return 0;
        }

        private int getFileType(TrashItemGroup group) {
            List<ITrashItem> list = group.getTrashs();
            if (list != null && list.size() > 0) {
                ITrashItem item = (ITrashItem) list.get(0);
                if (item instanceof DownloadAppTrashItem) {
                    return ((HwCustomDataItemTrash) ((DownloadAppTrashItem) item).getTrash()).getFileBigType();
                }
            }
            return -1;
        }
    };
    public static final int ORDER_STATE_FILES_FROM = 0;
    public static final int ORDER_STATE_FILES_TYPE = 1;
    private static final int SHOW_TIP_SIZE_PERCENT = 20;
    private static final String TAG = "DownloadItem";
    public static final int TRASH_TYPE = 81920;
    private static final Map<Integer, Integer> mTypeWeight = new HashMap<Integer, Integer>() {
        {
            put(Integer.valueOf(3), Integer.valueOf(0));
            put(Integer.valueOf(1), Integer.valueOf(1));
            put(Integer.valueOf(2), Integer.valueOf(2));
            put(Integer.valueOf(5), Integer.valueOf(3));
            put(Integer.valueOf(6), Integer.valueOf(4));
            put(Integer.valueOf(4), Integer.valueOf(5));
            put(Integer.valueOf(7), Integer.valueOf(6));
        }
    };

    private static class DownLoadTrashFileFromCovertor extends Convertor {
        private DownLoadTrashFileFromCovertor() {
        }

        public List<TrashItemGroup> convert(TrashScanHandler scanHandler) {
            if (scanHandler == null) {
                HwLog.e(DownloadItem.TAG, "convert handler is null");
                return Lists.newArrayList();
            }
            List<HwAppCustomDataTrash> resultList = DownloadItem.getDownloadTrashs(scanHandler);
            List<TrashItemGroup> result = Lists.newArrayList();
            TrashGroup apkGroup = scanHandler.getTrashByType(1024);
            List<Trash> apkList = Lists.newArrayList();
            if (apkGroup != null) {
                apkList = apkGroup.getTrashList();
            } else {
                HwLog.i(DownloadItem.TAG, "DownLoadTrashFileFromCovertor.get apk type is null.");
            }
            for (HwAppCustomDataTrash it : resultList) {
                Trash group = it.changeToAppGroup();
                ITrashItem item = DownloadAppGroupItem.getGroupTransFunc(81920).apply(group);
                if (item == null) {
                    HwLog.e(DownloadItem.TAG, "DownLoadTrashCovertor covert, item is null!");
                } else {
                    String title = item.getName();
                    List<Trash> list = group.getTrashList();
                    List<ITrashItem> itemList = Lists.newArrayListWithCapacity(list.size());
                    for (Trash trash : list) {
                        DownloadAppTrashItem trashItem = (DownloadAppTrashItem) DownloadAppTrashItem.sTransFunc.apply(trash);
                        if (trashItem != null) {
                            DownloadItem.setApkRelatedTrash(apkList, trashItem);
                            itemList.add(trashItem);
                        }
                    }
                    Collections.sort(itemList, DownloadItem.FILE_SIZE_COMPARATOR);
                    result.add(ExpandeItemGroup.create(81920, title, itemList));
                }
            }
            Collections.sort(result, DownloadItem.FILE_APP_NAME_COMPARATOR);
            return result;
        }
    }

    private static class DownLoadTrashFileTypeCovertor extends Convertor {
        private DownLoadTrashFileTypeCovertor() {
        }

        public List<TrashItemGroup> convert(TrashScanHandler scanHandler) {
            if (scanHandler == null) {
                HwLog.e(DownloadItem.TAG, "convert handler is null");
                return Lists.newArrayList();
            }
            List<HwAppCustomDataTrash> resultList = DownloadItem.getDownloadTrashs(scanHandler);
            List<TrashItemGroup> result = Lists.newArrayList();
            PathEntry pathEntry = null;
            HashMap<Integer, HwCustomTypeDataGroup> typeGroups = new HashMap();
            for (HwAppCustomDataTrash it : resultList) {
                if (pathEntry == null) {
                    pathEntry = it.getPathEntry();
                }
                for (String filePath : it.getFiles()) {
                    if (HwAppCustomMgr.isRuleMatched(it.getRule(), FileUtil.getFileName(filePath))) {
                        HwCustomDataItemTrash hwCustomDataItemTrash = new HwCustomDataItemTrash(filePath, pathEntry, it.getPackageName(), it.getType());
                        HwCustomTypeDataGroup typeGroup = (HwCustomTypeDataGroup) typeGroups.get(Integer.valueOf(hwCustomDataItemTrash.getFileBigType()));
                        if (typeGroup == null) {
                            HwCustomTypeDataGroup hwCustomTypeDataGroup = new HwCustomTypeDataGroup(it.getType(), false, hwCustomDataItemTrash.getFileBigType());
                            typeGroups.put(Integer.valueOf(hwCustomDataItemTrash.getFileBigType()), hwCustomTypeDataGroup);
                        }
                        typeGroup.addChild(hwCustomDataItemTrash);
                    }
                }
            }
            TrashGroup apkGroup = scanHandler.getTrashByType(1024);
            List<Trash> apkList = Lists.newArrayList();
            if (apkGroup != null) {
                apkList = apkGroup.getTrashList();
            } else {
                HwLog.i(DownloadItem.TAG, "DownLoadTrashFileTypeCovertor.get apk type is null.");
            }
            for (Entry<Integer, HwCustomTypeDataGroup> group : typeGroups.entrySet()) {
                Trash groupValue = (HwCustomTypeDataGroup) group.getValue();
                List<ITrashItem> itemList = Lists.newArrayListWithCapacity(groupValue.getTrashList().size());
                List<Trash> list = groupValue.getTrashList();
                ITrashItem groupItem = DownloadTypeGroupItem.getGroupTransFunc(81920).apply(groupValue);
                if (groupItem == null) {
                    HwLog.e(DownloadItem.TAG, "DownLoadTrashFileTypeCovertor covert, item is null!");
                } else {
                    String title = groupItem.getName();
                    for (Trash trash : list) {
                        DownloadAppTrashItem trashItem = (DownloadAppTrashItem) DownloadAppTrashItem.sTransFunc.apply(trash);
                        if (trashItem != null) {
                            DownloadItem.setApkRelatedTrash(apkList, trashItem);
                            itemList.add(trashItem);
                        }
                    }
                    Collections.sort(itemList, DownloadItem.FILE_SIZE_COMPARATOR);
                    result.add(ExpandeItemGroup.create(81920, title, itemList));
                }
            }
            Collections.sort(result, DownloadItem.FILE_TYPE_COMPARATOR);
            return result;
        }
    }

    public String getTitle(Context ctx) {
        return ctx.getString(R.string.space_clean_download_trash);
    }

    public Drawable getIcon(Context ctx) {
        return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_download);
    }

    public Intent getIntent(Context ctx) {
        OpenSecondaryParam params = new OpenSecondaryParam();
        params.setScanType(100);
        params.setTrashType(getTrashType());
        params.setOperationResId(R.string.common_delete);
        params.setEmptyTextID(R.string.no_file_trash_tip);
        params.setEmptyIconID(R.drawable.ic_no_folder);
        params.setDialogTitleId(R.plurals.space_clean_any_file_delete_title);
        params.setAllDialogTitleId(R.string.space_clean_all_file_delete_title);
        params.setDialogContentId(R.plurals.space_clean_file_delete_message);
        params.setDialogPositiveButtonId(R.string.common_delete);
        params.setTitleStr(getTitle(ctx));
        params.setDeepItemType(getDeepItemType());
        return new Intent(ctx, ListDownloadActivity.class).putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, params);
    }

    public String getTag() {
        return TAG;
    }

    public boolean showTip() {
        long maxSize = (StorageHelper.getStorage().getTotalSize(0) * 20) / 100;
        HwLog.i(TAG, "DeepItemType:" + getDeepItemType() + " maxSize:" + maxSize);
        long trashSize = getTrashSize();
        if (maxSize <= 0 || trashSize <= 0 || trashSize <= maxSize) {
            return false;
        }
        return true;
    }

    public boolean showInfrequentlyTip() {
        return false;
    }

    public int getTrashType() {
        return 81920;
    }

    public int getDeepItemType() {
        return 11;
    }

    public boolean onCheckFinished(TrashScanHandler handler) {
        return checkDownloadTrashFinished(handler);
    }

    private boolean checkDownloadTrashFinished(TrashScanHandler handler) {
        this.mTrashList.clear();
        int finishType = handler.getFinishedType();
        int trashType = getTrashType();
        HwLog.i(getTag(), "checkDownloadTrashFinished, item:" + getTag() + ", trashType:" + Integer.toBinaryString(trashType) + ",finishType:" + Integer.toBinaryString(finishType));
        if (!checkIfScanEnd(handler, finishType, trashType)) {
            return isFinished();
        }
        this.mTrashList.addAll(getDownloadTrashs(handler));
        setFinish();
        return isFinished();
    }

    public static List<HwAppCustomDataTrash> getDownloadTrashs(TrashScanHandler handler) {
        List<HwAppCustomDataTrash> resultList = Lists.newArrayList();
        TrashGroup TotalGroup = handler.getTrashByType(81920);
        if (TotalGroup == null) {
            HwLog.i(TAG, "getDownloadTrashs,but trash is null!");
            return resultList;
        }
        for (Trash it : TotalGroup.getTrashList()) {
            if (it instanceof HwCustAppGroup) {
                for (Trash item : ((HwCustAppGroup) it).getTrashListUnclened()) {
                    if (item instanceof HwAppCustomDataTrash) {
                        HwAppCustomDataTrash dataTrash = (HwAppCustomDataTrash) item;
                        if (dataTrash.getCustType() == 11) {
                            resultList.add(dataTrash);
                        }
                    } else {
                        HwLog.e(TAG, "HwCustAppCust's item is not HwAppCustomDataTrash");
                    }
                }
            }
        }
        return resultList;
    }

    public static List<TrashItemGroup> covert(TrashScanHandler handler, int showType) {
        Convertor convector = null;
        switch (showType) {
            case 0:
                convector = new DownLoadTrashFileFromCovertor();
                break;
            case 1:
                convector = new DownLoadTrashFileTypeCovertor();
                break;
        }
        if (convector != null) {
            return convector.convert(handler);
        }
        return Lists.newArrayList();
    }

    private static void setApkRelatedTrash(List<Trash> apkList, DownloadAppTrashItem trashItem) {
        if (trashItem.getFileType() == 4) {
            String trashPath = trashItem.getTrashPath();
            if (TextUtils.isEmpty(trashItem.getTrashPath())) {
                HwLog.e(TAG, "setApkRelatedTrash ,trashPath is empty");
                return;
            }
            for (Trash apkTrash : apkList) {
                if (apkTrash instanceof ApkFileTrash) {
                    ApkFileTrash apkfile = (ApkFileTrash) apkTrash;
                    if (trashPath.toUpperCase(Locale.ENGLISH).equals(apkfile.getPath().toUpperCase(Locale.ENGLISH))) {
                        trashItem.setRelateTrash(apkfile);
                    }
                }
            }
        }
    }
}
