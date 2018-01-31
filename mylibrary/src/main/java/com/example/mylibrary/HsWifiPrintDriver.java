package com.example.mylibrary;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class HsWifiPrintDriver extends BasePrintCmd implements Contants {
    private static HsWifiPrintDriver mWifiPrintDriver;
    private final String TAG;
    byte buf;
    private ConnectedThread mConnectedThread;
    private Handler mHandler;
    private ReadStateTask mReadStateTask;
    public InputStream mWifiInputStream;
    public OutputStream mWifiOutputStream;
    public Socket mysocket;

    class C02391 implements Runnable {
        C02391() {
        }

        public void run() {
            HsWifiPrintDriver.this.mConnectedThread.cancel();
            HsWifiPrintDriver.this.mConnectedThread = null;
        }
    }

    private class ConnectedReadThread extends Thread {
        private final InputStream mmInStream;

        public ConnectedReadThread(InputStream inStream) {
            this.mmInStream = inStream;
        }

        public void run() {
            try {
                HsWifiPrintDriver.this.buf = (byte) 0;
                for (int i = 0; i < 100; i++) {
                    sleep(50);
                    Log.e("buf", "bufx1x=" + i);
                    if (this.mmInStream.available() != 0) {
                        Log.e("buf", "bufxx=" + this.mmInStream.available());
                        HsWifiPrintDriver.this.buf = (byte) this.mmInStream.read();
                        Log.e("buf", "buf=" + HsWifiPrintDriver.this.buf);
                        return;
                    }
                }
            } catch (IOException e) {
                HsWifiPrintDriver.getInstance().stop();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        public void read() {
            try {
                HsWifiPrintDriver.this.buf = (byte) 0;
                for (int i = 0; i < 40; i++) {
                    sleep(50);
                    if (this.mmInStream.available() != 0) {
                        HsWifiPrintDriver.this.buf = (byte) this.mmInStream.read();
                        Log.e("buf", "buf=" + HsWifiPrintDriver.this.buf);
                        return;
                    }
                }
            } catch (IOException e) {
                HsWifiPrintDriver.getInstance().stop();
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final Socket wifiSocket;

        public ConnectedThread(Socket socket) {
            this.wifiSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
        }

        public void read() {
            try {
                HsWifiPrintDriver.this.buf = (byte) 0;
                for (int i = 0; i < 80; i++) {
                    sleep(50);
                    if (this.mmInStream.available() != 0) {
                        HsWifiPrintDriver.this.buf = (byte) this.mmInStream.read();
                        Log.e("buf", "buf=" + HsWifiPrintDriver.this.buf);
                        return;
                    }
                }
            } catch (IOException e) {
                HsWifiPrintDriver.getInstance().stop();
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }

        public void write(byte[] buffer) {
            try {
                HsWifiPrintDriver.this.mWifiOutputStream.write(buffer);
            } catch (IOException e) {
                HsWifiPrintDriver.getInstance().stop();
            }
        }

        public void write(byte[] buffer, int dataLen) {
            int i = 0;
            while (i < dataLen) {
                try {
                    HsWifiPrintDriver.this.mWifiOutputStream.write(buffer[i]);
                    i++;
                } catch (IOException e) {
                    HsWifiPrintDriver.getInstance().stop();
                    return;
                }
            }
        }

        public void cancel() {
            try {
                HsWifiPrintDriver.this.mysocket.close();
            } catch (IOException e) {
            }
        }
    }

    public class ReadStateTask extends AsyncTask<Void, Void, Byte> {
        private int handlerSign;
        private Handler mhandler;
        private InputStream minputStream = null;

        public ReadStateTask(int HandlerSign, Handler mHandler, InputStream inStream) {
            this.handlerSign = HandlerSign;
            this.mhandler = mHandler;
            this.minputStream = inStream;
        }

        protected Byte doInBackground(Void... avoid) {
            HsWifiPrintDriver.this.buf = (byte) 0;
            for (int i = 0; i < 100; i++) {
                try {
                    Thread.currentThread();
                    Thread.sleep(50);
                    if (this.minputStream.available() != 0) {
                        HsWifiPrintDriver.this.buf = (byte) this.minputStream.read();
                        int bb = HsWifiPrintDriver.this.buf & 255;
                        if (bb == 128) {
                            Log.d("bb------", bb + "");
                            break;
                        }
                    } else {
                        continue;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                    HsWifiPrintDriver.getInstance().stop();
                }
            }
            return Byte.valueOf(HsWifiPrintDriver.this.buf);
        }

        protected void onPostExecute(Byte byt) {
            super.onPostExecute(byt);
            Bundle bundle = new Bundle();
            bundle.putInt("flag", this.handlerSign);
            bundle.putInt("state", byt.byteValue());
            Message msg = new Message();
            msg.setData(bundle);
            this.mhandler.sendMessage(msg);
        }
    }

    public HsWifiPrintDriver() {
        this.TAG = "HsWifiPrintDriver";
        this.mysocket = null;
        this.mWifiOutputStream = null;
        this.mWifiInputStream = null;
        this.mState = 0;
    }

    public static HsWifiPrintDriver getInstance() {
        if (mWifiPrintDriver == null) {
            mWifiPrintDriver = new HsWifiPrintDriver();
        }
        return mWifiPrintDriver;
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    private void sendMessageToMainThread(int flag) {
        if (this.mHandler != null) {
            Message message = this.mHandler.obtainMessage();
            Bundle data = new Bundle();
            data.putInt("flag", flag);
            message.setData(data);
            this.mHandler.sendMessage(message);
        }
    }

    public void WriteCmd(String dataString) {
        WIFI_Write(dataString);
    }

    public void WriteCmd(String dataString, boolean bGBK) {
        WIFI_Write(dataString, bGBK);
    }

    public void WriteCmd(byte[] out) {
        WIFI_Write(out);
    }

    public void WriteCmd(byte[] out, int dataLen) {
        WIFI_Write(out, dataLen);
    }

    private void sendMessageToMainThread(int flag, int state) {
        if (this.mHandler != null) {
            Message message = this.mHandler.obtainMessage();
            Bundle data = new Bundle();
            data.putInt("flag", flag);
            data.putInt("state", state);
            message.setData(data);
            this.mHandler.sendMessage(message);
        }
    }

    private synchronized void setState(int state) {
        this.mState = state;
        switch (this.mState) {
            case 0:
                sendMessageToMainThread(33, 16);
                break;
            case 1:
                sendMessageToMainThread(32, 19);
                break;
        }
    }

    public synchronized void stop() {
        if (this.mConnectedThread != null) {
            new Thread(new C02391()).start();
        }
        setState(0);
    }

    public boolean WIFISocket(String ip, int port) {
        boolean error = false;
        if (this.mysocket != null) {
            try {
                this.mysocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.mysocket = null;
        }
        try {
            this.mysocket = new Socket();
            this.mysocket.connect(new InetSocketAddress(ip, port), 3000);
            if (this.mysocket != null) {
                this.mWifiOutputStream = this.mysocket.getOutputStream();
                this.mWifiInputStream = this.mysocket.getInputStream();
                this.mConnectedThread = new ConnectedThread(this.mysocket);
                this.mConnectedThread.start();
                setState(1);
            } else {
                this.mWifiOutputStream = null;
                this.mWifiInputStream = null;
                error = true;
            }
            if (error) {
                stop();
                return false;
            }
            sendMessageToMainThread(34);
            return true;
        } catch (IOException e2) {
            e2.printStackTrace();
            sendMessageToMainThread(33);
            return false;
        }
    }

    public void setCharsetName(String charsetName) {
        if (charsetName != null) {
            this.mCharsetName = charsetName;
        }
    }

    public void WIFI_Write(String dataString) {
        byte[] data = null;
        if (this.mState == 1) {
            ConnectedThread r = this.mConnectedThread;
            try {
                data = dataString.getBytes(this.mCharsetName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            r.write(data);
        }
    }

    public void setDhcp(boolean enable) {
        cmd = new byte[4];
        int i = 0 + 1;
        cmd[0] = (byte) 31;
        int i2 = i + 1;
        cmd[i] = (byte) 98;
        i = i2 + 1;
        cmd[i2] = (byte) 68;
        if (enable) {
            i2 = i + 1;
            cmd[i] = (byte) 1;
        } else {
            i2 = i + 1;
            cmd[i] = (byte) 0;
        }
        WIFI_Write(cmd);
    }

    public void setStaticIp(String ip, String subMask, String gateWay) {
        byte[] cmd = new byte[20];
        String[] ip_split = ip.split("\\.");
        String[] mask_split = subMask.split("\\.");
        String[] gateWay_split = gateWay.split("\\.");
        int i = 0 + 1;
        cmd[0] = (byte) 31;
        int i2 = i + 1;
        cmd[i] = (byte) 105;
        i = i2 + 1;
        cmd[i2] = (byte) Short.parseShort(ip_split[0]);
        i2 = i + 1;
        cmd[i] = (byte) Short.parseShort(ip_split[1]);
        i = i2 + 1;
        cmd[i2] = (byte) Short.parseShort(ip_split[2]);
        i2 = i + 1;
        cmd[i] = (byte) Short.parseShort(ip_split[3]);
        i = i2 + 1;
        cmd[i2] = (byte) 31;
        i2 = i + 1;
        cmd[i] = (byte) 37;
        i = i2 + 1;
        cmd[i2] = (byte) 0;
        i2 = i + 1;
        cmd[i] = (byte) Short.parseShort(mask_split[0]);
        i = i2 + 1;
        cmd[i2] = (byte) Short.parseShort(mask_split[0]);
        i2 = i + 1;
        cmd[i] = (byte) Short.parseShort(mask_split[0]);
        i = i2 + 1;
        cmd[i2] = (byte) Short.parseShort(mask_split[0]);
        i2 = i + 1;
        cmd[i] = (byte) 31;
        i = i2 + 1;
        cmd[i2] = (byte) 37;
        i2 = i + 1;
        cmd[i] = (byte) 1;
        i = i2 + 1;
        cmd[i2] = (byte) Short.parseShort(gateWay_split[0]);
        i2 = i + 1;
        cmd[i] = (byte) Short.parseShort(gateWay_split[0]);
        i = i2 + 1;
        cmd[i2] = (byte) Short.parseShort(gateWay_split[0]);
        i2 = i + 1;
        cmd[i] = (byte) Short.parseShort(gateWay_split[0]);
        WIFI_Write(cmd);
    }

    private void setOpen(String ssid, byte WifiType) {
        byte[] cmd = new byte[(((ssid.length() + 3) + 3) + 1)];
        int i = 0 + 1;
        cmd[0] = (byte) 31;
        int i2 = i + 1;
        cmd[i] = (byte) 119;
        i = i2 + 1;
        cmd[i2] = (byte) ssid.length();
        int i3 = 0;
        i2 = i;
        while (i3 < ssid.length()) {
            i = i2 + 1;
            cmd[i2] = (byte) ssid.charAt(i3);
            i3++;
            i2 = i;
        }
        i = i2 + 1;
        cmd[i2] = (byte) 0;
        i2 = i + 1;
        cmd[i] = (byte) 0;
        i = i2 + 1;
        cmd[i2] = (byte) 0;
        if (WifiType == (byte) 0) {
            i2 = i + 1;
            cmd[i] = (byte) 0;
        } else {
            i2 = i + 1;
            cmd[i] = (byte) 1;
        }
        WIFI_Write(cmd);
    }

    private void setWPA(String ssid, String password, byte WifiType, byte WPAType, byte WPAEncryType) {
        byte[] cmd = new byte[((((ssid.length() + 3) + 3) + password.length()) + 1)];
        int i = 0 + 1;
        cmd[0] = (byte) 31;
        int i2 = i + 1;
        cmd[i] = (byte) 119;
        i = i2 + 1;
        cmd[i2] = (byte) ssid.length();
        int i3 = 0;
        i2 = i;
        while (i3 < ssid.length()) {
            i = i2 + 1;
            cmd[i2] = (byte) ssid.charAt(i3);
            i3++;
            i2 = i;
        }
        if (WPAType == (byte) 0) {
            i = i2 + 1;
            cmd[i2] = (byte) 2;
        } else {
            i = i2 + 1;
            cmd[i2] = (byte) 3;
        }
        if (WPAEncryType == (byte) 0) {
            i2 = i + 1;
            cmd[i] = (byte) 1;
        } else {
            i2 = i + 1;
            cmd[i] = (byte) 0;
        }
        i = i2 + 1;
        cmd[i2] = (byte) password.length();
        i3 = 0;
        i2 = i;
        while (i3 < password.length()) {
            i = i2 + 1;
            cmd[i2] = (byte) password.charAt(i3);
            i3++;
            i2 = i;
        }
        if (WifiType == (byte) 0) {
            i = i2 + 1;
            cmd[i2] = (byte) 0;
            i2 = i;
        } else {
            i = i2 + 1;
            cmd[i2] = (byte) 1;
            i2 = i;
        }
        WIFI_Write(cmd);
    }

    private void setWEP(String ssid, String password, byte WifiType, byte WEPType) {
        byte[] cmd = new byte[((((ssid.length() + 3) + 3) + password.length()) + 1)];
        int i = 0 + 1;
        cmd[0] = (byte) 31;
        int i2 = i + 1;
        cmd[i] = (byte) 119;
        i = i2 + 1;
        cmd[i2] = (byte) ssid.length();
        int i3 = 0;
        i2 = i;
        while (i3 < ssid.length()) {
            i = i2 + 1;
            cmd[i2] = (byte) ssid.charAt(i3);
            i3++;
            i2 = i;
        }
        if (WEPType == (byte) 0) {
            i = i2 + 1;
            cmd[i2] = (byte) 0;
            i2 = i + 1;
            cmd[i] = (byte) 1;
        } else {
            i = i2 + 1;
            cmd[i2] = (byte) 1;
            i2 = i + 1;
            cmd[i] = (byte) 0;
        }
        i = i2 + 1;
        cmd[i2] = (byte) password.length();
        i3 = 0;
        i2 = i;
        while (i3 < password.length()) {
            i = i2 + 1;
            cmd[i2] = (byte) password.charAt(i3);
            i3++;
            i2 = i;
        }
        if (WifiType == (byte) 0) {
            i = i2 + 1;
            cmd[i2] = (byte) 0;
            i2 = i;
        } else {
            i = i2 + 1;
            cmd[i2] = (byte) 1;
            i2 = i;
        }
        WIFI_Write(cmd);
    }

    public void setWifiParam(String ssid, String password, byte type, byte WPAType, byte WPAEncryType, byte WEPType, byte WifiType) {
        if (ssid != null && ssid.length() >= 0) {
            if (type == (byte) 0) {
                try {
                    setOpen(ssid, WifiType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ((byte) 1 == type) {
                setWPA(ssid, password, WifiType, WPAType, WPAEncryType);
            } else {
                setWEP(ssid, password, WifiType, WEPType);
            }
        }
    }

    public void WIFI_Write(String dataString, boolean bGBK) {
        byte[] data = null;
        if (this.mState == 1) {
            ConnectedThread r = this.mConnectedThread;
            if (bGBK) {
                try {
                    data = dataString.getBytes("GBK");
                } catch (UnsupportedEncodingException e) {
                }
            } else {
                try {
                    data = dataString.getBytes(this.mCharsetName);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            r.write(data);
        }
    }

    public void WIFI_Write(byte[] out) {
        if (this.mState == 1) {
            this.mConnectedThread.write(out);
        }
    }

    public void WIFI_Write(byte[] out, int dataLen) {
        if (this.mState == 1) {
            this.mConnectedThread.write(out, dataLen);
        }
    }

    public byte WIFI_read() {
        new ConnectedReadThread(this.mWifiInputStream).start();
        return this.buf;
    }

    public boolean IsNoConnection() {
        if (this.mState != 1) {
            return true;
        }
        return false;
    }

    public boolean IsNoConnection(String ip) {
        if (this.mysocket == null) {
            return true;
        }
        try {
            if (Runtime.getRuntime().exec("ping -c 3 -w 5 " + ip).waitFor() == 0) {
                Log.d("fail=====", "状态--连接");
                return false;
            }
        } catch (IOException e) {
        } catch (InterruptedException e2) {
        }
        Log.d("fail=====", "状态--未连接");
        setState(0);
        return true;
    }

    public byte StatusInquiry(byte type) {
        byte[] cmd = new byte[]{(byte) 16, (byte) 4, (byte) 1};
        cmd[2] = type;
        WIFI_Write(cmd, 3);
        return WIFI_read();
    }

    public byte StatusInquiryFinish() {
        WIFI_Write(new byte[]{(byte) 31, (byte) 97, (byte) 1}, 3);
        return WIFI_read();
    }

    public void StatusInquiryFinish(int handlerSign, Handler mHandler) {
        if (this.mysocket != null && this.mState == 1) {
            Bundle bundle = new Bundle();
            bundle.putInt("flag", handlerSign);
            bundle.putInt("state", 0);
            Message msg = new Message();
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            SystemClock.sleep(1500);
            WIFI_Write(new byte[]{(byte) 31, (byte) 97, (byte) 1}, 3);
            new ReadStateTask(handlerSign, mHandler, this.mWifiInputStream).execute(new Void[0]);
        }
    }
}
