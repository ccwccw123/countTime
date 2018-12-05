package com.example.win7.counttimes;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * desc:
 * author：ccw
 * date:2018/11/23
 * time:9:40
 */
public class ToolSize {
    /**
     * 将dp值转换为px值
     *
     * @param context context
     * @param dpValue dp值
     * @return 转化后的px值
     */
    static int dp2Px(@NonNull Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
