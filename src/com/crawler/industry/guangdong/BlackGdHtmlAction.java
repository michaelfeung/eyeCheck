package com.crawler.industry.guangdong;

import com.crawler.selenium.SeleniumHtmlAction;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenshengju on 2017/10/9 0009.
 */
public class BlackGdHtmlAction implements SeleniumHtmlAction{
    private static final Logger log= LoggerFactory.getLogger(BlackGdHtmlAction.class);

    @Override
    public void execute(WebDriver driver, Page page) throws Exception {
        driver.findElement(By.xpath("//div[@id='black']")).click();
        (new WebDriverWait(driver, 10))
                .until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(WebDriver d) {
                        return "列入严重违法失信企业名单（黑名单）信息".equals(d.findElement(By.xpath("//span[@class='titleLabel']")).getText());
                    }
                });
        Thread.sleep(500);
        List<Page> pageList = new ArrayList<>();
        Page pageThis = new Page();
        pageThis.setRequest(new Request(driver.getCurrentUrl()));
        pageThis.setRawText(driver.getPageSource());
        pageList.add(pageThis);
        page.getRequest().putExtra("blackPages", pageList);
    }


    public static String getblack(Page page) {
        //行政处罚
        Object obj = page.getRequest().getExtra("blackPages");
        if (obj == null) {
            return null;
        }
        List<Page> abnormalPages = (List<Page>) obj;
        Page pageAbnormal = abnormalPages.get(0);
        return isBlack(pageAbnormal,"//tr[@class='tablebodytext']/td/allText()","//tr[@class='tablebodytext'][1]/td[1]/allText()");
    }
    //判断是否黑名单
    public static String isBlack(Page page,String xpath1,String xpath2)
    {
        String timoutPic = page.getHtml().xpath("//body/div[2]/div/img[1]/@src").get();
        if(timoutPic!=null&&timoutPic.contains("controlinfo_new2.gif"))
        {
            log.info("该网站已经提示你操作过于频繁"+ page.getRequest().getUrl());
            throw new RuntimeException("该网站已经提示你操作过于频繁" + page.getRequest().getUrl());
        }
        String noBlack = page.getHtml().xpath(xpath1).get();
        String haveBlack = page.getHtml().xpath(xpath2).get();
        String black="";
        if ("暂无数据".equals(noBlack))
        {
            black="2";
            return black;
        }
        if ("1".equals(haveBlack))
        {
            black="1";
            return black;
        }
        if(black.equals("")){
            throw new RuntimeException("抓取黑名单有误" + page.getRequest().getUrl());
        }
        return black;

    }
    //判断是否黑名单
    public static String isSZBlack(Page page,String xpath1,String xpath2)
    {
        String noBlack = page.getHtml().xpath(xpath1).get();
//        String haveBlack = page.getHtml().xpath(xpath2).get();
        String black="";
        if ("暂无相关信息".equals(noBlack))
        {
            black="2";
        }
        else
        {
            black="1";
        }
        return black;

    }
}
