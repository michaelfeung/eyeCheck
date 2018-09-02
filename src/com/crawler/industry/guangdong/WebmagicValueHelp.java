package com.crawler.industry.guangdong;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chenshengju on 2017/10/24 0024.
 * 用于在管道对象和爬虫主类对象传值的工具，因为该爬虫框架的限制性而写出的这样一个工具
 */
public class WebmagicValueHelp {
    //查不出来的公司名单
    public static Set<String> noUrlNameSet=new HashSet<>();
    //已经下载好的公司名单
    public static Set<String> lastCompanySet=new HashSet<>();
    public static Set<String> cacheUrlSet=new HashSet<>();
    //政府文件名demo:gov_20171024.txt
    public static String fileName;
    public static void init()
    {
        noUrlNameSet=new HashSet<>();
        lastCompanySet=new HashSet<>();
        cacheUrlSet=new HashSet<>();
        fileName=null;
    }
}
