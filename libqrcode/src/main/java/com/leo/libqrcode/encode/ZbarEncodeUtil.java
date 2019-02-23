package com.leo.libqrcode.encode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

/**
 * Created by LEO
 * on 2019/2/23
 */
public class ZbarEncodeUtil {
    private int frontColor = 0xff000000;
    private int bgColor = 0xffffffff;

    private ZbarEncodeUtil() {
    }

    public void setFrontColor(int frontColor) {
        this.frontColor = frontColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    /**
     * @param str
     * @param widthAndHeight
     * @param level
     * @return
     * @throws WriterException
     * @throws NullPointerException
     */
    public Bitmap createQRCode(String str, int widthAndHeight, ErrorCorrectionLevel level)
            throws WriterException, NullPointerException {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        if (null != level) {
            hints.put(EncodeHintType.ERROR_CORRECTION, level);// 指定纠错等级
        }
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");//指定编码格式
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE,
                widthAndHeight, widthAndHeight, hints);
        matrix = deleteWhite(matrix);//删除白边
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = frontColor;
                } else {
                    pixels[y * width + x] = bgColor;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }
        if (logo == null) {
            return src;
        }
        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();
        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }
        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;
    }

    /**
     * Open the colorful world
     *
     * @param mask background bitmap ,as mask bitmap
     * @param qr   qr
     * @return Bitmap
     */
    public Bitmap mask(Bitmap qr, Bitmap mask) {
        int width = qr.getWidth();
        int height = qr.getHeight();
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        canvas.save();
        canvas.drawBitmap(qr, 0, 0, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(mask, new Rect(0, 0, mask.getWidth(), mask.getHeight()), new Rect(0, 0, width, height), paint);
        paint.setXfermode(null);
        canvas.restore();
        return target;
    }

    /**
     * merge bitmap
     *
     * @param qrBitmap
     * @param bgBitmap
     * @return Bitmap
     */
    public Bitmap mergeBitmap(Bitmap qrBitmap, Bitmap bgBitmap) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(qrBitmap.getWidth(), qrBitmap.getHeight(), qrBitmap.getConfig());
        Canvas canvas = new Canvas(target);
        canvas.drawBitmap(bgBitmap, new Matrix(), null);
        canvas.drawBitmap(bgBitmap,
                new Rect(0, 0, bgBitmap.getWidth(), bgBitmap.getHeight()),
                new Rect(0, 0, qrBitmap.getWidth(), qrBitmap.getHeight()), paint);

        canvas.drawBitmap(qrBitmap, 0, 0, null);
        return target;
    }

    /*除白边*/
    private static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }

    public static class Builder {
        private String qrcode;
        private Bitmap bg;
        private Bitmap logo;
        private Bitmap mask;
        private int frontColor = 0xff000000;
        private int bgColor = 0xffffffff;
        private int size;

        public Builder(@NonNull String qrcode, int size) {
            this.qrcode = qrcode;
            this.size = size;
        }

        public static Builder getInstance(@NonNull String qrcode, int size) {
            if (size <= 100) {
                size = 100;
            }
            return new Builder(qrcode, size);
        }

        public Builder setQrcode(String s) {
            this.qrcode = s;
            return this;
        }

        public Builder setBg(Bitmap bg) {
            this.bg = bg;
            if (null != bg) {
                setBgColor(Color.parseColor("#00ffffff"));
            }
            return this;
        }

        public Builder setMask(Bitmap mask) {
            this.mask = mask;
            if (null != mask) {
                setBgColor(Color.parseColor("#00ffffff"));
            }
            return this;
        }

        public Builder setLogo(Bitmap logo) {
            this.logo = logo;
            return this;
        }

        public Builder setSize(int size) {
            this.size = size;
            return this;
        }

        public Builder setFrontColor(int frontColor) {
            this.frontColor = frontColor;
            return this;
        }

        public Builder setBgColor(int bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        public Bitmap build() {
            try {
                ZbarEncodeUtil zbarEncodeUtil = new ZbarEncodeUtil();
                zbarEncodeUtil.setBgColor(bgColor);
                zbarEncodeUtil.setFrontColor(frontColor);
                Bitmap qrCodeBmp = zbarEncodeUtil.createQRCode(qrcode, size, ErrorCorrectionLevel.H);
                if (null != logo) {
                    qrCodeBmp = zbarEncodeUtil.addLogo(qrCodeBmp, logo);
                } else if (null != mask) {
                    qrCodeBmp = zbarEncodeUtil.mask(qrCodeBmp, mask);
                } else if (null != bg) {
                    qrCodeBmp = zbarEncodeUtil.mergeBitmap(qrCodeBmp, bg);
                }
                return qrCodeBmp;
            } catch (WriterException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
