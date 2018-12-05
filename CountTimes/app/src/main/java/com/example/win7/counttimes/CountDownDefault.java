package com.example.win7.counttimes;

/**
 * desc:
 * author：ccw
 * date:2018/11/29
 * time:19:29
 */
public class CountDownDefault implements TimeViewListener{

//    private String[] timeStyle;
//
//    public String[] getTimeStyle() {
//        return timeStyle;
//    }
//
//    public void setTimeStyle(String[] timeStyle) {
//        this.timeStyle = timeStyle;
//    }

    @Override
    public String[] getTextStyle(int[] times) {
//
//        if(timeStyle!=null){
//            return timeStyle;
//        }
        return  new String[]{times[0] + "", "天", times[1] + "", "小时",times[2] + "", "分", times[3] + "", "秒"};
    }
}
