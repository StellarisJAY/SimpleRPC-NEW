package com.jay.rpc.constants;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2021/11/18
 **/
public class RpcConstants {

    public static final byte[] MAGIC_NUMBER = {(byte)'s', (byte)'r', (byte)'p', (byte)'c'};

    public static final byte VERSION = 1;

    public static final int MIN_TOTAL_LENGTH = 16;
    public static final int HEAD_LENGTH = 16;

    public static final byte TYPE_REQUEST = 1;
    public static final byte TYPE_RESPONSE = 2;
    public static final byte TYPE_HEARTBEAT_REQUEST = 3;
    public static final byte TYPE_HEARTBEAT_RESPONSE = 4;

    public static final int MAX_MESSAGE_LENGTH = 8 * 1024 * 1024;
}
