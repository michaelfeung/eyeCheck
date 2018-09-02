package com.crawler.industry.config;

import com.crawler.industry.guangdong.GeetestUtils;
import com.google.common.base.Splitter;
import org.apache.commons.collections.ListUtils;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.proxy.Proxy;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by chenshengju on 2017/10/15 0015.
 * 配置文件加载类
 */
public class CrawlerConfig {
    /**
     * 该网站是否能爬取，用于中断下一个流程不再继续
     */
    public static boolean canCrawler=true;
    /**
     * 当”企业经营至“提取不到时，默认的字段为
     */
    public static String businessTo="长期";
    /**
     * 用于测试网站的搜索引擎是不是出问题了，先用一个可以搜索出来的公司来测试，如果不能搜出来则说明这个网站有问题了
     */
    public static String testCompanyName="广东佛山";
    private static final Logger log= LoggerFactory.getLogger(CrawlerConfig.class);
    /**
     * 爬虫调试文件所在根目录
     */
    public static String filePath="";
    /**
     * 线程数
     */
    public static int theadCount;
    /**
     * 爬取一个页面后的休眠时间，越大速度越慢
     */
    public static int sleepTime=3000;
    /**
     * 失败后的尝试次数
     */
    public static int retryTimes;

    /**
     * 分割符
     */
    public static String spilt="^_^";
    /**
     * 读文件的分割符
     */
    public static String readSpilt="^_^";

    /**
     * 写程序文件的编码
     */
    public static String encode="gbk";
    /**
     * 写程序文件的编码
     */
    public static String gdDomain="gd.gsxt.gov.cn";
    /**
     * 写程序文件的编码
     */
    public static boolean refreshUrl=false;
    /**
     * 写程序文件的编码
     */
    public static String testPath="";
    /**
     * 是否使滑块通过率100%？
     */
    public static boolean isAllGeetestPass=true;
//    /**
//     * 临时sessionid
//     */
//    public static String JSESSIONID="";
//    /**
//     * 临时cookieId
//     */
//    public static String WEB="";
//    /**
//     * 临时极验challengeId
//     */
//    public static String geetest_challenge="";
//    /**
//     * 临时极验validateId
//     */
//    public static String geetest_validate="";
//    /**
//     * 临时极验seccodeId
//     */
//    public static String geetest_seccode="";

    /**
     * 当出现提示，访问太过频繁，则休息多久
     */
    public static long sleepIP=900000;
    /**
     * 配置代理
     */
    public static List<Proxy> ipProxyList;
    /**
     * 注册号提取字段
     */
    public static String[] regNums={"统一社会信用代码/注册号","注册号","注册号/统一社会信用代码"};
    /**
     * 企业名称提取字段
     */
    public static String[] companyNames={"企业名称","名称"};
    /**
     * 法人提取字段
     */
    public static String[] legalPersons={"法定代表人","负责人","经营者","投资人","执行事务合伙人"};
    /**
     * 营业期限至 提取字段
     */
    public static String[] businessTos={"营业期限至","合伙期限至"};
    /**
     * 注册资本提取字段
     */
    public static String[] regMoneys={"注册资本"};
    /**
     * 住所提取字段
     */
    public static String[] addresss={"住所","营业场所","经营场所","主要经营场所"};
    /**
     * 经营范围提取字段
     */
    public static String[] businessScopes={"经营范围"};
    /**
     * 登录用户名
     */
    public static String username="";
    /**
     * 登录密码
     */
    public static String password="";
    static{
        Properties p=new Properties();
        try {
            p.load(new InputStreamReader(new FileInputStream("resources/config.properties"),"utf-8"));
            filePath= (String) p.get("filePath");
            theadCount= Integer.parseInt((String) p.get("theadCount"));
            sleepTime= Integer.parseInt((String) p.get("sleepTime"));
            retryTimes= Integer.parseInt((String) p.get("retryTimes"));
            spilt= (String) p.get("spilt");
            encode= (String) p.get("encode");
            gdDomain= (String) p.get("gdDomain");
            readSpilt= (String) p.get("readSpilt");
            testPath= (String) p.get("testPath");
            refreshUrl= Boolean.parseBoolean((String) p.get("refreshUrl"));
            isAllGeetestPass= Boolean.parseBoolean((String) p.get("isAllGeetestPass"));
            //添加代理ip模块
            sleepIP= Long.parseLong((String) p.get("sleepIP"))*60000;
            try {
               String ipProxyLine=((String) p.get("ipProxyList"));
               if(StringUtil.isNotBlank(ipProxyLine))
               {
                   ipProxyList=new ArrayList<>();
                   String[] ipProxys =ipProxyLine.split(",");
                   for (String ipProxy : ipProxys) {
                       String[] ipLine = ipProxy.split(":");
                       ipProxyList.add(new Proxy(ipLine[0],Integer.parseInt(ipLine[1])));
                   }
                   //如果只是一个代理ip那么可以认为这是一个本地局域网代理
                   if(ipProxyList.size()==1)
                   {
                       System.setProperty("http.proxySet", "true");
                       System.setProperty("http.proxyHost", ipProxyList.get(0).getHost());
                       System.setProperty("http.proxyPort", ipProxyList.get(0).getPort()+"");
                   }
               }

            } catch (Exception e) {
                log.info("加载代理ip异常请确认代理配置的格式是否正确是否正确");
                ipProxyList=null;
            }
            //添加提取字段模块
            regNums= stringToArray((String) p.get("regNums"));
            companyNames= stringToArray((String) p.get("companyNames"));
            legalPersons= stringToArray((String) p.get("legalPersons"));
            businessTos= stringToArray((String) p.get("businessTos"));
            regMoneys= stringToArray((String) p.get("regMoneys"));
            addresss= stringToArray((String) p.get("addresss"));
            businessScopes= stringToArray((String) p.get("businessScopes"));
            //添加登录信息
            username = (String) p.getProperty("username");
            password = (String) p.getProperty("password");
            //添加快速查询模块

//            JSESSIONID= (String) p.get("JSESSIONID");
//            WEB= (String) p.get("WEB");
//            geetest_challenge= (String) p.get("geetest_challenge");
//            geetest_validate= (String) p.get("geetest_validate");
//            geetest_seccode= (String) p.get("geetest_seccode");
        } catch (IOException e) {
            log.error("属性文件无法找到");
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
