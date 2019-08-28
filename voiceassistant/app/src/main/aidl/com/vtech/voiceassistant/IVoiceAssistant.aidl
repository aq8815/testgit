// IVoiceAssistant.aidlrespire
package com.vtech.voiceassistant;

// Declare any non-default types here with import statements

interface IVoiceAssistant {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);

    void play(String aString);
    int  bpressure();
    int  distance();
    int  respire();
    void start_hwatch();
    void stop_hwatch();
}
