# 结合zxing与zbar的二维码扫描识别工具，可自定义布局
[![](https://jitpack.io/v/LongAgoLong/QrcodeDemo.svg)](https://jitpack.io/#LongAgoLong/QrcodeDemo)
------
## 1、优势
> * 具有zbar的识别速度；
> * 具有zxing的相机管理、二维码生成；
> * 支持不退出界面连续扫描；
> * 支持高效识别相册二维码图片（Zbar识别）；
> * 支持多种模式二维码生成（前景色背景色支持设置/中心带logo(支持圆角/圆形设置)/仿QQ二维码/图片背景二维码等）；
> * 支持自定义布局（产品想怎么玩都行）。
------
## 2、使用方式
### ①添加依赖
**gradle依赖**
```java
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
```java
implementation 'com.github.LongAgoLong:QrcodeDemo:$JitPack-Version$'
```
**maven依赖**
```java
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```
```java
<dependency>
	<groupId>com.github.LongAgoLong</groupId>
	<artifactId>QrcodeDemo</artifactId>
	<version>$JitPack-Version$</version>
</dependency>
```
### ②添加权限（Android6.0以上版本需自行动态请求）
```java
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.FLASHLIGHT" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<uses-feature
    android:name="android.hardware.camera"
    android:required="false" />
<uses-feature
    android:name="android.hardware.camera.autofocus"
    android:required="false" />
```
### ③继承类，处理结果
```java
public class QrcodeScanActivity extends CaptureActivity {
    @Override
    public void scanResult(String result) { 
        // 处理结果
        // 如果要继续扫描，处理完结果后调用restartScan()重启扫描识别
    }
}
```
### ④自定义布局(可选，有需要再使用)
```java
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
```
------
## 3、api
| 方法   |  描述  |
| ----- | ----  |
| ZbarDecodeUtil.decode(byte[] yuv, int width, int height, int x, int y, int cwidth, int cheight) |识别图片YUV数据，截取部分     |
| ZbarDecodeUtil.decode(byte[] yuv, int width, int height) | 识别图片YUV数据，不截取   |
| ZbarDecodeUtil.decode(String path) |  识别手机相册里的图片  |
| ZbarEncodeUtil.Builder |  通过建造者模式生成二维码  |
| restartScan() |  再次扫描  |

