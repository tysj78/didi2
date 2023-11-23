package com.naruto.didi2.activity.test;

import android.annotation.TargetApi;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DELL on 2021/9/9.
 */

public class UseTimeDataManager {
    public static final String TAG = "yylog";

    private static UseTimeDataManager mUseTimeDataManager;

    private Context mContext;

    private int mDayNum;
    private long mStartTime;
    private long mEndTime;

    //记录从系统中读取的数据
//    private ArrayList<UsageEvents.Event> mEventList;
//    private ArrayList<UsageEvents.Event> mEventListChecked;
//    private ArrayList<UsageStats> mStatsList;

    //记录打开一次应用，使用的activity详情
//    private ArrayList<OneTimeDetails> mOneTimeDetailList = new ArrayList<>();

    //记录某一次打开应用的使用情况（查询某一次使用情况的时候，用于界面显示）
//    private OneTimeDetails mOneTimeDetails;

    //主界面数据
    private ArrayList<PackageInfo> mPackageInfoList = new ArrayList<>();


    public UseTimeDataManager(Context context) {
        this.mContext = context;
    }

    public static UseTimeDataManager getInstance(Context context) {
        if (mUseTimeDataManager == null) {
            mUseTimeDataManager = new UseTimeDataManager(context);
        }

        return mUseTimeDataManager;
    }

    /**
     * 主要的数据获取函数
     *
     * @return int        0 : event usage 均查询到了
     * 1 : event 未查询到 usage 查询到了
     * 2 : event usage 均未查询到
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public int refreshData(List<String> pkg, long everyZeroMollis, long currentTimeMillis) {
//        mDayNum = dayNumber;
        ArrayList<UsageEvents.Event> mEventList = getEventList(everyZeroMollis, currentTimeMillis);
//        ArrayList<UsageStats> mStatsList = getUsageList(pkg, everyZeroMollis, currentTimeMillis);
        ArrayList<OneTimeDetails> mOneTimeDetailList = new ArrayList<>();

        if (mEventList == null || mEventList.size() == 0) {
            Log.i(TAG, " UseTimeDataManager-refreshData()   未查到events");

//            if (mStatsList == null || mStatsList.size() == 0) {
//                Log.i(TAG, " UseTimeDataManager-refreshData()   未查到stats");
//                return 2;
//            }

            return 1;
        } else {
//            if (mStatsList == null || mStatsList.size() == 0) {
//                Log.i(TAG, " UseTimeDataManager-refreshData()   未查到stats");
//                return 2;
//            }
        }

        Log.i(TAG, " UseTimeDataManager-refreshData():" + mEventList.size());
        //获取数据之后，进行数据的处理
//        ArrayList<UsageEvents.Event> mEventListChecked = getEventListChecked(mEventList);
        refreshOneTimeDetailList(0, mEventList, mOneTimeDetailList);
//        refreshPackageInfoList();


//        sendEventBus();

//        String packageName = mStatsList.get(0).getPackageName();
//        if (pkg.equals(packageName)) {
        calculateUseTime(pkg, mOneTimeDetailList);

//        } else {
//            LogUtils.e(pkg + ":未获取到应用使用时长");
//        }


//        for (com.naruto.didi2.activity.test.PackageInfo info : mPackageInfoList) {
//            LogUtils.e("应用运行信息：" + info.toString());
//        }
        return 0;
    }

    //分类完成，初始化主界面所用到的数据
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
/*    public void refreshPackageInfoList() {
        mPackageInfoList.clear();
        for (int i = 0; i < mStatsList.size(); i++) {
            //屏蔽系统应用
//            if (!isSystemApp(mContext, mStatsList.get(i).getPackageName())) {
                PackageInfo info = new PackageInfo(0, calculateUseTime(mStatsList.get(i).getPackageName()), mStatsList.get(i).getPackageName(), getApplicationNameByPackageName(mContext, mStatsList.get(i).getPackageName()));
                mPackageInfoList.add(info);
//            }
        }

        for (int n = 0; n < mPackageInfoList.size(); n++) {
            String pkg = mPackageInfoList.get(n).getmPackageName();
            for (int m = 0; m < mOneTimeDetailList.size(); m++) {
                if (pkg.equals(mOneTimeDetailList.get(m).getPkgName())) {
                    mPackageInfoList.get(n).addCount();
                }
            }
        }
    }*/

    //按照使用时间的长短进行排序，获取应用使用情况列表
    public ArrayList getmPackageInfoListOrderByTime() {
        Log.i(TAG, " UseTimeDataManager-getmPackageInfoListOrderByTime()   排序前：mPackageInfoList.size()" + mPackageInfoList.size());

        for (int n = 0; n < mPackageInfoList.size(); n++) {
            for (int m = n + 1; m < mPackageInfoList.size(); m++) {
                if (mPackageInfoList.get(n).getmUsedTime() < mPackageInfoList.get(m).getmUsedTime()) {
                    PackageInfo temp = mPackageInfoList.get(n);
                    mPackageInfoList.set(n, mPackageInfoList.get(m));
                    mPackageInfoList.set(m, temp);
                }
            }
        }

        Log.i(TAG, " UseTimeDataManager-getmPackageInfoListOrderByTime()   排序后：mPackageInfoList.size()" + mPackageInfoList.size());
        return mPackageInfoList;
    }

    //按照使用次数的多少进行排序，获取应用使用情况列表
    public ArrayList getmPackageInfoListOrderByCount() {
        Log.i(TAG, " UseTimeDataManager-getmPackageInfoListOrderByCount()   排序前：mPackageInfoList.size()" + mPackageInfoList.size());

        for (int n = 0; n < mPackageInfoList.size(); n++) {
            for (int m = n + 1; m < mPackageInfoList.size(); m++) {
                if (mPackageInfoList.get(n).getmUsedCount() < mPackageInfoList.get(m).getmUsedCount()) {
                    PackageInfo temp = mPackageInfoList.get(n);
                    mPackageInfoList.set(n, mPackageInfoList.get(m));
                    mPackageInfoList.set(m, temp);
                }
            }
        }

        Log.i(TAG, " UseTimeDataManager-getmPackageInfoListOrderByCount()   排序后：mPackageInfoList.size()" + mPackageInfoList.size());
        return mPackageInfoList;
    }

    /**
     * @TargetApi(Build.VERSION_CODES.LOLLIPOP) private void sendEventBus(){
     * TimeEvent event = new TimeEvent(0,0);
     * if(mEventListChecked != null && mEventListChecked.size() > 0){
     * event.setmStartTime(mEventListChecked.get(0).getTimeStamp());
     * event.setmEndTime(mEventListChecked.get(mEventListChecked.size()-1).getTimeStamp());
     * }
     * MsgEventBus.getInstance().post(event);
     * }
     **/

    //从系统中获取event数据
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ArrayList getEventList(long start, long dayNumber) {
/*        ArrayList mEventList = new ArrayList<>();
//        Calendar calendar = Calendar.getInstance();
//        long endTime = calendar.getTimeInMillis();
//        calendar.add(Calendar.YEAR, -1);
//        //long startTime = calendar.getTimeInMillis()- 3 * DateTransUtils.DAY_IN_MILLIS;
//        long startTime = calendar.getTimeInMillis();

        long endTime = 0, startTime = 0;
        if (dayNumber == 0) {
            endTime = System.currentTimeMillis();
            startTime = DateTransUtils.getZeroClockTimestamp(endTime);
        } else {
            endTime = DateTransUtils.getZeroClockTimestamp(System.currentTimeMillis() - (dayNumber - 1) * DateTransUtils.DAY_IN_MILLIS) - 1;
            startTime = endTime - DateTransUtils.DAY_IN_MILLIS + 1;
        }*/
//        return EventUtils.getEventList(mContext, startTime, endTime);
        return EventUtils.getEventList(mContext, start, dayNumber);
    }

    //从系统中获取Usage数据
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ArrayList getUsageList(String pkg, long start, long dayNumber) {
/*
        long endTime = 0, startTime = 0;
        if (dayNumber == 0) {
            endTime = System.currentTimeMillis();
            startTime = DateTransUtils.getZeroClockTimestamp(endTime);
        } else {
            endTime = DateTransUtils.getZeroClockTimestamp(System.currentTimeMillis() - (dayNumber - 1) * DateTransUtils.DAY_IN_MILLIS) - 1;
            startTime = endTime - DateTransUtils.DAY_IN_MILLIS + 1;
        }
*/

//        return EventUtils.getUsageList(mContext, startTime, endTime);
        return EventUtils.getUsageList(mContext, pkg, start, dayNumber);
    }

    //仅保留 event 中 type 为 1或者2 的
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ArrayList getEventListChecked(ArrayList<UsageEvents.Event> mEventList) {
        ArrayList mList = new ArrayList<>();
        for (int i = 0; i < mEventList.size(); i++) {
            if (mEventList.get(i).getEventType() == 1 || mEventList.get(i).getEventType() == 2) {
                mList.add(mEventList.get(i));
            }
        }
        return mList;
    }

    public String getApplicationNameByPackageName(Context context, String packageName) {

        PackageManager pm = context.getPackageManager();
        String Name;
        try {
            Name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Name = "";
        }
        return Name;

    }

   /* @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ArrayList getEventListCheckWithoutErrorData() {
        ArrayList mList = new ArrayList<>();
        for (int i = 0; i < mEventList.size(); i++) {
            if (mEventList.get(i).getEventType() == 1 || mEventList.get(i).getEventType() == 2) {
                mList.add(mEventList.get(i));
            }
        }
        return mList;
    }*/

    //从 startIndex 开始分类event  直至将event分完
    //每次从0开始，将原本的 mOneTimeDetailList 清除一次,然后开始分类
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void refreshOneTimeDetailList(int startIndex, ArrayList<UsageEvents.Event> mEventListChecked, ArrayList<OneTimeDetails> mOneTimeDetailList) {
//        Log.i(TAG, "  refreshOneTimeDetailList()     startIndex : " + startIndex);

        if (startIndex == 0) {
//            Log.i(TAG, "  refreshOneTimeDetailList()     每次从0开始，将原本的 mOneTimeDetailList 清除一次,然后开始分类 ");
            if (mOneTimeDetailList != null) {
                mOneTimeDetailList.clear();
            }
        }

        long totalTime = 0;
        int usedIndex = 0;
        String pkg = null;
        ArrayList<UsageEvents.Event> list = new ArrayList<>();
        for (int i = startIndex; i < mEventListChecked.size(); i++) {
            if (i == startIndex) {
//                if (mEventListChecked.get(i).getEventType() == 2) {
//                    Log.i(TAG, "  refreshOneTimeDetailList()     warning : 每次打开一个app  第一个activity的类型是 2     ");
//                }
                pkg = mEventListChecked.get(i).getPackageName();
                list.add(mEventListChecked.get(i));
            } else {
                if (pkg != null) {
                    if (pkg.equals(mEventListChecked.get(i).getPackageName())) {
                        list.add(mEventListChecked.get(i));
                        if (i == mEventListChecked.size() - 1) {
                            usedIndex = i;
                        }
                    } else {
                        usedIndex = i;
                        break;
                    }
                }
            }
        }

//        Log.i(TAG, "   mEventListChecked 分类:   before  check :   list.size() = " + list.size());
        checkEventList(list);
//        Log.i(TAG, "   mEventListChecked 分类:   after  check :   list.size() = " + list.size());
//        startTime = list.get(0).getTimeStamp();
//        endTime   = list.get( list.size() - 1 ).getTimeStamp();
//        Log.i(TAG, "   mEventListChecked 分类:  本次启动的包名：" + list.get(0).getPackageName() + "   时间：" + DateUtils.formatSameDayTime(list.get(0).getTimeStamp(), System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));
        for (int i = 1; i < list.size(); i += 2) {
            if (list.get(i).getEventType() == 2 && list.get(i - 1).getEventType() == 1) {
                totalTime += (list.get(i).getTimeStamp() - list.get(i - 1).getTimeStamp());
            }
        }
        OneTimeDetails oneTimeDetails = new OneTimeDetails(pkg, totalTime, null);
//        LogUtils.e("mOneTimeDetailList。add");
        mOneTimeDetailList.add(oneTimeDetails);

        if (usedIndex < mEventListChecked.size() - 1) {
            refreshOneTimeDetailList(usedIndex, mEventListChecked, mOneTimeDetailList);
        } else {
//            Log.i(TAG, "  refreshOneTimeDetailList()     已经将  mEventListChecked 分类完毕   ");
        }

    }

 /*   public ArrayList getPkgOneTimeDetailList(String pkg) {

        if ("all".equals(pkg)) {
            return mOneTimeDetailList;
        }

        ArrayList list = new ArrayList<>();
        if (mOneTimeDetailList != null && mOneTimeDetailList.size() > 0) {
            for (int i = 0; i < mOneTimeDetailList.size(); i++) {
                if (mOneTimeDetailList.get(i).getPkgName().equals(pkg)) {
                    list.add(mOneTimeDetailList.get(i));
                }
            }
        }
        return list;
    }*/

    // 采用回溯的思想：
    // 从头遍历EventList，如果发现异常数据，则删除该异常数据，并从头开始再次进行遍历，直至无异常数据
    // （异常数据是指：event 均为 type=1 和type=2 ，成对出现，一旦发现未成对出现的数据，即视为异常数据）
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void checkEventList(ArrayList<UsageEvents.Event> list) {
        boolean isCheckAgain = false;
        for (int i = 0; i < list.size() - 1; i += 2) {
            if (list.get(i).getClassName().equals(list.get(i + 1).getClassName())) {
                if (list.get(i).getEventType() != 1) {
                    Log.i(UseTimeDataManager.TAG, "   EventList 出错  ： " + list.get(i).getPackageName() + "  " + DateUtils.formatSameDayTime(list.get(i).getTimeStamp(), System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM).toString());
                    list.remove(i);
                    isCheckAgain = true;
                    break;
                }
                if (list.get(i + 1).getEventType() != 2) {
                    Log.i(UseTimeDataManager.TAG, "   EventList 出错 ： " + list.get(i + 1).getPackageName() + "  " + DateUtils.formatSameDayTime(list.get(i + 1).getTimeStamp(), System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM).toString());
                    list.remove(i);
                    isCheckAgain = true;
                    break;
                }
            } else {
                //i和i+1的className对不上，则删除第i个数据，重新检查
                list.remove(i);
                isCheckAgain = true;
                break;
            }
        }
        if (isCheckAgain) {
            checkEventList(list);
        }
    }

    // =======================================
    // service use
    // =======================================

    public ArrayList getPkgInfoListFromEventList() {
        return mPackageInfoList;
    }

   /* @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ArrayList getPkgInfoListFromUsageList() throws IllegalAccessException {
        ArrayList result = new ArrayList<>();

        if (mStatsList != null && mStatsList.size() > 0) {
            for (int i = 0; i < mStatsList.size(); i++) {

                result.add(new PackageInfo(getLaunchCount(mStatsList.get(i)), mStatsList.get(i).getTotalTimeInForeground(), mStatsList.get(i).getPackageName(), getApplicationNameByPackageName(mContext, mStatsList.get(i).getPackageName())));

            }
        }
        return result;
    }*/

    //判断app是否为系统qpp
    public boolean isSystemApp(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return ai != null && (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 利用反射，获取UsageStats中统计的应用使用次数
    private int getLaunchCount(UsageStats usageStats) throws IllegalAccessException {
        Field field = null;
        try {
            field = usageStats.getClass().getDeclaredField("mLaunchCount");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return (int) field.get(usageStats);
    }

    //根据event计算使用时间
    public void calculateUseTime(List<String> pkg, ArrayList<OneTimeDetails> mOneTimeDetailList) {
        for (String p : pkg) {
            long useTime = 0;
            for (int i = 0; i < mOneTimeDetailList.size(); i++) {
                if (mOneTimeDetailList.get(i).getPkgName().equals(p)) {
                    useTime += mOneTimeDetailList.get(i).getUseTime();
                }
            }
            Log.i(TAG, p + "  calculateUseTime : " + useTime);
        }
    }
    // =======================================
    // getter and setter
    // =======================================


    public int getmDayNum() {
        return mDayNum;
    }

    public void setmDayNum(int mDayNum) {
        this.mDayNum = mDayNum;
    }

  /*  public ArrayList getmOneTimeDetailList() {
        return mOneTimeDetailList;
    }*/

  /*  public OneTimeDetails getmOneTimeDetails() {
        return mOneTimeDetails;
    }

    public void setmOneTimeDetails(OneTimeDetails mOneTimeDetails) {
        this.mOneTimeDetails = mOneTimeDetails;
    }*/

 /*   @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UsageStats getUsageStats(String pkg) {
        for (int i = 0; i < mStatsList.size(); i++) {
            if (mStatsList.get(i).getPackageName().equals(pkg)) {
                return mStatsList.get(i);
            }
        }
        return null;
    }*/
}
