package com.vtech.voiceassistant.util;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.vtech.voiceassistant.wakeup.MyWakeup;
import com.vtech.voiceassistant.wakeup.listener.IWakeupListener;
import com.vtech.voiceassistant.wakeup.listener.RecogWakeupListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmConstraints;
import java.util.HashMap;
import java.util.Map;

import static com.vtech.voiceassistant.recog.IStatus.STATUS_WAKEUP_SUCCESS;

public class WakeupUtil  {
    private static final String TAG = "VoiceAssistant-wakeup";
    private String mSampleDirPath;

    private static WakeupUtil instance;
    private static Context context;

    private MyWakeup myWakeup;
    private Handler handler;
    private RecogUtil recogUtil;

    /**
     * 获取实例，非线程安全
     *
     * @return
     */
    public static WakeupUtil getInstance(Context context) {
        if (instance == null || WakeupUtil.context != context) {
            instance = new WakeupUtil(context);
        }
        return instance;
    }

    public WakeupUtil(Context context){
        this.context = context;
        initWakeUp();
    }

    private void initWakeUp() {

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                try {
                    handleMsg(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        IWakeupListener listener = new RecogWakeupListener(handler);
        myWakeup = new MyWakeup(context, listener);
        startWakeUp();
    }

    protected void handleMsg(Message msg) {
        Log.w(TAG, "handleMsg " + msg);
        switch (msg.what){
            case STATUS_WAKEUP_SUCCESS:
                recogUtil =  RecogUtil.getInstance(context);
                recogUtil.startRecog();
                //TODO:开启指令识别
                break;
            default:
                break;
        }
    }

    // 点击“开始识别”按钮
    // 基于DEMO唤醒词集成第2.1, 2.2 发送开始事件开始唤醒
    private void startWakeUp() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        // "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下

        params.put("appid", DefConst.APP_ID);
        params.put("com.baidu.speech.API_KEY", DefConst.APP_KEY);
        params.put("com.baidu.speech.SECRET_KEY", DefConst.SECRET_KEY);
        myWakeup.start(params);
    }

    // 基于DEMO唤醒词集成第4.1 发送停止事件
    public void stopWakeUp() {
        myWakeup.stop();
    }

    public void release(){
        myWakeup.release();
    }
}
