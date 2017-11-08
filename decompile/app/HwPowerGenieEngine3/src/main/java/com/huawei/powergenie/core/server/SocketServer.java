package com.huawei.powergenie.core.server;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class SocketServer {
    private static int mClientCount = 0;
    private ConcurrentHashMap<LocalSocket, BufferedWriter> mClient = new ConcurrentHashMap();
    private LocalServerSocket mServerSocket = null;

    protected SocketServer() {
    }

    protected void start() {
        listenClientConnection();
    }

    protected boolean hasClients() {
        return mClientCount > 0;
    }

    private void listenClientConnection() {
        try {
            this.mServerSocket = new LocalServerSocket("pg-socket");
            new Thread() {
                public void run() {
                    while (SocketServer.mClientCount < 10) {
                        try {
                            Log.i("SocketServer", "Waiting for connection...");
                            LocalSocket socket = SocketServer.this.mServerSocket.accept();
                            Log.i("SocketServer", "Got connection socket: " + socket);
                            if (!(socket == null || SocketServer.this.mClient.containsKey(socket))) {
                                SocketServer.this.mClient.put(socket, new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()), 1024));
                                SocketServer.mClientCount = SocketServer.mClientCount + 1;
                                Log.i("SocketServer", "clients num:" + SocketServer.mClientCount);
                            }
                        } catch (IOException e) {
                            Log.e("SocketServer", "Error in PG-server accept: ", e);
                        }
                    }
                }
            }.start();
        } catch (IOException e) {
            Log.e("SocketServer", "error, making server socket !", e);
        }
    }

    protected boolean handleActionInner(int exportActionId, String pkgName, String extend1, String extend2) {
        if (mClientCount == 0) {
            return false;
        }
        if (exportActionId == 10018 || exportActionId == 10019) {
            return true;
        }
        dispatchAction(exportActionId, pkgName + "\t" + extend1 + "\t" + extend2);
        return true;
    }

    private void dispatchAction(int action, String value) {
        String formatMsg = action + "|" + value;
        for (Entry entry : this.mClient.entrySet()) {
            sendMessage((LocalSocket) entry.getKey(), (BufferedWriter) entry.getValue(), formatMsg);
        }
    }

    private void sendMessage(LocalSocket socket, BufferedWriter bw, String msg) {
        if (socket != null && bw != null) {
            try {
                bw.write(msg, 0, msg.length());
                bw.newLine();
                bw.flush();
            } catch (IOException e) {
                Log.e("SocketServer", "sendMessage error, LocalSocket:" + socket + " BufferedWriter:" + bw, e);
                this.mClient.remove(socket);
                mClientCount--;
                try {
                    bw.close();
                } catch (IOException ioe) {
                    Log.e("SocketServer", "close bw error", ioe);
                }
            }
        }
    }
}
