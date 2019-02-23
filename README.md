# libqrcode(结合zxing与zbar的二维码扫描识别工具)
------
## 1、优势
> * 具有zbar的识别速度
> * zxing的相机管理、二维码生成
> * 支持相册二维码图片的识别
------
## 2、使用方式
### ①添加依赖
```java
implementation 'com.github.LongAgoLong:QrcodeDemo:1.1'
```
### ②继承类，处理结果
```java
public class QrcodeScanActivity extends CaptureActivity {
    @Override
    public void scanResult(String result) { 
        // 处理结果
        // 如果要继续扫描调用restartScan()方法
    }
}
```
## 3、api
参照demo，解析类ZbarDecodeUtil
