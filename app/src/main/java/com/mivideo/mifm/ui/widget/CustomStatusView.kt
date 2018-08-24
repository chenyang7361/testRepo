package com.mivideo.mifm.ui.widget

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.mivideo.mifm.R

/**
 * 自定义登录框loading
 */
class CustomStatusView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private val progressColor: Int    //进度颜色
    private val loadSuccessColor: Int    //成功的颜色
    private val loadFailureColor: Int   //失败的颜色
    private val progressWidth: Float    //进度宽度
    private val progressRadius: Float   //圆环半径

    private var mPaint: Paint? = null
    private var mStatus: StatusEnum? = null     //状态

    private var startAngle = -90
    private var minAngle = -90
    private var sweepAngle = 120
    private var curAngle = 0

    //追踪Path的坐标
    private var mPathMeasure: PathMeasure? = null
    //画圆的Path
    private var mPathCircle: Path? = null
    //截取PathMeasure中的path
    private var mPathCircleDst: Path? = null
    private var successPath: Path? = null
    private var failurePathLeft: Path? = null
    private var failurePathRight: Path? = null

    private var circleAnimator: ValueAnimator? = null
    private var circleValue: Float = 0.toFloat()
    private var successValue: Float = 0.toFloat()
    private var failValueRight: Float = 0.toFloat()
    private var failValueLeft: Float = 0.toFloat()

    init {
        val array = context.theme.obtainStyledAttributes(attrs, R.styleable.CustomStatusView, defStyleAttr, 0)
        progressColor = array.getColor(R.styleable.CustomStatusView_progress_color, ContextCompat.getColor(context, R.color.colorPrimary))
        loadSuccessColor = array.getColor(R.styleable.CustomStatusView_load_success_color, ContextCompat.getColor(context, R.color.load_success))
        loadFailureColor = array.getColor(R.styleable.CustomStatusView_load_failure_color, ContextCompat.getColor(context, R.color.load_failure))
        progressWidth = array.getDimension(R.styleable.CustomStatusView_progress_width, 6f)
        progressRadius = array.getDimension(R.styleable.CustomStatusView_progress_radius, 100f)
        array.recycle()

        initPaint()
        initPath()
        initAnim()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    private fun initPaint() {
        mPaint = Paint()
        mPaint!!.color = progressColor
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.isDither = true
        mPaint!!.isAntiAlias = true
        mPaint!!.strokeWidth = progressWidth
        mPaint!!.strokeCap = Paint.Cap.ROUND    //设置画笔为圆角笔触
    }

    private fun initPath() {
        mPathCircle = Path()
        mPathMeasure = PathMeasure()
        mPathCircleDst = Path()
        successPath = Path()
        failurePathLeft = Path()
        failurePathRight = Path()
    }

    private fun initAnim() {
        circleAnimator = ValueAnimator.ofFloat(0f, 1f)
        circleAnimator!!.addUpdateListener { animation ->
            circleValue = animation.animatedValue as Float
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //将当前画布的点移到getPaddingLeft,getPaddingTop,后面的操作都以该点作为参照点
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        if (mStatus === StatusEnum.Loading) {    //正在加载
            if (startAngle == minAngle) {
                sweepAngle += 6
            }
            if (sweepAngle >= 300 || startAngle > minAngle) {
                startAngle += 6
                if (sweepAngle > 20) {
                    sweepAngle -= 6
                }
            }
            if (startAngle > minAngle + 300) {
                startAngle %= 360
                minAngle = startAngle
                sweepAngle = 20
            }
            curAngle = curAngle + 4
            canvas.rotate(curAngle.toFloat(), progressRadius, progressRadius)  //旋转的弧长为4
            canvas.drawArc(RectF(0f, 0f, progressRadius * 2, progressRadius * 2), startAngle.toFloat(), sweepAngle.toFloat(), false, mPaint!!)
            invalidate()
        } else if (mStatus === StatusEnum.LoadSuccess) {     //加载成功
            mPaint!!.color = loadSuccessColor
            mPathCircle!!.addCircle((width / 2).toFloat(), (width / 2).toFloat(), progressRadius, Path.Direction.CW)
            mPathMeasure!!.setPath(mPathCircle, false)
            mPathMeasure!!.getSegment(0f, circleValue * mPathMeasure!!.length, mPathCircleDst, true)   //截取path并保存到mPathCircleDst中
            canvas.drawPath(mPathCircleDst!!, mPaint!!)

            if (circleValue == 1f) {      //表示圆画完了,可以钩了
                successPath!!.moveTo((width / 8 * 3).toFloat(), (width / 2).toFloat())
                successPath!!.lineTo((width / 2).toFloat(), (width / 5 * 3).toFloat())
                successPath!!.lineTo((width / 3 * 2).toFloat(), (width / 5 * 2).toFloat())
                mPathMeasure!!.nextContour()
                mPathMeasure!!.setPath(successPath, false)
                mPathMeasure!!.getSegment(0f, successValue * mPathMeasure!!.length, mPathCircleDst, true)
                canvas.drawPath(mPathCircleDst!!, mPaint!!)
            }
        } else {      //加载失败
            mPaint!!.color = loadFailureColor
            mPathCircle!!.addCircle((width / 2).toFloat(), (width / 2).toFloat(), progressRadius, Path.Direction.CW)
            mPathMeasure!!.setPath(mPathCircle, false)
            mPathMeasure!!.getSegment(0f, circleValue * mPathMeasure!!.length, mPathCircleDst, true)
            canvas.drawPath(mPathCircleDst!!, mPaint!!)

            if (circleValue == 1f) {  //表示圆画完了,可以画叉叉的右边部分
                failurePathRight!!.moveTo((width / 3 * 2).toFloat(), (width / 3).toFloat())
                failurePathRight!!.lineTo((width / 3).toFloat(), (width / 3 * 2).toFloat())
                mPathMeasure!!.nextContour()
                mPathMeasure!!.setPath(failurePathRight, false)
                mPathMeasure!!.getSegment(0f, failValueRight * mPathMeasure!!.length, mPathCircleDst, true)
                canvas.drawPath(mPathCircleDst!!, mPaint!!)
            }

            if (failValueRight == 1f) {    //表示叉叉的右边部分画完了,可以画叉叉的左边部分
                failurePathLeft!!.moveTo((width / 3).toFloat(), (width / 3).toFloat())
                failurePathLeft!!.lineTo((width / 3 * 2).toFloat(), (width / 3 * 2).toFloat())
                mPathMeasure!!.nextContour()
                mPathMeasure!!.setPath(failurePathLeft, false)
                mPathMeasure!!.getSegment(0f, failValueLeft * mPathMeasure!!.length, mPathCircleDst, true)
                canvas.drawPath(mPathCircleDst!!, mPaint!!)
            }
        }
    }

    private fun setStatus(status: StatusEnum) {
        mStatus = status
    }

    fun loadLoading() {
        setStatus(StatusEnum.Loading)
        invalidate()
    }

    fun loadSuccess() {
        setStatus(StatusEnum.LoadSuccess)
        startSuccessAnim()
    }

    fun loadFailure() {
        setStatus(StatusEnum.LoadFailure)
        startFailAnim()
    }

    private fun startSuccessAnim() {
        val success = ValueAnimator.ofFloat(0f, 1.0f)
        success.addUpdateListener { animation ->
            successValue = animation.animatedValue as Float
            invalidate()
        }
        //组合动画,一先一后执行
        val animatorSet = AnimatorSet()
        animatorSet.play(success).after(circleAnimator)
        animatorSet.duration = 200
        animatorSet.start()
    }

    private fun startFailAnim() {
        val failLeft = ValueAnimator.ofFloat(0f, 1.0f)
        failLeft.addUpdateListener { animation ->
            failValueRight = animation.animatedValue as Float
            invalidate()
        }
        val failRight = ValueAnimator.ofFloat(0f, 1.0f)
        failRight.addUpdateListener { animation ->
            failValueLeft = animation.animatedValue as Float
            invalidate()
        }
        //组合动画,一先一后执行
        val animatorSet = AnimatorSet()
        animatorSet.play(failLeft).after(circleAnimator).before(failRight)
        animatorSet.duration = 200
        animatorSet.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width: Int
        val height: Int
        var mode = View.MeasureSpec.getMode(widthMeasureSpec)
        var size = View.MeasureSpec.getSize(widthMeasureSpec)

        if (mode == View.MeasureSpec.EXACTLY) {
            width = size
        } else {
            width = (2 * progressRadius + progressWidth + paddingLeft.toFloat() + paddingRight.toFloat()).toInt()
        }

        mode = View.MeasureSpec.getMode(heightMeasureSpec)
        size = View.MeasureSpec.getSize(heightMeasureSpec)
        if (mode == View.MeasureSpec.EXACTLY) {
            height = size
        } else {
            height = (2 * progressRadius + progressWidth + paddingTop.toFloat() + paddingBottom.toFloat()).toInt()
        }
        setMeasuredDimension(width, height)
    }
}