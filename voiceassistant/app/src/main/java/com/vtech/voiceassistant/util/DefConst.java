package com.vtech.voiceassistant.util;

import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DefConst {
    public static final String APP_ID = "17003922";
    public static final String APP_KEY = "LQiWxRwcfppyqTcaGSRxhyyv";
    public static final String SECRET_KEY = "K9MoWd2VIIsE7cuzptG7MTltP3pxOdtv";

    //没有网络
    public static final int NETWORK_NONE = 1;
    //移动网络
    public static final int NETWORK_MOBILE = 0;
    //无线网络
    public static final int NETWORK_WIFI = 2;
    //有线网络
    public static final int NETWORK_ETH0 = 3;


    // ================选择TtsMode.ONLINE  不需要设置以下参数; 选择TtsMode.MIX 需要设置下面2个离线资源文件的路径
    public static final String TEMP_DIR = "/sdcard/baiduTTS"; // 重要！请手动将assets目录下的3个dat 文件复制到该目录

    // 请确保该PATH下有这个文件
    public static final String TEXT_FILENAME = TEMP_DIR + "/" + "bd_etts_text.dat";

    // 请确保该PATH下有这个文件 ，m15是离线男声
    public static final String MODEL_FILENAME =
            TEMP_DIR + "/" + "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";

    //外部数据存储目录
    public static final String EXTDATA_DIR_NAME = "VoiceAssistant";
    //离线女生
    public static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_common_speech_m15.dat";
    //离线男生
    public static final String SPEECH_MALE_MODEL_NAME = "bd_etts_common_speech_f7.dat";
    //逍遥度
    public static final String SPEECH_XYD_MODEL_NAME = "bd_etts_common_speech_yyjw.dat";
    //度丫丫
    public static final String SPEECH_YY_MODEL_NAME = "bd_etts_common_speech_as.dat";
    public static final String TEXT_MODEL_NAME = "bd_etts_text.dat";

    public static final String WAKEUP_WORD = "全能助手";

    public static final String WORD_START = "启动";  //启动XXX
    public static final String WORD_START1 = "打开";  //打开XXX
    public static final String WORD_DAIL = "电话";  //打电话给XXX;给XXX打电话;
    public static final String WORD_DAIL1 = "拨号";  //拨号给XXX;给XXX拨号;
    public static final String WORD_SCHEDULE = "播报日程";
    public static final String WORD_SCHEDULE1 = "今日计划";
    public static final String WORD_HEALTHY = "播报健康";
    public static final String WORD_HEALTHY1 = "当前健康";
    public static final String WORD_SASETY = "播报安全";
    public static final String WORD_SASETY1 = "安全状态";


    public static final int ASR_CMD_DAIL = 0x00000001;    //拨号
    public static final int ASR_CMD_START_APP = 0x00000002;    //打开应用
    public static final int ASR_CMD_PLAY_SCHEDULE = 0x00000003;    //播报日程
    public static final int ASR_CMD_PLAY_HEALTHY = 0x00000004;    //播报健康
    public static final int ASR_CMD_PLAY_SASETY = 0x0000005;       //播报安全
    public static final int ASR_CMD_UNKNOW = -0x1;       //为止命令


    //

    public static final String BPRESURE_DEV_PATH = "/dev/tm1629c";
    public static final String RESPIRE_DEV_PATH =  "/dev/ttyS0";//"/dev/ttyMT1";

    //set RUNING MODE，正常运行模式（X2M1000）
    public static final byte[] X2M200_RUNING_MODE = {0x7D, 0x20, 0x01, 0x5C, 0x7E};
    //set IDLE MODE
    public static final byte[] X2M200_IDLE_MODE = {0x7D, 0x20, 0x11, 0x4C, 0x7E};
    public static final byte[] X2M200_RESET_MODE = {0x7D, 0x22, 0x5F, 0x7E};
    public static final byte[] X2M200_PULSE_MODE = {(0xFC - 0xFF), 0x00, 0x00, 0x00, 0x00, 0x00};
    //ping 检测模块的连接情况
    //static final short[] X2M200_PING = {0x7D, 0x01, 0xAE, 0xEA, 0xAA, 0xEE, 0x7C, 0x7E};
    public static final byte[] X2M200_PING = {0x7D, 0x01, (0xAE - 0xFF), (0xEA - 0xFF), (0xAA - 0XFF), (0xEE - 0xFF), 0x7C, 0x7E};
    //加载呼吸应用
    public static final byte[] X2M200_RESPIRE = {0X7D, 0X21, (0XD6 - 0xFF), (0XA2 - 0xFF), 0X23, 0X14, 0X1F, 0X7E};
    public static final short[] X2M200_RESPIRE_NEW = {0X7D, 0X21, 0XAD, 0X57, 0X4E, 0X06, 0XEE, 0X7E};

    public static final int X2M200_STATE_GOOD = 0;
    public static final int X2M200_STATE_DIST_ONLY = 1;
    public static final int X2M200_STATE_INSTABLE = 2;
    public static final int X2M200_STATE_UNSUPPORT = 3;
    public static final int X2M200_STATE_UNKNOW = 4;
    public static final int X2M200_MSG_SPIRE_UPDATE = 100;
    public static final int X2M200_MSG_DIST_UPDATE = 101;
    public static final int X2M200_MSG_MT2_UPDATE = 200;
    public static final int X2M200_MSG_MT2_TIMER = 201;
    public static final int X2M200_MSG_SLEEP_HELP = 300;
    public static final int X2M00_MSG_LIGHT_BEAUTY = 301;

    public static Map<String, Object> fetchSlotDataParam() {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            JSONObject json = new JSONObject();
            json.put("name", new JSONArray().put("妈妈").put("老伍"))
                    .put("appname", new JSONArray().put("手百").put("度秘"));
            map.put(SpeechConstant.SLOT_DATA, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }


    public static Map<String, Object> fetchOfflineParams() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(SpeechConstant.DECODER, 2);
        map.put(SpeechConstant.ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH, "assets:///baidu_speech_grammar.bsg");
        map.putAll(fetchSlotDataParam());
        return map;
    }

    public static int getInt(byte[] arr, int idex) {
        return (0x000000FF & (arr[idex + 0]) |
                0x0000FF00 & (arr[idex + 1] << 8) |
                0x00FF0000 & (arr[idex + 2] << 16) |
                0xFF000000 & (arr[idex + 3] << 24));
    }


    public static float getFloat(byte[] arr, int idex) {

        return Float.intBitsToFloat(0x000000FF & (arr[idex + 0]) |
                0x0000FF00 & (arr[idex + 1] << 8) |
                0x00FF0000 & (arr[idex + 2] << 16) |
                0xFF000000 & (arr[idex + 3] << 24));
    }

    public static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public static int bytesToInt(byte[] bs) {
        int a = (bs[0] << 24) | (bs[1] << 16) | (bs[2] << 8) | bs[3];
        return a;
    }
}

