package com.android.settings;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LicenseHelper {
    static final String[] NOTICES_ARRAY = new String[]{"/system/etc/NOTICE.txt", "/vendor/etc/NOTICE.txt", "/cust/etc/NOTICE.txt", "/product/etc/NOTICE.txt", "/version/etc/NOTICE.txt"};
    private Context mContext;

    public static class NoticeParser {
        Map<String, String> mFileSets = new HashMap();
        List<String> mLicenses = new ArrayList();

        public void parse(String filePath) {
            IOException e;
            FileNotFoundException e2;
            Throwable th;
            InputStreamReader inputStreamReader = null;
            BufferedReader reader = null;
            try {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(filePath), "utf-8");
                try {
                    BufferedReader reader2 = new BufferedReader(isr);
                    int mode = -1;
                    try {
                        ArrayList<String> tmpFiles = new ArrayList();
                        StringBuilder sb = new StringBuilder();
                        while (true) {
                            String line = reader2.readLine();
                            if (line == null) {
                                mode = 2;
                            }
                            if ("============================================================".equals(line) || (line == null && mode == 2)) {
                                String content = sb.toString();
                                if (content.isEmpty()) {
                                    mode = 0;
                                } else {
                                    if (!this.mLicenses.contains(content)) {
                                        this.mLicenses.add(content);
                                    }
                                    for (String file : tmpFiles) {
                                        this.mFileSets.put(file, content);
                                    }
                                    if (sb.length() > 0) {
                                        sb.delete(0, sb.length() - 1);
                                    }
                                    tmpFiles.clear();
                                    if (mode == 2) {
                                        break;
                                    }
                                    mode = 0;
                                }
                            } else if ("------------------------------------------------------------".equals(line)) {
                                mode = 1;
                            } else if (mode == 0) {
                                if (!("Notices for file(s):".equals(line) || tmpFiles.contains(line))) {
                                    tmpFiles.add(line);
                                }
                            } else if (mode == 1) {
                                sb.append(line);
                                sb.append("\n");
                            } else {
                                continue;
                            }
                        }
                        if (reader2 != null) {
                            try {
                                reader2.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        if (isr != null) {
                            try {
                                isr.close();
                            } catch (IOException e32) {
                                e32.printStackTrace();
                            }
                        }
                        reader = reader2;
                    } catch (FileNotFoundException e4) {
                        e2 = e4;
                        reader = reader2;
                        inputStreamReader = isr;
                    } catch (IOException e5) {
                        e32 = e5;
                        reader = reader2;
                        inputStreamReader = isr;
                    } catch (Throwable th2) {
                        th = th2;
                        reader = reader2;
                        inputStreamReader = isr;
                    }
                } catch (FileNotFoundException e6) {
                    e2 = e6;
                    inputStreamReader = isr;
                    try {
                        e2.printStackTrace();
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e322) {
                                e322.printStackTrace();
                            }
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (IOException e3222) {
                                e3222.printStackTrace();
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e32222) {
                                e32222.printStackTrace();
                            }
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (IOException e322222) {
                                e322222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (IOException e7) {
                    e322222 = e7;
                    inputStreamReader = isr;
                    e322222.printStackTrace();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e3222222) {
                            e3222222.printStackTrace();
                        }
                    }
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e32222222) {
                            e32222222.printStackTrace();
                        }
                    }
                } catch (Throwable th4) {
                    th = th4;
                    inputStreamReader = isr;
                    if (reader != null) {
                        reader.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e8) {
                e2 = e8;
                e2.printStackTrace();
                if (reader != null) {
                    reader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
            } catch (IOException e9) {
                e32222222 = e9;
                e32222222.printStackTrace();
                if (reader != null) {
                    reader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
            }
        }

        public void setWrittenOffer(OutputStreamWriter writer) {
            try {
                writer.append("<p class=MsoNormal><span lang=EN-US>Please note we provide open source software notice along with this product. You may access the open source software notice through the device menu or by connecting the device to your computer - depending on the build of your device.</span></p>");
                writer.append("<p class=MsoNormal><b><u><span lang=EN-US style='font-size:16.0pt'>Warranty Disclaimer</span></u></b></p>");
                writer.append("<p class=MsoNormal><b><span lang=EN-US style='text-transform:uppercase'>The open source software in this product is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY, without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the applicable licenses for more details.</span></b></p>");
                writer.append("<p class=MsoNormal><b><span lang=EN-US style='text-transform:uppercase'>&nbsp;</span></b></p>");
                writer.append("<p class=MsoNormal><b><span lang=EN-US>Written Offer</span></b></p>");
                writer.append("<p class=MsoNormal><span lang=EN-US>This product contains software whose rights holders license it on the terms of the GNU General Public License, version 2 (GPLv2) and/or other open source software licenses. We will provide you and any third party with the source code of the software licensed under an open source software license for a charge no more than our costs for physically performing such distribution on a medium customarily used for software interchange (e.g.CD) if you send us a written request by mail or email to the following addresses: </span></p>");
                writer.append("<p class=MsoNormal><span lang=EN-US>mobile@huawei.com</span></p>");
                writer.append("<p class=MsoNormal><span lang=EN-US>detailing the name of the product and the firmware version for which you need the source code and indicating how we can contact you.</span></p>");
                writer.append("<p class=MsoNormal><span lang=EN-US>&nbsp;</span></p>");
                writer.append("<p class=MsoNormal><b><span lang=EN-US style='text-transform:uppercase'>This offer is valid for three years from the moment we distributed the product and valid for as long as we offer spare parts or customer support for that product model.</span></b><span lang=EN-US> </span></p>");
            } catch (IOException e) {
                Log.e("settings-LicenseHelper", "setWrittenOffer-->e: " + e);
            }
        }

        public void writeToHtml(String outFilePath) {
            IOException e;
            String HTML_OUTPUT_CSS = "<style type=\"text/css\">body { padding: 0; font-family: sans-serif; }.same-license { background-color: #eeeeee; border-top: 20px solid white; padding: 10px; }.label { font-weight: bold; }.file-list { margin-left: 1em; color: blue; }</style>";
            OutputStreamWriter outputStreamWriter = null;
            try {
                OutputStreamWriter writer = new OutputStreamWriter(LicenseHelper.openOutputStream(new File(outFilePath), false), "utf-8");
                try {
                    writer.append("<html><head>");
                    setWrittenOffer(writer);
                    writer.append(HTML_OUTPUT_CSS);
                    writer.append("</head><body topmargin=\"0\" leftmargin=\"0\" rightmargin=\"0\" bottommargin=\"0\">");
                    writer.append("<div class=\"toc\">");
                    writer.append("<ul>");
                    List<String> list = new ArrayList(this.mFileSets.keySet());
                    Collections.sort(list);
                    for (String file : list) {
                        int id = this.mLicenses.indexOf((String) this.mFileSets.get(file));
                        writer.append(String.format("<li><a href=\"#id%d\">%s</a></li>", new Object[]{Integer.valueOf(id), file}));
                    }
                    writer.append("</ul>");
                    writer.append("</div><!-- table of contents -->");
                    writer.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
                    for (int i = 0; i < this.mLicenses.size(); i++) {
                        String license = (String) this.mLicenses.get(i);
                        if (license != null) {
                            writer.append(String.format("<tr id=\"id%d\"><td class=\"same-license\">", new Object[]{Integer.valueOf(i)}));
                            writer.append("<div class=\"label\">Notices for file(s):</div>");
                            writer.append("<div class=\"file-list\">");
                            for (Entry<String, String> entry : this.mFileSets.entrySet()) {
                                if (((String) entry.getValue()).equals(license)) {
                                    writer.append(String.format("%s <br/>", new Object[]{((Entry) entry$iterator.next()).getKey()}));
                                }
                            }
                            writer.append("</div><!-- file-list -->");
                            writer.append("<pre class=\"license-text\">");
                            writer.append(htmlEscapeLicense(license));
                            writer.append("</pre><!-- license-text -->");
                            writer.append("</td></tr><!-- same-license -->");
                        }
                    }
                    writer.append("</table>");
                    writer.append("</body></html>");
                    writer.flush();
                    writer.close();
                } catch (IOException e2) {
                    e = e2;
                    outputStreamWriter = writer;
                    if (outputStreamWriter != null) {
                        try {
                            outputStreamWriter.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    e.printStackTrace();
                }
            } catch (IOException e3) {
                e = e3;
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
                e.printStackTrace();
            }
        }

        private String htmlEscapeLicense(String license) {
            String result = license;
            return license.replace("&", "&amp;").replace("\"", "&quot;").replace("'", "&apos;").replace(">", "&gt;").replace("<", "&lt;");
        }
    }

    public LicenseHelper(Context ctx) {
        this.mContext = ctx;
    }

    public boolean isFlyHorse30() {
        if (new File(NOTICES_ARRAY[0]).exists()) {
            return true;
        }
        return false;
    }

    public void clearCache() {
        File fMd5 = new File(getLicenseMd5Map());
        if (fMd5.exists() && !fMd5.delete()) {
            Log.d("settings-LicenseHelper", "delete file fail: " + fMd5.getAbsolutePath());
        }
        File fHtml = new File(getLicenseHtml());
        if (fHtml.exists() && !fHtml.delete()) {
            Log.d("settings-LicenseHelper", "delete file fail: " + fHtml.getAbsolutePath());
        }
    }

    public String getLicenseMd5Map() {
        return this.mContext.getCacheDir().getAbsolutePath() + "/license_md5_map";
    }

    public String getLicenseHtml() {
        return this.mContext.getCacheDir().getAbsolutePath() + "/NOTICE-all.html";
    }

    public String rebuildLicenseFile() {
        NoticeParser parser = new NoticeParser();
        for (String path : NOTICES_ARRAY) {
            parser.parse(path);
        }
        parser.writeToHtml(getLicenseHtml());
        return null;
    }

    public boolean updateMd5IfNeed() {
        return updateMd5IfNeed(NOTICES_ARRAY, getLicenseMd5Map());
    }

    public boolean updateMd5IfNeed(String[] paths, String conFile) {
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            try {
                sb.append(path).append(":").append(md5(path)).append("\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String newMd5s = sb.toString();
        if (newMd5s.equals(readStringFromFile(conFile))) {
            return false;
        }
        writeStringToFile(conFile, newMd5s);
        return true;
    }

    private static void writeStringToFile(String path, String content) {
        IOException e;
        Throwable th;
        OutputStreamWriter outputStreamWriter = null;
        try {
            OutputStreamWriter writer = new OutputStreamWriter(openOutputStream(new File(path), false), "utf-8");
            try {
                writer.write(content);
                writer.flush();
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                outputStreamWriter = writer;
            } catch (IOException e3) {
                e2 = e3;
                outputStreamWriter = writer;
                try {
                    e2.printStackTrace();
                    if (outputStreamWriter != null) {
                        try {
                            outputStreamWriter.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (outputStreamWriter != null) {
                        try {
                            outputStreamWriter.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                outputStreamWriter = writer;
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
                throw th;
            }
        } catch (IOException e4) {
            e222 = e4;
            e222.printStackTrace();
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
        }
    }

    private static String readStringFromFile(String path) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        BufferedReader bufferedReader = null;
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "utf-8"));
            while (true) {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                    sb.append("\n");
                } catch (FileNotFoundException e3) {
                    e = e3;
                    bufferedReader = reader;
                } catch (IOException e4) {
                    e2 = e4;
                    bufferedReader = reader;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = reader;
                }
            }
            String stringBuilder = sb.toString();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
            return stringBuilder;
        } catch (FileNotFoundException e5) {
            e = e5;
            try {
                e.printStackTrace();
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e2222) {
                        e2222.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (IOException e6) {
            e2222 = e6;
            e2222.printStackTrace();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e22222) {
                    e22222.printStackTrace();
                }
            }
            return null;
        }
    }

    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (!(parent == null || parent.mkdirs() || parent.isDirectory())) {
                throw new IOException("Directory '" + parent + "' could not be created");
            }
        } else if (file.isDirectory()) {
            throw new IOException("File '" + file + "' exists but is a directory");
        } else if (!file.canWrite()) {
            throw new IOException("File '" + file + "' cannot be written to");
        }
        return new FileOutputStream(file, append);
    }

    private static byte[] createChecksum(String filename) {
        IOException e;
        FileNotFoundException e2;
        NoSuchAlgorithmException e3;
        Throwable th;
        byte[] buffer = new byte[1024];
        InputStream inputStream = null;
        try {
            InputStream fis = new FileInputStream(filename);
            try {
                MessageDigest complete = MessageDigest.getInstance("MD5");
                int numRead;
                do {
                    numRead = fis.read(buffer);
                    if (numRead > 0) {
                        complete.update(buffer, 0, numRead);
                    }
                } while (numRead != -1);
                byte[] digest = complete.digest();
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                return digest;
            } catch (FileNotFoundException e5) {
                e2 = e5;
                inputStream = fis;
                e2.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e42) {
                        e42.printStackTrace();
                    }
                }
                return new byte[0];
            } catch (NoSuchAlgorithmException e6) {
                e3 = e6;
                inputStream = fis;
                e3.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e422) {
                        e422.printStackTrace();
                    }
                }
                return new byte[0];
            } catch (IOException e7) {
                e422 = e7;
                inputStream = fis;
                try {
                    e422.printStackTrace();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e4222) {
                            e4222.printStackTrace();
                        }
                    }
                    return new byte[0];
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e42222) {
                            e42222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = fis;
                if (inputStream != null) {
                    inputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e8) {
            e2 = e8;
            e2.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
            return new byte[0];
        } catch (NoSuchAlgorithmException e9) {
            e3 = e9;
            e3.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
            return new byte[0];
        } catch (IOException e10) {
            e42222 = e10;
            e42222.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
            return new byte[0];
        }
    }

    private static String md5(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        if (b.length == 0) {
            throw new Exception("createChecksum is empty: " + filename);
        }
        StringBuilder sb = new StringBuilder();
        for (byte b2 : b) {
            sb.append(Integer.toString((b2 & 255) + 256, 16).substring(1));
        }
        return sb.toString();
    }
}
