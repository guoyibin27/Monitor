// IUdpConnectionInterface.aidl
package com.zte.monitor.app.udp.aidl;

interface IUdpConnectionInterface {

    void sendRequest(in byte[] request);
}
