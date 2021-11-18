package com.jay.rpc.compress;

import com.jay.common.extention.SPI;

/**
 * Compression tools
 * @author Jay
 */
@SPI
public interface Compressor {

    /**
     * compress
     * @param src source
     * @return result
     */
    byte[] compress(byte[] src);

    /**
     * decompress
     * @param src source
     * @return result
     */
    byte[] decompress(byte[] src);
}
