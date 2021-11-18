package com.jay.common.enums;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2021/11/18
 **/
public enum CompressorTypeEnum {
    /**
     * GZIP
     */
    GZIP((byte)1, "gzip");

    public byte code;
    public String type;

    CompressorTypeEnum(byte code, String type) {
        this.code = code;
        this.type = type;
    }

    public static String getType(byte code){
        for(CompressorTypeEnum typeEnum : CompressorTypeEnum.values()){
            if(typeEnum.code == code){
                return typeEnum.type;
            }
        }
        return null;
    }
}
