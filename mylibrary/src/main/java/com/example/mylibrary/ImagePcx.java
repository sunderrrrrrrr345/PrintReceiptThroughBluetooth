package com.example.mylibrary;

import android.graphics.Bitmap;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ImagePcx {
    private PCXHEAD m_Head = new PCXHEAD();
    private Bitmap m_Image;

    private class PCXHEAD {
        public byte[] m_Data;

        public byte Manufacturer() {
            return this.m_Data[0];
        }

        public byte Version() {
            return this.m_Data[1];
        }

        public void SetVersion(byte value) {
            this.m_Data[1] = value;
        }

        public byte Encoding() {
            return this.m_Data[2];
        }

        public void SetEncoding(byte value) {
            this.m_Data[2] = value;
        }

        public byte Bits_Per_Pixel() {
            return this.m_Data[3];
        }

        public void SetBits_Per_Pixel(byte value) {
            this.m_Data[3] = value;
        }

        public int GetUShort(int p_Index) {
            return ImagePcx.byte2Int(this.m_Data, p_Index, 2);
        }

        public void SetUshort(int p_Index, int value) {
            byte[] b = ImagePcx.int2Byte(value, 2);
            this.m_Data[p_Index] = b[0];
            this.m_Data[p_Index + 1] = b[1];
        }

        public int Xmin() {
            return GetUShort(4);
        }

        public void SetXmin(int value) {
            SetUshort(4, value);
        }

        public int Ymin() {
            return GetUShort(6);
        }

        public void SetYmin(int value) {
            SetUshort(6, value);
        }

        public int Xmax() {
            return GetUShort(8);
        }

        public void SetXmax(int value) {
            SetUshort(8, value);
        }

        public int Ymax() {
            return GetUShort(10);
        }

        public void SetYmax(int value) {
            SetUshort(10, value);
        }

        public int Hres1() {
            return GetUShort(12);
        }

        public void SetHres1(int value) {
            SetUshort(12, value);
        }

        public int Vres1() {
            return GetUShort(14);
        }

        public void SetVres1(int value) {
            SetUshort(14, value);
        }

        public byte[] Palette() {
            byte[] _Palette = new byte[48];
            for (int i = 0; i < 48; i++) {
                _Palette[i] = this.m_Data[i + 16];
            }
            return _Palette;
        }

        public void SetPalette(byte[] value) throws Exception {
            if (value.length != 48) {
                throw new Exception("错误的byte[]长度不是48");
            }
            for (int i = 0; i < 48; i++) {
                this.m_Data[i + 16] = value[i];
            }
        }

        public int Reserved() {
            return this.m_Data[64];
        }

        public void SetReserved(byte value) {
            this.m_Data[64] = value;
        }

        public int Colour_Planes() {
            return this.m_Data[65];
        }

        public void SetColour_Planes(byte value) {
            this.m_Data[65] = value;
        }

        public int Bytes_Per_Line() {
            return GetUShort(66);
        }

        public void SetBytes_Per_Line(byte value) {
            SetUshort(66, value);
        }

        public int Palette_Type() {
            return GetUShort(68);
        }

        public void SetPalette_Type(byte value) {
            SetUshort(68, value);
        }

        public byte[] Filler() {
            byte[] _Palette = new byte[58];
            for (int i = 0; i < 58; i++) {
                _Palette[i] = this.m_Data[i + 70];
            }
            return _Palette;
        }

        public PCXHEAD(byte[] p_Data) {
            this.m_Data = new byte[128];
            for (int i = 0; i < 128; i++) {
                this.m_Data[i] = p_Data[i];
            }
        }

        public PCXHEAD() {
            this.m_Data = new byte[128];
            this.m_Data[0] = (byte) 10;
            SetVersion((byte) 5);
            SetEncoding((byte) 1);
            SetBits_Per_Pixel((byte) 8);
            try {
                SetPalette(new byte[]{(byte) 0, (byte) 0, (byte) -51, (byte) 0, (byte) -112, (byte) -25, (byte) 55, (byte) 1, Byte.MIN_VALUE, (byte) -10, (byte) -107, (byte) 124, (byte) 40, (byte) -5, (byte) -107, (byte) 124, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 35, (byte) -5, (byte) -107, (byte) 124, (byte) -77, (byte) 22, (byte) 52, (byte) 124, (byte) 0, (byte) 0, (byte) -51, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) -72, (byte) 22, (byte) 52, (byte) 124, (byte) 100, (byte) -13, (byte) 55, (byte) 1, (byte) -40, (byte) 84, (byte) -72, (byte) 0});
            } catch (Exception e) {
                e.printStackTrace();
            }
            SetReserved((byte) 1);
            SetColour_Planes((byte) 3);
            SetPalette_Type((byte) 1);
        }

        public int Width() {
            return (Xmax() - Xmin()) + 1;
        }

        public int Height() {
            return (Ymax() - Ymin()) + 1;
        }
    }

    public static int byte2Int(byte[] b, int index, int len) {
        int intValue = 0;
        for (int i = 0; i < len; i++) {
            intValue += (b[i + index] & 255) << (((len - i) - 1) * 8);
        }
        return intValue;
    }

    public static byte[] int2Byte(int intValue, int Len) {
        byte[] b = new byte[Len];
        for (int i = 0; i < Len; i++) {
            b[i] = (byte) ((intValue >> (((Len - i) - 1) * 8)) & 255);
        }
        return b;
    }

    public Bitmap PcxImage() {
        return this.m_Image;
    }

    public void SetPcxImage(Bitmap value) {
        this.m_Image = value;
    }

    public ImagePcx(String p_FileFullName) {
    }

    public ImagePcx(byte[] p_Data) {
    }

    public void Save(String p_FileFullName) {
        if (this.m_Image != null) {
            this.m_Head.SetXmax(this.m_Image.getWidth() - 1);
            this.m_Head.SetYmax(this.m_Image.getHeight() - 1);
            this.m_Head.SetVres1(this.m_Head.Xmax() + 1);
            this.m_Head.SetHres1(this.m_Head.Ymax() + 1);
            this.m_Head.SetBytes_Per_Line((byte) this.m_Head.Width());
            try {
                FileOutputStream _SaveData = new FileOutputStream(p_FileFullName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int[] pixels = new int[(this.m_Image.getWidth() * this.m_Image.getHeight())];
            this.m_Image.getPixels(pixels, 0, this.m_Image.getWidth(), 0, 0, this.m_Image.getWidth(), this.m_Image.getHeight());
        }
    }
}
