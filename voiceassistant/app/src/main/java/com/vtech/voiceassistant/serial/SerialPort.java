package com.vtech.voiceassistant.serial;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by changyan on 17-10-2.
 */

public class SerialPort {
    private static final String TAG = "SerialPort";

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;


    public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            Log.w(TAG, "can read" + device.canRead() + " canWrite " + device.canWrite());
        }
        mFd = open(device.getAbsolutePath(), baudrate, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }
    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }
    // JNI
    private native static FileDescriptor open(String path, int baudrate, int flags);
    public native void close();
    public native static void lightcontrol(String path, int val);
    public native static int BPressure(String path);
    public native static void cribrate(String path, short dy, short gy, short xl );

    static {
        //System.loadLibrary("serial_port");
        System.loadLibrary("native-lib");
    }

}
