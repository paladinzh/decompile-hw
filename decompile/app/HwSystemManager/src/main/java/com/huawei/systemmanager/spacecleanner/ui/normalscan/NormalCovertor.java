package com.huawei.systemmanager.spacecleanner.ui.normalscan;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.Convertor;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ExpandeItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import java.util.List;
import java.util.Map;

public class NormalCovertor extends Convertor {
    public List<TrashItemGroup> getScanEndList(Map<Integer, TrashGroup> trashMap) {
        List<TrashItemGroup> result = Lists.newArrayList();
        for (TrashTransFunc transFunc : getNormalTranFuncs()) {
            TrashItemGroup item = Convertor.transToExpandeGroup(trashMap, transFunc);
            if (!item.isEmpty()) {
                result.add(item);
            }
        }
        result.add(createSaveMoreGroup());
        return result;
    }

    public List<TrashItemGroup> getScanningList() {
        List<TrashTransFunc<? extends ITrashItem>> transFuncs = getNormalTranFuncs();
        List<TrashItemGroup> result = Lists.newArrayListWithCapacity(transFuncs.size());
        for (TrashTransFunc transFunc : transFuncs) {
            result.add(Convertor.transToExpandeGroup(null, transFunc));
        }
        return result;
    }

    private List<TrashTransFunc<? extends ITrashItem>> getNormalTranFuncs() {
        List<TrashTransFunc<? extends ITrashItem>> transFuncs = Lists.newArrayList();
        transFuncs.add(AppProcessTrashItem.sTransFunc);
        transFuncs.add(AppCacheTrashItem.sTransFunc);
        transFuncs.add(AppCustomTrashItem.sAppDataTransFunc);
        if (TMSEngineFeature.isSupportTMS()) {
            transFuncs.add(AppCustomTrashItem.sAppResidueTransFunc);
        }
        transFuncs.add(ApkFileTrashItem.sTransFunc);
        return transFuncs;
    }

    private TrashItemGroup createSaveMoreGroup() {
        List<TrashTransFunc> funcs = Lists.newArrayList();
        funcs.add(SaveMoreTrashItem.sSaveMoreTransFunc);
        int type = 0;
        List<ITrashItem> list = Lists.newArrayListWithCapacity(funcs.size());
        for (TrashTransFunc func : funcs) {
            list.add(func.apply(null));
            type |= func.getTrashType();
        }
        return ExpandeItemGroup.create(type, GlobalContext.getContext().getString(R.string.space_clean_save_more), list);
    }
}
