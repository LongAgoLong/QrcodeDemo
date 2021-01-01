package com.leo.xqrcodeui.analyzer

import android.graphics.ImageFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.leo.libqrcode.decode.ZbarDecodeUtil
import java.util.concurrent.locks.ReentrantReadWriteLock

class QRCodeAnalyzer(private val listener: QRCodeDecodeListener) : ImageAnalysis.Analyzer {
    private val mUIHandler: Handler = Handler(Looper.getMainLooper())
    private val rwl: ReentrantReadWriteLock = ReentrantReadWriteLock()
    var isAbortDecode: Boolean = false
        set(value) {
            rwl.writeLock().lock()
            field = value
            rwl.writeLock().unlock()
        }
        get() {
            try {
                rwl.readLock().lock()
                return field
            } finally {
                rwl.readLock().unlock()
            }
        }

    override fun analyze(image: ImageProxy) {
        if (ImageFormat.YUV_420_888 != image.format) {
            Log.e("BarcodeAnalyzer", "expect YUV_420_888, now = ${image.format}")
            return
        }
        if (!isAbortDecode) {
            val buffer = image.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            val height = image.height
            val width = image.width
            buffer.get(data)
            val decode = ZbarDecodeUtil.decode(data, width, height)
            decode?.run {
                mUIHandler.post {
                    listener.onDecode(decode)
                }
                isAbortDecode = true
            }
        }
        image.close()
    }


}