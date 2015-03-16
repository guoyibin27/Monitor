package com.zte.monitor.app.model.response;

import com.zte.monitor.app.model.UserModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 14-9-4.
 */
public class MonitorResponse extends UdpResponse {

    public List<Entry> entryList = new ArrayList<Entry>();

    public static class Entry {
        public UserModel userModel;
        public byte channelNo;
    }
}
