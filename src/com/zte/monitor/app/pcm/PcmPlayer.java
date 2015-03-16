package com.zte.monitor.app.pcm;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Sylar on 14-9-15.
 */
public class PcmPlayer {

    private ByteArrayOutputStream outputStream;
    private AudioTrack mAudioTrack;
    private boolean isPrepared;
    private PcmAudioParam mPcmAudioParam;
    private boolean mThreadExitFlag = false; // 线程退出标志
    private int mPrimePlaySize = 0; // 较优播放块大小
    private int mPlayOffset = 0; // 当前播放位置
    private int mPlayState = 0; // 当前播放状态
    private PlayThread mPlayAudioThread;
    private final static Object locker = new Object();

    public PcmPlayer(PcmAudioParam param) {
        this.mPcmAudioParam = param;
        outputStream = new ByteArrayOutputStream();
    }

    public void setPcmData(byte[] data) {
        try {
            outputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        synchronized (locker) {
            locker.notifyAll();
        }
    }

    public boolean prepare() {
        if (mPcmAudioParam == null)
            return false;
        if (isPrepared)
            return true;
        try {
            initAudioTrack();
        } catch (Exception e) {
            e.printStackTrace();
        }

        isPrepared = true;
        setPlayState(PlayState.MPS_PREPARE);
        return true;
    }

    private synchronized void setPlayState(int state) {
        mPlayState = state;
    }

    private void initAudioTrack() throws Exception {

        // 获得构建对象的最小缓冲区大小一般情况 minBufferSize  会导致播出吱吱声音。
        int minBufSize = AudioTrack.getMinBufferSize(mPcmAudioParam.mFrequency,
                mPcmAudioParam.mChannel,
                mPcmAudioParam.mSampBit);

        Log.e("TAG", "minBufSize " + minBufSize);
        mPrimePlaySize = minBufSize * 2;

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                mPcmAudioParam.mFrequency,
                mPcmAudioParam.mChannel,
                mPcmAudioParam.mSampBit,
                minBufSize,
                AudioTrack.MODE_STREAM);
    }

    public void release() {
        stop();
        releaseAudioTrack();
        isPrepared = false;
        setPlayState(PlayState.MPS_UNINIT);

    }

    private void releaseAudioTrack() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    public void play() {
        if (!isPrepared) {
            return;
        }
        switch (mPlayState) {
            case PlayState.MPS_PREPARE:
                mPlayOffset = 0;
                setPlayState(PlayState.MPS_PLAYING);
                mAudioTrack.play();
                startThread();
                break;
            case PlayState.MPS_PAUSE:
                setPlayState(PlayState.MPS_PLAYING);
//                startThread();
                mAudioTrack.play();
                synchronized (locker) {
                    locker.notifyAll();
                }
                break;
        }
    }

    public void pause() {
        if (!isPrepared) {
            return;
        }

        if (mPlayState == PlayState.MPS_PLAYING) {
            setPlayState(PlayState.MPS_PAUSE);
//            stopThread();
            mAudioTrack.pause();
        }
    }


    public void stop() {
        if (!isPrepared) {
            return;
        }

        setPlayState(PlayState.MPS_PREPARE);
        stopThread();
        synchronized (locker) {
            locker.notifyAll();
        }
    }

    private void startThread() {
        if (mPlayAudioThread == null) {
            mThreadExitFlag = false;
            mPlayAudioThread = new PlayThread();
            mPlayAudioThread.start();
        }
    }

    private void stopThread() {
        if (mPlayAudioThread != null) {
            mThreadExitFlag = true;
            mPlayAudioThread = null;
            try {
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream.reset();
        }
    }

    public void onPlayComplete() {
        mPlayAudioThread = null;
        if (mPlayState != PlayState.MPS_PAUSE) {
            setPlayState(PlayState.MPS_PREPARE);
        }

    }

    class PlayThread extends Thread {
        @Override
        public void run() {
            Log.e("TAG", "PlayAudioThread run");
            mAudioTrack.play();
            while (true) {
                if (mThreadExitFlag) {
                    try {
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    outputStream.reset();
                    mAudioTrack.flush();
                    mAudioTrack.stop();
                    break;
                }

                while (outputStream.size() == 0) {
                    synchronized (locker) {
                        try {
                            locker.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }

                try {
                    int size = mAudioTrack.write(outputStream.toByteArray(), mPlayOffset, mPrimePlaySize);
                    Log.e("TAG", "size = " + size);
                    if (size == 0 || size == -2) {
                        synchronized (locker) {
                            try {
                                locker.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    } else {
                        mPlayOffset += size;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onPlayComplete();
                    break;
                }
            }
            try {
                mAudioTrack.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("TAG", "PlayAudioThread complete...");
        }
    }
}
