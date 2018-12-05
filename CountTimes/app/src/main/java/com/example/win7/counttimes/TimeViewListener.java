package com.example.win7.counttimes;

/**
 * desc:
 * author：ccw
 * date:2018/11/29
 * time:19:29
 */
public interface TimeViewListener {

    default String[] getTextStyle(int[] times){
         return  new String[]{times[1] + "", ":", times[2] + "", ":", times[3] + "", ""};
    }

}
