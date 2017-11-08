package com.fyusion.sdk.viewer.internal.e;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/* compiled from: Unknown */
public abstract class a<T> {
    public T a(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                stringBuilder.append(readLine);
            }
            T a = a(stringBuilder.toString());
            return a;
        } finally {
            bufferedReader.close();
        }
    }

    abstract T a(String str);
}
