package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import java.util.Iterator;
import java.util.LinkedList;
import tmsdk.common.TMSService;
import tmsdk.common.creator.BaseManagerC;

/* compiled from: Unknown */
final class ql extends BaseManagerC {
    private LinkedList<b> Jy = new LinkedList();
    private jh Jz;
    private Context mContext;

    /* compiled from: Unknown */
    interface a {
        void cW(String str);
    }

    /* compiled from: Unknown */
    static final class b implements qj {
        private qj JA;

        public b(qj qjVar) {
            this.JA = qjVar;
        }

        public final void bQ(final String str) {
            jq.ct().c(new Runnable(this) {
                final /* synthetic */ b JC;

                public void run() {
                    this.JC.JA.bQ(str);
                }
            }, "onPackageAddedThread").start();
        }

        public void bR(final String str) {
            jq.ct().c(new Runnable(this) {
                final /* synthetic */ b JC;

                public void run() {
                    this.JC.JA.bR(str);
                }
            }, "onPackageReinstallThread").start();
        }

        public final void bS(final String str) {
            jq.ct().c(new Runnable(this) {
                final /* synthetic */ b JC;

                public void run() {
                    this.JC.JA.bS(str);
                }
            }, "onPackageRemovedThread").start();
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof b)) {
                return false;
            }
            return this.JA.getClass().equals(((b) obj).JA.getClass());
        }
    }

    /* compiled from: Unknown */
    private final class c extends jh {
        private d JD;
        final /* synthetic */ ql JE;

        private c(ql qlVar) {
            this.JE = qlVar;
        }

        public IBinder onBind() {
            return null;
        }

        public void onCreate(Context context) {
            super.onCreate(context);
            this.JD = new d();
            this.JD.register();
        }

        public void onDestory() {
            this.JD.hM();
            super.onDestory();
        }
    }

    /* compiled from: Unknown */
    private final class d extends jj {
        final /* synthetic */ ql JE;
        private a JF;
        private a JG;
        private a JH;

        private d(ql qlVar) {
            this.JE = qlVar;
            this.JF = new a(this) {
                final /* synthetic */ d JI;

                {
                    this.JI = r1;
                }

                public void cW(String str) {
                    Iterator it = this.JI.JE.Jy.iterator();
                    while (it.hasNext()) {
                        ((b) it.next()).bQ(str);
                    }
                }
            };
            this.JG = new a(this) {
                final /* synthetic */ d JI;

                {
                    this.JI = r1;
                }

                public void cW(String str) {
                    Iterator it = this.JI.JE.Jy.iterator();
                    while (it.hasNext()) {
                        ((b) it.next()).bS(str);
                    }
                }
            };
            this.JH = new a(this) {
                final /* synthetic */ d JI;

                {
                    this.JI = r1;
                }

                public void cW(String str) {
                    Iterator it = this.JI.JE.Jy.iterator();
                    while (it.hasNext()) {
                        ((b) it.next()).bR(str);
                    }
                }
            };
        }

        private void a(final a aVar, final String str) {
            jq.ct().c(new Runnable(this) {
                final /* synthetic */ d JI;

                public void run() {
                    synchronized (this.JI.JE.Jy) {
                        aVar.cW(str);
                    }
                }
            }, "handlePackageChangeThread").start();
        }

        public void doOnRecv(Context context, Intent intent) {
            Object obj = null;
            String action = intent.getAction();
            Bundle extras = intent.getExtras();
            if (extras == null || !extras.containsKey("android.intent.extra.REPLACING")) {
                obj = -1;
            } else if (!extras.getBoolean("android.intent.extra.REPLACING")) {
                obj = 1;
            }
            if (action.equals("android.intent.action.PACKAGE_ADDED") && r0 != null) {
                a(this.JF, intent.getDataString().substring(8));
            } else if (action.equals("android.intent.action.PACKAGE_REMOVED") && r0 != null) {
                a(this.JG, intent.getDataString().substring(8));
            } else if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                a(this.JH, intent.getDataString().substring(8));
            }
        }

        public void hM() {
            this.JE.mContext.unregisterReceiver(this);
        }

        public void register() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
            intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
            intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            intentFilter.setPriority(Integer.MAX_VALUE);
            intentFilter.addDataScheme("package");
            this.JE.mContext.registerReceiver(this, intentFilter);
        }
    }

    ql() {
    }

    public qj c(qj qjVar) {
        synchronized (this.Jy) {
            Object bVar = qjVar == null ? null : new b(qjVar);
            if (bVar != null) {
                if (!this.Jy.contains(bVar)) {
                    this.Jy.add(bVar);
                    return qjVar;
                }
            }
            return null;
        }
    }

    public int getSingletonType() {
        return 1;
    }

    public void onCreate(Context context) {
        this.mContext = context;
        this.Jz = new c();
        TMSService.startService(this.Jz, null);
    }
}
