package com.autonavi.amap.mapcore;

import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;

public abstract class BaseMapCallImplement implements IBaseMapCallback, IMapCallback {
    private ArrayList<MapSourceGridData> bldReqMapGrids = new ArrayList();
    ConnectionManager connectionManager = null;
    private ArrayList<MapSourceGridData> curBldMapGrids = new ArrayList();
    private ArrayList<MapSourceGridData> curIndoorMapGirds = new ArrayList();
    private ArrayList<MapSourceGridData> curPoiMapGrids = new ArrayList();
    private ArrayList<MapSourceGridData> curRegionMapGrids = new ArrayList();
    private ArrayList<MapSourceGridData> curRoadMapGrids = new ArrayList();
    private ArrayList<MapSourceGridData> curScreenGirds = new ArrayList();
    private ArrayList<MapSourceGridData> curStiMapGirds = new ArrayList();
    private ArrayList<MapSourceGridData> curVectmcMapGirds = new ArrayList();
    private ArrayList<MapSourceGridData> indoorMapGrids = new ArrayList();
    Object mapGridFillLock = new Object();
    private ArrayList<MapSourceGridData> poiReqMapGrids = new ArrayList();
    private ArrayList<MapSourceGridData> regionReqMapGrids = new ArrayList();
    private ArrayList<MapSourceGridData> roadReqMapGrids = new ArrayList();
    private ArrayList<MapSourceGridData> stiReqMapGirds = new ArrayList();
    TextTextureGenerator textTextureGenerator = null;
    TilesProcessingCtrl tileProcessCtrl = null;
    private ArrayList<MapSourceGridData> vectmcReqMapGirds = new ArrayList();
    private ArrayList<MapSourceGridData> versionMapGrids = new ArrayList();

    public void OnMapDataRequired(MapCore mapCore, int i, String[] strArr) {
        if (strArr != null && strArr.length != 0) {
            ArrayList reqGridList = getReqGridList(i);
            if (reqGridList != null) {
                reqGridList.clear();
                for (String mapSourceGridData : strArr) {
                    reqGridList.add(new MapSourceGridData(mapSourceGridData, i));
                }
                if (i != 5) {
                    proccessRequiredData(mapCore, reqGridList, i);
                }
            }
        }
    }

    public void OnMapProcessEvent(MapCore mapCore) {
    }

    public ArrayList<MapSourceGridData> getReqGridList(int i) {
        if (i == 0) {
            return this.roadReqMapGrids;
        }
        if (i == 1) {
            return this.bldReqMapGrids;
        }
        if (i == 7) {
            return this.regionReqMapGrids;
        }
        if (i == 8) {
            return this.poiReqMapGrids;
        }
        if (i == 4) {
            return this.vectmcReqMapGirds;
        }
        if (i == 5) {
            return this.curScreenGirds;
        }
        if (i == 3) {
            return this.stiReqMapGirds;
        }
        if (i == 9) {
            return this.versionMapGrids;
        }
        if (i != 10) {
            return null;
        }
        return this.indoorMapGrids;
    }

    public void OnMapSurfaceRenderer(GL10 gl10, MapCore mapCore, int i) {
        if (i == 11) {
            synchronized (this.mapGridFillLock) {
                try {
                    mapCore.fillCurGridListWithDataType(this.curPoiMapGrids, 8);
                    mapCore.fillCurGridListWithDataType(this.curRoadMapGrids, 0);
                    mapCore.fillCurGridListWithDataType(this.curRegionMapGrids, 7);
                    mapCore.fillCurGridListWithDataType(this.curBldMapGrids, 1);
                    mapCore.fillCurGridListWithDataType(this.curVectmcMapGirds, 4);
                    mapCore.fillCurGridListWithDataType(this.curStiMapGirds, 3);
                    mapCore.fillCurGridListWithDataType(this.curIndoorMapGirds, 10);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        }
    }

    public ArrayList<MapSourceGridData> getCurGridList(int i) {
        if (i == 0) {
            return this.curRoadMapGrids;
        }
        if (i == 1) {
            return this.curBldMapGrids;
        }
        if (i == 7) {
            return this.curRegionMapGrids;
        }
        if (i == 8) {
            return this.curPoiMapGrids;
        }
        if (i == 4) {
            return this.curVectmcMapGirds;
        }
        if (i == 5) {
            return this.curScreenGirds;
        }
        if (i == 10) {
            return this.curIndoorMapGirds;
        }
        if (i != 3) {
            return null;
        }
        return this.curStiMapGirds;
    }

    public boolean isGridsInScreen(ArrayList<MapSourceGridData> arrayList, int i) {
        try {
            if (arrayList.size() == 0 || !isMapEngineValid()) {
                return false;
            }
            synchronized (this.mapGridFillLock) {
                ArrayList curGridList = getCurGridList(i);
                if (curGridList != null) {
                    for (int i2 = 0; i2 < arrayList.size(); i2++) {
                        if (isGridInList(((MapSourceGridData) arrayList.get(i2)).getGridName(), curGridList)) {
                            return true;
                        }
                    }
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean isGridInScreen(int i, String str) {
        try {
            if (!isMapEngineValid()) {
                return false;
            }
            synchronized (this.mapGridFillLock) {
                if (isGridInList(str, getCurGridList(i))) {
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isGridInList(String str, ArrayList<MapSourceGridData> arrayList) {
        for (int i = 0; i < arrayList.size(); i++) {
            if (((MapSourceGridData) arrayList.get(i)).getGridName().equals(str)) {
                return true;
            }
        }
        return false;
    }

    public boolean isIndoorGridsInScreen(ArrayList<MapSourceGridData> arrayList, int i) {
        try {
            if (arrayList.size() == 0 || !isMapEngineValid()) {
                return false;
            }
            synchronized (this.mapGridFillLock) {
                ArrayList curGridList = getCurGridList(i);
                if (curGridList != null) {
                    for (int i2 = 0; i2 < arrayList.size(); i2++) {
                        if (isIndoorGridInList(((MapSourceGridData) arrayList.get(i2)).getKeyGridName(), curGridList)) {
                            return true;
                        }
                    }
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean isIndoorGridInScreen(int i, String str, short s) {
        try {
            if (!isMapEngineValid()) {
                return false;
            }
            synchronized (this.mapGridFillLock) {
                if (isIndoorGridInList(i + "-" + str + "-" + s, getCurGridList(i))) {
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isIndoorGridInList(String str, ArrayList<MapSourceGridData> arrayList) {
        for (int i = 0; i < arrayList.size(); i++) {
            if (((MapSourceGridData) arrayList.get(i)).getKeyGridName().equals(str)) {
                return true;
            }
        }
        return false;
    }

    protected void proccessRequiredData(MapCore mapCore, ArrayList<MapSourceGridData> arrayList, int i) {
        int i2 = 0;
        ArrayList arrayList2 = new ArrayList();
        for (int i3 = 0; i3 < arrayList.size(); i3++) {
            MapSourceGridData mapSourceGridData = (MapSourceGridData) arrayList.get(i3);
            if (this.tileProcessCtrl != null) {
                if (this.tileProcessCtrl.isProcessing(mapSourceGridData.getKeyGridName())) {
                }
            }
            if (i != 4) {
                try {
                    String str = mapSourceGridData.gridName;
                    if (i == 10) {
                        str = mapSourceGridData.gridName + "-" + mapSourceGridData.mIndoorIndex;
                    }
                    VMapDataRecoder recoder = VMapDataCache.getInstance().getRecoder(str, i);
                    if (!(recoder == null || recoder.mGridName == null || recoder.mGridName.length() > 0)) {
                    }
                    arrayList2.add(mapSourceGridData);
                } catch (Exception e) {
                }
            } else {
                VTMCDataCache instance = VTMCDataCache.getInstance();
                VTmcData data = instance.getData(mapSourceGridData.getGridName(), true);
                VTmcData data2 = instance.getData(mapSourceGridData.getGridName(), false);
                if (data != null) {
                    mapSourceGridData.obj = data.eTag;
                }
                if (data2 == null || data2.data == null || data2.data.length <= 0) {
                    arrayList2.add(mapSourceGridData);
                } else {
                    mapCore.putMapData(data2.data, 0, data2.data.length, i, data2.createTime);
                }
            }
        }
        if (arrayList2.size() > 0) {
            BaseMapLoader normalMapLoader = i != 10 ? i != 11 ? new NormalMapLoader(mapCore, this, i) : null : new IndoorMapLoader(mapCore, this, i);
            while (i2 < arrayList2.size()) {
                mapSourceGridData = (MapSourceGridData) arrayList2.get(i2);
                this.tileProcessCtrl.addProcessingTile(mapSourceGridData.getKeyGridName());
                normalMapLoader.addReuqestTiles(mapSourceGridData);
                i2++;
            }
            if (this.connectionManager != null) {
                this.connectionManager.insertConntionTask(normalMapLoader);
            }
        }
    }

    public void OnMapSurfaceCreate(MapCore mapCore) {
    }

    public synchronized void onPause() {
        try {
            if (this.connectionManager != null) {
                this.connectionManager.threadFlag = false;
                if (this.connectionManager.isAlive()) {
                    this.connectionManager.interrupt();
                    this.connectionManager.shutDown();
                    this.connectionManager = null;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public synchronized void onResume(MapCore mapCore) {
        try {
            this.connectionManager = new ConnectionManager();
            this.tileProcessCtrl = new TilesProcessingCtrl();
            this.connectionManager.start();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void OnMapDestory(MapCore mapCore) {
        try {
            destoryMap(mapCore);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public byte[] OnMapCharsWidthsRequired(MapCore mapCore, int[] iArr, int i, int i2) {
        if (this.textTextureGenerator == null) {
            this.textTextureGenerator = new TextTextureGenerator();
        }
        return this.textTextureGenerator.getCharsWidths(iArr);
    }

    public void OnMapLabelsRequired(MapCore mapCore, int[] iArr, int i) {
        if (iArr != null && i > 0) {
            for (int i2 = 0; i2 < i; i2++) {
                int i3 = iArr[i2];
                this.textTextureGenerator = new TextTextureGenerator();
                byte[] textPixelBuffer = this.textTextureGenerator.getTextPixelBuffer(i3);
                if (textPixelBuffer != null) {
                    mapCore.putCharbitmap(i3, textPixelBuffer);
                }
            }
        }
    }

    public synchronized void newMap(MapCore mapCore) {
        this.connectionManager = new ConnectionManager();
        this.tileProcessCtrl = new TilesProcessingCtrl();
        this.connectionManager.start();
    }

    public synchronized void destoryMap(MapCore mapCore) {
        if (this.connectionManager != null) {
            this.connectionManager.threadFlag = false;
            if (this.connectionManager.isAlive()) {
                try {
                    this.connectionManager.interrupt();
                } catch (Throwable th) {
                } finally {
                    this.connectionManager.shutDown();
                    this.connectionManager = null;
                }
            }
        }
        if (this.tileProcessCtrl != null) {
            this.tileProcessCtrl.clearAll();
        }
    }
}
