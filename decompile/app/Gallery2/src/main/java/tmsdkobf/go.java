package tmsdkobf;

import java.io.File;
import java.util.HashMap;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class go {
    private static qa pi = ((qa) ManagerCreatorC.getManager(qa.class));
    private static HashMap<String, py> pj;

    public static void a(fy fyVar) {
        if (fyVar.getPackageName() == null || fyVar.getPackageName().equals("")) {
            fyVar.an(6);
            return;
        }
        int c = TMServiceFactory.getSystemInfoService().c(fyVar.getPackageName(), fyVar.hB());
        if (c == 0) {
            fyVar.an(2);
        } else if (c == 2) {
            fyVar.an(9);
        } else if (c == -1) {
            fyVar.an(1);
        } else if (c == 1) {
            fyVar.an(11);
        }
    }

    public static fy aJ(String str) {
        py i;
        py pyVar = pj == null ? null : (py) pj.get(str);
        if (pyVar == null) {
            try {
                i = pi.i(str, 73);
            } catch (Exception e) {
                d.f("ApkUtil", e.getMessage());
            }
            if (i == null) {
                i = aK(str);
            }
            if (i == null) {
                return null;
            }
            fy a = lr.a(i);
            a(a);
            return a;
        }
        i = pyVar;
        if (i == null) {
            i = aK(str);
        }
        if (i == null) {
            return null;
        }
        fy a2 = lr.a(i);
        a(a2);
        return a2;
    }

    public static py aK(String str) {
        py pyVar = new py();
        File file = new File(str);
        if (file != null) {
            String name = file.getName();
            if (!(name == null || name.equals(""))) {
                name = name.substring(0, name.length() - 4);
            }
            pyVar.cS(null);
            pyVar.aS(str);
            pyVar.setAppName(name);
            pyVar.setSize(file.length());
            pyVar.O(true);
        }
        return pyVar;
    }
}
