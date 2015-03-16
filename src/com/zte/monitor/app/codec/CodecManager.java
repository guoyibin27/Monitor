package com.zte.monitor.app.codec;

import com.zte.monitor.app.model.SmsModel;
import com.zte.monitor.app.model.UserModel;
import com.zte.monitor.app.model.response.*;
import com.zte.monitor.app.util.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;

import java.io.UnsupportedEncodingException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 14-9-3.
 */
public class CodecManager {
    private final static String CHARSET = "GBK";
    private static CodecManager manager = null;
    private CharsetDecoder decoder;
    private CharsetEncoder encoder;

    private CodecManager() {
        decoder = Charset.forName(CHARSET).newDecoder();
        encoder = Charset.forName(CHARSET).newEncoder();
    }

    public static CodecManager getManager() {
        if (manager == null) {
            synchronized (CodecManager.class) {
                if (manager == null)
                    manager = new CodecManager();
            }
        }
        return manager;
    }

    private IoBuffer createIoBuffer() {
        IoBuffer ioBuffer = IoBuffer.allocate(1);
        ioBuffer.setAutoExpand(true);
        ioBuffer.setAutoShrink(true);
        addHeader(ioBuffer);
        return ioBuffer;
    }

    private void addHeader(IoBuffer buffer) {
        for (int i = 0; i < 6; i++)
            buffer.put((byte) 0);
    }

    private void parseHeader(IoBuffer buffer) {
//        byte[] header = new byte[6];
//        buffer.get(header);
        buffer.rewind();
//        buffer.flip();
    }

    private void encodeDist(IoBuffer buffer, List<UserModel> userModelList) {
        if (userModelList != null && userModelList.size() > 0) {
            buffer.put((byte) userModelList.size());
            for (UserModel userModel : userModelList) {
                if (!StringUtils.isBlank(userModel.imsi)) {
                    buffer.put((byte) userModel.imsi.length());
                    buffer.put(userModel.imsi.getBytes());
                } else {
                    buffer.put((byte) 0);
                }
                if (!StringUtils.isBlank(userModel.username)) {
                    try {
                        buffer.put((byte) userModel.username.getBytes(CHARSET).length);
                        buffer.put(userModel.username.getBytes(CHARSET));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    buffer.put((byte) 0);
                }
                if (!StringUtils.isBlank(userModel.imei)) {
                    buffer.put((byte) userModel.imei.length());
                    buffer.put(userModel.imei.getBytes());
                } else {
                    buffer.put((byte) 0);
                }
            }
        } else {
            buffer.put((byte) 0);
        }
    }

    /**
     * 登陆时心跳编码
     *
     * @param action
     * @param clientId
     * @param serverId
     * @return
     */
    public IoBuffer heartBeatReqEncode(byte action, byte clientId, byte serverId) {
        IoBuffer ioBuffer = createIoBuffer();
        ioBuffer.put((byte) 0x48);
        ioBuffer.put(action);
        ioBuffer.put(clientId);
        ioBuffer.put(serverId);
        return ioBuffer;
    }

    /**
     * 登陆时心跳解码
     *
     * @param ioBuffer
     */
    public HeartBeatResponse heartBeatRspDecode(IoBuffer ioBuffer) {
        HeartBeatResponse response = null;
        if (ioBuffer != null) {
            parseHeader(ioBuffer);
            response = new HeartBeatResponse();
            response.clientId = ioBuffer.get();
            response.serverId = ioBuffer.get();
            response.msgId = ioBuffer.get();
            response.reserveSlot = ioBuffer.get();
            response.msgLength = ioBuffer.get();
            response.msgBody = ioBuffer.get();
        }
        return response;
    }

    /**
     * 任务配置请求
     *
     * @param action
     * @param set
     * @param userModeList
     * @param condFlag
     * @param mcc
     * @param mnc
     * @param lac
     * @param freq
     * @param power
     * @param ncFreq
     * @param initType
     * @param reselOffset
     * @return
     */
    public IoBuffer taskCfgReqEncode(byte action, byte set, List<UserModel> userModeList,
                                     byte condFlag, short mcc, byte mnc, short lac, short freq, byte power, short ncFreq,
                                     byte initType, byte reselOffset) {
        IoBuffer ioBuffer = createIoBuffer();
        ioBuffer.put((byte) 0x00);
        ioBuffer.put(action);
        ioBuffer.put(set);
        encodeDist(ioBuffer, userModeList);
        ioBuffer.put(condFlag);
        if (condFlag == 1) {
            ioBuffer.putShort(mcc);
            ioBuffer.put(mnc);
            ioBuffer.putShort(lac);
            ioBuffer.putShort(freq);
            ioBuffer.put(power);
            ioBuffer.putShort(ncFreq);
            ioBuffer.put(initType);
            ioBuffer.put(reselOffset);
        }
        return ioBuffer;
    }

    /**
     * 任务配置应答
     *
     * @param ioBuffer
     */
    public UdpResponse taskCfgRspDecode(IoBuffer ioBuffer) {
        UdpResponse response = null;
        if (ioBuffer != null) {
            response = new UdpResponse();
            parseHeader(ioBuffer);
            response.msgId = ioBuffer.get();
            response.action = ioBuffer.get();
            response.state = ioBuffer.get();
            if (response.state != 0)
                response.cause = ioBuffer.get();
            response.state1 = ioBuffer.get();
            if (response.state1 != 0)
                response.cause1 = ioBuffer.get();
        }
        return response;
    }

    /**
     * 扫频请求
     *
     * @param action
     * @param mnc
     * @return
     */
    public IoBuffer scanFreqReqEncode(byte action, byte mnc) {
        IoBuffer ioBuffer = createIoBuffer();
        ioBuffer.put((byte) 0x0d);
        ioBuffer.put(action);
        ioBuffer.put(mnc);
        return ioBuffer;
    }

    /**
     * 扫频应答
     *
     * @param buffer
     */
    public ScanFreqResponse scanFreqRspDecode(IoBuffer buffer) {
        ScanFreqResponse s = null;
        if (buffer != null) {
            s = new ScanFreqResponse();
            parseHeader(buffer);
            s.msgId = buffer.get();
            s.action = buffer.get();
            s.state = buffer.get();
            if (s.state == 0) {
                s.cellIdx = buffer.get();
                s.cellsSrcCount = buffer.get();
                s.selectSCell = buffer.getShort();
                s.selectNCell = buffer.getShort();
                for (int i = 0; i < 125; i++) {
                    s.cellsCount.add(buffer.get());
                }
                for (int i = 0; i < 125; i++) {
                    s.cellsCountRxLev.add(buffer.get());
                }
                for (int i = 0; i < s.cellsSrcCount; i++) {
                    ScanFreqResponse.Entry1 entry1 = new ScanFreqResponse.Entry1();
                    entry1.netCuler = buffer.get();
                    entry1.baseCuler = buffer.get();
                    entry1.scellC1 = buffer.get();
                    entry1.scellC2 = buffer.get();
                    entry1.scellRxLev = buffer.get();
                    entry1.bestCellsCount = buffer.get();
                    entry1.nCellsCount = buffer.get();
                    for (int j = 0; j < entry1.bestCellsCount; j++) {
                        entry1.bestCells.add(buffer.getShort());
                    }
                    entry1.serverCell = buffer.getShort();
                    entry1.lac = buffer.getShort();
                    entry1.cellId = buffer.getInt();
                    for (int j = 0; j < entry1.nCellsCount; j++) {
                        ScanFreqResponse.Entry2 entry2 = new ScanFreqResponse.Entry2();
                        entry2.c1 = buffer.get();
                        entry2.c2 = buffer.get();
                        entry2.rxlev = buffer.get();
                        entry2.bsic = buffer.get();
                        entry2.nCell = buffer.getShort();
                        entry2.nLac = buffer.getShort();
                        entry2.nCellId = buffer.getInt();
                        entry1.entry2List.add(entry2);
                    }
                    s.entry1List.add(entry1);
                }
            } else {
                s.cause = buffer.get();
            }
        }
        return s;
    }


    /**
     * 基站参数设置请求
     *
     * @param action
     * @param mcc
     * @param mnc
     * @param freq
     * @param ncFreq
     * @param lac
     * @param power
     * @return
     */
    public IoBuffer confParamReqEncode(byte action, short mcc, byte mnc, short freq, short ncFreq, int lac, byte power) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x32);
        buffer.put(action);
        buffer.putShort(mcc);
        buffer.put(mnc);
        buffer.putShort(freq);
        buffer.putShort(ncFreq);
        buffer.putInt(lac);
        buffer.put(power);
        return buffer;
    }

    /**
     * 基站参数设置响应
     *
     * @param buffer
     */
    public UdpResponse confParamRspDecode(IoBuffer buffer) {
        UdpResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new UdpResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            response.state = buffer.get();
            if (response.state != 0)
                response.cause = buffer.get();
            response.state1 = buffer.get();
            if (response.state1 != 0)
                response.cause1 = buffer.get();
        }
        return response;
    }

    /**
     * 接管模式请求
     *
     * @param action
     * @param mode
     * @return
     */
    public IoBuffer workModeReqEncode(byte action, byte mode) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x34);
        buffer.put(action);
        buffer.put(mode);
        return buffer;
    }

    /**
     * 接管模式响应解码
     *
     * @param buffer
     * @return UdpResponse
     */
    public UdpResponse workModeRspDecode(IoBuffer buffer) {
        UdpResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new UdpResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            response.state1 = buffer.get();
            if (response.state1 != 0)
                response.cause1 = buffer.get();
            response.state2 = buffer.get();
            if (response.state2 != 0)
                response.cause2 = buffer.get();
        }
        return response;
    }

    /**
     * 短信广播请求
     *
     * @param action
     * @param userModelList
     * @param content
     * @param smsNo
     * @param smsCenter
     * @return
     */
    public IoBuffer smsBroadcastReqEncode(byte action, List<UserModel> userModelList,
                                          String content, String smsNo, String smsCenter) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x04);
        buffer.put(action);
        encodeDist(buffer, userModelList);
        try {
            buffer.putShort((short) content.getBytes("GBK").length);
            buffer.put(content.getBytes("GBK"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        buffer.put((byte) smsNo.getBytes().length);
        buffer.put(smsNo.getBytes());
        buffer.put((byte) smsCenter.getBytes().length);
        buffer.put(smsCenter.getBytes());
        return buffer;
    }

    /**
     * 短信广发请求响应
     *
     * @param buffer
     * @return
     */
    public UdpResponse smsBroadcastRspDecode(IoBuffer buffer) {
        UdpResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new UdpResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            response.state = buffer.get();
            if (response.state != 0) {
                response.cause = buffer.get();
            }
        }
        return response;
    }

    /**
     * 语音广播请求
     *
     * @param action
     * @param pcmFile
     * @param telephoneNo
     * @return
     */
    public IoBuffer pcmBroadcastReqEncode(byte action, List<UserModel> userModelList,
                                          String pcmFile, String telephoneNo) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x05);
        buffer.put(action);
        encodeDist(buffer, userModelList);
        if (action != 1) {
            buffer.put((byte) pcmFile.length());
            buffer.put(pcmFile.getBytes());
            buffer.put((byte) telephoneNo.length());
            buffer.put(telephoneNo.getBytes());
        }
        return buffer;
    }

    /**
     * 语音广播请求响应
     *
     * @param buffer
     * @return
     */
    public UdpResponse pcmBroadcastRspDecode(IoBuffer buffer) {
        UdpResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new UdpResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            response.state = buffer.get();
            if (response.state != 0)
                response.cause = buffer.get();
        }
        return response;
    }

    /**
     * 获取号码请求
     *
     * @param action
     * @param imsi
     * @return
     */
    public IoBuffer taskGetPhNumReqEncode(byte action, String imsi) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x36);
        buffer.put(action);
        buffer.put((byte) imsi.length());
        buffer.put(imsi.getBytes());
        return buffer;
    }

    /**
     * 获取号码响应
     *
     * @param buffer
     * @return
     */
    public TaskGetPhNumResponse taskGetPhNumRspDecode(IoBuffer buffer) {
        TaskGetPhNumResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new TaskGetPhNumResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            if (response.action == 1)
                try {
                    response.phNum = buffer.getString(buffer.get(), decoder);
                } catch (CharacterCodingException e) {
                    e.printStackTrace();
                }
            response.state = buffer.get();
            if (response.state != 0)
                response.cause = buffer.get();
        }
        return response;
    }

    /**
     * 监听用户请求
     *
     * @param action        0:监听 ，1:取消监听，2:切换监听目标，3:目标通话结束
     * @param userModelList
     * @return
     */
    public IoBuffer monitorReqEncode(byte action, List<UserModel> userModelList) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x02);
        buffer.put(action);
        encodeDist(buffer, userModelList);
        return buffer;
    }

    /**
     * 监听应答
     *
     * @param buffer
     * @return
     */
    public MonitorResponse monitorRspDecode(IoBuffer buffer) {
        MonitorResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new MonitorResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            response.state = buffer.get();
            if (response.state != 0) {
                response.cause = buffer.get();
            } else {
                byte num = buffer.get();
                for (int i = 0; i < num; i++) {
                    MonitorResponse.Entry entry = new MonitorResponse.Entry();
                    UserModel userModel = new UserModel();
                    try {
                        userModel.imsi = buffer.getString(buffer.get(), decoder);
                        userModel.username = buffer.getString(buffer.get(), decoder);
                        userModel.imei = buffer.getString(buffer.get(), decoder);
                        entry.userModel = userModel;
                    } catch (CharacterCodingException e) {
                        e.printStackTrace();
                    }
                    entry.channelNo = buffer.get();
                    response.entryList.add(entry);
                }
            }
        }
        return response;
    }

    /**
     * 特殊用户请求
     *
     * @param action
     * @param userModel
     * @return
     */
    public IoBuffer spcUserReqEncode(byte action, UserModel userModel) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x38);
        buffer.put(action);
        buffer.put((byte) 1);
        buffer.put((byte) userModel.imsi.length());
        buffer.put(userModel.imsi.getBytes());
        if (!StringUtils.isBlank(userModel.username)) {
            try {
                buffer.put((byte) userModel.username.getBytes(CHARSET).length);
                buffer.put(userModel.username.getBytes(CHARSET));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            buffer.put((byte) 0);
        }
        if (!StringUtils.isBlank(userModel.imei)) {
            buffer.put((byte) userModel.imei.length());
            buffer.put(userModel.imei.getBytes());
        } else {
            buffer.put((byte) 0);
        }
        if (!StringUtils.isBlank(userModel.tmsi)) {
            buffer.put((byte) userModel.tmsi.length());
            buffer.put(userModel.tmsi.getBytes());
        } else {
            buffer.put((byte) 0);
        }
        buffer.put(Byte.valueOf(userModel.property));
        if (!StringUtils.isBlank(userModel.phoneNumber)) {
            buffer.put((byte) userModel.phoneNumber.length());
            buffer.put(userModel.phoneNumber.getBytes());
        } else {
            buffer.put((byte) 0);
        }
        return buffer;
    }

    /**
     * 特殊用户应答
     *
     * @param buffer
     * @return
     */
    public UdpResponse spcUserRspDecode(IoBuffer buffer) {
        UdpResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new UdpResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            response.state = buffer.get();
            if (response.state != 0)
                response.cause = buffer.get();
        }
        return response;
    }

    /**
     * 用户定位请求
     *
     * @param action
     * @param userList
     * @return
     */
    public IoBuffer locationReqEncode(byte action, List<UserModel> userList) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x01);
        buffer.put(action);
        encodeDist(buffer, userList);
        return buffer;
    }

    /**
     * 用户定位响应
     *
     * @param buffer
     * @return
     */
    public LocationResponse locationRspDecode(IoBuffer buffer) {
        LocationResponse resp = null;
        if (buffer != null) {
            parseHeader(buffer);
            resp = new LocationResponse();
            resp.msgId = buffer.get();
            resp.action = buffer.get();
            resp.state = buffer.get();
            if (resp.state != 0) {
                resp.cause = buffer.get();
            } else {
                byte num = buffer.get();
                resp.userModelList = new ArrayList<UserModel>(num);
                try {
                    for (int i = 0; i < num; i++) {
                        UserModel userModel = new UserModel();
                        userModel.imsi = buffer.getString(buffer.get(), decoder);
                        userModel.username = buffer.getString(buffer.get(), decoder);
                        userModel.imei = buffer.getString(buffer.get(), decoder);
                        resp.userModelList.add(userModel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return resp;
    }

    /**
     * 功率检测请求
     *
     * @param action
     * @param imsi
     * @return
     */
    public IoBuffer checkPowerReqEncode(byte action, String imsi) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x39);
        buffer.put(action);
        buffer.put((byte) imsi.length());
        buffer.put(imsi.getBytes());
        return buffer;
    }

    /**
     * 功率检测应答
     *
     * @param buffer
     * @return
     */
    public UdpResponse checkPowerRspDecode(IoBuffer buffer) {
        UdpResponse resp = null;
        if (buffer != null) {
            parseHeader(buffer);
            resp = new UdpResponse();
            resp.msgId = buffer.get();
            resp.action = buffer.get();
            resp.state = buffer.get();
            if (resp.state != 0)
                resp.cause = buffer.get();
        }
        return resp;
    }

    /**
     * 回铃音请求
     *
     * @param action
     * @param ringNameType
     * @return
     */
    public IoBuffer checkPowerRingReqEncode(byte action, byte ringNameType) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x41);
        buffer.put(action);
        buffer.put(ringNameType);
        return buffer;
    }

    /**
     * 采集用户请求
     *
     * @param action
     * @return
     */
    public IoBuffer collectUserReqEncode(byte action) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x42);
        buffer.put(action);
        return buffer;
    }

    /**
     * 录音播放列表请求
     *
     * @param action
     * @return
     */
    public IoBuffer taskRcdListReqEncode(byte action) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x43);
        buffer.put(action);
        return buffer;
    }

    /**
     * 录音文件列表响应
     *
     * @param buffer
     * @return
     */
    public TaskRcdListResponse taskRcdListRspDecode(IoBuffer buffer) {
        TaskRcdListResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new TaskRcdListResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            int repeatNum = buffer.get();
            for (int i = 0; i < repeatNum; i++) {
                byte length = buffer.get();
                try {
                    response.pcmListNameList.add(buffer.getString(length, decoder));
                } catch (CharacterCodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return response;
    }

    /**
     * 播放录音文件请求
     *
     * @param action
     * @param fileType
     * @param fileName
     * @return
     */
    public IoBuffer playRecordReqEncode(byte action, byte fileType, String fileName) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x09);
        buffer.put(action);
        if (action != 2) {
            buffer.put(fileType);
            buffer.put((byte) fileName.length());
            buffer.put(fileName.getBytes());
        }
        return buffer;
    }

    /**
     * 播放录音响应
     *
     * @param buffer
     * @return
     */
    public PlayRecordResponse playRecordRspDecode(IoBuffer buffer) {
        PlayRecordResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new PlayRecordResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            response.state = buffer.get();
            if (response.state != 0) {
                response.cause = buffer.get();
            } else {
                if (response.action == 2) {
                    byte num = buffer.get();
                    for (int i = 0; i < num; i++) {
                        int length = buffer.get();
                        byte[] pcmFile = new byte[length];
                        buffer.get(pcmFile);
                        response.pcmFileList.add(pcmFile);
                    }
                } else if (response.action == 5) {
                    response.pcmLength = buffer.getShort();
                    response.pcmBytes = new byte[response.pcmLength];
                    buffer.get(response.pcmBytes);
                }
            }
        }
        return response;
    }

    /**
     * 短信审计请求
     *
     * @param action
     * @param userList
     * @return
     */
    public IoBuffer shortMsgSwithReqEncode(byte action, List<UserModel> userList) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x45);
        buffer.put(action);
        buffer.put((byte) userList.size());
        for (UserModel user : userList) {
            buffer.put((byte) user.imsi.length());
            buffer.put(user.imsi.getBytes());
        }
        return buffer;
    }

    /**
     * 短信审计响应
     *
     * @param buffer
     * @return
     */
    public UdpResponse shortMsgSwithRspDecode(IoBuffer buffer) {
        UdpResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new UdpResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            response.state = buffer.get();
            if (response.state != 0) {
                response.cause = buffer.get();
            }
            if (response.state1 != 0)
                response.cause1 = buffer.get();
        }
        return response;
    }

    /**
     * 基站上报用户，短信，基站状态应答
     *
     * @param buffer
     * @return
     */
    public MsgResponse msgRspDecode(IoBuffer buffer) {
        MsgResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new MsgResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            response.msgType = buffer.get();
            if (response.msgType == 1) {
                UserModel user = new UserModel();
                try {
                    user.imsi = buffer.getString(buffer.get(), decoder);
                    user.imei = buffer.getString(buffer.get(), decoder);
                    user.tmsi = buffer.getString(buffer.get(), decoder);
                    user.lastUpdated = buffer.getString(buffer.get(), decoder);
                    user.phoneNumber = buffer.getString(buffer.get(), decoder);
                    user.area = buffer.getString(buffer.get(), decoder);
                    user.status = buffer.get();
                    user.property = buffer.get() + "";
                    user.username = buffer.getString(buffer.get(), decoder);
                    response.userModel = user;
                } catch (CharacterCodingException e) {
                    e.printStackTrace();
                }
            } else if (response.msgType == 2) {
                try {
                    response.powerImsi = buffer.getString(buffer.get(), decoder);
                    response.power = buffer.get();
                } catch (CharacterCodingException e) {
                    e.printStackTrace();
                }
            } else if (response.msgType == 4) {
                try {
                    SmsModel smsModel = new SmsModel();
                    smsModel.imsi = buffer.getString(buffer.get(), decoder);
                    smsModel.phNum = buffer.getString(buffer.get(), decoder);
                    smsModel.sc = buffer.getString(buffer.get(), decoder);
                    smsModel.upTime = buffer.getString(buffer.get(), decoder);
                    smsModel.content = buffer.getString(buffer.get(), decoder);
                    smsModel.direction = buffer.get();
                    response.smsModel = smsModel;
                } catch (CharacterCodingException e) {
                    e.printStackTrace();
                }
            } else if (response.msgType == 8) {
                response.btsState = buffer.get();
            }
        }
        return response;
    }

    /**
     * 监听语音主动响应
     *
     * @param buffer
     * @return
     */
    public MonitorPcmResponse monitorPcmRspDecode(IoBuffer buffer) {
        MonitorPcmResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new MonitorPcmResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            try {
                response.imsi = buffer.getString(buffer.get(), decoder);
            } catch (CharacterCodingException e) {
                e.printStackTrace();
            }
            response.pcm = new byte[320];
            buffer.get(response.pcm);
        }
        return response;
    }

    /**
     * 特殊用户信息查询请求
     *
     * @param action
     * @param userModel
     * @return
     */
    public IoBuffer findSpcInfoReqEncode(byte action, UserModel userModel, String userPro) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x52);
        buffer.put(action);
        if (userModel != null) {
            if (!StringUtils.isBlank(userModel.imsi)) {
                byte[] imsiByte = userModel.imsi.getBytes();
                buffer.put((byte) imsiByte.length);
                buffer.put(imsiByte);
            } else {
                buffer.put((byte) 0);
            }

            if (!StringUtils.isBlank(userModel.imei)) {
                byte[] imeiByte = userModel.imei.getBytes();
                buffer.put((byte) imeiByte.length);
                buffer.put(imeiByte);
            } else {
                buffer.put((byte) 0);
            }

            if (!StringUtils.isBlank(userModel.phoneNumber)) {
                byte[] phoneByte = userModel.phoneNumber.getBytes();
                buffer.put((byte) phoneByte.length);
                buffer.put(phoneByte);
            } else {
                buffer.put((byte) 0);
            }

            if (!StringUtils.isBlank(userModel.username)) {
                byte[] nameByte = userModel.username.getBytes();
                buffer.put((byte) nameByte.length);
                buffer.put(nameByte);
            } else {
                buffer.put((byte) 0);
            }
        } else {
            buffer.put((byte) 0);
            buffer.put((byte) 0);
            buffer.put((byte) 0);
            buffer.put((byte) 0);
        }
        buffer.put(Byte.parseByte(userPro));
        return buffer;
    }

    /**
     * 特殊用户信息查询应答
     *
     * @param buffer
     * @return
     */
    public UserResponse findSpcInfoRspDecode(IoBuffer buffer) {
        UserResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new UserResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            response.state = buffer.get();
            if (response.state != 0)
                response.cause = buffer.get();
            else {
                response.userModel = new UserModel();
                try {
                    response.userModel.imsi = buffer.getString(buffer.get(), decoder);
                    response.userModel.imei = buffer.getString(buffer.get(), decoder);
                    byte phLen = buffer.get();
                    if (((byte) 0) != phLen) {
                        response.userModel.phoneNumber = buffer.getString(phLen, decoder);
                    }

                    byte nameLen = buffer.get();
                    if (((byte) 0) != nameLen) {
                        response.userModel.username = buffer.getString(nameLen, decoder);
                    }

                    response.userModel.property = String.valueOf(buffer.get());
                } catch (CharacterCodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return response;
    }

    /**
     * 短信信息查询请求
     *
     * @param action
     * @param imsi
     * @param dataFrom
     * @param dataTo
     * @return
     */
    public IoBuffer findSMSInfoReqEncode(byte action, String imsi, String dataFrom, String dataTo) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x54);
        buffer.put(action);
        buffer.put((byte) imsi.length());
        buffer.put(imsi.getBytes());
        buffer.put((byte) dataFrom.length());
        buffer.put(dataFrom.getBytes());
        buffer.put((byte) dataTo.length());
        buffer.put(dataTo.getBytes());
        return buffer;
    }

    /**
     * 短信信息查询响应
     *
     * @param buffer
     * @return
     */
    public SmsInfoResponse findSmsInfoRspDecode(IoBuffer buffer) {
        SmsInfoResponse response = null;
        try {
            if (buffer != null) {
                parseHeader(buffer);
                response = new SmsInfoResponse();
                response.msgId = buffer.get();
                response.action = buffer.get();
                response.state = buffer.get();
                if (response.state != 0)
                    response.cause = buffer.get();
                else {
                    response.imsi = buffer.getString(buffer.get(), decoder);
                    response.datetime = buffer.getString(buffer.get(), decoder);
                    response.smsNum = buffer.getString(buffer.get(), decoder);
                    response.modSmsNum = buffer.getString(buffer.get(), decoder);
                    response.message = buffer.getString(buffer.get(), decoder);
                    response.modMessage = buffer.getString(buffer.get(), decoder);
                    response.scenter = buffer.getString(buffer.get(), decoder);
                    response.smsType = buffer.getString(buffer.get(), decoder);
                    response.disCardFlag = buffer.get();
                    response.taskId = buffer.get();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * 呼叫信息查询请求
     *
     * @param action
     * @param imsi
     * @param dateFrom
     * @param dateTo
     * @return
     */
    public IoBuffer findCallInfoReqEncode(byte action, String imsi, String dateFrom, String dateTo) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x56);
        buffer.put(action);
        buffer.put((byte) imsi.length());
        buffer.put(imsi.getBytes());
        buffer.put((byte) dateFrom.length());
        buffer.put(dateFrom.getBytes());
        buffer.put((byte) dateTo.length());
        buffer.put(dateTo.getBytes());
        return buffer;
    }

    /**
     * 呼叫信息查询响应
     *
     * @param buffer
     * @return
     */
    public CallInfoResponse findCallInfoRspDecode(IoBuffer buffer) {
        CallInfoResponse response = null;
        try {
            if (buffer != null) {
                parseHeader(buffer);
                response = new CallInfoResponse();
                response.imsi = buffer.getString(buffer.get(), decoder);
                response.callTime = buffer.getString(buffer.get(), decoder);
                response.phNum = buffer.getString(buffer.get(), decoder);
                response.callType = buffer.getString(buffer.get(), decoder);
                response.taskId = buffer.get();
                response.readFlag = buffer.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * 呼叫信息添加请求
     *
     * @param action
     * @param callType
     * @param imsi
     * @param imei
     * @param dateTime
     * @param phNum
     * @param readFlag
     * @return
     */
    public IoBuffer addCallInfoReqEncode(byte action, String callType, String imsi, String imei,
                                         String dateTime, String phNum, byte readFlag) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x58);
        buffer.put(action);
        buffer.put((byte) callType.length());
        buffer.put(callType.getBytes());
        buffer.put((byte) imsi.length());
        buffer.put(imsi.getBytes());
        buffer.put((byte) imei.length());
        buffer.put(imei.getBytes());
        buffer.put((byte) dateTime.length());
        buffer.put(dateTime.getBytes());
        buffer.put((byte) phNum.length());
        buffer.put(phNum.getBytes());
        buffer.put(readFlag);
        return buffer;
    }

    /**
     * 短信更新请求
     *
     * @param action
     * @param smsType
     * @param imsi
     * @param dateTime
     * @param modMessage
     * @param modSmsNum
     * @return
     */
    public IoBuffer updateSmsInfoReqEncode(byte action, String smsType, String imsi, String dateTime,
                                           String modMessage, String modSmsNum) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x59);
        buffer.put(action);
        buffer.put((byte) smsType.length());
        buffer.put(smsType.getBytes());
        buffer.put((byte) imsi.length());
        buffer.put(imsi.getBytes());
        buffer.put((byte) dateTime.length());
        buffer.put(dateTime.getBytes());
        buffer.put((byte) modMessage.length());
        buffer.put(modMessage.getBytes());
        buffer.put((byte) modSmsNum.length());
        buffer.put(modSmsNum.getBytes());
        return buffer;
    }

    /**
     * 实时语音通话
     *
     * @param action
     * @param audioData
     * @return
     */
    public IoBuffer voiceCallReqEncode(byte action, byte[] audioData) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x60);
        buffer.put(action);
        buffer.put(audioData);
        return buffer;
    }

    /**
     * 设置电话号码
     *
     * @param action
     * @param phoneNumber
     * @return
     */
    public IoBuffer setPhoneNumberReqEncode(byte action, String phoneNumber) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x63);
        buffer.put(action);
        buffer.put((byte) phoneNumber.length());
        buffer.put(phoneNumber.getBytes());
        return buffer;
    }

    /**
     * 设置电话号码相应
     *
     * @param buffer
     * @return
     */
    public SetPhoneNumberResponse setPhoneNumberRspDecode(IoBuffer buffer) {
        SetPhoneNumberResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new SetPhoneNumberResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            try {
                response.phoneNumber = buffer.getString(buffer.get(), decoder);
            } catch (CharacterCodingException e) {
                e.printStackTrace();
            }
        }
        return response;
    }


    /**
     * 退出程序
     *
     * @param action
     * @return
     */
    public IoBuffer shutServerReqEncode(byte action) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x65);
        buffer.put(action);
        return buffer;
    }

    /**
     * 获取卡状态
     *
     * @param buffer
     * @return
     */
    public CardStateResponse getCardStateRspDecode(IoBuffer buffer) {
        CardStateResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new CardStateResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            response.proxyCardState = buffer.get();
            response.getPhoneNumberCardState = buffer.get();
        }
        return response;
    }

    /**
     * 语音回复
     *
     * @param buffer
     * @return
     */
    public MonitorPcmResponse getVoiceCallFeedbackRspDecode(IoBuffer buffer) {
        MonitorPcmResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new MonitorPcmResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
            try {
                response.imsi = buffer.getString(buffer.get(), decoder);
            } catch (CharacterCodingException e) {
                e.printStackTrace();
            }
            response.pcm = new byte[320];
            buffer.get(response.pcm);
        }
        return response;
    }

    /**
     * 新的心跳包编码
     *
     * @return
     */
    public IoBuffer newHeartBeatReqEncode(byte action) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x67);
        buffer.put(action);
        return buffer;
    }

    /**
     * 新的心跳解码
     */
    public UdpResponse newHeartBeatRspDecode(IoBuffer buffer) {
        UdpResponse response = null;
        if (buffer != null) {
            parseHeader(buffer);
            response = new UdpResponse();
            response.msgId = buffer.get();
            response.action = buffer.get();
        }
        return response;
    }


    /**
     * 查询用户基本信息编码
     *
     * @param action
     * @param imsi
     * @param imei
     * @param dateFrom
     * @param dateTo
     * @return
     */
    public IoBuffer findBaseInfoReqEncode(byte action, String imsi, String imei, String dateFrom, String dateTo) {
        IoBuffer buffer = createIoBuffer();
        buffer.put((byte) 0x50);
        buffer.put(action);
        if (!StringUtils.isBlank(imsi)) {
            byte[] imsiByte = imsi.getBytes();
            buffer.put((byte) imsiByte.length);
            buffer.put(imsiByte);
        } else {
            buffer.put((byte) 0);
        }

        if (!StringUtils.isBlank(imei)) {
            byte[] imeiByte = imei.getBytes();
            buffer.put((byte) imeiByte.length);
            buffer.put(imeiByte);
        } else {
            buffer.put((byte) 0);
        }

        if (!StringUtils.isBlank(dateFrom)) {
            byte[] dateFromByte = dateFrom.getBytes();
            buffer.put((byte) dateFromByte.length);
            buffer.put(dateFromByte);
        } else {
            buffer.put((byte) 0);
        }

        if (!StringUtils.isBlank(dateTo)) {
            byte[] dateToByte = dateTo.getBytes();
            buffer.put((byte) dateToByte.length);
            buffer.put(dateToByte);
        } else {
            buffer.put((byte) 0);
        }
        return buffer;
    }

    /**
     * 查询基本信息解码
     *
     * @param buffer
     * @return
     */
    public UserResponse findBaseInfoRspDecode(IoBuffer buffer) {
        UserResponse response = null;
        if (buffer != null) {
            response = new UserResponse();
            parseHeader(buffer);
            response.msgId = buffer.get();
            response.action = buffer.get();
            response.state = buffer.get();
            if (response.state != 0) {
                response.cause = buffer.get();
            }
            response.userModel = new UserModel();
            try {
                response.userModel.imsi = buffer.getString(buffer.get(), decoder);
                response.userModel.imei = buffer.getString(buffer.get(), decoder);
                byte phLen = buffer.get();
                if (((byte) 0) != phLen) {
                    response.userModel.phoneNumber = buffer.getString(phLen, decoder);
                }

                byte nameLen = buffer.get();
                if (((byte) 0) != nameLen) {
                    response.userModel.username = buffer.getString(nameLen, decoder);
                }

                byte timeLen = buffer.get();
                if (((byte) 0) != timeLen) {
                    response.userModel.lastUpdated = buffer.getString(timeLen, decoder);
                }

                response.userModel.property = String.valueOf(buffer.get());

            } catch (CharacterCodingException e) {
                e.printStackTrace();
            }
        }
        return response;
    }
}
