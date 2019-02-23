package com.leo.libqrcode.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.leo.libqrcode.ZbarDecode;

import java.io.File;

public final class ZbarDecodeUtil {
    private ZbarDecodeUtil() {
    }

    public static String decode(byte[] yuv, int width, int height, int x, int y, int cwidth, int cheight) {
        ZbarDecode zbarDecode = new ZbarDecode();
        return zbarDecode.decode(yuv, width, height, true, x, y, cwidth, cheight);
    }

    public static String decode(byte[] yuv, int width, int height) {
        ZbarDecode zbarDecode = new ZbarDecode();
        return zbarDecode.decode(yuv, width, height, false, 0, 0, 0, 0);
    }

    /**
     * 用于识别手机相册里的图片
     *
     * @param path 图片文件路径
     * @return 识别结果
     */
    public static String decode(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        return decode(Bmp2YUVUtil.getBitmapYUVBytes(bitmap), bitmapWidth, bitmapHeight);
    }
}
