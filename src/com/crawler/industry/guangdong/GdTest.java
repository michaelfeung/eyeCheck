package com.crawler.industry.guangdong;

import com.crawler.industry.config.CrawlerConfig;
import com.crawler.selenium.WebPhantomJsDriverPool;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.util.Map;

/**
 * Created by chenshengju on 2017/10/18 0018.
 */
public class GdTest {
    public static void main(String[] args) {
        GuangDongIndustry guangDongIndustry=new GuangDongIndustry();
        guangDongIndustry.startCrawler(CrawlerConfig.testPath);
    }
    public static void main1(String[] args) throws Exception {
//        GuangDongIndustry guangDongIndustry=new GuangDongIndustry();
// 	       guangDongIndustry.startCrawler(CrawlerConfig.testPath);
//        GuangDongIndustryFast guangDongIndustryFast=new GuangDongIndustryFast();
//        guangDongIndustryFast.startFast(CrawlerConfig.testPath);
//        Map map=GeetestUtils.getGeetestKey();
        WebPhantomJsDriverPool webPhantomJsDriverPool=new WebPhantomJsDriverPool();
//        webPhantomJsDriverPool.setProxy();
        PhantomJSDriver driver = (PhantomJSDriver) webPhantomJsDriverPool.get();

//        driver.executePhantomJS("var page=this;" +
//                "page.onResourceRequested = function(requestData, networkRequest) {" +
//                "if(requestData.url=='http://www.gsxt.gov.cn/index.html'){"+
////                "requestData.url='https://www.baidu.com'"+
//                "requestData.url='https://www.baidu.com'"+
//                "}};");
        driver.get("http://gd.gsxt.gov.cn/");
//        driver.get("http://gsxt.gdgs.gov.cn//aiccips");

        long start=System.currentTimeMillis();
//        driver.get("https://www.baidu.com");
        System.out.println(driver.getPageSource());
        System.out.println(driver.getTitle());
        long end=System.currentTimeMillis();
        System.out.println("一共用了"+(end-start)/1000);
    }

    public static void main2(String[] args) throws Exception {
        Map geetestKey = GeetestUtils.getGeetestKey();
        geetestKey.forEach((a,b)-> System.out.println(a+":"+b));
    }
}
