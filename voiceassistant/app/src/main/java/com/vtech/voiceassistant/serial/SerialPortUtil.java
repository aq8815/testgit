package com.vtech.voiceassistant.serial;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.vtech.voiceassistant.util.DefConst;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

/**
 * Created by changyan on 17-10-2.
 */

public class SerialPortUtil {
    private static final String TAG = "SerialPort";

    private int baudrate = 115200;
    private String path;
    private static Context context;
    private static SerialPortUtil instance;
    private SerialPort mSerialPort = null;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private OnDataReceiveListener onDataReceiveListener = null;
    private boolean isStop = false;
    private ReadThread mReadThread;

    /**
     * 获取实例，非线程安全
     *
     * @return
     */
    public static SerialPortUtil getInstance(Context context) {
        if (instance == null || SerialPortUtil.context != context) {
            instance = new SerialPortUtil(context, DefConst.RESPIRE_DEV_PATH);
        }
        return instance;
    }

    public  SerialPortUtil(String path) {
        this(null, path);

    }

    public  SerialPortUtil(Context context, String path) {
        this.context = context;
        this.path = path;
    }

    public interface OnDataReceiveListener {
        void onDataReceive(byte[] buffer, int size);
    }

    public void setOnDataReceiveListener(
            OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }


    /**
     * 初始化串口信息
     */
    public void onCreate() {
        try {
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            mReadThread = new ReadThread();
            isStop = true;
            mReadThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "onCreate EXCEP " + e);
        }
    }

    public void setStart(){
        isStop = false;
        mReadThread.interrupt();
    }

    public void setStop(){
        if (!isStop) {
            isStop = true;
            mReadThread.interrupt();
            try {
                mOutputStream.close();
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public void setClose() {
        setStop();
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    /**
     * 发送指令到串口        mSerial.onCreate();
        mSerial.setOnDataReceiveListener(serialReceiveListener);
     *
     * @param cmd
     * @return
     */
    public boolean sendCmds(String cmd) {
        boolean result = true;
        byte[] mBuffer = (cmd+"\r\n").getBytes();
        //注意：我得项目中需要在每次发送后面加\r\n，大家根据项目项目做修改，也可以去掉，直接发送mBuffer
        try {
            if (mOutputStream != null) {
                mOutputStream.write(mBuffer);
            } else {
                result = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean sendBuffer(byte[] mBuffer) {
        boolean result = true;
        String tail = "\r\n";
        byte[] tailBuffer = tail.getBytes();
        byte[] mBufferTemp = new byte[mBuffer.length+tailBuffer.length];
        System.arraycopy(mBuffer, 0, mBufferTemp, 0, mBuffer.length);
        System.arraycopy(tailBuffer, 0, mBufferTemp, mBuffer.length, tailBuffer.length);
        //注意：我得项目中需要在每次发送后面加\r\n，大家根据项目项目做修改，也可以去掉，直接发送mBuffer
        try {
            if (mOutputStream != null) {
                mOutputStream.write(mBufferTemp);
            } else {
                result = false;
                Log.w(TAG, "sendBuffer fail mOutputStream is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "sendBuffer fail " + e);
            result = false;
        }
        Log.w(TAG, "sendBuffer result is " + result);
        return result;
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            Log.w(TAG, "ReadThread isInterrupted " + isInterrupted() + " mInputStream is " + mInputStream );
            while (!isStop && !isInterrupted()) {
                int size;
                try {

                    if (mInputStream == null)
                        return;
                    byte[] buffer = new byte[512];
                    Log.w(TAG, "ReadThread bf mInputStream.read");
                    size = mInputStream.read(buffer);
                    Log.w(TAG, "ReadThread read size is  " + size);
                    /*
                    for (int i = 0; i < size; i++){
                        Log.w(TAG, "0x" +  Integer.toHexString(buffer[i]));
                    }*/

                    if (size > 0) {
                        String hs = "";
                        String stmp = "";
                        for (int n = 0; n < size; n++) {
                            stmp = Integer.toHexString(buffer[n] & 0xff);
                            if (stmp.length() == 1) {
                                hs = hs + "0" + stmp;
                            } else {
                                hs = hs + stmp;
                            }
                        }

                        Log.w(TAG, "length is:"+size+",data is: "+ hs);
                        if (null != onDataReceiveListener) {
                            onDataReceiveListener.onDataReceive(buffer, size);
                        }
                    }
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public SerialPort openSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            /* Read serial port parameters */
//            String path = "/dev/ttyGS0";
//            int baudrate = 9600;
            /* Check parameters */
            if ( (path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

            /* Open the serial port */
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        mInputStream = mSerialPort.getInputStream();
        mOutputStream = mSerialPort.getOutputStream();
        return mSerialPort;
    }

    public void write2port(String s){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mOutputStream.write(s.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
