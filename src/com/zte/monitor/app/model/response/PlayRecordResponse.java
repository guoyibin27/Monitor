package com.zte.monitor.app.model.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 14-9-4.
 */
public class PlayRecordResponse extends UdpResponse {

    public List<byte[]> pcmFileList = new ArrayList<byte[]>();
    public short pcmLength;
    public byte[] pcmBytes;
}
