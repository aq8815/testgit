package com.vtech.voiceassistant.util;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.vtech.voiceassistant.serial.SerialPort;
import com.vtech.voiceassistant.serial.SerialPortUtil;

import java.util.Random;

public class HealthUtil {
    private static final String TAG = "VoiceAssistant-health";

    private static HealthUtil instance;
    private static Context context;

    private  static int HU_STATUS_START = 0x1000;
    private  static int HU_STATUS_STOP = 0x1001;


    private int iDistance = 0;
    private int iResprieRate = 0;
    private int iSpirePersist = 0;
/*
    private SerialPortUtil mSerial;

*/

    private int iBpressure = 0;
    private Random random = new Random();
    private boolean bVirtralData = true;
    private int iStatus;


    /**
     * 获取实例，非线程安全
     *
     * @return
     */
    public static HealthUtil getInstance(Context context) {
        if (instance == null || HealthUtil.context != context) {
            instance = new HealthUtil(context);
        }
        return instance;
    }

    public HealthUtil(Context context){
        this.context = context;
        initHealth();
    }

    private void updateBPressure() {
        if(bVirtralData) {
            byte[] bp=new byte[4];
            //收缩压
            bp[0] = (byte)random.nextInt(120*2);
            //舒张压
            bp[1] = (byte)random.nextInt(70*2);
            //心率
            bp[2] =  (byte)random.nextInt(80*2);
            bp[3] = 0;
            iBpressure =  (bp[0] << 24) | (bp[1] << 16) | (bp[2] << 8) | bp[3];
        }else {
            iBpressure =  SerialPort.BPressure(DefConst.BPRESURE_DEV_PATH);
        }
        return;
    }

    //获取血压
    public int   getBPressure() {
        return  iBpressure;
    }

    //收缩压
    public int getSBP(){
        byte[] buf = DefConst.intToByteArray(iBpressure);
        return buf[0] & 0xff;

    }
    //舒张压
    public int getDBP(){
        byte[] buf = DefConst.intToByteArray(iBpressure);
        return buf[1] & 0xff;
    }

    //获取心率
    public int getHRate() {
        byte[] buf = DefConst.intToByteArray(iBpressure);
        return buf[2] & 0xff;
    }

    //开始监测
    public void start() {

        if (iStatus != HU_STATUS_START) {
            iStatus = HU_STATUS_START;
            if (!bVirtralData) {
               // mSerial.setStart();
            }else {
                threadVirtual.interrupt();
            }
        }
        else {
            Log.w(TAG, "Health util already started");
        }
    }

    //停止监测
    public void stop() {
        if (iStatus != HU_STATUS_STOP) {
            iStatus = HU_STATUS_STOP;
            if (!bVirtralData) {
        //  mSerial.setStop();
            } else {
                threadVirtual.interrupt();
            }
        }else {
            Log.w(TAG, "Health util already stoped");
        }
    }

    //获取距离
    public int getDist() {
        return iDistance;
    }

    //获取呼吸
    public int getRRate(){
        return iResprieRate;
    }


    private void initHealth() {
        Log.w(TAG, "on initHealth");


        if(!bVirtralData) {
            updateBPressure();
            // current not support distance
            /*
            mSerial = new SerialPortUtil(DefConst.RESPIRE_DEV_PATH);
            mSerial.onCreate();
            mSerial.setOnDataReceiveListener(serialReceiveListener);
             */
        }else {
            threadVirtual.start();
        }
    }

    SerialPortUtil.OnDataReceiveListener serialReceiveListener = new SerialPortUtil.OnDataReceiveListener() {
        @Override
        public void onDataReceive(byte[] buffer, int size){
            if (!(size == 32  || size == 36)) {
                Log.d(TAG, "mt1 get buf size " + size + " skip");
                return;
            }
            Message msg =new Message();
            switch (buffer[10]) {
                case DefConst.X2M200_STATE_GOOD:
                    iSpirePersist = 300;
                    iDistance = Math.abs(Math.round(DefConst.getFloat(buffer, 18)*100));
                    iResprieRate = DefConst.getInt(buffer, 14);
//                    iHeartRate = (iResprieRate-1)*4 + random.nextInt(8);
                    break;
                case DefConst.X2M200_STATE_DIST_ONLY:
                    if (iSpirePersist-->0) {
                        iDistance = (int)Math.abs(Math.round(DefConst.getFloat(buffer, 18)*100));
                    } else {
                        iDistance = (int)Math.abs(Math.round(DefConst.getFloat(buffer, 18)*100));
                        iResprieRate = 0;
//                        iHeartRate = iResprieRate*4;
                    }
                    break;
                case DefConst.X2M200_STATE_INSTABLE:
                    iDistance = Math.abs(Math.round(DefConst.getFloat(buffer, 18)*100));
                    Log.w(TAG, " X2M200_STATE_INSTABLE " + iDistance);
                    break;
                case DefConst.X2M200_STATE_UNSUPPORT:
                    Log.w(TAG, "MT1 get UNSUPPORT STATE");
                    break;
                case DefConst.X2M200_STATE_UNKNOW:
                    Log.w(TAG, "MT1 get UNKONW STATE");
                    break;
                default:
                    Log.w(TAG, "MT1 get undef state value");
                    break;
            }
        }
    };

    Thread threadVirtual = new Thread(new Runnable() {
        @Override
        public void run() {
            int i= 0;

            while (true){

                if (iStatus == HU_STATUS_STOP) {
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                i++;
                if (i%5 == 0) {
                    iResprieRate = random.nextInt(30);
                }else {
                    iResprieRate=20;
                }
                iDistance = random.nextInt(100);
                Log.d(TAG, "Gen virtual data distance " + iDistance + " iResprateRate " + iResprieRate);
                updateBPressure();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });
}
