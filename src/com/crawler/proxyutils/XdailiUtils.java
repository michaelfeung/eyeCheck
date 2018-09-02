package com.crawler.proxyutils;

import java.util.Date;

/**
 * Created by chenshengju on 2017/10/5 0005.
 */
public class XdailiUtils {
    public static String getProxyHeader()
    {
        int timestamp = (int) (new Date().getTime()/1000);
        String orderno="ZF20171057597WPygbV";
        String secret="a34d37f5fe134566ab359695bb241183";
        //拼装签名字符串
        String planText = String.format("orderno=%s,secret=%s,timestamp=%d", orderno, secret, timestamp);

        //计算签名
        String sign = org.apache.commons.codec.digest.DigestUtils.md5Hex(planText).toUpperCase();

        //拼装请求头Proxy-Authorization的值
        String authHeader = String.format("sign=%s&orderno=%s&timestamp=%d", sign, orderno, timestamp);
        return authHeader;
    }
}
