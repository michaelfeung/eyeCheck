package com.crawler.selenium;

import com.crawler.industry.config.CrawlerConfig;
import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

/**
 * Created by chenshengju on 2017/10/30 0030.
 */
public class PhantomjsConfig {
    private static final Logger log= LoggerFactory.getLogger(PhantomjsConfig.class);

    public static String[] phantomArgs={};
    static{
        Properties p=new Properties();
        try {
            p.load(new InputStreamReader(new FileInputStream("resources/config.properties"),"utf-8"));
            String phantomArgss= (String) p.get("phantomArgs");
            phantomArgs=stringToArray(phantomArgss);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static String[] stringToArray(String ss)
    {
        String[] sArray=null;
        try {
            Splitter splitter=Splitter.on(",");
            List<String> list = splitter.trimResults().omitEmptyStrings().splitToList(ss);
            sArray=new String[list.size()];
            list.toArray(sArray);
        } catch (Exception e) {
            log.info("该{}字段解析失败配置有误，本次使用默认的配置");
        }
        return sArray;

    }
}
