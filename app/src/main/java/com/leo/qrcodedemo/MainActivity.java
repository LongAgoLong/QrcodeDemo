package com.leo.qrcodedemo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.libqrcode.decode.ZbarDecodeUtil;
import com.leo.libqrcode.encode.ZbarEncodeUtil;
import com.leo.qrcode.QrcodeScanActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String qr = "君不见黄河之水天上来，奔流到海不复回";

    private Button mBtn0;
    private Button mBtn1;
    private Button mQrcodeBtn;
    private Button mLogoQrcodeBtn;
    private Button mBgQrcodeBtn;
    private Button mMaskQrcodeBtn;
    private TextView mResultTv;
    private ImageView mResultImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtn0 = findViewById(R.id.btn0);
        mBtn0.setOnClickListener(this);
        mBtn1 = findViewById(R.id.btn1);
        mBtn1.setOnClickListener(this);
        mQrcodeBtn = findViewById(R.id.qrcodeBtn);
        mQrcodeBtn.setOnClickListener(this);
        mLogoQrcodeBtn = findViewById(R.id.logoQrcodeBtn);
        mLogoQrcodeBtn.setOnClickListener(this);
        mBgQrcodeBtn = findViewById(R.id.bgQrcodeBtn);
        mBgQrcodeBtn.setOnClickListener(this);
        mMaskQrcodeBtn = findViewById(R.id.maskQrcodeBtn);
        mMaskQrcodeBtn.setOnClickListener(this);

        mResultImg = findViewById(R.id.resultImg);
        mResultTv = findViewById(R.id.resultTv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn0:
                Intent intent = new Intent(this, QrcodeScanActivity.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.btn1:
                Intent intent1 = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent1, 1);
                break;
            case R.id.qrcodeBtn:
                Bitmap bmp = ZbarEncodeUtil.Builder
                        .getInstance(qr, 300)
                        .build();
                if (null != bmp) {
                    mResultImg.setImageBitmap(bmp);
                }
                break;
            case R.id.logoQrcodeBtn:
                Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.abc);
                Bitmap logoBmp = ZbarEncodeUtil.Builder
                        .getInstance(qr, 300)
                        .setLogo(logo)
                        .build();
                if (null != logoBmp) {
                    mResultImg.setImageBitmap(logoBmp);
                }
                break;
            case R.id.bgQrcodeBtn:
                Bitmap bg = BitmapFactory.decodeResource(getResources(), R.mipmap.abc);
                Bitmap bgBmp = ZbarEncodeUtil.Builder
                        .getInstance(qr, 300)
                        .setBg(bg)
                        .build();
                if (null != bgBmp) {
                    mResultImg.setImageBitmap(bgBmp);
                }
                break;
            case R.id.maskQrcodeBtn:
                Bitmap mask = BitmapFactory.decodeResource(getResources(), R.mipmap.abc);
                Bitmap maskBmp = ZbarEncodeUtil.Builder
                        .getInstance(qr, 300)
                        .setMask(mask)
                        .build();
                if (null != maskBmp) {
                    mResultImg.setImageBitmap(maskBmp);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null == data) {
            return;
        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    String s = data.getStringExtra("data");
                    if (!TextUtils.isEmpty(s)) {
                        mResultTv.append("\n");
                        mResultTv.append(s);
                    } else {
                        mResultTv.append("\n");
                        mResultTv.append("result is empty");
                    }
                    break;
                case 1:
                    try {
                        Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
                        if (cursor != null) {
                            cursor.moveToFirst();
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            String path = cursor.getString(columnIndex);  //获取照片路径
                            cursor.close();
                            String decode = ZbarDecodeUtil.decode(path);
                            if (!TextUtils.isEmpty(decode)) {
                                mResultTv.append("\n");
                                mResultTv.append(decode);
                            } else {
                                mResultTv.append("\n");
                                mResultTv.append("result is empty");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
