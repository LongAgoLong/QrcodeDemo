package com.leo.xqrcodeui.weight

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class MaskerView : View {
    private var overView: View? = null
    private var overRect: Rect? = null
    private var mPaint: Paint? = null

    constructor(context: Context?) : super(context) {
        initPaint()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initPaint()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initPaint()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initPaint()
    }

    private fun initPaint() {
        mPaint = Paint()
        overRect = Rect()
    }

    fun setOverView(overView: View) {
        this.overView = overView
        val viewLocation = IntArray(2)
        overView.getLocationOnScreen(viewLocation)
        overRect!!.left = viewLocation[0]
        overRect!!.top = viewLocation[1]
        overRect!!.right = overRect!!.left + overView.width
        overRect!!.bottom = overRect!!.top + overView.height
        postInvalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackGround(canvas)
        drawOverRect(canvas)
    }

    /**
     * 画一个半透明的背景
     */
    private fun drawBackGround(canvas: Canvas) {
        mPaint!!.xfermode = null
        mPaint!!.color = Color.parseColor("#8A000000")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), mPaint!!)
    }

    /**
     * 画出透明区域
     */
    private fun drawOverRect(canvas: Canvas) {
        if (null != overView) {
            mPaint!!.color = Color.WHITE
            mPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            canvas.drawRect(overRect!!, mPaint!!)
        }
    }
}