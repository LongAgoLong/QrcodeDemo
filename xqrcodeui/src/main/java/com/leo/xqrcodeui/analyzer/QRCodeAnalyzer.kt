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
    private val mLock: Any = Any()

    private val readLock: ReentrantReadWriteLock.ReadLock = rwl.readLock()
    private val writeLock: ReentrantReadWriteLock.WriteLock = rwl.writeLock()

    var isAbortDecode: Boolean = false
        set(value) {
            writeLock.lock()
            field = value
            writeLock.unlock()
        }
        get() {
            try {
                readLock.lock()
                return field
            } finally {
                readLock.unlock()
            }
        }

    override fun analyze(image: ImageProxy) {
        if (ImageFormat.YUV_420_888 != image.format) {
            Log.e("BarcodeAnalyzer", "expect YUV_420_888, now = ${image.format}")
            return
        }
        synchronized(mLock, block = {
            if (!isAbortDecode) {
                val buffer = image.planes[0].buffer
                val data = ByteArray(buffer.remaining())
                buffer.get(data)
                val decode = ZbarDecodeUtil.decode(data, image.width, image.height)
                decode?.run {
                    mUIHandler.post {
                        listener.onDecode(decode)
                    }
                    isAbortDecode = true
                }
            }
        })
        image.close()
    }
}