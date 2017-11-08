package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import android.util.SparseArray;
import android.util.SparseIntArray;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import java.util.LinkedList;
import java.util.List;

public class PhotoManagerBean {
    private static final String TAG = "PhotoManagerBean";
    private static SparseArray<Class> photoClassMap = new SparseArray();
    private static SparseIntArray photoIconMap = new SparseIntArray();
    private static SparseIntArray photoNameMap = new SparseIntArray();
    private static SparseArray<OpenSecondaryParam> photoParamsMap = new SparseArray();
    public final Class mClass;
    public final int mIconID;
    public final int mNameId;
    public final OpenSecondaryParam mParam;
    public final String mSizeDes;

    public PhotoManagerBean(int id, String des, Class c, OpenSecondaryParam p, int iconId) {
        this.mNameId = id;
        this.mSizeDes = des;
        this.mClass = c;
        this.mParam = p;
        this.mIconID = iconId;
    }

    static {
        photoNameMap.put(4194304, R.string.space_clean_trash_similar_photo);
        photoNameMap.put(8388608, R.string.space_clean_trash_blur_photo);
        photoNameMap.put(128, R.string.space_clean_trash_all_photo);
        photoClassMap.put(4194304, PhotoGridActivity.class);
        photoClassMap.put(8388608, PhotoGridActivity.class);
        photoClassMap.put(128, ListPhotoSetActivity.class);
        photoIconMap.put(4194304, R.drawable.ic_duplicate_photo_list);
        photoIconMap.put(8388608, R.drawable.ic_blur_photo_list);
        photoIconMap.put(128, R.drawable.ic_all_photo_list);
        createListPhotoParams();
        createSimilarPhotoParams();
        createBlurPhotoParams();
    }

    public static PhotoManagerBean creator(int type, long size) {
        int id = photoNameMap.get(type);
        Class c = (Class) photoClassMap.get(type);
        String des = size > 0 ? FileUtil.getFileSize(size) : "";
        OpenSecondaryParam param = (OpenSecondaryParam) photoParamsMap.get(type);
        int iconId = photoIconMap.get(type);
        if (c == null || size <= 0) {
            return null;
        }
        return new PhotoManagerBean(id, des, c, param, iconId);
    }

    public static List<Integer> getPhotoType() {
        List<Integer> list = new LinkedList();
        list.add(Integer.valueOf(4194304));
        list.add(Integer.valueOf(8388608));
        list.add(Integer.valueOf(128));
        return list;
    }

    private static void createListPhotoParams() {
        OpenSecondaryParam params = new OpenSecondaryParam();
        params.setScanType(100);
        params.setTrashType(128);
        params.setDialogTitleId(R.plurals.space_clean_any_file_delete_title);
        params.setAllDialogTitleId(R.string.space_clean_all_file_delete_title);
        params.setDialogContentId(R.plurals.space_clean_file_delete_message);
        params.setDialogPositiveButtonId(R.string.common_delete);
        params.setTitleStr(GlobalContext.getString(R.string.space_clean_trash_all_photo));
        params.setEmptyIconID(R.drawable.ic_no_camera);
        params.setEmptyTextID(R.string.no_photo_trash_tip);
        params.setOperationResId(R.string.file_trash_clean_menu_title);
        photoParamsMap.put(128, params);
    }

    private static void createSimilarPhotoParams() {
        OpenSecondaryParam params = new OpenSecondaryParam();
        params.setScanType(100);
        params.setTrashType(4194304);
        params.setDialogTitleId(R.plurals.space_clean_any_file_delete_title);
        params.setAllDialogTitleId(R.string.space_clean_all_file_delete_title);
        params.setDialogContentId(R.plurals.space_clean_file_delete_message);
        params.setDialogPositiveButtonId(R.string.common_delete);
        params.setEmptyIconID(R.drawable.ic_no_camera);
        params.setEmptyTextID(R.string.no_photo_trash_tip);
        params.setOperationResId(R.string.file_trash_clean_menu_title);
        params.setTitleStr(GlobalContext.getString(R.string.space_clean_trash_similar_photo));
        photoParamsMap.put(4194304, params);
    }

    private static void createBlurPhotoParams() {
        OpenSecondaryParam params = new OpenSecondaryParam();
        params.setScanType(100);
        params.setTrashType(8388608);
        params.setDialogTitleId(R.plurals.space_clean_any_file_delete_title);
        params.setAllDialogTitleId(R.string.space_clean_all_file_delete_title);
        params.setDialogContentId(R.plurals.space_clean_file_delete_message);
        params.setDialogPositiveButtonId(R.string.common_delete);
        params.setEmptyIconID(R.drawable.ic_no_camera);
        params.setEmptyTextID(R.string.no_photo_trash_tip);
        params.setOperationResId(R.string.file_trash_clean_menu_title);
        params.setTitleStr(GlobalContext.getString(R.string.space_clean_trash_blur_photo));
        photoParamsMap.put(8388608, params);
    }
}
