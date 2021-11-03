package com.jay.rpc.util;

import org.springframework.beans.factory.config.YamlMapFactoryBean;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <p>
 *    properties文件处理工具
 * </p>
 *
 * @author Jay
 * @date 2021/11/1
 **/
public class PropertiesUtil {

    private static Properties properties;

    static{
        try {
            properties = readProperties("application.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties readProperties(String path) throws IOException {
        InputStream propertiesStream = PropertiesUtil.class.getClassLoader().getResourceAsStream("application.properties");
        Properties properties = new Properties();
        properties.load(propertiesStream);
        return properties;
    }

    public static String getAttribute(String name){
        return properties.getProperty(name);
    }

    public static int getIntegerAttribute(String name){
        return Integer.parseInt(properties.getProperty(name));
    }
}
