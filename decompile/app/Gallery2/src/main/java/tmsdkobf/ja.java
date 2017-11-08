package tmsdkobf;

import android.telephony.PhoneStateListener;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.DualSimTelephonyManager;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class ja {
    private int tn = 0;
    private PhoneStateListener to;
    private PhoneStateListener tp;
    private List<b> tq = new ArrayList();

    /* compiled from: Unknown */
    public interface b {
        void bx(String str);

        void by(String str);

        void bz(String str);

        void i(String str, String str2);
    }

    /* compiled from: Unknown */
    private static class a {
        static ja ts = new ja();
    }

    public ja() {
        register();
    }

    private void bC(String str) {
        d.d("PhoneStateManager", "onOutCall number=" + str);
        synchronized (this.tq) {
            for (b bz : this.tq) {
                bz.bz(str);
            }
        }
    }

    private void bx(String str) {
        d.d("PhoneStateManager", "onConnect number=" + str);
        synchronized (this.tq) {
            for (b bx : this.tq) {
                bx.bx(str);
            }
        }
    }

    private void by(String str) {
        d.d("PhoneStateManager", "onHoldOff number=" + str);
        synchronized (this.tq) {
            for (b by : this.tq) {
                by.by(str);
            }
        }
    }

    public static ja ce() {
        return a.ts;
    }

    private void j(String str, String str2) {
        d.d("PhoneStateManager", "onCallComing number=" + str);
        synchronized (this.tq) {
            for (b i : this.tq) {
                i.i(str, str2);
            }
        }
    }

    private void register() {
        int i;
        DualSimTelephonyManager instance;
        qz qzVar = jq.uh;
        if (qzVar != null && qzVar.il()) {
            try {
                this.to = new PhoneStateListener(this, 0) {
                    final /* synthetic */ ja tr;

                    public void onCallStateChanged(int i, String str) {
                        String str2 = null;
                        switch (i) {
                            case 0:
                                this.tr.by(str);
                                break;
                            case 1:
                                qz qzVar = jq.uh;
                                if (qzVar != null) {
                                    str2 = qzVar.cz(0);
                                }
                                this.tr.j(str, str2);
                                break;
                            case 2:
                                if (this.tr.tn != 1) {
                                    if (this.tr.tn == 0) {
                                        this.tr.bC(str);
                                        break;
                                    }
                                }
                                this.tr.bx(str);
                                break;
                                break;
                        }
                        this.tr.tn = i;
                        super.onCallStateChanged(i, str);
                    }
                };
                this.tp = new PhoneStateListener(this, 1) {
                    final /* synthetic */ ja tr;

                    public void onCallStateChanged(int i, String str) {
                        String str2 = null;
                        switch (i) {
                            case 0:
                                this.tr.by(str);
                                break;
                            case 1:
                                qz qzVar = jq.uh;
                                if (qzVar != null) {
                                    str2 = qzVar.cz(1);
                                }
                                if (str2 == null) {
                                    d.c("PhoneStateManager", "Incoming call from 2nd sim card but no card value!");
                                }
                                this.tr.j(str, str2);
                                break;
                            case 2:
                                if (this.tr.tn != 1) {
                                    if (this.tr.tn == 0) {
                                        this.tr.bC(str);
                                        break;
                                    }
                                }
                                this.tr.bx(str);
                                break;
                                break;
                        }
                        this.tr.tn = i;
                        super.onCallStateChanged(i, str);
                    }
                };
                i = 0;
            } catch (Throwable th) {
            }
            if (i != 0) {
                this.to = new PhoneStateListener(this) {
                    final /* synthetic */ ja tr;

                    {
                        this.tr = r1;
                    }

                    public void onCallStateChanged(int i, String str) {
                        String str2 = null;
                        qz qzVar = jq.uh;
                        if (qzVar != null) {
                            String im = qzVar.im();
                            if (im != null && im.indexOf("htc") > -1) {
                                if (im.indexOf("t328w") > -1 || im.indexOf("t328d") > -1) {
                                    super.onCallStateChanged(i, str);
                                    return;
                                }
                            }
                        }
                        switch (i) {
                            case 0:
                                this.tr.by(str);
                                break;
                            case 1:
                                if (qzVar != null) {
                                    str2 = qzVar.cz(0);
                                }
                                this.tr.j(str, str2);
                                break;
                            case 2:
                                if (this.tr.tn != 1) {
                                    if (this.tr.tn == 0) {
                                        this.tr.bC(str);
                                        break;
                                    }
                                }
                                this.tr.bx(str);
                                break;
                                break;
                        }
                        this.tr.tn = i;
                        super.onCallStateChanged(i, str);
                    }
                };
                this.tp = new PhoneStateListener(this) {
                    final /* synthetic */ ja tr;

                    {
                        this.tr = r1;
                    }

                    public void onCallStateChanged(int i, String str) {
                        String str2 = null;
                        switch (i) {
                            case 0:
                                this.tr.by(str);
                                break;
                            case 1:
                                qz qzVar = jq.uh;
                                if (qzVar != null) {
                                    str2 = qzVar.cz(1);
                                }
                                if (str2 == null) {
                                    d.c("PhoneStateManager", "Incoming call from 2nd sim card but no card value!");
                                }
                                this.tr.j(str, str2);
                                break;
                            case 2:
                                if (this.tr.tn != 1) {
                                    if (this.tr.tn == 0) {
                                        this.tr.bC(str);
                                        break;
                                    }
                                }
                                this.tr.bx(str);
                                break;
                                break;
                        }
                        this.tr.tn = i;
                        super.onCallStateChanged(i, str);
                    }
                };
            }
            instance = DualSimTelephonyManager.getInstance();
            instance.listenPhonesState(0, this.to, 32);
            instance.listenPhonesState(1, this.tp, 32);
        }
        i = 1;
        if (i != 0) {
            this.to = /* anonymous class already generated */;
            this.tp = /* anonymous class already generated */;
        }
        instance = DualSimTelephonyManager.getInstance();
        instance.listenPhonesState(0, this.to, 32);
        instance.listenPhonesState(1, this.tp, 32);
    }

    @Deprecated
    public void a(b bVar) {
        synchronized (this.tq) {
            this.tq.add(0, bVar);
        }
    }

    public boolean b(b bVar) {
        boolean remove;
        synchronized (this.tq) {
            remove = !this.tq.contains(bVar) ? true : this.tq.remove(bVar);
        }
        return remove;
    }
}
