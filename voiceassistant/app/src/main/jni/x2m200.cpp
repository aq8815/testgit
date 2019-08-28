#include <jni.h>
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <string>
#include <jni.h>

//#include "SerialPort.h"

#include <android/log.h>
#define LOG_TAG    "serial_port" // 这个是自定义的LOG的标识
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static speed_t getBaudrate(jint baudrate)
{
    switch(baudrate) {
        case 0: return B0;
        case 50: return B50;
        case 75: return B75;
        case 110: return B110;
        case 134: return B134;
        case 150: return B150;
        case 200: return B200;
        case 300: return B300;
        case 600: return B600;
        case 1200: return B1200;
        case 1800: return B1800;
        case 2400: return B2400;
        case 4800: return B4800;
        case 9600: return B9600;
        case 19200: return B19200;
        case 38400: return B38400;
        case 57600: return B57600;
        case 115200: return B115200;
        case 230400: return B230400;
        case 460800: return B460800;
        case 500000: return B500000;
        case 576000: return B576000;
        case 921600: return B921600;
        case 1000000: return B1000000;
        case 1152000: return B1152000;
        case 1500000: return B1500000;
        case 2000000: return B2000000;
        case 2500000: return B2500000;
        case 3000000: return B3000000;
        case 3500000: return B3500000;
        case 4000000: return B4000000;
        default: return -1;
    }
}
#define TM1629C   0x77

#define TM1629C_TURN_ON_LEDS _IOW(TM1629C, 0x01, int)
#define TM1629C_TURN_OFF_LEDS _IOW(TM1629C, 0x02, int)
#define TM1629C_GET_XINLV           _IOR(TM1629C, 0x03, int)
#define TM1629C_SET_CLIAXINLV           _IOR(TM1629C, 0x04, int)

extern "C"
JNIEXPORT void JNICALL
Java_com_vtech_voiceassistant_serial_SerialPort_lightcontrol
        (JNIEnv *env, jclass thiz, jstring path, jint val) {
    jboolean iscopy;
    const char *path_utf = env->GetStringUTFChars(path, &iscopy);
    int fd = open(path_utf, O_RDWR);
    if (val < 0) {
        ioctl(fd, TM1629C_TURN_OFF_LEDS, 0);
    } else if (val < 7) {
    ioctl(fd, TM1629C_TURN_ON_LEDS, val);
    }

    close(fd);

}
struct STM32F0_DATA
{
    unsigned short gy;
    unsigned short dy;
    unsigned short xl;
};
STM32F0_DATA tmpbytearry;


extern "C"
JNIEXPORT void JNICALL
Java_com_vtech_voiceassistant_serial_SerialPort_cribrate
        (JNIEnv *env, jclass thiz, jstring path, jshort dy, jshort gy, jshort xl) {
    jboolean iscopy;
    int val = 1;
    tmpbytearry.dy= dy;
    tmpbytearry.gy = gy;
    tmpbytearry.xl= xl;
    const char *path_utf = env->GetStringUTFChars(path, &iscopy);
    int fd = open(path_utf, O_RDWR);

    ioctl(fd, TM1629C_SET_CLIAXINLV, &tmpbytearry);
    close(fd);

    __android_log_print(ANDROID_LOG_INFO, "cylog", " cribrate 0x%x, diya 0x%x, xinlv 0x%x",tmpbytearry.gy, tmpbytearry.dy,tmpbytearry.xl);
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_vtech_voiceassistant_serial_SerialPort_BPressure
        (JNIEnv *env, jclass thiz, jstring path) {
    jboolean iscopy;
    int val = 1;
    tmpbytearry.dy= 0;
    tmpbytearry.gy = 0;
    tmpbytearry.xl= 0;
    const char *path_utf = env->GetStringUTFChars(path, &iscopy);
    int fd = open(path_utf, O_RDWR);

    ioctl(fd, TM1629C_GET_XINLV, &tmpbytearry);
    close(fd);

    __android_log_print(ANDROID_LOG_INFO, "cylog", " gaoya 0x%x, diya 0x%x, xinlv 0x%x",tmpbytearry.gy, tmpbytearry.dy,tmpbytearry.xl);

    val = (tmpbytearry.gy << 24)&0XFF000000  | (tmpbytearry.dy << 16) &0X00FF0000 | tmpbytearry.xl << 8&0X0000FF00;

    __android_log_print(ANDROID_LOG_INFO, "cylog", " gaoya 0x%x", val);
    return val;
}

/*
 * Class:     android_serialport_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
extern "C"
JNIEXPORT jobject JNICALL
Java_com_vtech_voiceassistant_serial_SerialPort_open
        (JNIEnv *env, jclass thiz, jstring path, jint baudrate, jint flags)
{
    int fd;
    speed_t speed;
    jobject mFileDescriptor;

    /* Check arguments */
    {
        speed = getBaudrate(baudrate);
        if (speed == -1) {
            /* TODO: throw an exception */
            LOGE("Invalid baudrate");
            return NULL;
        }
    }
    /* Opening device */
    {
        jboolean iscopy;
        const char *path_utf = env->GetStringUTFChars(path, &iscopy);
        LOGD("SerialPort Opening serial port %s with flags 0x%x", path_utf, O_RDWR | flags);
        fd = open(path_utf, O_RDWR | flags);
        LOGD("SerialPort open() fd = %d", fd);
        env->ReleaseStringUTFChars(path, path_utf);
        if (fd == -1)
        {
            /* Throw an exception */
            LOGE("Cannot open port");
            /* TODO: throw an exception */
            return NULL;
        }
    }
    /* Configure device */
    {
        struct termios cfg;
        LOGD("Configuring serial port");
        if (tcgetattr(fd, &cfg))
        {
            LOGE("tcgetattr() failed");
            close(fd);
            /* TODO: throw an exception */
            return NULL;
        }

        cfmakeraw(&cfg);
        cfsetispeed(&cfg, speed);
        cfsetospeed(&cfg, speed);

        if (tcsetattr(fd, TCSANOW, &cfg))
        {
            LOGE("tcsetattr() failed");
            close(fd);
            /* TODO: throw an exception */
            return NULL;
        }
    }
    /* Create a corresponding file descriptor */
    {
        jclass cFileDescriptor = env->FindClass("java/io/FileDescriptor");
        jmethodID iFileDescriptor = env->GetMethodID(cFileDescriptor, "<init>", "()V");
        jfieldID descriptorID = env->GetFieldID(cFileDescriptor, "descriptor", "I");
        mFileDescriptor = env->NewObject(cFileDescriptor, iFileDescriptor);
        env->SetIntField(mFileDescriptor, descriptorID, (jint)fd);
    }

    return mFileDescriptor;
}

/*
 * Class:     cedric_serial_SerialPort
 * Method:    close
 * Signature: ()V
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_vtech_voiceassistant_serial_SerialPort_close
        (JNIEnv *env, jobject thiz)
{
    jclass SerialPortClass = env->GetObjectClass(thiz);
    jclass FileDescriptorClass = env->FindClass("java/io/FileDescriptor");

    jfieldID mFdID = env->GetFieldID(SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
    jfieldID descriptorID = env->GetFieldID(FileDescriptorClass, "descriptor", "I");

    jobject mFd = env->GetObjectField(thiz, mFdID);
    jint descriptor = env->GetIntField(mFd, descriptorID);

    LOGD("close(fd = %d)", descriptor);
    close(descriptor);
}
