package com.leo.libqrcode.encode;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({LogoType.NORMAL,
        LogoType.ROUND,
        LogoType.CIRCLE})
@Retention(RetentionPolicy.SOURCE)
public @interface LogoType {
    int NORMAL = 0;
    int ROUND = 1;
    int CIRCLE = 2;
}
