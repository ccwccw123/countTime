package com.example.win7.counttimes;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

/**
 * desc:
 * author：ccw
 * date:2018/11/26
 * time:10:12
 */
public class TextViewTimer4 extends View {

    @IntDef({TimeBgType.CIRCLE, TimeBgType.ROUND_CIRCLE, TimeBgType.CUSTOM})
    @Retention(RetentionPolicy.SOURCE)
    @interface TimeBgType {
        /**
         * 圆
         */
        int CIRCLE = 10;
        /**
         * 圆角矩形
         */
        int ROUND_CIRCLE = 11;
        /**
         * 自定义
         */
        int CUSTOM = 12;
    }

    private final Paint mRadiusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);// 圆角矩形画笔
    private final Paint mTimePaint = new Paint(Paint.ANTI_ALIAS_FLAG);// 时间值画笔
    private final Paint mSepPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);// “:”画笔
    private final RectF mRectRadius = new RectF();// 绘制的椭圆矩形
    private float mBaseline;//基准线
    private float mRadius;//圆角或者圆的半径
    private int mTimeTextBg;//倒计时背景色
    private float mBgWidth;//倒计时单个模块背景色宽度
    private float mBgHeight;//倒计时单个模块背景色高度
    private float mSepWidth;//符号宽度
    private int mBitmapId;//自定义背景

    /**
     * 倒计时背景类型,默认为圆角矩形或者自定义图片背景<br/>
     * 值为：{@link TimeBgType#CIRCLE},{@link TimeBgType#ROUND_CIRCLE},{@link TimeBgType#CUSTOM}
     */
    private int mTimeBgType;
    //private final Locale mLocale = LanguageManager.getInstance().isLanguageFa() ? Locale.getDefault() : Locale.US;// 波斯语下显示为波斯数字，其他语言统一为阿拉伯数字
    private final Locale mLocale = Locale.getDefault();

    private int[] times;

    private TimeViewListener countDown;


    public TextViewTimer4(Context context) {
        super(context);
    }

    public TextViewTimer4(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint(context, attrs);
    }

    private void initPaint(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TextViewTimer4);
        //背景颜色
        mTimeTextBg = typedArray.getColor(R.styleable.TextViewTimer4_time_background, ContextCompat.getColor(context, R.color.black));
        mBgWidth = typedArray.getDimension(R.styleable.TextViewTimer4_background_width, ToolSize.dp2Px(context, 18));
        mBgHeight = typedArray.getDimension(R.styleable.TextViewTimer4_background_height, ToolSize.dp2Px(context, 16));
        int colonColor = typedArray.getColor(R.styleable.TextViewTimer4_colon_color, ContextCompat.getColor(context, R.color.black));
        int timeColor = typedArray.getColor(R.styleable.TextViewTimer4_time_color, ContextCompat.getColor(context, R.color.white));
        float fontSize = typedArray.getDimension(R.styleable.TextViewTimer4_time_size, ToolSize.dp2Px(getContext(), 12));
        mRadius = typedArray.getDimension(R.styleable.TextViewTimer4_radius, 5);
        mSepWidth = typedArray.getDimension(R.styleable.TextViewTimer4_sep_width, ToolSize.dp2Px(context, 10));
        mTimeBgType = typedArray.getInt(R.styleable.TextViewTimer4_time_background_type, TimeBgType.ROUND_CIRCLE);
        mBitmapId = typedArray.getResourceId(R.styleable.TextViewTimer4_custom_background, 0);
        //设置背景
        mRadiusPaint.setColor(mTimeTextBg);
        mRadiusPaint.setAlpha(180);

        //设置字体
        mTimePaint.setTypeface(Typeface.DEFAULT);
        mTimePaint.setColor(timeColor);
        mTimePaint.setTextAlign(Paint.Align.CENTER);// 水平居中
        mTimePaint.setTextSize(fontSize);

        //设置符号
        mSepPaint.setColor(colonColor);
        mSepPaint.setTextAlign(Paint.Align.CENTER);// 水平居中
        mSepPaint.setTextSize(fontSize);
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTimeView(canvas);
    }

    /**
     * @param canvas 绘制倒计时
     */
    private void drawTimeView(Canvas canvas) {
        if (times == null) {
            return;
        }
        String[] stringTimes=countDown.getTextStyle(times);
        float width = mBgWidth;
        float left = 0;
        float right;
        float timeViewWidth = calculateTimeViewWidth(stringTimes, width, mSepWidth);
        left = ((getWidth() - timeViewWidth) / 2) < 0 ? 0 : ((getWidth() - timeViewWidth) / 2);// 初始时绘制时间的起始位置
        right = left + width;// 初始时右侧位置
        for (int i = 0; i < stringTimes.length; i++) {
            if (i % 2 != 0) {
                float textWidth=mSepPaint.measureText(stringTimes[i]);
                left =left+ textWidth/2+mSepWidth/2;
                canvas.drawText(stringTimes[i], left, mBaseline, mSepPaint);
                left =left+textWidth/2+mSepWidth/2;
                right +=mSepWidth+textWidth;
            } else {
                final String timeStr = fixTimeString(stringTimes[i]);
                //绘制背景色
                mRadiusPaint.setColor(mTimeTextBg);
                if (mTimeBgType == TimeBgType.CIRCLE) {
                    drawCircleBg(canvas, left + mRadius, mBgHeight / 2);
                } else if (mTimeBgType == TimeBgType.ROUND_CIRCLE) {
                    drawRoundCircleBg(canvas, left, right, mBgHeight);
                } else {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),mBitmapId);
                    Bitmap mBitmap = Bitmap.createScaledBitmap(bitmap,  (int)width, (int)mBgHeight, false);
                    canvas.drawBitmap(mBitmap, left, 0, mRadiusPaint);
                    drawTextTimeView(canvas, left + mBitmap.getWidth() / 2, timeStr);
                    bitmap.recycle();
                    mBitmap.recycle();
                }
                //绘制时间值
                if (mTimeBgType != TimeBgType.CUSTOM) {
                    drawTextTimeView(canvas, left + mRadius, timeStr);
                }

                // 重新计算位置
                left += width;
                right += width;
            }
        }

    }

    /**
     * 绘制时间视图
     *
     * @param canvas  Canvas
     * @param x       圆形背景时圆心的X坐标
     * @param timeStr 时间字符串
     */
    private void drawTextTimeView(Canvas canvas, float x, @NonNull String timeStr) {
        initFontBaseLine();
        if (mTimeBgType == TimeBgType.ROUND_CIRCLE) {//圆角矩形背景
            canvas.drawText(timeStr, mRectRadius.centerX(), mBaseline, mTimePaint);
        } else if (mTimeBgType == TimeBgType.CIRCLE) {//圆形背景
            canvas.drawText(timeStr, x, mBaseline, mTimePaint);
        } else {
            canvas.drawText(timeStr, x, mBaseline, mTimePaint);
        }
    }

    /**
     * 个位数时，直接补0，如'03'
     *
     * @param time 时间
     * @return 返回填充后的时间字符串
     */
    private String fixTimeString(String time) {
        int timeCode;
        try {
            timeCode = Integer.parseInt(time);
        } catch (Exception e) {
            timeCode = 0;
        }
        if (timeCode <= 9) {
            return String.format(mLocale, "%1$d%2$d", 0, timeCode);
        }
        return String.format(mLocale, "%1$d", timeCode);
    }

    public void refreshTime(int[] times) {
        this.times = times;
        invalidate();
    }



    /**
     * 计算时间所占的宽度
     *
     * @param drawItemArr 需要绘制的内容
     * @param width       一个倒计时单元所占的宽度  如 04:11:43  43所占的宽度
     * @param sepWidth    分隔符所占的宽度
     */
    protected float calculateTimeViewWidth(String[] drawItemArr, float width, float sepWidth) {
        float timeViewWidth = 0;
        for (int i = 0; i < drawItemArr.length; i++) {// 计算绘制需要占的宽度
            if (i % 2 != 0) {
                timeViewWidth += sepWidth+mSepPaint.measureText(drawItemArr[i]);
            } else {
                timeViewWidth += width;
            }
        }
        return timeViewWidth;
    }

    public void setListener(TimeViewListener listener) {
        this.countDown = listener;
    }

    /**
     * 绘制圆形背景
     *
     * @param canvas Canvas
     * @param x      圆心X坐标
     * @param y      圆心Y坐标
     */
    private void drawCircleBg(Canvas canvas, float x, float y) {
        if (mTimeBgType != TimeBgType.CIRCLE) {
            return;
        }
        canvas.drawCircle(x, y, mRadius, mRadiusPaint);
    }

    /**
     * 初始化基准线
     */
    private void initFontBaseLine() {
        if (mBaseline < 0.01) {
            Paint.FontMetricsInt fontMetrics = mTimePaint.getFontMetricsInt();
            mBaseline = ((mBgHeight - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top);// 垂直居中
        }
    }

    /**
     * 绘制圆角矩形背景
     *
     * @param canvas Canvas
     * @param left   左边宽度
     * @param right  右边宽度
     * @param bottom 底部高度
     */
    private void drawRoundCircleBg(Canvas canvas, float left, float right, float bottom) {
        if (mTimeBgType != TimeBgType.ROUND_CIRCLE) {
            return;
        }
        mRectRadius.set(left, 0, right, bottom);
        canvas.drawRoundRect(mRectRadius, mRadius, mRadius, mRadiusPaint);
    }
}
