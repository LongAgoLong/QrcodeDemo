package com.leo.qrcode;

import android.content.Intent;

import com.leo.libqrcode.CaptureActivity;

public class QrcodeScanActivity extends CaptureActivity {
    @Override
    public void scanResult(String result) {
        Intent intent = new Intent();
        intent.putExtra("data", result);
        setResult(RESULT_OK, intent);
        finish();
    }
}
