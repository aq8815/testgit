package com.vtech.voiceassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


public class VoiceReceiver extends BroadcastReceiver {
    private static final String TAG = "VoiceAssistant";
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ACTION_BOOT)) {
            Log.i(TAG, "BootBroadcastReceiver onReceive(), start VoiceAssistantService");
            Intent intent1 = new Intent(context , VoiceAssistantService.class);
            //context.startService(intent1);
            context.startForegroundService(intent1);
            if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent1);
            } else {
                context.startService(intent1);
            }
        }
    }
}
