package com.spe3d;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class Spe3DViewer extends Activity implements OnTouchListener, OnKeyListener {
    public static final int MAX_LIGHT_NUMBER = 10;
    private static final String MODEL_PATH = "3DModel_File_Path";
    private static final String PORTRAIT3D_DRESS_UP_URL = "http://a.vmall.com/appdl/C10789169";
    private static final String PORTRAIT3D_PRINT_URL = "http://a.vmall.com/appdl/C10740613";
    public static String SPE3D_TAG = "SPE3DVIEWER";
    private static final int VIEWER_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1024;
    public static int lightNum = 1;
    public static Light[] lights;
    private static final long modifyTime = System.currentTimeMillis();
    private ImageView backButton;
    EGLview mView;
    private String model = "";
    private ImageView modifyButton;
    private ImageView printButton;
    private ImageView restoreButton;
    private ServiceHostWrapper serviceHostWrapper;
    private long time = System.currentTimeMillis();

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (lights == null) {
            lights = new Light[10];
            for (int i = 0; i < 10; i++) {
                lights[i] = new Light();
            }
            lights[0].lightDiffuse = 0.2f;
            lights[0].lightAmbient = 0.5f;
            lights[0].lightSpecular = 0.0f;
            float[] fArr = new float[]{0.0f, 5.0f, 10.0f};
            lights[0].lightPos = fArr;
            lights[1].lightDiffuse = 0.2f;
            lights[1].lightAmbient = 0.0f;
            lights[1].lightSpecular = 0.0f;
            fArr = new float[]{0.0f, -10.0f, 10.0f};
            lights[1].lightPos = fArr;
            lights[2].lightDiffuse = 0.2f;
            lights[2].lightAmbient = 0.0f;
            lights[2].lightSpecular = 0.0f;
            fArr = new float[]{10.0f, 8.0f, 10.0f};
            lights[2].lightPos = fArr;
            lights[3].lightDiffuse = 0.2f;
            lights[3].lightAmbient = 0.0f;
            lights[3].lightSpecular = 0.0f;
            fArr = new float[]{-10.0f, 8.0f, 10.0f};
            lights[3].lightPos = fArr;
            lightNum = 4;
        }
        setContentView(R.layout.ui_layout_gles);
        String title = getIntent().getStringExtra("title");
        if (!(title == null || title.isEmpty())) {
            setTitle(title);
        }
        this.mView = (EGLview) findViewById(R.id.surfaceGLES);
        this.mView.setOnTouchListener(this);
        this.mView.setOnKeyListener(this);
        this.restoreButton = (ImageView) findViewById(R.id.restoreButton);
        this.restoreButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Spe3DNativeLib.home();
            }
        });
        this.printButton = (ImageView) findViewById(R.id.printButton);
        this.printButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (Spe3DViewer.this.isAppInstalled(Spe3DViewer.this, "com.eazer.app.huawei")) {
                    Intent intent = new Intent("android.intent.action.3dpreview");
                    intent.setPackage("com.eazer.app.huawei");
                    intent.putExtra(Spe3DViewer.MODEL_PATH, Spe3DViewer.this.model);
                    Spe3DViewer.this.startActivity(intent);
                    return;
                }
                new Builder(Spe3DViewer.this).setMessage(R.string.portrait3d_tips_install_printing_again).setPositiveButton(Spe3DViewer.this.getResources().getString(R.string.portrait3d_install), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Spe3DViewer.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(Spe3DViewer.PORTRAIT3D_PRINT_URL)));
                    }
                }).setNegativeButton(Spe3DViewer.this.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        });
        this.modifyButton = (ImageView) findViewById(R.id.modifyButton);
        this.modifyButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (Spe3DViewer.this.isAppInstalled(Spe3DViewer.this, "co.spe3d.paipai_huawei")) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("co.spe3d.paipai_huawei", "co.spe3d.paipai_huawei.HWActivity"));
                    intent.putExtra(Spe3DViewer.MODEL_PATH, Spe3DViewer.this.model);
                    Spe3DViewer.this.startActivity(intent);
                    return;
                }
                new Builder(Spe3DViewer.this).setMessage(Spe3DViewer.this.getResources().getString(R.string.portrait3d_tips_install_dress_up_again)).setPositiveButton(Spe3DViewer.this.getResources().getString(R.string.portrait3d_install), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Spe3DViewer.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(Spe3DViewer.PORTRAIT3D_DRESS_UP_URL)));
                    }
                }).setNegativeButton(Spe3DViewer.this.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        });
        this.backButton = (ImageView) findViewById(R.id.backButton);
        this.backButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Spe3DViewer.this.onBackPressed();
            }
        });
        getModelPath();
        loadModel();
        Log.i(SPE3D_TAG, "onCreate: process id: " + Process.myPid());
    }

    public void getModelPath() {
        if (getIntent().getStringExtra(MODEL_PATH) != null) {
            this.model = getIntent().getStringExtra(MODEL_PATH);
        } else if (getIntent().getStringExtra("model") != null) {
            this.model = getIntent().getStringExtra("model");
        } else if (getIntent().getData() != null) {
            this.model = getPath(getIntent().getData());
        }
    }

    public String getPath(Uri uri) {
        if (!CheckPermission()) {
            return null;
        }
        String path = uri.getPath();
        if (new File(uri.getPath()).exists()) {
            return path;
        }
        Cursor cursor = getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
        if (cursor == null) {
            return null;
        }
        int column_index = cursor.getColumnIndexOrThrow("_data");
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        if (s == null || s.isEmpty()) {
            return path.replace("/document/primary:", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator);
        }
        return null;
    }

    public static void updateLightSetting() {
        Spe3DNativeLib.setLights(lights, lightNum);
    }

    private void loadModel() {
        if (CheckPermission()) {
            try {
                Spe3DNativeLib.clearContents();
                if (this.model != null && !this.model.equals("")) {
                    File file = new File(this.model);
                    if (file.exists()) {
                        this.time = file.lastModified();
                        if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equals("jpg")) {
                            loadJpg(file);
                        } else if (file.isDirectory()) {
                            loadFolder(file);
                        } else {
                            loadFile(file);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(SPE3D_TAG, e.toString());
                finish();
            }
        }
    }

    private void loadFile(File file) {
        if (file.isFile()) {
            Spe3DNativeLib.loadObject(file.getAbsolutePath());
        }
    }

    private void loadFolder(File folder) {
        if (folder.isDirectory()) {
            for (String objFile : folder.list(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.length() > 4 && filename.substring(filename.length() - 4).equalsIgnoreCase(".obj");
                }
            })) {
                Spe3DNativeLib.loadObject(objFile);
            }
        }
    }

    private void loadJpg(File file) throws RemoteException {
        try {
            this.serviceHostWrapper = new ServiceHostWrapper(file.getAbsolutePath());
        } catch (IllegalArgumentException e) {
            Log.e(SPE3D_TAG, e.toString());
            return;
        } catch (Exception e2) {
            this.serviceHostWrapper = new ServiceHostWrapper(file.getAbsolutePath());
        }
        if (this.serviceHostWrapper.getContentMap() != null) {
            for (Entry<String, String> entry : this.serviceHostWrapper.getObjmtlname().entrySet()) {
                Spe3DNativeLib.loadObjectWithServiceHost(this.serviceHostWrapper, (String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    protected void onPause() {
        super.onPause();
        this.mView.onPause();
    }

    protected void onResume() {
        super.onResume();
        this.mView.onResume();
        if (this.model != null && !this.model.equals("")) {
            File file = new File(this.model);
            if (!file.exists()) {
                finish();
            } else if (file.lastModified() != this.time) {
                loadModel();
            }
        }
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Spe3DNativeLib.keyboardDown(event.getUnicodeChar());
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 4:
                super.onDestroy();
                finish();
                break;
            case 82:
                openOptionsMenu();
                break;
            case 84:
                break;
            default:
                Spe3DNativeLib.keyboardUp(event.getUnicodeChar());
                break;
        }
        return true;
    }

    public boolean onTouch(View v, MotionEvent event) {
        int numPoints = event.getPointerCount();
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        float factor = displayMetrics.density / ((float) displayMetrics.densityDpi);
        if (numPoints > 1) {
            factor /= 2.0f;
        }
        long ptr = 0;
        int i = 0;
        while (i < numPoints) {
            float x = (numPoints <= 1 ? event.getX(i) : ((float) v.getWidth()) - event.getX(i)) * factor;
            float y = event.getY(i) * factor;
            switch (event.getActionMasked()) {
                case 0:
                case 5:
                    if (ptr != 0) {
                        Spe3DNativeLib.addTouchPoint(ptr, event.getPointerId(i), 1, x, y);
                        break;
                    }
                    ptr = Spe3DNativeLib.touchBeganEvent(event.getPointerId(i), x, y);
                    continue;
                case 1:
                    v.performClick();
                    break;
                case 2:
                    if (ptr != 0) {
                        Spe3DNativeLib.addTouchPoint(ptr, event.getPointerId(i), 2, x, y);
                        break;
                    }
                    ptr = Spe3DNativeLib.touchMovedEvent(event.getPointerId(i), x, y);
                    continue;
                case 6:
                    break;
                default:
                    break;
            }
            if (ptr == 0) {
                ptr = Spe3DNativeLib.touchEndedEvent(event.getPointerId(i), x, y, 1);
            } else {
                Spe3DNativeLib.addTouchPoint(ptr, event.getPointerId(i), 4, x, y);
            }
            i++;
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    public boolean isAppInstalled(Context context, String packageName) {
        List<PackageInfo> pinfo = context.getPackageManager().getInstalledPackages(0);
        List<String> pName = new ArrayList();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                pName.add(((PackageInfo) pinfo.get(i)).packageName);
            }
        }
        return pName.contains(packageName);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1024 && grantResults.length > 0 && grantResults[0] == 0) {
            getModelPath();
            loadModel();
        }
    }

    protected boolean needToRequestPermissions() {
        return true;
    }

    protected String[] getPermissionsType() {
        return new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
    }

    protected int getPermissionsCode(String permission) {
        int i = -1;
        switch (permission.hashCode()) {
            case 1365911975:
                if (permission.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
                    i = 0;
                    break;
                }
                break;
        }
        switch (i) {
            case 0:
                return 1024;
            default:
                return 0;
        }
    }

    protected boolean CheckPermission() {
        String[] permissions = getPermissionsType();
        boolean checkPermission = true;
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != 0) {
                if (needToRequestPermissions()) {
                    ActivityCompat.requestPermissions(this, permissions, getPermissionsCode(permissions[i]));
                }
                checkPermission = false;
            }
        }
        return checkPermission;
    }
}
