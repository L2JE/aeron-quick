package org.jetc.aeron.quick.samples.rpc;

import org.jetc.aeron.quick.AeronQuickManager;

public class BasicRpcCommunicationExample {
    public static void main(String[] args){
        System.out.println("HEllo from BasicRpcCommunicationExample");
        AeronQuickManager.initAeron("from BasicRpcCommunicationExample");
    }
}
