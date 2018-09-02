package com.crawler.selenium;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by chenshengju on 2017/10/11 0011.
 */
public class SeleniumException {
    private static final Logger log= LoggerFactory.getLogger(SeleniumException.class);

    public static String TakesScreenshotLog(WebDriver driver,String path)
    {
        if("".equals(path)||path==null)
        {
            path=SeleniumException.class.getResource("/").getFile();
        }
        File file=((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        long time = new Date().getTime();
        try {
            FileUtils.copyFile(file, new File(path+File.separator+time+".jpg"));
            log.info("一张图片{}",time);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return time+"";
    }

}
