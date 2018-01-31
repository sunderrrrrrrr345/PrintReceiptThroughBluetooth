package com.example.mylibrary;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.net.Uri;
import android.support.v4.media.TransportMediator;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import com.usbsdk.pos.Cmd.Constant;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapConvertUtil {
    private static int[][] Floyd16x16 = new int[][]{new int[]{0, 128, 32, 160, 8, 136, 40, 168, 2, TransportMediator.KEYCODE_MEDIA_RECORD, 34, Constant.OEM_INFO_LEN, 10, 138, 42, 170}, new int[]{192, 64, 224, 96, 200, 72, 232, 104, 194, 66, 226, 98, 202, 74, 234, 106}, new int[]{48, 176, 16, 144, 56, 184, 24, 152, 50, 178, 18, 146, 58, 186, 26, 154}, new int[]{240, 112, 208, 80, 248, 120, 216, 88, 242, 114, 210, 82, 250, 122, 218, 90}, new int[]{12, 140, 44, 172, 4, 132, 36, 164, 14, 142, 46, 174, 6, 134, 38, 166}, new int[]{204, 76, 236, 108, 196, 68, 228, 100, 206, 78, 238, 110, 198, 70, 230, 102}, new int[]{60, 188, 28, 156, 52, 180, 20, 148, 62, 190, 30, 158, 54, 182, 22, 150}, new int[]{Constant.CODEPAGE_SHIFT_JIS, 124, 220, 92, 244, 116, 212, 84, Constant.CODEPAGE_BIG5, TransportMediator.KEYCODE_MEDIA_PLAY, 222, 94, 246, 118, 214, 86}, new int[]{3, 131, 35, 163, 11, 139, 43, 171, 1, 129, 33, 161, 9, 137, 41, 169}, new int[]{195, 67, 227, 99, 203, 75, 235, 107, 193, 65, 225, 97, 201, 73, 233, 105}, new int[]{51, 179, 19, 147, 59, 187, 27, 155, 49, 177, 17, 145, 57, 185, 25, 153}, new int[]{243, 115, 211, 83, Constant.CODEPAGE_EUC_KR, 123, 219, 91, 241, 113, 209, 81, 249, 121, 217, 89}, new int[]{15, 143, 47, 175, 7, 135, 39, 167, 13, 141, 45, 173, 5, 133, 37, 165}, new int[]{207, 79, 239, 111, 199, 71, 231, 103, 205, 77, 237, 109, 197, 69, 229, 101}, new int[]{63, 191, 31, 159, 55, 183, 23, 151, 61, 189, 29, 157, 53, 181, 21, 149}, new int[]{Constant.CODEPAGE_BIG5, TransportMediator.KEYCODE_MEDIA_PAUSE, 223, 95, 247, 119, 215, 87, Constant.CODEPAGE_UTF_8, 125, 221, 93, 245, 117, 213, 85}};
    private static final String TAG = "BitmapConvertUtil";

    private BitmapConvertUtil() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static Bitmap convertToBlackWhite(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[(width * height)];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[(width * i) + j];
                grey = (int) (((((double) ((16711680 & grey) >> 16)) * 0.3d) + (((double) ((MotionEventCompat.ACTION_POINTER_INDEX_MASK & grey) >> 8)) * 0.59d)) + (((double) (grey & 255)) * 0.11d));
                pixels[(width * i) + j] = grey | (((grey << 16) | -16777216) | (grey << 8));
            }
        }
        Bitmap newBmp = Bitmap.createBitmap(width, height, Config.RGB_565);
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        return newBmp;
    }

    public static Bitmap convert2GreyImg(Bitmap img) {
        int i;
        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = new int[(width * height)];
        img.getPixels(pixels, 0, width, 0, 0, width, height);
        byte[] bytePixels = new byte[(width * height)];
        for (i = 0; i < pixels.length; i++) {
            bytePixels[i] = (byte) pixels[i];
        }
        for (i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[(width * i) + j];
                grey = (int) (((((double) ((float) ((16711680 & grey) >> 16))) * 0.3d) + (((double) ((float) ((MotionEventCompat.ACTION_POINTER_INDEX_MASK & grey) >> 8))) * 0.59d)) + (((double) ((float) (grey & 255))) * 0.11d));
                pixels[(width * i) + j] = (grey | (((grey << 16) | -16777216) | (grey << 8))) ^ -1;
            }
        }
        for (i = 0; i < pixels.length; i++) {
            bytePixels[i] = (byte) pixels[i];
        }
        Bitmap result = Bitmap.createBitmap(width, height, Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    public static Bitmap bitmap2Gray(Bitmap bmSrc) {
        Bitmap bmpGray = Bitmap.createBitmap(bmSrc.getWidth(), bmSrc.getHeight(), Config.RGB_565);
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.0f);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        c.drawBitmap(bmSrc, 0.0f, 0.0f, paint);
        return bmpGray;
    }

    public static byte[] getNVLogoByte(Bitmap bm) {
        int i;
        int j;
        int oldWidth = bm.getWidth();
        int height = bm.getHeight();
        Log.e("logolist============", " " + oldWidth + " " + height);
        int[] intPixels = new int[(oldWidth * height)];
        bm.getPixels(intPixels, 0, oldWidth, 0, 0, oldWidth, height);
        for (i = 0; i < height; i++) {
            for (j = 0; j < oldWidth; j++) {
                int grey = intPixels[(oldWidth * i) + j];
                int i2 = (oldWidth * i) + j;
                intPixels[i2] = (int) (((((double) ((float) ((16711680 & grey) >> 16))) * 0.3d) + (((double) ((float) ((MotionEventCompat.ACTION_POINTER_INDEX_MASK & grey) >> 8))) * 0.59d)) + (((double) ((float) (grey & 255))) * 0.11d));
            }
        }
        for (i = 0; i < height; i++) {
            for (j = 0; j < oldWidth; j++) {
                if ((intPixels[(i * oldWidth) + j] & 255) > Floyd16x16[j & 15][i & 15]) {
                    intPixels[(i * oldWidth) + j] = 0;
                } else {
                    intPixels[(i * oldWidth) + j] = 1;
                }
            }
        }
        int desHeight = ((height + 7) / 8) * 8;
        int desWidth = ((oldWidth + 7) / 8) * 8;
        byte[] bytePixels = new byte[((desHeight * desWidth) / 8)];
        byte[] bitToByte = new byte[]{Byte.MIN_VALUE, (byte) 64, (byte) 32, (byte) 16, (byte) 8, (byte) 4, (byte) 2, (byte) 1};
        int r = 0;
        while (r < desWidth) {
            int h = 0;
            while (h < desHeight) {
                try {
                    int m;
                    if (h + 8 < bm.getHeight()) {
                        m = 0;
                        while (m < 8) {
                            int grayIndex = ((h + m) * bm.getWidth()) + r;
                            int dataIndex = ((r * desHeight) / 8) + (h / 8);
                            if (r < oldWidth && h + m < bm.getHeight() && intPixels[grayIndex] > 0) {
                                bytePixels[dataIndex] = (byte) (bytePixels[dataIndex] | bitToByte[m]);
                            }
                            m++;
                        }
                    } else {
                        m = 0;
                        while (m < 8 - (desHeight - bm.getHeight())) {
                            if (r < oldWidth && h + m < bm.getHeight() && intPixels[((h + m) * bm.getWidth()) + r] > 0) {
                                i2 = ((r * desHeight) / 8) + (h / 8);
                                bytePixels[i2] = (byte) (bytePixels[i2] | bitToByte[m]);
                            }
                            m++;
                        }
                    }
                    h += 8;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            r++;
        }
        Log.e("getNVLogoByte finish", "");
        return bytePixels;
    }

    public static byte[] convert(Bitmap bm) {
        int oldWidth = bm.getWidth();
        int height = bm.getHeight();
        int[] intPixels = new int[(oldWidth * height)];
        bm.getPixels(intPixels, 0, oldWidth, 0, 0, oldWidth, height);
        int newWidth = ((oldWidth - 1) / 8) + 1;
        byte[] bytePixels = new byte[(newWidth * height)];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < oldWidth; j++) {
                int y = (newWidth * i) + (j / 8);
                int z = 7 - (j % 8);
                if ((intPixels[(oldWidth * i) + j] & 255) < Floyd16x16[i & 15][j & 15]) {
                    bytePixels[y] = (byte) (bytePixels[y] | (1 << z));
                }
            }
        }
        return bytePixels;
    }

    public static byte[] convert2(Bitmap bm) {
        int oldWidth = bm.getWidth();
        int height = bm.getHeight();
        int[] intPixels = new int[(oldWidth * height)];
        byte correctByte = (byte) (255 >> (oldWidth % 8));
        bm.getPixels(intPixels, 0, oldWidth, 0, 0, oldWidth, height);
        int newWidth = ((oldWidth - 1) / 8) + 1;
        byte[] bytePixels = new byte[(newWidth * height)];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < oldWidth; j++) {
                int y = (newWidth * i) + (j / 8);
                int z = 7 - (j % 8);
                if ((intPixels[(oldWidth * i) + j] & 255) > Floyd16x16[i & 15][j & 15]) {
                    bytePixels[y] = (byte) (bytePixels[y] | (1 << z));
                }
            }
            int i2 = ((i + 1) * newWidth) - 1;
            bytePixels[i2] = (byte) (bytePixels[i2] | correctByte);
        }
        return bytePixels;
    }

    public static int calculateOutsideInSampleSize(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static int calculateOutsideInSampleSize(Bitmap bm, int reqWidth, int reqHeight) {
        int height = bm.getHeight();
        int width = bm.getWidth();
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap returnBitmap(String url) {
        File fileUrl = null;
        Bitmap bitmap = null;
        try {
            fileUrl = new File(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            InputStream is = new FileInputStream(fileUrl);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
            return bitmap;
        } catch (IOException e2) {
            e2.printStackTrace();
            return bitmap;
        } catch (Exception e3) {
            e3.printStackTrace();
            return bitmap;
        }
    }

    public static int calculateInsideInSampleSize(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        if (width > reqWidth && width / reqWidth > height / reqHeight) {
            return width / reqWidth;
        }
        if (height > reqHeight) {
            return height / reqHeight;
        }
        return 1;
    }

    public static int calculateInsideInSampleSize(Bitmap bm, int reqWidth, int reqHeight) {
        int height = bm.getHeight();
        int width = bm.getWidth();
        if (width > reqWidth && width / reqWidth > height / reqHeight) {
            return width / reqWidth;
        }
        if (height > reqHeight) {
            return height / reqHeight;
        }
        return 1;
    }

    public static Bitmap decodeSampledBitmapFromStream(InputStream is, int reqWidth, int reqHeight) {
        BufferedInputStream bis = new BufferedInputStream(is);
        bis.mark(0);
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(bis, null, options);
        options.inSampleSize = calculateInsideInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        try {
            bis.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(bis, null, options);
    }

    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight) {
        String filePath = getFilePath(context, uri);
        if (filePath == null) {
            return null;
        }
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInsideInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static Bitmap decodeSampledBitmapFromBitmap(Bitmap bm, int limitWidth) {
        float scaleSizeInvert;
        if (bm.getWidth() > limitWidth) {
            scaleSizeInvert = ((float) limitWidth) / ((float) bm.getWidth());
        } else {
            scaleSizeInvert = 1.0f;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scaleSizeInvert, scaleSizeInvert);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
    }

    public static Bitmap decodeSampledBitmapFromBitmap(Bitmap bm, int limitWidth, int limitHeight) {
        float scaleSizeInvert;
        Log.d("mao", "limitWidth=" + limitWidth);
        Log.d("mao", "limitHeight=" + limitHeight);
        if (bm.getWidth() > limitWidth || bm.getHeight() > limitHeight) {
            float temp1 = ((float) limitWidth) / ((float) bm.getWidth());
            float temp2 = ((float) limitHeight) / ((float) bm.getHeight());
            if (temp1 < temp2) {
                scaleSizeInvert = temp1;
            } else {
                scaleSizeInvert = temp2;
            }
        } else {
            scaleSizeInvert = 1.0f;
        }
        Log.d("mao", "scaleSizeInvert=" + scaleSizeInvert);
        Matrix matrix = new Matrix();
        matrix.postScale(scaleSizeInvert, scaleSizeInvert);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInsideInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static byte[] encodeBitmapToPixelsByteArray(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        int[] intPixels = new int[(width * height)];
        bm.getPixels(intPixels, 0, width, 0, 0, width, height);
        byte[] bytePixels = new byte[(width * height)];
        for (int i = 0; i < bytePixels.length; i++) {
            bytePixels[i] = (byte) intPixels[i];
        }
        return bytePixels;
    }

    public static Bitmap scale(Bitmap bitmap, float sx, float sy) {
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap scaleToRequiredWidth(Bitmap bitmap, int reqWidth) {
        Matrix matrix = new Matrix();
        matrix.postScale((((float) reqWidth) * 1.0f) / ((float) bitmap.getWidth()), (((float) reqWidth) * 1.0f) / ((float) bitmap.getWidth()));
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap createBitmapFromPicture(Picture picture) {
        Bitmap bitmap = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), Config.ARGB_8888);
        picture.draw(new Canvas(bitmap));
        return bitmap;
    }

    private static String getFilePath(Context context, Uri uri) {
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
            if (cursor == null) {
                return null;
            }
            try {
                if (cursor.moveToNext()) {
                    filePath = cursor.getString(cursor.getColumnIndex("_data"));
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        return filePath;
    }
}
