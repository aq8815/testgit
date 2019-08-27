package com.vtech.voiceassistant.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SpeechUtil implements SpeechSynthesizerListener {
    private static final String TAG = "VoiceAssistant-tts";
    private String mSampleDirPath;

    private static SpeechUtil instance;
    private static Context context;

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private TtsMode ttsMode = TtsMode.MIX;
    private SpeechSynthesizer mSpeechSynthesizer;



    /**
     * 获取实例，非线程安全
     *
     * @return
     */
    public static SpeechUtil getInstance(Context context) {
        if (instance == null || SpeechUtil.context != context) {
            instance = new SpeechUtil(context);
        }
        return instance;
    }


    public SpeechUtil(Context context){
        this.context = context;
        InitEnv();
        initTts();
    }

    public void speak(String s) {
        if (s!= null) {
            Log.v(TAG, "播放 " + s);
            mSpeechSynthesizer.speak(s);
        }
    }

    public void stop(){
        mSpeechSynthesizer.stop();
    }

    public void release(){
        mSpeechSynthesizer.release();
    }

    private void InitEnv() {
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + DefConst.EXTDATA_DIR_NAME;
        }
        FileUtil.makeDir(mSampleDirPath);
        copyFromAssetsToSdcard(false, DefConst.SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" + DefConst.SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, DefConst.SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/" + DefConst.SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, DefConst.TEXT_MODEL_NAME, mSampleDirPath + "/" + DefConst.TEXT_MODEL_NAME);
        //copyFromAssetsToSdcard(false, LICENSE_FILE_NAME, mSampleDirPath + "/" + LICENSE_FILE_NAME);
        copyFromAssetsToSdcard(false, DefConst.SPEECH_XYD_MODEL_NAME, mSampleDirPath + "/" + DefConst.SPEECH_XYD_MODEL_NAME);
        copyFromAssetsToSdcard(false, DefConst.SPEECH_YY_MODEL_NAME, mSampleDirPath + "/" + DefConst.SPEECH_YY_MODEL_NAME);
    }

     private void initTts() {

       //  LoggerProxy.printable(true); // 日志打印在logcat中

        //获取实例
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(context);
        mSpeechSynthesizer.setAppId(DefConst.APP_ID);
        mSpeechSynthesizer.setApiKey(DefConst.APP_KEY, DefConst.SECRET_KEY);

         // 检查离线授权文件是否下载成功，离线授权文件联网时SDK自动下载管理，有效期3年，3年后的最后一个月自动更新。
         if (!checkAuth()) {
             return;
         }
        //文本模型文件路径 (离线引擎使用)
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + DefConst.TEXT_MODEL_NAME);
        //声学模型文件路径 (离线引擎使用)
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + DefConst.SPEECH_XYD_MODEL_NAME);
        Log.i(TAG, "initTts param: " + mSampleDirPath + "/" + DefConst.TEXT_MODEL_NAME);
        Log.i(TAG, "initTts param: " + mSampleDirPath + "/" + DefConst.SPEECH_XYD_MODEL_NAME);

        //模式:离在线混合
        mSpeechSynthesizer.auth(TtsMode.MIX);
        //对语音合成进行监听
        mSpeechSynthesizer.setSpeechSynthesizerListener(this);

        //设置参数
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");//标准女声
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5");//音量 范围["0" - "15"], 不支持小数。 "0" 最轻，"15" 最响。
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");//语速 范围["0" - "15"], 不支持小数。 "0" 最慢，"15" 最快
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");//语调 范围["0" - "15"], 不支持小数。 "0" 最慢，"15" 最快
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_NETWORK);//WIFI,4G,3G 使用在线合成，其他使用离线合成 6s超时
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/" + DefConst.TEXT_MODEL_NAME);//文本模型文件路径
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/" + DefConst.SPEECH_XYD_MODEL_NAME);//声学模型文件路径
        mSpeechSynthesizer.initTts(TtsMode.MIX);
    }


    /**
     * 检查appId ak sk 是否填写正确，另外检查官网应用内设置的包名是否与运行时的包名一致。本demo的包名定义在build.gradle文件中
     *
     * @return
     */
    private boolean checkAuth() {
        AuthInfo authInfo = mSpeechSynthesizer.auth(ttsMode);
        if (!authInfo.isSuccess()) {
            // 离线授权需要网站上的应用填写包名。本demo的包名是com.baidu.tts.sample，定义在build.gradle中
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.w(TAG, "【error】鉴权失败 errorMsg=" + errorMsg);
            return false;
        } else {
            Log.w(TAG, "验证通过，离线正式授权文件存在。");
            return true;
        }
    }

    /**
     * 将离线资源文件拷贝到SD卡中
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source  dat文件
     * @param dest    保存文件路径
     */
    private void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = context.getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onSynthesizeStart(String s) {
        Log.i(TAG, "合成开始");
    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {
        Log.i(TAG, "合成进度 :"+i);
    }

    @Override
    public void onSynthesizeFinish(String s) {

        Log.i(TAG, "合成结束");
    }

    @Override
    public void onSpeechStart(String s) {
        Log.i(TAG, "开始播放");
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {
        Log.i(TAG, "播放进度 :"+i);
    }

    @Override
    public void onSpeechFinish(String s) {
        Log.i(TAG, "合成结束");
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        Log.i(TAG, "error :"+speechError);
    }
}
