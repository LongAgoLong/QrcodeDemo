package com.leo.qrcode;

import android.content.Intent;

import com.leo.libqrcode.CaptureActivity;
import com.leo.libqrcode.util.NotifyUtil;

public class QrcodeScanActivity extends CaptureActivity {

    @Override
    protected void setUI() {
        super.setUI();
        /**
         * 如果需要自定义布局，按此步骤重写；
         * 1.重写此方法，屏蔽super.setUI();
         * 2.调用setContentView(R.layout.activity_qr_scan)设置你自己的布局；
         * 3.需确保以下几个id在布局中存在,具体可参照R.layout.activity_qr_scan布局：
         *         // 根布局
         *         mContainer = (RelativeLayout) findViewById(R.id.capture_containter);
         *         // 识别框
         *         mCropLayout = (RelativeLayout) findViewById(R.id.capture_crop_layout);
         *         // 闪光灯按钮
         *         lightImg = (ImageView) findViewById(R.id.light_img);
         *         lightImg.setOnClickListener(new View.OnClickListener() {
         *             @Override
         *             public void onClick(View v) {
         *                 light();
         *             }
         *         });
         *         // 上下扫描线
         *         mQrLineView = (ImageView) findViewById(R.id.capture_scan_line);
         */
    }

    @Override
    public void scanResult(String result) {
        NotifyUtil.getInstance().playBeepSoundAndVibrate(vibrate);
//        NotifyUtil.getInstance().playBeepSoundAndVibrate(vibrate,3);
        Intent intent = new Intent();
        intent.putExtra("data", result);
        setResult(RESULT_OK, intent);
        finish();
    }
}
