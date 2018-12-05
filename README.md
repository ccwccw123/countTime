# countTime
自定义View时间倒计时，支持文字自定义，支持背景圆形，矩形及圆角

首先看一下效果<br/>
<img src="https://i.imgur.com/NRhhi3l.jpg" height = "500" div align=center /><br/>
可以自定义时间背景和可以自定义文字样式
<img src="https://i.imgur.com/GpVFCmZ.jpg" height = "500" div align=center /><br/>
<img src="https://i.imgur.com/ygboMyd.jpg" height = "500" div align=center /><br/>

默认实现这种样式，例如12:26:34<br/>


	public interface TimeViewListener {

    default String[] getTextStyle(int[] times){
         return  new String[]{times[1] + "", ":", times[2] + "", ":", times[3] + "", ""};
   	 }
	}

可以通过继承实现TimeViewListener自定义文字样式，例如4天12小时3分23秒这样

	public class CountDownDefault implements TimeViewListener{

    @Override
    public String[] getTextStyle(int[] times) {
        return  new String[]{times[0] + "", "天", times[1] + "", "小时",times[2] + "", "分", times[3] + "", "秒"};
   	 }
	}

目前支持三种样式，通过设置time_background_type属性，可以设置背景，圆，矩形样式

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

这里做了优化，添加了map可以维护多个时间倒计时

    /**
     * 添加并启动倒计时任务。注意：<br/>
     * 1, 不能重复添加参数uniqueTag值相同的倒计时任务。<br/>
     * 2, 调用该函数后，一定要调用 {@link #remove(String)}，否则会导致内存泄漏。<br/>
     *

     * @param hook      倒计时回调
     */
    public void start(String uniqueTag, AbsCountdownHook hook) {
        if ((!TextUtils.isEmpty(uniqueTag)) && (hook != null)) {
         //   checkEnvironment(uniqueTag, hook);
            initDefaultBeginTime(hook);
            getHooksOrCreateIfNull().put(uniqueTag, hook);
            startTickJob();
        }
    }

TextViewTimer4为自定义view实现类，只做view绘制，不做倒计时操作

	public class TextViewTimer4 extends View {
		......
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
	......
	}

最后使用在XML下自定义布局文件

    <com.example.win7.timertest.TextViewTimer4
        android:id="@+id/tvFlashTime"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        tvtime:colon_color="#ffffff"
        tvtime:custom_background="@drawable/ic_detail_red_wish"
        tvtime:sep_width="10dp"
        tvtime:time_background="#FFEC8B"
        tvtime:time_color="#ffffff"
        tvtime:radius="5dp"
        android:layout_marginTop="30dp"
        tvtime:time_background_type="roundCircle"
        tvtime:time_size="14sp"
        android:layout_marginBottom="30dp"
        tvtime:background_height="30dp"
        tvtime:background_width="30dp"
    />
activity下，这样写就好了

 	  mFlashHook = new AbsCountdownHook(countdown) {
            @Override
            public TextViewTimer4 getCountdownView() {
                return tvFlashTime;
            }
            @Override
            public boolean isViewActive() {
                return getContext() != null;
            }

            @Override
            public void doTimeOver() {
        /*        if (mTimeOverListener != null) {
                    mTimeOverListener.doTimeOver();
                }*/
                Toast.makeText(TimeActivity.this,"时间到了",Toast.LENGTH_SHORT).show();
            }
        };
        TimeViewListener timeViewListener=new CountDownDefault();
        tvFlashTime.setListener(timeViewListener);

        CountdownManager.getInstance().start("haha", mFlashHook);


具体用法大家可以看代码哈，也欢迎大家帮我测试下有无bug，日后进行优化<br/><br/><br/>
<font size="5">给小编点零钱，打赏小编吧！！</font><br/>
<img src="https://i.imgur.com/ff6o3lj.jpg" height = "500" div align="center" /><br/>

					