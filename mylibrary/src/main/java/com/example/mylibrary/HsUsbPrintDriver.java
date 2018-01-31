package com.example.mylibrary;

import android.app.PendingIntent;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.usbsdk.pos.Pos;
import com.usbsdk.rw.PL2303Driver;
import com.usbsdk.rw.TTYTermios;
import com.usbsdk.rw.TTYTermios.FlowControl;
import com.usbsdk.rw.TTYTermios.Parity;
import com.usbsdk.rw.TTYTermios.StopBits;
import com.usbsdk.rw.USBPort;
import com.usbsdk.rw.USBSerialPort;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class HsUsbPrintDriver extends BasePrintCmd implements Contants {
    protected static Pos mPos;
    protected static PL2303Driver mSerial;
    protected static HsUsbPrintDriver mUsbPrintDriver;
    protected static USBSerialPort serialPort;
    private final String TAG;
    private ConnectedThread mConnectedThread;
    private Handler mHandler;
    private UsbManager mUsbManager;

    private class ConnectedThread extends Thread {
        private UsbEndpoint mInEndpoint;
        private UsbEndpoint mOutEndpoint;
        private UsbDeviceConnection mUsbDeviceConnection;
        private UsbInterface mUsbInterface;
        private boolean stop = false;

        public ConnectedThread(UsbDeviceConnection usbDeviceConnection, UsbInterface usbInterface) {
            super("ConnectedThread");
            this.mUsbDeviceConnection = usbDeviceConnection;
            this.mUsbInterface = usbInterface;
            this.mInEndpoint = this.mUsbInterface.getEndpoint(0);
            this.mOutEndpoint = this.mUsbInterface.getEndpoint(1);
            this.stop = false;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            while (!this.stop) {
                synchronized (this) {
                    this.mUsbDeviceConnection.bulkTransfer(this.mInEndpoint, buffer, buffer.length, 3000);
                }
            }
        }

        public void write(byte[] bytes) {
            int start = 0;
            int end = 0;
            while (end != bytes.length) {
                end = start + 16384 < bytes.length ? start + 16384 : bytes.length;
                byte[] temp = Arrays.copyOfRange(bytes, start, end);
                int transferLen = this.mUsbDeviceConnection.bulkTransfer(this.mOutEndpoint, temp, temp.length, 3000);
                start = end;
            }
        }

        public void cancel() {
            this.stop = true;
            if (this.mUsbDeviceConnection != null) {
                if (this.mUsbInterface != null) {
                    this.mUsbDeviceConnection.releaseInterface(this.mUsbInterface);
                    this.mUsbInterface = null;
                }
                this.mUsbDeviceConnection.close();
                this.mUsbDeviceConnection = null;
            }
        }
    }

    public HsUsbPrintDriver() {
        this.TAG = "HsUsbPrintDriver";
        this.mState = 0;
    }

    public static HsUsbPrintDriver getInstance() {
        if (mUsbPrintDriver == null) {
            mUsbPrintDriver = new HsUsbPrintDriver();
            if (mSerial == null) {
                mSerial = new PL2303Driver();
                serialPort = new USBSerialPort(null, null);
                mPos = new Pos(serialPort, mSerial);
            }
        }
        return mUsbPrintDriver;
    }

    public void setUsbManager(UsbManager usbManager) {
        this.mUsbManager = usbManager;
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
        USB_Write(dataString);
    }

    public void WriteCmd(String dataString, boolean bGBK) {
        USB_Write(dataString, bGBK);
    }

    public void WriteCmd(byte[] out) {
        USB_Write(out);
    }

    public void WriteCmd(byte[] out, int dataLen) {
        USB_Write(out, dataLen);
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
                sendMessageToMainThread(32, 16);
                break;
            case 1:
                sendMessageToMainThread(32, 18);
                break;
        }
    }

    public synchronized void stop() {
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        setState(0);
        disconnect();
    }

    public boolean connect(UsbDevice usbDevice, Context context, PendingIntent mPermissionIntent) {
        if (this.mUsbManager == null) {
            throw new UsbManagerNotInitedException("the UsbManager has not been set,please invoke setUsbManager mothod to set UsbManager");
        }
        serialPort.port = new USBPort(this.mUsbManager, context, usbDevice, mPermissionIntent);
        if (mSerial.pl2303_probe(serialPort) == 0) {
            sendMessageToMainThread(34);
            setState(1);
            return true;
        }
        sendMessageToMainThread(33);
        return false;
    }

    private int open(int baudrate, Parity parity) {
        TTYTermios termios = serialPort.termios;
        serialPort.termios = new TTYTermios(baudrate, FlowControl.NONE, parity, StopBits.ONE, 8);
        return mSerial.pl2303_open(serialPort, termios);
    }

    public void setCharsetName(String charsetName) {
        if (charsetName != null) {
            this.mCharsetName = charsetName;
        }
    }

    public void USB_Write(String dataString) {
        open(115200, Parity.NONE);
        try {
            byte[] out = dataString.getBytes(this.mCharsetName);
            mPos.POS_Write(out, 0, out.length, 5000);
            close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void USB_Write(String dataString, boolean bGBK) {
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
            open(115200, Parity.NONE);
            mPos.POS_Write(data, 0, data.length, 5000);
            close();
        }
    }

    public void USB_Write(byte[] out) {
        open(115200, Parity.NONE);
        mPos.POS_Write(out, 0, out.length, 5000);
        close();
    }

    public void USB_Write(byte[] out, int dataLen) {
        open(115200, Parity.NONE);
        mPos.POS_Write(out, 0, dataLen, 5000);
        close();
    }

    private void close() {
        mSerial.pl2303_close(serialPort);
    }

    public void disconnect() {
        close();
        mSerial.pl2303_disconnect(serialPort);
        setState(0);
    }

    public boolean IsNoConnection() {
        if (this.mState != 1) {
            return true;
        }
        return false;
    }
}
