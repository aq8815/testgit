package com.vtech.voiceassistant;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.baidu.speech.asr.SpeechConstant;
import com.vtech.voiceassistant.serial.SerialPort;
import com.vtech.voiceassistant.util.DefConst;
import com.vtech.voiceassistant.util.HealthUtil;
import com.vtech.voiceassistant.util.SpeechUtil;
import com.vtech.voiceassistant.util.WakeupUtil;



public class VoiceAssistantService extends Service  {
    private static final String TAG = "VoiceAssistant";

    private SpeechUtil speechUtil;
    private WakeupUtil wakeupUtil;
    private HealthUtil healthUtil;

    public VoiceAssistantService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.i(TAG, "VoiceAssistantService onBind");
        return new VoiceBinder();//return MyBinder通过ServiceConnection在activity中拿到MyBinder
    }

    @Override
    public void onCreate(){
        Log.i(TAG, "VoiceAssistantService onCreate");
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //NotificationCompat.Builder builder =
            //new NotificationCompat.Builder(this, CHANNEL_ID)
            //         .setContentTitle("")
            //         .setContentText("");
            NotificationChannel channel = new NotificationChannel("baidu", getString(R.string.app_name), NotificationManager.IMPORTANCE_MIN);
            channel.enableVibration(false);//去除振动

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(channel);

            Notification.Builder builder = new Notification.Builder(getApplicationContext(), "baidu");
//                    .setContentTitle("正在后台运行")
//                    .setSmallIcon(R.mipmap.logo);
            startForeground(1, builder.build());//id must not be 0,即禁止是0

        }*/

        speechUtil = SpeechUtil.getInstance(this);
        wakeupUtil = WakeupUtil.getInstance(this);
        healthUtil = HealthUtil.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeupUtil.release();
        speechUtil.release();

    }

    //播放语音
    public void  playVoice(String txt) {
        speechUtil.speak(txt);
    }

    //获取血压
        public int   getBPressure() {
        return healthUtil.getBPressure();
    }

    //获取距离
    public int getDist() {
        return healthUtil.getDist();
    }

    //获取呼吸
    public int getRRate(){
        return healthUtil.getRRate();
    }


    public void startHWatch(){
        healthUtil.start();
    }

    public void stopHWatch(){
        healthUtil.stop();
    }

    class VoiceBinder extends IVoiceAssistant.Stub {

        public void play(String txt){
            playVoice(txt);
        }
        public int bpressure(){ return  getBPressure(); }
        public int distance() { return getDist(); }
        public int respire() { return getRRate(); }
        public void start_hwatch(){ startHWatch();}
        public void stop_hwatch(){stopHWatch();}
    }
}
