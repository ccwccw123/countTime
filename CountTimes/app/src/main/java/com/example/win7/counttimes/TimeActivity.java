package com.example.win7.counttimes;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import static java.security.AccessController.getContext;

/**
 * desc:
 * author：ccw
 * date:2018/11/23
 * time:9:51
 */
public class TimeActivity extends AppCompatActivity {
    TextViewTimer4 tvFlashTime;
  /*  @BindView(R.id.tvTimer)
    TextViewTimer6 tvTimer;*/

    /**
     * 闪购倒计时的hook
     */
    private AbsCountdownHook mFlashHook;
    /**
     * 倒计时(秒)
     */
    private int countdown=32;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int[] timeArr = ToolDate.getDayHourMinSecond(95457);
        tvFlashTime=findViewById(R.id.tvFlashTime);
     //   tvFlashTime.refreshTime(timeArr);
     //   String[] strings={timeArr[0]+"","天",timeArr[1]+"","小时",timeArr[2]+"","分",timeArr[3]+"","秒"};
     //   tvFlashTime.setCustomStyle(strings);

        // 顶部倒计时
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


        // 顶部倒计时
  /*      mFlashHook2 = new AbsCountdownHook2(countdown) {
            @Override
            public TextViewTimer6 getCountdownView() {
                return tvTimer;
            }
            //子线程
            @Override
            public void doTimeOver() {
              *//*  if (mTimeOverListener != null) {
                    mTimeOverListener.doTimeOver();
                }*//*
              runOnUiThread(()-> Toast.makeText(TimeActivity.this,"时间到了",Toast.LENGTH_SHORT).show());
            }
        };
        tvTimer.setListener(times-> new String[]{times[0]+"","day",times[1]+"","小时",times[2]+"","分钟",times[3]+"","秒"});
        CountdownManager.getInstance().start2(mFlashHook2);*/
    }
}
