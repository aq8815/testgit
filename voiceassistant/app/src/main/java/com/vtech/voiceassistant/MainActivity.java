package com.vtech.voiceassistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.baidu.speech.asr.SpeechConstant;
import com.vtech.voiceassistant.recog.MyRecognizer;
import com.vtech.voiceassistant.recog.listener.IRecogListener;
import com.vtech.voiceassistant.recog.listener.MessageStatusRecogListener;
import com.vtech.voiceassistant.util.RecogUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    protected MyRecognizer myRecognizer;
    ServiceConnection conn;
    VoiceAssistantService.VoiceBinder binder = null;

    private static final String TAG = "VoiceAssistant";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder){
                Log.w(TAG, "binder service connected");
                binder = (VoiceAssistantService.VoiceBinder)iBinder;
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        Intent intent = new Intent(MainActivity.this,VoiceAssistantService.class);
        bindService(intent,conn, BIND_AUTO_CREATE);//开启服务


        new Handler().postDelayed(new Runnable()
        {
            public void run()
            {
                if (binder != null){
                    binder.play("语音助手已启动");

                }
            }
        }, 4000);


        /*
        Intent intent1 = new Intent(MainActivity.this , VoiceAssistantService.class);
        startForegroundService(intent1);
        */
    }

    private void asr_init(){

        Handler handler = new Handler() {

            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handleMsg(msg);
            }

        };

        IRecogListener listener = new MessageStatusRecogListener(handler);
        myRecognizer = new MyRecognizer(this, listener);

    }
     public void onClick( View v) {

         Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + 10086));
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(intent);


/*
         Map<String, Object> params = new LinkedHashMap<String, Object>();
         params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
         params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
         // 如识别短句，不需要需要逗号，使用1536搜索模型。其它PID参数请看文档
         params.put(SpeechConstant.PID, 1536);
         myRecognizer.start(params);
         */
     }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }
    protected void handleMsg(Message msg) {
        if (msg.obj != null) {
            // txtLog.append(msg.obj.toString() + "\n");
            Log.e("baidu", "msg is " + msg.obj.toString());
            String result = msg.obj.toString();
            if(result.contains("说话结束到识别结束耗时")) {
                Log.e("baidu", "the first 引号 是 " + result.indexOf("”"));
                int first = result.indexOf("”");
                result = result.substring(first+1);
                Log.e("baidu", "first 引号 is " + result);
                int second = result.indexOf("”");
                Log.e("baidu", "the second 引号 是 " + second);
                if(second>0)
                    result = result.substring(0, second);
                // else
                //    result = "unknow";
                Log.e("baidu", "result is " + result);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }

}
