package com.spe3d;

import android.os.RemoteException;
import android.util.Log;
import com.huawei.servicehost.IImageProcessSession;
import com.huawei.servicehost.ImageProcessManager;
import com.huawei.servicehost.ImageWrap;
import com.huawei.servicehost.d3d.IImage3D;
import com.huawei.servicehost.d3d.IImage3D.Stub;
import com.huawei.servicehost.d3d.IMtlFile;
import com.huawei.servicehost.d3d.ISegment3D;
import com.huawei.servicehost.d3d.ITextureJpeg;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class ServiceHostWrapper {
    private int[] attribute = new int[]{3, 1, 2, 5, 4};
    private Map<String, ImageWrap> contentMap;
    private ExecutorService executorService;
    private IImage3D image3D;
    private Map<String, String> objMtlMap;
    private int[] qyAttribute = new int[]{6, 3, 5, 8, 7, 4};

    public void setContentMap() {
        this.contentMap = this.contentMap;
    }

    public Map<String, ImageWrap> getContentMap() {
        return this.contentMap;
    }

    public Map<String, String> getObjmtlname() {
        return this.objMtlMap;
    }

    public ServiceHostWrapper(String image3DPath) throws RemoteException {
        IImageProcessSession imageProcessSession = ImageProcessManager.get().createIPSession("d3d");
        if (imageProcessSession != null) {
            this.contentMap = new HashMap();
            this.objMtlMap = new HashMap();
            IImage3D image3D = Stub.asInterface(imageProcessSession.createIPObject("image3d"));
            image3D.read(image3DPath);
            if (image3D.getSegment3D(7) == null) {
                for (int segment3D : this.attribute) {
                    GetImageWrap(image3D.getSegment3D(segment3D));
                }
            } else {
                for (int segment3D2 : this.qyAttribute) {
                    GetImageWrap(image3D.getSegment3D(segment3D2));
                }
            }
        }
    }

    public ByteBuffer getContent(String name) {
        ImageWrap imageWrap = (ImageWrap) this.contentMap.get(name);
        if (imageWrap == null) {
            return null;
        }
        return imageWrap.getData();
    }

    private void GetImageWrap(ISegment3D segment3D) {
        if (segment3D != null) {
            try {
                ImageWrap objFileContent = segment3D.getObjFile().getContent();
                String objFileName = segment3D.getName();
                if (!this.contentMap.containsKey(objFileName)) {
                    this.contentMap.put(objFileName, objFileContent);
                }
                IMtlFile mtlFile = segment3D.getMtlFile();
                ImageWrap mtlFileContent = mtlFile.getContent();
                String mtlFileName = mtlFile.getName();
                if (!this.contentMap.containsKey(mtlFileName)) {
                    this.contentMap.put(mtlFileName, mtlFileContent);
                }
                this.objMtlMap.put(objFileName, mtlFileName);
                int count = segment3D.getTextureJpegCount();
                if (count >= 1) {
                    for (int j = 0; j < count; j++) {
                        ITextureJpeg textureJpeg = segment3D.getTextureJpeg(j);
                        String textureJpegName = textureJpeg.getName();
                        ImageWrap textureJpegContent = textureJpeg.getContent();
                        if (!this.contentMap.containsKey(textureJpegName)) {
                            this.contentMap.put(textureJpegName, textureJpegContent);
                        }
                    }
                }
            } catch (RemoteException e) {
                Log.e(Spe3DViewer.SPE3D_TAG, e.toString());
            }
        }
    }
}
