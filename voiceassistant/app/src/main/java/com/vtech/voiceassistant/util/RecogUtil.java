package com.vtech.voiceassistant.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.baidu.speech.asr.SpeechConstant;
import com.vtech.voiceassistant.AsrCmdParse;
import com.vtech.voiceassistant.recog.MyRecognizer;
import com.vtech.voiceassistant.recog.listener.IRecogListener;
import com.vtech.voiceassistant.recog.listener.MessageStatusRecogListener;
import com.vtech.voiceassistant.wakeup.MyWakeup;
import com.vtech.voiceassistant.wakeup.listener.IWakeupListener;
import com.vtech.voiceassistant.wakeup.listener.RecogWakeupListener;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.vtech.voiceassistant.recog.IStatus.STATUS_ASR_FINAL_RESULT;


public class RecogUtil {
    private static final String TAG = "VoiceAssistant-recog";



    /**
     * 0: 方案1， backTrackInMs > 0,唤醒词说完后，直接接句子，中间没有停顿。
     *              开启回溯，连同唤醒词一起整句识别。推荐4个字 1500ms
     *          backTrackInMs 最大 15000，即15s
     *
     * >0 : 方案2：backTrackInMs = 0，唤醒词说完后，中间有停顿。
     *       不开启回溯。唤醒词识别回调后，正常开启识别。
     * <p>
     *
     */
    private int backTrackInMs = 0;
    private static RecogUtil instance;
    private static Context context;
    boolean enableOffline = false;
    MyRecognizer myRecognizer;
    private Handler handler;
    private AsrCmdParse asrCmdParae;

    /**
     * 获取实例，非线程安全
     *
     * @return
     */
    public static RecogUtil getInstance(Context context) {
        if (instance == null || RecogUtil.context != context) {
            instance = new RecogUtil(context);
        }
        return instance;
    }

    public RecogUtil(Context context){
        this.context = context;
        asrCmdParae = new AsrCmdParse(context);
        initRecog();
    }


    private void initRecog() {

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

        IRecogListener listener = new MessageStatusRecogListener(handler);
        // DEMO集成步骤 1.1 1.3 初始化：new一个IRecogListener示例 & new 一个 MyRecognizer 示例,并注册输出事件
        myRecognizer = new MyRecognizer(context, listener);

        if (enableOffline) {
            // 基于DEMO集成1.4 加载离线资源步骤(离线时使用)。offlineParams是固定值，复制到您的代码里即可
            Map<String, Object> offlineParams = DefConst.fetchOfflineParams();
            myRecognizer.loadOfflineEngine(offlineParams);
        }
    }

    protected void handleMsg(Message msg) {
        Log.w(TAG, "handleMsg " + msg);
        switch (msg.what){
            case STATUS_ASR_FINAL_RESULT:
                String keyword = msg.obj.toString();
                asrCmdParae.parseCmd(keyword);
                break;
            default:
                break;
        }
    }

    /**
     * 开始录音，点击“开始”按钮后调用。
     * 基于DEMO集成2.1, 2.2 设置识别参数并发送开始事件
     */
    public void startRecog() {
/*
        // DEMO集成步骤2.1 拼接识别参数： 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        final Map<String, Object> params = fetchParams();
        // params 也可以根据文档此处手动修改，参数会以json的格式在界面和logcat日志中打印
        Log.i("autoservice", "设置的start输入参数：" + params);
        // 复制此段可以自动检测常规错误
        (new AutoCheck(context, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
//                        txtLog.append(message + "\n");
                        ; // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
                return true;
            }
        }), enableOffline)).checkAsr(params);
*/
        // 此处 开始正常识别流程
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);// 是否需要语音音量数据回调
        params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN); // 语音活动检测
        // 如识别短句，不需要需要逗号，使用1536搜索模型。其它PID参数请看文档
        params.put(SpeechConstant.PID, 1536);

        //params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 2000); // 不开启长语音。开启VAD尾点检测，即静音判断的毫秒数。建议设置800ms-3000ms
        //params.put(SpeechConstant.DECODER, 0); // 纯在线(默认)
        //params.put(SpeechConstant.ACCEPT_AUDIO_DATA, false);// 是否需要语音音频数据回调


        if (backTrackInMs > 0) {
            // 方案1  唤醒词说完后，直接接句子，中间没有停顿。开启回溯，连同唤醒词一起整句识别。
            // System.currentTimeMillis() - backTrackInMs ,  表示识别从backTrackInMs毫秒前开始
            params.put(SpeechConstant.AUDIO_MILLS, System.currentTimeMillis() - backTrackInMs);
        }
        myRecognizer.start(params);
    }

    /**
     * 开始录音后，手动点击“停止”按钮。
     * SDK会识别不会再识别停止后的录音。
     * 基于DEMO集成4.1 发送停止事件 停止录音
     */
    protected void stop() {

        myRecognizer.stop();
    }

    /**
     * 开始录音后，手动点击“取消”按钮。
     * SDK会取消本次识别，回到原始状态。
     * 基于DEMO集成4.2 发送取消事件 取消本次识别
     */
    protected void cancel() {

        myRecognizer.cancel();
    }

    protected void releae() {

        myRecognizer.release();
    }
}
