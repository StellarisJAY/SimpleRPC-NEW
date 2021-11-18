package com.jay.common.enums;

/**
 * @author Jay
 */

public enum SerializerTypeEnum {

    /**
     * protostuff
     */
    PROTOSTUFF((byte)1, "protostuff");

    public byte code;
    public String type;

    SerializerTypeEnum(byte code, String type) {
        this.code = code;
        this.type = type;
    }

    public static String getType(byte code){
        for(SerializerTypeEnum enums : SerializerTypeEnum.values()){
            if(enums.code == code){
                return enums.type;
            }
        }
        return null;
    }
}
