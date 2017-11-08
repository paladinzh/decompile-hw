package com.android.settings.deviceinfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class CpuManager {
    public static String getMaxCpuFreq() {
        int cpuNum = getCpuCount();
        float maxCpuFreq = 0.0f;
        String sMaxCpuFreq = "N/A";
        for (int cpuIndex = 0; cpuIndex < cpuNum; cpuIndex++) {
            String sCpuFrep = getCpuFrep(cpuIndex);
            if (!"N/A".equals(sCpuFrep)) {
                try {
                    float temp = Float.parseFloat(sCpuFrep);
                    if (temp > maxCpuFreq) {
                        maxCpuFreq = temp;
                        sMaxCpuFreq = sCpuFrep;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return sMaxCpuFreq;
    }

    private static String getCpuFrep(int cpuIndex) {
        String sCpuIndex = "cpu" + cpuIndex;
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        try {
            inputStream = new ProcessBuilder(new String[]{"/system/bin/cat", "/sys/devices/system/cpu/" + sCpuIndex + "/cpufreq/cpuinfo_max_freq"}).start().getInputStream();
            byte[] re = new byte[24];
            while (inputStream.read(re) != -1) {
                sb.append(new String(re, "UTF-8"));
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (IOException e32) {
            e32.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            }
        }
        if (sb.length() == 0) {
            sb.append("N/A");
        }
        return sb.toString().trim();
    }

    public static int getCpuCount() {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        int cpuCount = 0;
        ArrayList<String> list = new ArrayList();
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        FileInputStream fileInputStream = null;
        try {
            FileInputStream fis = new FileInputStream("/proc/cpuinfo");
            try {
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                try {
                    BufferedReader br = new BufferedReader(isr);
                    while (true) {
                        try {
                            String text = br.readLine();
                            if (text == null) {
                                break;
                            }
                            String[] array = text.split(":\\s+", 2);
                            if ("processor".equals(array[0].trim())) {
                                list.add(array[1]);
                            }
                        } catch (FileNotFoundException e3) {
                            e = e3;
                            fileInputStream = fis;
                            bufferedReader = br;
                            inputStreamReader = isr;
                        } catch (IOException e4) {
                            e2 = e4;
                            fileInputStream = fis;
                            bufferedReader = br;
                            inputStreamReader = isr;
                        } catch (Throwable th2) {
                            th = th2;
                            fileInputStream = fis;
                            bufferedReader = br;
                            inputStreamReader = isr;
                        }
                    }
                    for (int i = 0; i < list.size(); i++) {
                        cpuCount = Math.max(cpuCount, Integer.parseInt((String) list.get(i)));
                    }
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (isr != null) {
                        try {
                            isr.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e2222) {
                            e2222.printStackTrace();
                        }
                    }
                } catch (FileNotFoundException e5) {
                    e = e5;
                    fileInputStream = fis;
                    inputStreamReader = isr;
                    try {
                        e.printStackTrace();
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e22222) {
                                e22222.printStackTrace();
                            }
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (IOException e222222) {
                                e222222.printStackTrace();
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e2222222) {
                                e2222222.printStackTrace();
                            }
                        }
                        return cpuCount + 1;
                    } catch (Throwable th3) {
                        th = th3;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e22222222) {
                                e22222222.printStackTrace();
                            }
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (IOException e222222222) {
                                e222222222.printStackTrace();
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e2222222222) {
                                e2222222222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (IOException e6) {
                    e2222222222 = e6;
                    fileInputStream = fis;
                    inputStreamReader = isr;
                    e2222222222.printStackTrace();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e22222222222) {
                            e22222222222.printStackTrace();
                        }
                    }
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e222222222222) {
                            e222222222222.printStackTrace();
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e2222222222222) {
                            e2222222222222.printStackTrace();
                        }
                    }
                    return cpuCount + 1;
                } catch (Throwable th4) {
                    th = th4;
                    fileInputStream = fis;
                    inputStreamReader = isr;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e7) {
                e = e7;
                fileInputStream = fis;
                e.printStackTrace();
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return cpuCount + 1;
            } catch (IOException e8) {
                e2222222222222 = e8;
                fileInputStream = fis;
                e2222222222222.printStackTrace();
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return cpuCount + 1;
            } catch (Throwable th5) {
                th = th5;
                fileInputStream = fis;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            e = e9;
            e.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return cpuCount + 1;
        } catch (IOException e10) {
            e2222222222222 = e10;
            e2222222222222.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return cpuCount + 1;
        }
        return cpuCount + 1;
    }
}
