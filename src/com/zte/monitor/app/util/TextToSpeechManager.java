package com.zte.monitor.app.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import com.iflytek.speech.*;

import java.util.List;

/**
 * Created by Sylar on 14-10-17.
 */
public class TextToSpeechManager {
    private static final String TAG = "TextToSpeechManager";
    private SpeechSynthesizer mTts;
    private Context mContext;
    private TextToSpeechCallback callback;
    private static TextToSpeechManager manager = null;

    public static interface TextToSpeechCallback {
        void onBegin();
    }

    private TextToSpeechManager(Context context) {
        mContext = context;
    }

    public static TextToSpeechManager getManager(Context context) {
        if (manager == null) {
            synchronized (TextToSpeechManager.class) {
                if (manager == null) {
                    manager = new TextToSpeechManager(context);
                }
            }
        }
        return manager;
    }

    public void setTextToSpeechCallback(TextToSpeechCallback callback) {
        this.callback = callback;
    }

    public void initialize() {
        initSpeechService();
    }

    /**
     * 开始播放
     *
     * @param text
     */
    public void play(String text) {
        if (mTts.isSpeaking()) {
            return;
        }
        mTts.startSpeaking(text, mTtsListener);
    }

    /**
     * 停止播放
     */
    public void stop() {
        mTts.stopSpeaking(mTtsListener);
    }

    /**
     * 释放语音链接
     */
    public void release() {
        if (mTts != null) {
            if (mTtsListener != null) {
                mTts.stopSpeaking(mTtsListener);
            }
            mTts.destory();
        }
        manager = null;
    }


    // 判断手机中是否安装了讯飞语音+
    public boolean checkSpeechServiceInstall() {
        String packageName = "com.iflytek.speechcloud";
        List<PackageInfo> packages = mContext.getPackageManager().getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(packageName)) {
                return true;
            } else {
                continue;
            }
        }
        return false;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String assetsApk = "SpeechService.apk";
                    if (processInstall(mContext, assetsApk)) {
                        Message message = new Message();
                        message.what = 1;
                        mHandler.sendMessage(message);
                    }
                    break;
            }
        }
    };

    /**
     * 如果服务组件没有安装，有两种安装方式。
     * 1.直接打开语音服务组件下载页面，进行下载后安装。
     * 2.把服务组件apk安装包放在assets中，为了避免被编译压缩，修改后缀名为mp3，然后copy到SDcard中进行安装。
     */
    public boolean processInstall(Context context, String assetsApk) {
        // 本地安装方式
        if (!ApkInstaller.installFromAssets(context, assetsApk)) {
            Toast.makeText(mContext, "安装失败", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void initSpeechService() {
//        if (!checkSpeechServiceInstall()) {
//            new AlertDialog.Builder(mContext).setTitle("提示")
//                    .setMessage("缺少语音包，请点击确认安装").setCancelable(false)
//                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            mHandler.sendEmptyMessage(0);
//                        }
//                    }).create().show();
//            return;
//        }
        // 设置你申请的应用appid
        SpeechUtility.getUtility(mContext).setAppid("4d6774d0");
        mTts = new SpeechSynthesizer(mContext, mTtsInitListener);
        setParam();
    }

    /**
     * 初期化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {

        @Override
        public void onInit(ISpeechModule arg0, int code) {
            if (code == ErrorCode.SUCCESS) {
                Log.e("TAG", "SpeechModule Init Success");
            }
        }
    };

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener.Stub() {
        @Override
        public void onBufferProgress(int progress) throws RemoteException {
            Log.d(TAG, "onBufferProgress :" + progress);
        }

        @Override
        public void onCompleted(int code) throws RemoteException {
            Log.d(TAG, "onCompleted code =" + code);
            mTts.stopSpeaking(mTtsListener);
        }

        @Override
        public void onSpeakBegin() throws RemoteException {
            Log.d(TAG, "onSpeakBegin");
            callback.onBegin();
        }

        @Override
        public void onSpeakPaused() throws RemoteException {
            Log.d(TAG, "onSpeakPaused.");
        }

        @Override
        public void onSpeakProgress(int progress) throws RemoteException {
            Log.d(TAG, "onSpeakProgress :" + progress);
        }

        @Override
        public void onSpeakResumed() throws RemoteException {
            Log.d(TAG, "onSpeakResumed.");
        }
    };

    /**
     * 参数设置
     */
    private void setParam() {
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, "local");
        mTts.setParameter(SpeechSynthesizer.VOICE_NAME, "xiaoyan");
        mTts.setParameter(SpeechSynthesizer.SPEED, "50");
        mTts.setParameter(SpeechSynthesizer.PITCH, "50");
        mTts.setParameter(SpeechSynthesizer.VOLUME, "50");
    }
}
