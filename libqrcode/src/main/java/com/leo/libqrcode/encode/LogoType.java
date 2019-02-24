package com.leo.libqrcode.encode;

import android.support.annotation.IntDef;

@IntDef({LogoType.NORMAL,
        LogoType.ROUND,
        LogoType.CIRCLE})
public @interface LogoType {
    int NORMAL = 0;
    int ROUND = 1;
    int CIRCLE = 2;
}
