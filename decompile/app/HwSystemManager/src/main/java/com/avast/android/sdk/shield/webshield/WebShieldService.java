package com.avast.android.sdk.shield.webshield;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.UrlCheckResultStructure;
import com.avast.android.sdk.engine.UrlCheckResultStructure.UrlCheckResult;
import com.avast.android.sdk.engine.obfuscated.ao;
import java.util.Date;
import java.util.List;

/* compiled from: Unknown */
public abstract class WebShieldService extends Service {
    private Looper a;
    private a b;
    private Date c;
    private b d;
    private b e;
    private b f;
    private b g;
    private b h;
    private b i;
    private b j;
    private b k;
    private b l;

    /* compiled from: Unknown */
    private class a extends Handler {
        final /* synthetic */ WebShieldService a;

        public a(WebShieldService webShieldService, Looper looper) {
            this.a = webShieldService;
            super(looper);
        }

        public void handleMessage(Message message) {
            Cursor query;
            SupportedBrowser supportedBrowser = (SupportedBrowser) message.obj;
            ao.a(supportedBrowser + " content URI changed");
            try {
                query = this.a.getContentResolver().query(supportedBrowser.b(), supportedBrowser.d(), supportedBrowser.g() == null ? null : supportedBrowser.g() + "=0", null, supportedBrowser.e() + " DESC ");
            } catch (Exception e) {
                query = null;
            }
            if (query == null) {
                return;
            }
            if (query.moveToFirst()) {
                int columnIndex = query.getColumnIndex(supportedBrowser.e());
                String string = query.getString(query.getColumnIndex(supportedBrowser.f()));
                long j = query.getLong(columnIndex);
                query.close();
                if (this.a.c.before(new Date(j))) {
                    this.a.c.setTime(j);
                    ao.a(supportedBrowser + " browser surfed to " + string);
                    UrlAction onNewUrlDetected = this.a.onNewUrlDetected(string, supportedBrowser);
                    if (onNewUrlDetected != null) {
                        switch (b.b[onNewUrlDetected.ordinal()]) {
                            case 1:
                                return;
                            case 2:
                                WebShieldBrowserHelper.blockBrowser(this.a.getApplicationContext(), supportedBrowser, this.a.c());
                                break;
                            case 3:
                                this.a.a(string, supportedBrowser);
                                break;
                            default:
                                break;
                        }
                    }
                    ao.c("Scan was skipped because 'onNewUrlDetected(url, browser)' return null.");
                    return;
                }
                return;
            }
            query.close();
        }
    }

    /* compiled from: Unknown */
    private class b extends ContentObserver {
        final /* synthetic */ WebShieldService a;
        private final SupportedBrowser b;

        public b(WebShieldService webShieldService, Handler handler, SupportedBrowser supportedBrowser) {
            this.a = webShieldService;
            super(handler);
            this.b = supportedBrowser;
        }

        public boolean deliverSelfNotifications() {
            return true;
        }

        public void onChange(boolean z) {
            super.onChange(z);
            if (!z) {
                Message message = new Message();
                message.obj = this.b;
                this.a.b.sendMessage(message);
            }
        }
    }

    private void a() {
        this.d = new b(this, new Handler(), b());
        this.f = new b(this, new Handler(), SupportedBrowser.SILK);
        this.g = new b(this, new Handler(), SupportedBrowser.CHROME);
        this.h = new b(this, new Handler(), SupportedBrowser.CHROME_2);
        this.e = new b(this, new Handler(), SupportedBrowser.DOLPHIN);
        this.i = new b(this, new Handler(), SupportedBrowser.BOAT);
        this.j = new b(this, new Handler(), SupportedBrowser.BOAT_MINI);
        this.k = new b(this, new Handler(), SupportedBrowser.SBROWSER);
        this.l = new b(this, new Handler(), SupportedBrowser.BOAT_TABLET);
        getContentResolver().registerContentObserver(SupportedBrowser.STOCK.b(), true, this.d);
        getContentResolver().registerContentObserver(SupportedBrowser.SILK.b(), true, this.f);
        getContentResolver().registerContentObserver(SupportedBrowser.CHROME.b(), true, this.g);
        getContentResolver().registerContentObserver(SupportedBrowser.CHROME_2.b(), true, this.h);
        getContentResolver().registerContentObserver(SupportedBrowser.DOLPHIN.b(), true, this.e);
        getContentResolver().registerContentObserver(SupportedBrowser.BOAT.b(), true, this.i);
        getContentResolver().registerContentObserver(SupportedBrowser.BOAT_MINI.b(), true, this.j);
        getContentResolver().registerContentObserver(SupportedBrowser.SBROWSER.b(), true, this.k);
        getContentResolver().registerContentObserver(SupportedBrowser.BOAT_TABLET.b(), true, this.l);
    }

    private void a(String str, SupportedBrowser supportedBrowser) {
        UrlCheckResultStructure urlCheckResultStructure = new UrlCheckResultStructure(UrlCheckResult.RESULT_UNKNOWN_ERROR);
        List list = null;
        for (int i = 0; i < 3 && UrlCheckResult.RESULT_UNKNOWN_ERROR.equals(urlCheckResultStructure.result); i++) {
            list = EngineInterface.checkUrl(this, null, str, supportedBrowser.h());
            if (!(list == null || list.isEmpty())) {
                urlCheckResultStructure = (UrlCheckResultStructure) list.get(0);
            }
        }
        ScannedUrlAction onUrlScanResult = onUrlScanResult(str, list, supportedBrowser);
        if (onUrlScanResult == null) {
            onUrlScanResult = !UrlCheckResult.RESULT_TYPO_SQUATTING.equals(urlCheckResultStructure.result) ? !UrlCheckResult.RESULT_OK.equals(urlCheckResultStructure.result) ? ScannedUrlAction.BLOCK : ScannedUrlAction.DO_NOTHING : ScannedUrlAction.TYPOSQUATTING_AUTOCORRECT;
        }
        switch (b.a[onUrlScanResult.ordinal()]) {
            case 1:
                WebShieldBrowserHelper.blockBrowser(this, supportedBrowser, c());
                break;
            case 2:
                if (urlCheckResultStructure.desiredSite == null) {
                    WebShieldBrowserHelper.redirectBrowserToCorrectUrl((Context) this, supportedBrowser, c());
                    break;
                } else {
                    WebShieldBrowserHelper.redirectBrowserToCorrectUrl((Context) this, supportedBrowser, Uri.parse(urlCheckResultStructure.desiredSite));
                    break;
                }
            case 3:
                return;
        }
    }

    private boolean a(Intent intent) {
        return getPackageManager().resolveActivity(intent, 0) != null;
    }

    private SupportedBrowser b() {
        SupportedBrowser supportedBrowser = SupportedBrowser.STOCK;
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("http://avast.com"));
        intent.setClassName(SupportedBrowser.STOCK_JB.a(), SupportedBrowser.STOCK_JB.c());
        return !a(intent) ? supportedBrowser : SupportedBrowser.STOCK_JB;
    }

    private Uri c() {
        Uri blockUrl = getBlockUrl();
        return blockUrl != null ? blockUrl : Uri.parse(WebShieldAccessibilityService.EMPTY_PAGE);
    }

    public abstract Uri getBlockUrl();

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread("WebShieldService[helper]", 1);
        handlerThread.start();
        this.a = handlerThread.getLooper();
        this.b = new a(this, this.a);
        this.c = new Date();
    }

    public void onDestroy() {
        if (this.d != null) {
            getContentResolver().unregisterContentObserver(this.d);
            this.d = null;
        }
        if (this.g != null) {
            getContentResolver().unregisterContentObserver(this.g);
            this.g = null;
        }
        if (this.h != null) {
            getContentResolver().unregisterContentObserver(this.h);
            this.h = null;
        }
        if (this.f != null) {
            getContentResolver().unregisterContentObserver(this.f);
            this.f = null;
        }
        if (this.e != null) {
            getContentResolver().unregisterContentObserver(this.e);
            this.e = null;
        }
        if (this.i != null) {
            getContentResolver().unregisterContentObserver(this.i);
            this.i = null;
        }
        if (this.j != null) {
            getContentResolver().unregisterContentObserver(this.j);
            this.j = null;
        }
        if (this.k != null) {
            getContentResolver().unregisterContentObserver(this.k);
            this.k = null;
        }
        if (this.l != null) {
            getContentResolver().unregisterContentObserver(this.l);
        }
        this.a.quit();
        super.onDestroy();
    }

    public abstract UrlAction onNewUrlDetected(String str, SupportedBrowser supportedBrowser);

    public int onStartCommand(Intent intent, int i, int i2) {
        a();
        return super.onStartCommand(intent, i, i2);
    }

    public abstract ScannedUrlAction onUrlScanResult(String str, List<UrlCheckResultStructure> list, SupportedBrowser supportedBrowser);
}
