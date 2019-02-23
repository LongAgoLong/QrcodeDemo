package com.leo.libqrcode.encode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.annotation.DrawableRes;
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
public final class EncodingHandler {
    private static final int BLACK = 0xff000000;
    private static final int WHITE = 0xffffffff;

    /**
     * @param str
     * @param widthAndHeight
     * @return
     * @throws WriterException
     * @throws NullPointerException
     */
    public static Bitmap createQRCode(String str, int widthAndHeight)
            throws WriterException, NullPointerException {
        return createQRCode(str, widthAndHeight, ErrorCorrectionLevel.H);
    }

    public static Bitmap createQRCode(String str, int widthAndHeight, ErrorCorrectionLevel level)
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
                    pixels[y * width + x] = BLACK;
                } else {
                    pixels[y * width + x] = WHITE;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static Bitmap createQRCode(Context context, String str, int widthAndHeight,
                                      ErrorCorrectionLevel level, @DrawableRes int iconId)
            throws WriterException, NullPointerException {
        Bitmap bitmap = createQRCode(str, widthAndHeight, level);
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        Bitmap resource = BitmapFactory.decodeResource(context.getResources(), iconId, opt);
        bitmap = addLogo(bitmap, resource);
        return bitmap;
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
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
        float scaleFactor = srcWidth * 1.0f / 7 / logoWidth;
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
}
