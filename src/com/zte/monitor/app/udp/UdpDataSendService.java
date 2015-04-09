package com.zte.monitor.app.udp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.zte.monitor.app.MonitorApplication;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.codec.CodecHandler;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.database.dao.BtsStateDao;
import com.zte.monitor.app.database.dao.ServerInfoDao;
import com.zte.monitor.app.database.dao.SettingDao;
import com.zte.monitor.app.database.dao.SmsDao;
import com.zte.monitor.app.handler.*;
import com.zte.monitor.app.model.SmsModel;
import com.zte.monitor.app.model.StateModel;
import com.zte.monitor.app.model.UserModel;
import com.zte.monitor.app.model.response.HeartBeatResponse;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class UdpDataSendService extends Service {
    private static final String TAG = UdpDataSendService.class.getSimpleName();
    public static final String UPDATE_SMS_INFO = "com.zte.monitor.app.ACTION_UPDATE_SMS_INFO";
    private static final Object lockObject = new Object();

    private IoSession mSession;
    private Context mContext;
    private NioDatagramConnector mConnector;
    private IBinder mBinder = new IUdpConnectionInterface.Stub() {

        @Override
        public void sendRequest(byte[] request) throws RemoteException {
            sendMessage(request);
        }
    };
    private CodecHandler handler;
    private ServerInfoDao serverInfoDao;

    private SmsDao smsDao;
    private SettingDao settingDao;
    private Worker worker;
    private BtsStateDao btsStateDao;
    private MessageResponseHandler messageResponseHandler;
    private HeartBeatResponseHandler heartBeatResponseHandler;

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "UdpDataSendService.onCreate()");
        mContext = this;
        messageResponseHandler = new MessageResponseHandler(this);
        messageResponseHandler.registerReceiver();
        heartBeatResponseHandler = new HeartBeatResponseHandler(this);
        initResponseHandlerChain();
        serverInfoDao = new ServerInfoDao(this);
        smsDao = new SmsDao(this);
        settingDao = new SettingDao(this);
        btsStateDao = new BtsStateDao(this);
        worker = new Worker();
        worker.start();
    }

    /**
     * 初始化响应处理累连
     */
    private void initResponseHandlerChain() {
        handler = CodecHandler.getHandler();
        handler.getChain().add((byte) 0x8d, new ScanFreqResponseHandler(this));
        handler.getChain().add((byte) 0x33, new ConfigParamResponseHandler(this));
        handler.getChain().add((byte) 0x39, new AddUserResponseHandler(this));
        handler.getChain().add((byte) 0x84, new SmsBroadcastResponseHandler(this));
        handler.getChain().add((byte) 0x85, new PcmBroadcastResponseHandler(this));
        handler.getChain().add((byte) 0x37, new TaskGetPhNumResponseHandler(this));
        handler.getChain().add((byte) 0x80, new TaskConfigResponseHandler(this));
        handler.getChain().add((byte) 0x47, messageResponseHandler);
        handler.getChain().add((byte) 0x49, new MonitorPcmResponseHandler(this));
        handler.getChain().add((byte) 0x40, new CheckPowerResponseHandler(this));
        handler.getChain().add((byte) 0x44, new TaskRcdListResponseHandler(this));
        handler.getChain().add((byte) 0x89, new PlayRecordResponseHandler(this));
        handler.getChain().add((byte) 0x46, new ShortMsgSwithResponseHandler(this));
        handler.getChain().add((byte) 0x81, new LocationResponseHandler(this));
        handler.getChain().add((byte) 0x82, new MonitorResponseHandler(this));
        handler.getChain().add((byte) 0x62, new CardStateResponseHandler(this));
        handler.getChain().add((byte) 0x61, new VoiceCallFeedbackResponseHandler(this));
        handler.getChain().add((byte) 0x51, new FindBaseInfoResponseHandler(this));
        handler.getChain().add((byte) 0x53, new FindSpcInfoResponseHandler(this));
        handler.getChain().add((byte) 0x68, heartBeatResponseHandler);
        handler.getChain().add((byte) 0x55, new FindSMSInfoResponseHandler(this));
        handler.getChain().add((byte) 0x57, new FindCallInfoResponseHandler(this));
    }

    /**
     * 初始化UDP链接参数
     */
    private void initConnector() {
        mConnector = new NioDatagramConnector();
        mConnector.setConnectTimeoutMillis(30 * 1000);
        mConnector.setConnectTimeoutCheckInterval(1000 * 30);
        mConnector.setHandler(new UdpHandler());

//        mConnector.getFilterChain().addLast("heart", new HeartBeatFilter(this)); //心跳
        mConnector.getFilterChain().addLast("logger", new LoggingFilter());
        DatagramSessionConfig sessionConfig = mConnector.getSessionConfig();
        sessionConfig.setCloseOnPortUnreachable(false);
        sessionConfig.setBothIdleTime(25);
        connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                closeConnection();
                initConnector();
            }
        }).start();
        synchronized (lockObject) {
            lockObject.notifyAll();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 链接UDP server
     */
    private void connect() {
        String[] serverInfo = serverInfoDao.get();
        if (serverInfo == null) return;
        String serverAddress = serverInfo[0];
        int serverPort = Integer.parseInt(serverInfo[1]);
        ConnectFuture _connectFuture = mConnector.connect(new InetSocketAddress(serverAddress, serverPort));
        _connectFuture.awaitUninterruptibly(1000);
        _connectFuture.addListener(new IoFutureListener<IoFuture>() {
            @Override
            public void operationComplete(IoFuture ioFuture) {
                ConnectFuture _connectFuture = (ConnectFuture) ioFuture;
                if (_connectFuture.isConnected()) {
                    mSession = _connectFuture.getSession();
                } else {
                    if (_connectFuture.getException() != null) {
                        _connectFuture.getException().printStackTrace();
                        Log.e(TAG, "connect error", _connectFuture.getException());
                    }
                }
            }
        });
    }

    private class UdpHandler extends IoHandlerAdapter {
        @Override
        public void sessionCreated(IoSession session) throws Exception {
            Log.e("TAG", "sessionCreated");
        }

        @Override
        public void sessionOpened(IoSession session) throws Exception {
            super.sessionOpened(session);
            Log.e("TAG", "sessionOpened");
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            super.sessionClosed(session);
            Log.e("TAG", "sessionClosed");
            StateModel stateModel = btsStateDao.get();
            if (stateModel == null) {
                stateModel = new StateModel();
            }
            stateModel.connState = SystemConstants.CONN_STATUS.DISCONNECT;
            btsStateDao.saveOrUpdate(stateModel);
            mContext.sendBroadcast(new Intent("com.zte.monitor.action.ACTION_CHECK_CONNECTION"));
        }

        @Override
        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
            super.sessionIdle(session, status);
            Log.e("TAG", "sessionIdle");
            if (session != null) {
                IoBuffer buffer = CodecManager.getManager().newHeartBeatReqEncode(MonitorApplication.heartBeatAction);
                byte[] out = new byte[buffer.limit()];
                buffer.flip();
                buffer.get(out);
                sendMessage(out);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //5秒钟检测心跳响应是否收到，如果没有，说明链路中断
                        try {
                            Thread.sleep(5 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        StateModel stateModel = btsStateDao.get();
                        if (stateModel == null) {
                            stateModel = new StateModel();
                        }
                        if (heartBeatResponseHandler.isReceiveHeartBeatRsp()) {
                            stateModel.connState = SystemConstants.CONN_STATUS.CONNECTED;
                        } else {
                            stateModel.connState = SystemConstants.CONN_STATUS.DISCONNECT;
                        }
                        btsStateDao.saveOrUpdate(stateModel);
                        mContext.sendBroadcast(new Intent("com.zte.monitor.action.ACTION_CHECK_CONNECTION"));
                    }
                }).start();
            } else {
                StateModel stateModel = btsStateDao.get();
                if (stateModel == null) {
                    stateModel = new StateModel();
                }
                stateModel.connState = SystemConstants.CONN_STATUS.DISCONNECT;
                btsStateDao.saveOrUpdate(stateModel);
                mContext.sendBroadcast(new Intent("com.zte.monitor.action.ACTION_CHECK_CONNECTION"));
            }
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            Log.e(TAG, "exceptionCaught", cause);
        }

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            if (message instanceof IoBuffer) {
                IoBuffer ioBuffer = (IoBuffer) message;
                ioBuffer.capacity(ioBuffer.limit());
                Log.e(TAG, "RCV : " + session.getServiceAddress().toString() + " DATA :" + ioBuffer);
                if (ioBuffer.limit() > 2) {
                    if (ioBuffer.get(0) == MonitorApplication.clientId && ioBuffer.get(2) == 0x04) {
                        HeartBeatResponse response = CodecManager.getManager().heartBeatRspDecode((IoBuffer) message);
                        StateModel stateModel = btsStateDao.get();
                        if (stateModel == null) {
                            stateModel = new StateModel();
                        }
                        if (response.clientId == MonitorApplication.clientId) {
                            stateModel.connState = SystemConstants.CONN_STATUS.CONNECTED;
                            btsStateDao.saveOrUpdate(stateModel);
                            sendBroadcast(new Intent("com.zte.monitor.action.ACTION_HEART_BEAT_SUCCESS"));
                        } else {
                            stateModel.connState = SystemConstants.CONN_STATUS.DISCONNECT;
                            btsStateDao.saveOrUpdate(stateModel);
                            sendBroadcast(new Intent("com.zte.monitor.action.ACTION_HEART_BEAT_FAILED"));
                            closeConnection();
                            initConnector();
                        }
                    } else {
                        handler.handleMessage(ioBuffer);
                    }
                } else {
                    handler.handleMessage(ioBuffer);
                }
            }
        }

        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            super.messageSent(session, message);
            Log.e(TAG, "SENT : " + session.getServiceAddress().toString() + " DATA :" + message);
        }
    }

    private void sendMessage(byte[] message) {
        if (mSession != null) {
            IoBuffer buffer = IoBuffer.allocate(message.length);
            buffer.put(message);
            buffer.flip();
            mSession.write(buffer);
        } else {
//            closeConnection();
//            initConnector();
        }
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            Log.e("TAG", "AutoSendSmsAlertService.Worker started...");
            LOOP:
            while (!isInterrupted()) {
                //配置是不自动发送，Worker wait
                while (settingDao.getSetting().autoSendSms == SystemConstants.AUTO_SEND_SMS.DO_NOT_SEND) {
                    synchronized (lockObject) {
                        try {
                            Log.e("TAG", "don not send sms");
                            lockObject.wait();
                        } catch (InterruptedException e) {
                            break LOOP;
                        }
                    }
                }

                List<SmsModel> newArrivedSmsList = smsDao.getNewArrivedSmsList();
                //无新上报短信,Worker等待
                while (newArrivedSmsList == null || newArrivedSmsList.size() == 0) {
                    synchronized (lockObject) {
                        try {
                            Log.e("TAG", "no new arrived sms");
                            lockObject.wait();
                            newArrivedSmsList = smsDao.getNewArrivedSmsList();
                        } catch (InterruptedException e) {
                            break LOOP;
                        }
                    }
                }

                if (newArrivedSmsList.size() > 0) {
                    //遍历所有新上报的短信，查看短信倒计时，如果时间＝＝0，则自动发送，如果不为0，倒计时减1后更新数据库中的所有倒计时
                    for (SmsModel smsModel : newArrivedSmsList) {
                        if (smsModel.countdownNum != 0) {
                            smsModel.countdownNum--;
                        } else {
                            Log.e("TAG", "send sms to " + smsModel.phNum);
                            sendSMS(smsModel);
                        }
                    }
                    //批量更新新收取的短信倒计时时间
                    smsDao.batchUpdate(newArrivedSmsList);
                    //发送更新短信信息广播
                    sendBroadcast(new Intent(UPDATE_SMS_INFO));
                }
                try {
                    //休眠一秒
                    sleep(1000);
                } catch (InterruptedException e) {
                    break LOOP;
                }
            }
            Log.e("TAG", "AutoSendSmsAlertService.Worker done...");
        }
    }

    //发送短信
    private void sendSMS(SmsModel smsModel) {
        smsModel.status = SystemConstants.SMS_STATUS.SENT;
        smsDao.update(smsModel);

        List<UserModel> userModelList = new ArrayList<UserModel>(1);
        UserModel userModel = new UserModel();
        userModel.imsi = smsModel.imsi;
        userModelList.add(userModel);
        IoBuffer buffer;
        if (smsModel.direction == 1) {
            buffer = CodecManager.getManager().smsBroadcastReqEncode((byte) 1, userModelList, smsModel.content,
                    smsModel.phNum, smsModel.sc);
        } else {
            buffer = CodecManager.getManager().smsBroadcastReqEncode((byte) 0, userModelList, smsModel.content,
                    smsModel.phNum, smsModel.sc);
        }
        buffer.flip();
        byte[] out = new byte[buffer.limit()];
        buffer.get(out);
        sendMessage(out);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messageResponseHandler.unRegisterReceiver();
        messageResponseHandler.clearCachedList();
        closeConnection();
        if (!worker.isInterrupted()) {
            worker.interrupt();
        }
        stopSelf();
        Log.e(TAG, "UdpDataSendService.onDestroy()");
    }

    private void closeConnection() {
        if (mSession != null) {
            mSession.close(true);
            mSession = null;
        }

        if (mConnector != null && !mConnector.isDisposing()) {
            mConnector.dispose(true);
            mConnector = null;
        }
    }
}
