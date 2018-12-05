package com.example.win7.counttimes;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * desc: 倒计时器管理类（单例）。原理：Handler延时刷新，1秒刷新一次。<br/>
 * 注意：<br/>
 * 1，倒计时不可见状态，需要手动停止倒计时{@link #stop(String)}，重启倒计时{@link #restart(String)}。<br/>
 * 2，页面被销毁的时候，一定要手动调用{@link #remove(String)}，建议在View生命周期函数onDestroy调用。<br/>
 * 3, 在倒计时回调函数中({@link AbsCountdownHook})，不要处理耗时任务，保持极速运行状态。<br/>
 * 4, 如果当JollyChic App 隐藏后台请主动停止倒计时，节约用户电量。<br/>
 * time: 2017-3-1 下午1:45:00 <br/>
 * author: ccw <br/>
 * since V 5.9 <br/>
 */
public class CountdownManager {

    private static final byte WHAT_TICK = 1; // 倒计时Handler what值

    private static CountdownManager mInstance;
    private Handler mTimerTickHandler; // 计时器handler
    private Map<String, AbsCountdownHook> mCountdownHookMap; // 倒计时回调对象集合


    public static CountdownManager getInstance() {
        if (mInstance == null) {
            mInstance = new CountdownManager();
        }

        return mInstance;
    }
//* @param uniqueTag 倒计时唯一标识。一般使用 {@link IBaseView#getTagClassName()}值。
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
    /**
     * 重启倒计时任务。
     *
     * @param uniqueTag 倒计时唯一标识。
     */
    public void restart(String uniqueTag) {
        changeHookTickState(mCountdownHookMap, uniqueTag, false);
        toggleTick(mCountdownHookMap);
    }

    /**
     * 停止倒计时任务。
     *
     * @param uniqueTag 倒计时唯一标识。
     */
    public void stop(String uniqueTag) {
        changeHookTickState(mCountdownHookMap, uniqueTag, true);
    }

    /**
     * 删除倒计时任务 (一般在页面destroy时候使用）
     *
     * @param uniqueTag 倒计时唯一标识。
     */
    public void remove(String uniqueTag) {
        if (mCountdownHookMap != null && uniqueTag != null) {
            mCountdownHookMap.remove(uniqueTag);
            toggleTick(mCountdownHookMap);
        }
    }

    /**
     * 清除所有倒计时任务 (一般在退出App的时候使用)
     */
    public void clear() {
        if (mCountdownHookMap != null) {
            mCountdownHookMap.clear();
        }
    }

    /**
     * 倒计时任务列表中是否含有当前指定的任务；<br/>
     * true：有
     */
    public boolean containsTag(String uniqueTag) {
        return (uniqueTag != null
                && mCountdownHookMap != null
                && mCountdownHookMap.containsKey(uniqueTag));
    }

    //========== private

    /**
     * 检查运行环境
     */
 /*   private void checkEnvironment(@NonNull String uniqueTag, @NonNull AbsCountdownHook hook) {
        if (BuildConfig.IS_LOG_DEBUG) {
            // 后台线程拦截
            if (Looper.myLooper() != Looper.getMainLooper()) {
                ToolException.throwIllegalStateExceptionError("checkEnvironment()",
                        "CountdownManager不支持后台线程调用！");
            }

            if (hook.getCountdownView() == null) {
                ToolException.throwIllegalStateExceptionError("checkEnvironment()",
                        "hook.getCountdownView()返回是null，应该返回倒计时UI组件！");
            }

            if (!hook.isViewActive()) {
                ToolException.throwIllegalStateExceptionError("checkEnvironment()",
                        "视图已经过期，不应该加入倒计时任务中！");
            }

            if (mCountdownHookMap != null && mCountdownHookMap.containsKey(uniqueTag)) {
                ToolException.throwIllegalStateExceptionError("checkEnvironment()",
                        "uniqueTag：" + uniqueTag + "已经存在倒计时任务中。有三个解决方案：" +
                                "1.优先使用restart()函数重启倒计时；2.使用remove()函数后再start()；3.重新设置uniqueTag值.");
            }
        }
    }*/

    /**
     * 设置倒计时默认开始时间
     */
    private void initDefaultBeginTime(@NonNull AbsCountdownHook hook) {
        if (hook.getBeginTime() <= 0) {
            hook.setBeginTime(System.currentTimeMillis() / 1000);
        }
    }
//    /**
//     * 设置倒计时默认开始时间
//     */
//    private void initDefaultBeginTime2(@NonNull AbsCountdownHook2 hook) {
//        if (hook.getBeginTime() <= 0) {
//            hook.setBeginTime(System.currentTimeMillis() / 1000);
//        }
//    }
    @NonNull
    private Map<String, AbsCountdownHook> getHooksOrCreateIfNull() {
        if (mCountdownHookMap == null) {
            mCountdownHookMap = new ConcurrentHashMap<>(5);
        }

        return mCountdownHookMap;
    }

    /**
     * 修改倒计时任务运行状态
     *
     * @param isStop true：停止派发倒计时回调事件；false：开启回调
     */
    private void changeHookTickState(Map<String, AbsCountdownHook> countdownHookMap,
                                     String uniqueTag, boolean isStop) {
        if ((!TextUtils.isEmpty(uniqueTag)) && (countdownHookMap != null)) {
            AbsCountdownHook hook = countdownHookMap.get(uniqueTag);

            if (hook != null) {
                hook.setStop(isStop);
            }
        }
    }

    /**
     * 开关倒计时任务
     */
    private void toggleTick(Map<String, AbsCountdownHook> countdownHookMap) {
        if (hasTickHook(countdownHookMap)) {
            startTickJob();
        } else {
            stopTickJob();
        }
    }

    /**
     * 是否含有未关闭的倒计时任务
     *
     * @return true：有；false：无
     */
    private boolean hasTickHook(Map<String, AbsCountdownHook> countdownHookMap) {
        if (countdownHookMap != null) {
            for (AbsCountdownHook hook : countdownHookMap.values()) {
                if (!hook.isStop()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 启动倒计时器
     */
    private void startTickJob() {
        if (!getTickHandlerOrCreateIfNull().hasMessages(WHAT_TICK)) {
            getTickHandlerOrCreateIfNull().sendEmptyMessageDelayed(WHAT_TICK, 1000);
        }
    }

    /**
     * 停止倒计时任务
     */
    private void stopTickJob() {
        if (mTimerTickHandler != null) {
            getTickHandlerOrCreateIfNull().removeMessages(WHAT_TICK);
        }
    }

    /**
     * 获取到计时handler
     *
     * @return handler
     */
    @NonNull
    private Handler getTickHandlerOrCreateIfNull() {
        if (mTimerTickHandler == null) {
            mTimerTickHandler = new Handler(msg -> {
                // ToolLog.v2Console("== tid:" + Thread.currentThread().getDesignId() + ", size:" + mCountdownHookMap.size());
                doHooksCountdown(mCountdownHookMap);
                toggleTick(mCountdownHookMap);
                return false;
            });
        }

        return mTimerTickHandler;
    }

    /**
     * 处理所有倒计时
     */
    private void doHooksCountdown(Map<String, AbsCountdownHook> countdownHookMap) {
        if (countdownHookMap != null) {
            for (String uniqueTag : countdownHookMap.keySet()) {
                AbsCountdownHook hook = countdownHookMap.get(uniqueTag);

                if (hook != null) {
                    if (!hook.isViewActive()) {// 视图过期
                        countdownHookMap.remove(uniqueTag);
                    } else if (!hook.isStop()) {// hook非停止状态，处理倒计时
                        doSingleCountdown(countdownHookMap, uniqueTag);
                    }
                }
            }
        }
    }

    /**
     * 处理单个倒计时
     */
    private void doSingleCountdown(@NonNull Map<String, AbsCountdownHook> countdownHookMap, @NonNull String uniqueTag) {
        AbsCountdownHook hook = countdownHookMap.get(uniqueTag);
        long subTime = (System.currentTimeMillis() / 1000) - hook.getBeginTime();

        if (subTime <= hook.getCountdownTime()) {// 继续倒计时
            if (hook.getCountdownView() != null) {
                //刷新倒计时时间
                refreshTime(hook,hook.getCountdownTime() - subTime);
                // 倒计时事件派发
                hook.doCountdown();
            } else {// 这种属于异常情况
                countdownHookMap.remove(uniqueTag);
            }
        } else {// 倒计时结束
            countdownHookMap.remove(uniqueTag);
            hook.setCountdownTime(0);
            hook.doTimeOver();
        }
    }
    /**
     * 刷新时间
     *
     * @param countdownTime 倒计时时间
     * @return true：停止刷新：false：继续刷新时间
     */
    public void refreshTime(AbsCountdownHook hook,long countdownTime) {
        int[] timeArrWithDay = ToolDate.getDayHourMinSecond(countdownTime);
        hook.getCountdownView().refreshTime(timeArrWithDay);
    }
}
