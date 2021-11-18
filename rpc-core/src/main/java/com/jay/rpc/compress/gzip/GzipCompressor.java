package com.jay.rpc.compress.gzip;

import com.jay.rpc.compress.Compressor;
import io.protostuff.ByteArrayInput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * <p>
 *  GZIP 压缩工具
 * </p>
 *
 * @author Jay
 * @date 2021/11/18
 **/
public class GzipCompressor implements Compressor {

    private static final int BUFFER_SIZE = 1024;

    @Override
    public byte[] compress(byte[] src) {
        if(src == null || src.length == 0){
            throw new NullPointerException();
        }
        try(ByteArrayOutputStream out = new ByteArrayOutputStream(); GZIPOutputStream gzip = new GZIPOutputStream(out)){
            gzip.write(src);
            gzip.flush();
            gzip.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip compress error", e);
        }
    }

    @Override
    public byte[] decompress(byte[] src) {
        if(src == null || src.length == 0){
            throw new NullPointerException();
        }
        try(ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(src))){

            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while((n = gzip.read(buffer,0, BUFFER_SIZE)) > -1){
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip decompress error", e);
        }
    }
}
