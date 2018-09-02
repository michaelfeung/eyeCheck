package com.crawler.industry.guangdong;

import com.crawler.industry.config.CrawlerConfig;
import com.crawler.selenium.SeleniumHtmlAction;
import com.crawler.selenium.SeleniumUtils;
import com.crawler.selenium.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.utils.UrlUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenshengju on 2017/9/24 0024.
 * 进入页面然后进行搜索动作的类
 */
public class SearchAction implements SeleniumHtmlAction {
    private  final Logger log= LoggerFactory.getLogger(getClass());
    @Override
    public void execute(WebDriver driver,Page page) throws Exception {
        driver.manage().window().maximize();
        String url = page.getRequest().getUrl();
        if(driver.getCurrentUrl().contains("aiccips?name="))
        {
            while (true)
            {
                try {
                    String name = (String) page.getRequest().getExtra("name");
                    log.debug("开始输入公司名",name); 
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
                    Actions actionMy = new Actions(driver);
                    //输入企业名称进行搜索
                    actionMy.sendKeys(driver.findElement(By.id("searchVal")), name).perform();//content keyword
                    WebElement gt_popup_wrap_e = driver.findElement(By.xpath("//div[@class='input-group-addon search_button']"));// search btn_query
                    Thread.sleep(2000);
                    gt_popup_wrap_e.click();
                    /*log.info("等待查询结果页加载完毕");
                    //等待目录页加载出来
                    SeleniumUtils.waitForLoad(driver, By.xpath("//div[@class='tyc-header']"),20);
                    log.info("跳转到查询结果页成功");*/
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("卡住在跳转阶段"+driver.getCurrentUrl());
                    driver.navigate().to(url);
                }
            }
        }
    }
}
