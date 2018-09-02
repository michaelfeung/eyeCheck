package com.crawler.selenium;

import com.crawler.industry.guangdong.GuangDongIndustryFast;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by chenshengju on 2017/10/27 0027.
 */
public class SeleniumUtils {
    private static final Logger log = LoggerFactory.getLogger(GuangDongIndustryFast.class);
    /**
     * 等待元素加载，10s超时
     * @param driver
     * @param by
     */
    public static void waitForLoad(final WebDriver driver, final By by, final int timeOutSeconds){
        try {
            new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver d) {
                    WebElement element = driver.findElement(by);
                    if (element != null){
                        return true;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            System.out.println(driver.getPageSource());
        }
    }
    /**
     * 将某个url301重定向到时本地的磁盘文件中
     * @param url 浏览器请求的url
     * @param filePath 文件路径
     * @param phantomJSDriver 驱动
     */
    public static void changeUrl(String url,String filePath,PhantomJSDriver phantomJSDriver)
    {
        String os = System.getProperty("os.name");
        String jsPath="";
        try {
            if(os.toLowerCase().startsWith("win")){
                jsPath = "file://"+"/"+(new File(filePath).getCanonicalPath().replaceAll("\\\\","/"));
            }else
            {
                jsPath ="file://"+(new File(filePath).getCanonicalPath());
            }
        } catch (IOException e) {

            log.info("{}文件没有找到",filePath);
        }
        phantomJSDriver.executePhantomJS("var page=this;" +
                "page.onResourceRequested = function(requestData, networkRequest) {" +
                "if(requestData.url=='"+url+"')"+
                "{"+
                "networkRequest.changeUrl('"+jsPath+"');"+
                "}"+
                "};");
    }
}
