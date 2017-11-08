package com.huawei.openalliance.ad.utils.db;

import android.content.Context;
import com.huawei.openalliance.ad.utils.b.d;
import com.huawei.openalliance.ad.utils.db.bean.AdEventRecord;
import com.huawei.openalliance.ad.utils.db.bean.MaterialRecord;
import com.huawei.openalliance.ad.utils.db.bean.TestMaterialRecord;
import com.huawei.openalliance.ad.utils.db.bean.ThirdPartyEventRecord;
import com.huawei.openalliance.ad.utils.db.bean.a;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* compiled from: Unknown */
public class c {
    private static List<a> b = new ArrayList(4);
    private static List<a> c = new ArrayList(4);
    private a a;

    static {
        b.add(new MaterialRecord());
        b.add(new AdEventRecord());
        b.add(new TestMaterialRecord());
        b.add(new ThirdPartyEventRecord());
        c.add(new MaterialRecord());
        c.add(new AdEventRecord());
        c.add(new ThirdPartyEventRecord());
    }

    public c(a aVar, Context context) {
        this.a = aVar;
    }

    private String a(String[] strArr, String[] strArr2) {
        StringBuilder stringBuilder = new StringBuilder();
        List arrayList = new ArrayList(4);
        if (strArr2 != null) {
            arrayList = Arrays.asList(strArr2);
        }
        if (strArr == null || strArr.length <= 0 || strArr2 == null) {
            return null;
        }
        for (int i = 0; i < strArr.length; i++) {
            String str = strArr[i];
            if (arrayList.contains(str)) {
                stringBuilder.append(str);
            } else {
                stringBuilder.append("\"\"");
            }
            if (i != strArr.length - 1) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    private void a(String str) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" INSERT INTO ");
        stringBuilder.append(str);
        stringBuilder.append(" SELECT ");
        try {
            try {
                String a = a(this.a.d(str), this.a.d("_temp_" + str));
                if (a == null) {
                    throw new Exception("DbUpdateHelper insertData sInsertColumns is null. [tableName=" + str + "]");
                }
                stringBuilder.append(a);
                stringBuilder.append(" FROM ");
                stringBuilder.append("_temp_" + str);
                try {
                    this.a.c(stringBuilder.toString());
                } catch (Exception e) {
                    throw new Exception("DbUpdateHelper insertData mDbHelper.executeSQL error");
                }
            } catch (Exception e2) {
                throw new Exception("DbUpdateHelper insertData mDbHelper.getOldColumnNames error ");
            }
        } catch (Exception e3) {
            throw e3;
        }
    }

    public void a() throws Exception {
        for (a aVar : c) {
            String s = aVar.s();
            if (this.a.e(s)) {
                this.a.f(s);
                d.b("DbUpdateHelper", "tableName exist moidfy table successfully.");
                try {
                    this.a.c(aVar.r());
                    a(s);
                    d.b("DbUpdateHelper", "insert data to table successfully.");
                    this.a.a(s);
                    d.b("DbUpdateHelper", "drop table temp table successfully.");
                } catch (Exception e) {
                    throw e;
                }
            }
            try {
                this.a.c(aVar.r());
            } catch (Exception e2) {
                throw e2;
            }
        }
    }

    public void b() throws Exception {
        for (a s : b) {
            String s2 = s.s();
            if (this.a.e(s2)) {
                try {
                    this.a.b(s2);
                } catch (Exception e) {
                    throw e;
                }
            }
        }
        for (a s3 : c) {
            try {
                this.a.c(s3.r());
            } catch (Exception e2) {
                throw e2;
            }
        }
    }
}
