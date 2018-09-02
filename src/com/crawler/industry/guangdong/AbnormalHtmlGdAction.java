package com.crawler.industry.guangdong;

import com.crawler.selenium.SeleniumHtmlAction;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenshengju on 2017/10/3 0003.
 */
public class AbnormalHtmlGdAction implements SeleniumHtmlAction {
    private static final Logger log= LoggerFactory.getLogger(AbnormalHtmlGdAction.class);

    @Override
    public void execute(WebDriver driver, Page page) throws Exception {
        driver.findElement(By.xpath("//div[@id='abnor']")).click();
        (new WebDriverWait(driver, 20))
                .until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(WebDriver d) {
                        return "列入经营异常名录信息".equals(d.findElement(By.xpath("//span[@class='titleLabel']")).getText());
                    }
                });
        Thread.sleep(500);
        List<Page> pageList = new ArrayList<>();
        Page pageThis = new Page();
        pageThis.setRequest(new Request(driver.getCurrentUrl()));
        pageThis.setRawText(driver.getPageSource());
        pageList.add(pageThis);
        page.getRequest().putExtra("abnormalPages", pageList);
    }


    public static String getAbnormal(Page page) {
        //行政处罚
        Object obj = page.getRequest().getExtra("abnormalPages");
        if (obj == null) {
            return null;
        }
        List<Page> abnormalPages = (List<Page>) obj;
        Page pageAbnormal = abnormalPages.get(0);
        return isAbnormal(pageAbnormal,"//tr[@class='nothing']/td/allText()","//tr[@class='tablebodytext']/td[1]/allText()");
    }
    //判断是否经营异常逻辑
    public static String isAbnormal(Page page,String xpath1,String xpath2)
    {
        String timoutPic = page.getHtml().xpath("//body/div[2]/div/img[1]/@src").get();
        if(timoutPic!=null&&timoutPic.contains("controlinfo_new2.gif"))
        {
            log.info("该网站已经提示你操作过于频繁"+ page.getRequest().getUrl());
            throw new RuntimeException("该网站已经提示你操作过于频繁" + page.getRequest().getUrl());
        }
        String noAbnormal = page.getHtml().xpath(xpath1).get();
        String haveAbnormal = page.getHtml().xpath(xpath2).get();
        String abnormal="";
        if ("暂无数据".equals(noAbnormal)&&haveAbnormal==null)
        {
            abnormal="2";
        }
        if ("1".equals(haveAbnormal))
        {

            //如果是已经移除的经营异常那么不算入经营异常如：（佛山市南海区亚港商务酒店 ）
            List<String> trList=page.getHtml().xpath("//tr[@class='tablebodytext']").all();
            List<String> abnormalTds=page.getHtml().xpath("//tr[@class='tablebodytext']/td[5]/allText()").all();
            int abnormalCount=0;
            for (String abnormalTd : abnormalTds) {
               if(StringUtils.isNotBlank(abnormalTd))
               {
                   abnormalCount++;
               }
            }
            if(trList.size()==abnormalCount)
            {
             	abnormal="2";
            }else
            {
                abnormal="1";
            }
        }
        if(abnormal.equals("")){
            throw new RuntimeException("抓取经营异常有误" + page.getRequest().getUrl());
        }
        return abnormal;
    }
    //判断是否经营异常逻辑
    public static String isSZAbnormal(Page page,String xpath1,String xpath2)
    {
        String noAbnormal = page.getHtml().xpath(xpath1).get();
        String haveAbnormal = page.getHtml().xpath(xpath2).get();
        String abnormal="";
        if ("暂无相关信息".equals(noAbnormal))
        {
            abnormal="2";
        }
        if ("1".equals(haveAbnormal))
        {
            //如果是已经移除的经营异常那么不算入经营异常如：（深圳市米联信息技术有限公司 ）
            List<String> trList=page.getHtml().xpath("//div[@id='JYYCMLXX']//tr").all();
            List<String> abnormalTds=page.getHtml().xpath("//div[@id='JYYCMLXX']//tr/td[5]/allText()").all();
            int abnormalCount=0;
            for (String abnormalTd : abnormalTds) {
                if(StringUtils.isNotBlank(abnormalTd))
                {
                    abnormalCount++;
                }
            }
            if((trList.size()-1)==abnormalCount)
            {
            	System.out.println(trList.get(0));
                abnormal="2";
            }else
            {
                abnormal="1";
            }
        }
        if(abnormal.equals("")){
            throw new RuntimeException("抓取经营异常有误" + page.getRequest().getUrl());
        }
        return abnormal;
    }
}
