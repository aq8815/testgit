package com.vtech.voiceassistant;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.vtech.voiceassistant.util.DefConst;
import com.vtech.voiceassistant.util.HealthUtil;
import com.vtech.voiceassistant.util.SpeechUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AsrCmdParse {
    private static final String TAG = "VoiceAssistant-parse";
    private Context context;
    private String keyWrod;
    int type;
    private HealthUtil healthUtil;

    public AsrCmdParse(Context context) {
        this.context = context;
    }


    public int parseCmd(String val) {

        if (val == null || val.length() < 3) {
            type = DefConst.ASR_CMD_UNKNOW;
            Log.w(TAG, "unknow asr cmd");
            SpeechUtil.getInstance(context).speak("未识别到您的命令");
        } else if (val.startsWith(DefConst.WORD_START) ||
                val.startsWith(DefConst.WORD_START1)) {
            type = DefConst.ASR_CMD_START_APP;
            Log.w(TAG, "val is " + val);
            actionOpen(val.substring(2));
        } else if (val.indexOf(DefConst.WORD_DAIL) != -1 ||
                val.indexOf(DefConst.WORD_DAIL1) != -1) {
            type = DefConst.ASR_CMD_DAIL;
            actionDailer(val);
        } else if (val.indexOf(DefConst.WORD_HEALTHY) != -1 ||
                val.indexOf(DefConst.WORD_HEALTHY1) != -1) {
            type = DefConst.ASR_CMD_PLAY_HEALTHY;
            actionHealthReport();
        } else if (val.indexOf(DefConst.WORD_SCHEDULE) != -1 ||
                val.indexOf(DefConst.WORD_SCHEDULE1) != -1) {
            type = DefConst.ASR_CMD_PLAY_SCHEDULE;
        } else if (val.indexOf(DefConst.WORD_SASETY) != -1 ||
                val.indexOf(DefConst.WORD_SASETY1) != -1) {
            type = DefConst.ASR_CMD_PLAY_SASETY;
        } else {
            type = DefConst.ASR_CMD_UNKNOW;
        }
        return type;
    }

    private void actionHealthReport() {
        healthUtil = HealthUtil.getInstance(context);

        int heart_rate = healthUtil.getHRate();
        int sbp = healthUtil.getSBP();
        int dbp = healthUtil.getDBP();

        SpeechUtil.getInstance(context).speak("当前心率 " + heart_rate + " 高压" + sbp + "低压" + dbp);
    }

    private int actionDailer(String val) {

        String keyword = getDailerWord(val);
        String phone = "";
        if (keyword.isEmpty()) {
            Log.w(TAG, "not recognize dailer");
        } else if (isPhoneNum(keyword)) {
            phone = keyword;
            SpeechUtil.getInstance(context).speak("拨号给" + phone);
        } else {
            List<String> nums = queryNumber(keyword, context);
            if (nums.isEmpty()) {
                Log.w(TAG, "not found phonenumber for " + val);
                SpeechUtil.getInstance(context).speak(" 没有找到联系人" + keyword);
            } else {
                SpeechUtil.getInstance(context).speak("正在给" + keyword + "拨号");
                phone = nums.get(0);
            }
        }
        if(!phone.isEmpty()) {
            //Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        return 0;
    }

    private void  actionOpen(String name) {
        String pkgname= getPackageByName(name);
        Log.w(TAG, "正在打开 " + name + " package name is " + pkgname);
        if(!pkgname.isEmpty()) {
            SpeechUtil.getInstance(context).speak("正在打开" + name);
            turnApp(pkgname);
        } else {
            SpeechUtil.getInstance(context).speak("未找到" + name);
        }
    }

    int actionPlay(String val) {
        return  0;
    }


    /**
     *
     * @param phone
     * @return
     */
    private boolean isPhoneNum(String phone) {
        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(phone);

        return m.matches();

    }

    private String getDailerWord(String val) {
        //FIXME: user baidu unit
        String word;
        if (val.startsWith("打电话给")) {
            word = val.substring("打电话给".length());
        } else if (val.startsWith("拨号给")) {
            word = val.substring("拨号给".length());
        } else if (val.endsWith("拨号") && val.startsWith("给")) {
            word = val.substring("给".length(), val.length()-"拨号".length());
        } else if (val.endsWith("打电话")&&val.startsWith("给")) {
            word = val.substring("给".length(), val.length()-"打电话".length());
        }else {
            word = "";
        }

        return word;
    }

    /**
     * @param name 联系人姓名
     * @描述 查询该联系人下的所有号码
     */
    private List<String> queryNumber(String name, Context context) {
        List<String> numbers = new ArrayList<>();
        Cursor cursor = null;
        Cursor phoneCursor = null;
        try {
            //使用ContentResolver查找联系人数据
            cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            //遍历查询结果，找到所需号码
            while (cursor.moveToNext()) {
                //获取联系人ID
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                //获取联系人的名字
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                if (name.equalsIgnoreCase(contactName)) {
                    // 查看联系人有多少个号码，如果没有号码，返回0
                    int phoneCount = cursor
                            .getInt(cursor
                                    .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    if (phoneCount > 0) {
                        // 获得联系人的电话号码列表
                        phoneCursor = context.getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                        + "=" + contactId, null, null);
                        if (phoneCursor.moveToFirst()) {
                            do {
                                //遍历所有的联系人下面所有的电话号码
                                String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                numbers.add(phoneNumber);
                            } while (phoneCursor.moveToNext());
                        }
                    }
                    //使用ContentResolver查找联系人的电话号码
                    //                Cursor phone = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                    //                if (phone.moveToNext()) {
                    //                    String phoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    //                    numbers.add(phoneNumber);
                    //                }
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.w(TAG, e.toString());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            if (phoneCursor != null) {
                phoneCursor.close();
                phoneCursor = null;
            }
        }
        return numbers;
    }



    private String getPackageByName(String name){
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> list = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES );
        int i = 0;
        for (PackageInfo packageInfo : list) {
            // 获取到设备上已经安装的应用的名字,即在AndriodMainfest中的app_name。
            String appName = packageInfo.applicationInfo.loadLabel(
                    context.getPackageManager()).toString();
            // 获取到应用所在包的名字,即在AndriodMainfest中的package的值。
            Log.i("zyn", "应用的名字:" + appName);
            Log.i("zyn", "应用的包名字:" + packageInfo.packageName);
            if (name.equals(appName)) {
                return packageInfo.packageName;
            }
            i++;
        }
        Log.i(TAG, "应用的总个数:" + i);
        return "";

    }


    private void turnApp(String packagename) {
        try {
            //方法1：通过包名获取类名
            /*
            PackageInfo packageInfo = getPackageManager().getPackageInfo(apppackage,0);
            Intent intentWeixin = new Intent(Intent.ACTION_MAIN,null);
            intentWeixin.setPackage(packageInfo.packageName);
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> apps = packageManager.queryIntentActivities(intentWeixin,0);
            ResolveInfo resolveInfo = apps.iterator().next();
            if (resolveInfo != null) {
                apppackage = resolveInfo.activityInfo.packageName;
                String className = resolveInfo.activityInfo.name;
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName componentName = new ComponentName(apppackage,className);
                textResult.append(apppackage + "\n" + className);
                intent.setComponent(componentName);
                startActivity(intent);
            }*/

            //方法2：通过包名直接启动应用
            Intent intent;
            PackageManager packageManager=context.getPackageManager();
            intent = packageManager.getLaunchIntentForPackage(packagename);
            context.startActivity(intent);
        } catch (Exception e){
            install(packagename);
        }
    }

    //判断app是否安装
    protected boolean isAppInstalled(String packagename) {
        try {
            context.getPackageManager().getPackageInfo(packagename,0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    //自动下载
    protected void install(String packagename) {
        Uri uri = Uri.parse("market://details?id=" + packagename);
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e){
        }
    }
}
