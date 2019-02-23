package com.leo.libqrcode;
/**
 * Created by LEO
 * on 2019/2/23
 */
public class ZbarDecode {
    static {
        System.loadLibrary("zbar");
    }

    public native String decode(byte[] data, int width, int height, boolean isCrop, int x, int y, int cwidth, int cheight);
}
