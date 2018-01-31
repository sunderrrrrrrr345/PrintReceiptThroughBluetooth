package com.example.mylibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
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
import java.util.UUID;

public class HsBluetoothPrintDriver extends BasePrintCmd implements Contants {
    private static HsBluetoothPrintDriver mBluetoothPrintDriver;
    private final UUID MY_UUID;
    private final String NAME;
    private final String TAG;
    byte buf;
    byte buf2;
    private AcceptThread mAcceptThread;
    private BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private Context mContext;
    private Handler mHandler;
    private PrintImageTask mPrintImageTask;
    private BluetoothSocket pBluetoothSocket;

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = HsBluetoothPrintDriver.this.mAdapter.listenUsingRfcommWithServiceRecord("BluetoothChatService", HsBluetoothPrintDriver.this.MY_UUID);
            } catch (Exception e) {
                Log.e("HsBluetoothPrintDriver", "listen() failed", e);
            }
            this.mmServerSocket = tmp;
        }

        public void run() {
            setName("AcceptThread");
            while (HsBluetoothPrintDriver.this.mState != 1) {
                try {
                    BluetoothSocket socket = this.mmServerSocket.accept();
                    if (socket != null) {
                        synchronized (HsBluetoothPrintDriver.this) {
                            switch (HsBluetoothPrintDriver.this.mState) {
                                case 0:
                                case 1:
                                    try {
                                        socket.close();
                                        break;
                                    } catch (IOException e) {
                                        break;
                                    }
                                case 2:
                                case 3:
                                    HsBluetoothPrintDriver.this.connected(socket, socket.getRemoteDevice());
                                    break;
                            }
                        }
                    }
                } catch (IOException e2) {
                    return;
                }
            }
            return;
        }

        public void cancel() {
            try {
                this.mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(HsBluetoothPrintDriver.this.MY_UUID);
            } catch (Exception e) {
            }
            this.mmSocket = tmp;
        }

        public void run() {
            setName("ConnectThread");
            HsBluetoothPrintDriver.this.mAdapter.cancelDiscovery();
            try {
                this.mmSocket.connect();
                synchronized (HsBluetoothPrintDriver.this) {
                    HsBluetoothPrintDriver.this.mConnectThread = null;
                }
                HsBluetoothPrintDriver.this.connected(this.mmSocket, this.mmDevice);
            } catch (IOException e) {
                HsBluetoothPrintDriver.this.sendMessageToMainThread(33);
                try {
                    this.mmSocket.close();
                } catch (IOException e2) {
                }
                HsBluetoothPrintDriver.this.start();
            }
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket) {
            this.mmSocket = socket;
            HsBluetoothPrintDriver.this.pBluetoothSocket = socket;
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
                HsBluetoothPrintDriver.this.buf = (byte) 0;
                for (int i = 0; i < 120; i++) {
                    Log.e("cur", "cur=" + i);
                    sleep(50);
                    if (this.mmInStream.available() != 0) {
                        HsBluetoothPrintDriver.this.buf = (byte) this.mmInStream.read();
                        Log.e("buf", "buf=" + HsBluetoothPrintDriver.this.buf);
                        break;
                    }
                }
                Log.e("buf", "buf111=" + HsBluetoothPrintDriver.this.buf);
            } catch (IOException e) {
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }

        public void read(int handlerSign, Handler mhandler) {
            try {
                HsBluetoothPrintDriver.this.buf = (byte) 0;
                for (int i = 0; i < 200; i++) {
                    Log.e("buf", "buf=" + i);
                    sleep(50);
                    if (this.mmInStream.available() != 0) {
                        HsBluetoothPrintDriver.this.buf = (byte) this.mmInStream.read();
                        Log.e("buf", "buf=" + HsBluetoothPrintDriver.this.buf);
                        break;
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putInt("flag", handlerSign);
                bundle.putInt("state", HsBluetoothPrintDriver.this.buf);
                Message msg = new Message();
                msg.setData(bundle);
                mhandler.sendMessage(msg);
            } catch (IOException e) {
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }

        public void write(byte[] buffer) {
            try {
                this.mmOutStream.write(buffer);
            } catch (IOException e) {
            }
        }

        public void write(byte[] buffer, int dataLen) {
            int i = 0;
            while (i < dataLen) {
                try {
                    this.mmOutStream.write(buffer[i]);
                    i++;
                } catch (IOException e) {
                    return;
                }
            }
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public class ReadStateTask extends AsyncTask<Void, Void, Byte> {
        private int handlerSign;
        private Handler mhandler;
        private InputStream minputStream = null;

        public ReadStateTask(int HandlerSign, Handler mHandler) {
            this.handlerSign = HandlerSign;
            this.mhandler = mHandler;
            try {
                this.minputStream = HsBluetoothPrintDriver.this.pBluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        protected Byte doInBackground(Void... avoid) {
            HsBluetoothPrintDriver.this.buf = (byte) 0;
            for (int i = 0; i < 200; i++) {
                try {
                    Thread.currentThread();
                    Thread.sleep(50);
                    if (this.minputStream.available() != 0) {
                        HsBluetoothPrintDriver.this.buf = (byte) this.minputStream.read();
                        if ((HsBluetoothPrintDriver.this.buf & 255) == 128) {
                            break;
                        }
                    } else {
                        continue;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            return Byte.valueOf(HsBluetoothPrintDriver.this.buf);
        }

        protected void onPostExecute(Byte byt) {
            super.onPostExecute(byt);
            Log.d("byt------", byt + "");
            Bundle bundle = new Bundle();
            bundle.putInt("flag", this.handlerSign);
            bundle.putInt("state", byt.byteValue());
            Message msg = new Message();
            msg.setData(bundle);
            this.mhandler.sendMessage(msg);
        }
    }

    public HsBluetoothPrintDriver() {
        this.TAG = "HsBluetoothPrintDriver";
        this.NAME = "BluetoothChatService";
        this.MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        this.mState = 0;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static HsBluetoothPrintDriver getInstance() {
        if (mBluetoothPrintDriver == null) {
            mBluetoothPrintDriver = new HsBluetoothPrintDriver();
        }
        return mBluetoothPrintDriver;
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
        BT_Write(dataString);
    }

    public void WriteCmd(String dataString, boolean bGBK) {
        BT_Write(dataString, bGBK);
    }

    public void WriteCmd(byte[] out) {
        BT_Write(out);
    }

    public void WriteCmd(byte[] out, int dataLen) {
        BT_Write(out, dataLen);
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

    public synchronized int getState() {
        return this.mState;
    }

    private synchronized void setState(int state) {
        System.out.println("setState()= " + this.mState + " -> " + state);
        this.mState = state;
        switch (this.mState) {
            case 0:
                sendMessageToMainThread(32, 16);
                break;
            case 1:
                sendMessageToMainThread(32, 17);
                break;
        }
    }

    public synchronized void start() {
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread == null) {
            this.mAcceptThread = new AcceptThread();
            this.mAcceptThread.start();
        }
        setState(3);
    }

    public synchronized void connect(BluetoothDevice device) {
        if (this.mState == 2 && this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        this.mConnectThread = new ConnectThread(device);
        this.mConnectThread.start();
        setState(2);
    }

    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }
        this.mConnectedThread = new ConnectedThread(socket);
        this.mConnectedThread.start();
        sendMessageToMainThread(34);
        setState(1);
    }

    public synchronized void stop() {
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }
        setState(0);
    }

    public void write(byte[] out) {
        synchronized (this) {
            if (this.mState != 1) {
                return;
            }
            ConnectedThread r = this.mConnectedThread;
            r.write(out);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void write2(byte[] r5) throws IOException {
        /*
        r4 = this;
        monitor-enter(r4);
        r2 = r4.mState;	 Catch:{ all -> 0x001b }
        r3 = 1;
        if (r2 == r3) goto L_0x0008;
    L_0x0006:
        monitor-exit(r4);	 Catch:{ all -> 0x001b }
    L_0x0007:
        return;
    L_0x0008:
        r1 = r4.mConnectedThread;	 Catch:{ all -> 0x001b }
        monitor-exit(r4);	 Catch:{ all -> 0x001b }
        r0 = 0;
    L_0x000c:
        r2 = r5.length;
        if (r0 >= r2) goto L_0x0007;
    L_0x000f:
        r2 = r1.mmOutStream;
        r3 = r5[r0];
        r2.write(r3);
        r0 = r0 + 1;
        goto L_0x000c;
    L_0x001b:
        r2 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x001b }
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.rtdriver.driver.HsBluetoothPrintDriver.write2(byte[]):void");
    }

    public void setCharsetName(String charsetName) {
        if (charsetName != null) {
            this.mCharsetName = charsetName;
        }
    }

    public void BT_Write(String dataString) {
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

    public void BT_Write(String dataString, boolean bGBK) {
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

    public void BT_Write(byte[] out) {
        if (this.mState == 1) {
            this.mConnectedThread.write(out);
        }
    }

    public byte BT_read() {
        if (this.mState != 1) {
            this.mConnectedThread.read();
        } else {
            this.mConnectedThread.read();
        }
        return this.buf;
    }

    public void BT_read(int handlerSign, Handler mhandler) {
        if (this.mState != 1) {
            this.mConnectedThread.read(handlerSign, mhandler);
        } else {
            this.mConnectedThread.read(handlerSign, mhandler);
        }
    }

    public void BT_Write(byte[] out, int dataLen) {
        if (this.mState == 1) {
            this.mConnectedThread.write(out, dataLen);
        }
    }

    public boolean IsNoConnection() {
        return this.mState != 1;
    }

    public byte StatusInquiry(byte type) {
        byte[] cmd = new byte[]{(byte) 16, (byte) 4, (byte) 1};
        cmd[2] = type;
        BT_Write(cmd, 3);
        return BT_read();
    }

    public byte StatusInquiryFinish() {
        BT_Write(new byte[]{(byte) 31, (byte) 97, (byte) 1}, 3);
        return BT_read();
    }

    public void StatusInquiryFinish(int handlerSign, Handler mhandler) {
        if (this.mState == 1) {
            Bundle bundle = new Bundle();
            bundle.putInt("flag", handlerSign);
            bundle.putInt("state", 0);
            Message msg = new Message();
            msg.setData(bundle);
            mhandler.sendMessage(msg);
            SystemClock.sleep(1500);
            BT_Write(new byte[]{(byte) 31, (byte) 97, (byte) 1}, 3);
            new ReadStateTask(handlerSign, mhandler).execute(new Void[0]);
        }
    }
}
