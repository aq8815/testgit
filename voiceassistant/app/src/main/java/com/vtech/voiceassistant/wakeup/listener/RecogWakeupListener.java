package com.vtech.voiceassistant.wakeup.listener;

import android.os.Handler;
import android.util.Log;

import com.vtech.voiceassistant.recog.IStatus;
import com.vtech.voiceassistant.util.DefConst;
import com.vtech.voiceassistant.wakeup.WakeUpResult;

/**
 * Created by fujiayi on 2017/9/21.
 */

public class RecogWakeupListener extends SimpleWakeupListener implements IStatus {

    private static final String TAG = "RecogWakeupListener";

    private Handler handler;

    public RecogWakeupListener(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onSuccess(String word, WakeUpResult result) {
        super.onSuccess(word, result);
        Log.w(TAG, "RecogWakeupListener onSuccess  " + word);
        if (word.equals(DefConst.WAKEUP_WORD))
            handler.sendMessage(handler.obtainMessage(STATUS_WAKEUP_SUCCESS));
    }
}
