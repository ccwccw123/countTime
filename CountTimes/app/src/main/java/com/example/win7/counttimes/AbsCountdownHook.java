package com.example.win7.counttimes;


/**
 * author: ccw <br/>
 */
public abstract class AbsCountdownHook {

    /**
     * 倒计时开始时间，单位：秒。 <br/>
     * 如：2017年3月2日 12:12:12 时间点的Long类型秒数。
     */
    private long mBeginTime;
    /**
     * 倒计时时间，单位：秒。<br/>
     * 如：有8个小时倒计时时间，该值为8个小时转换后的Long类型秒数。
     */
    private long mCountdownTime;
    /**
     * 是否停止派发回调，默认false。 <br/>
     * true:停止；false：继续倒计时回调
     */
    private boolean mStop;



    //================== 构造函数
    public AbsCountdownHook(long countdownTime) {
        this.mCountdownTime = countdownTime;
    }

    public AbsCountdownHook(long countdownTime, long beginTimeMillisecond) {
        this.mCountdownTime = countdownTime;
        this.mBeginTime = beginTimeMillisecond / 1000;
    }


    //================== 抽象函数

    /**
     * 获取倒计时UI组件
     */
    public abstract TextViewTimer4 getCountdownView();

    /**
     * 倒计时组件容器View是否处于活动状态。<br/>
     * 可以使用{@link IBaseView#isActive()}返回值
     *
     * @return true：是；false：view被回收
     */
    public abstract boolean isViewActive();

    /**
     * 倒计时事件
     */
    public void doCountdown() {
        // 具体子类重写
    }

    /**
     * 倒计时结束事件
     */
    public void doTimeOver() {
        // 具体子类重写
    }

    //================== getter and setter
    public long getBeginTime() {
        return mBeginTime;
    }

    public void setBeginTime(long beginTime) {
        this.mBeginTime = beginTime;
    }

    public long getCountdownTime() {
        return mCountdownTime;
    }

    public void setCountdownTime(long countdownTime) {
        this.mCountdownTime = countdownTime;
    }

    public boolean isStop() {
        return mStop;
    }

    public void setStop(boolean isStop) {
        this.mStop = isStop;
    }

}
