package com.crawler.industry.skysearch;

import com.crawler.selenium.SeleniumHtmlAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenshengju on 2017/9/30 0030.
 */
public class PubshHtmlAction implements SeleniumHtmlAction{
    @Override
    public void execute(WebDriver driver, Page page) throws Exception {
        List<Page> pageList=new ArrayList<>();
        String text = null;
        try {
            text = driver.findElement(By.xpath("//div[21]/div[@id='_container_punish']/div/div[@class='company_pager']/div[@class='total']")).getText();
        } catch (Exception e) {
            System.out.println("没有惩罚信息");
            return;
        }
        //TODO 待优化
        Pattern pattern=Pattern.compile("\\d");
        Matcher matcher = pattern.matcher(text);
        matcher.find();
        text=matcher.group();
        int pageCount=Integer.parseInt(text);
        Page pageThis=new Page();
        pageThis.setRequest(new Request(driver.getCurrentUrl()));
        pageThis.setRawText(driver.getPageSource());
        pageList.add(pageThis);
        for(int i=0;i<pageCount-1;i++)
        {
            WebElement element = driver.findElement(By.xpath("//div[@id='_container_punish']/div/div[@class='company_pager']/ul[@class='pagination-sm pagination']/li[@class='pagination-next  ']/a"));
            element.click();
            Thread.sleep(SkyConfig.clickPagesleepTime);
            Page pageNow=new Page();
            pageNow.setRequest(new Request(driver.getCurrentUrl()));
            pageNow.setRawText(driver.getPageSource());
            pageList.add(pageNow);
        }
        page.getRequest().putExtra("publishPages",pageList);
    }
    public static List<Map> getPubsh(Page page)
    {
        //行政处罚

        Object obj = page.getRequest().getExtra("publishPages");
        if(obj==null)
        {
            return null;
        }
        List<Map> mapListPubsh=new ArrayList<>();
        List<Page> publishPages = (List<Page>)obj;
        for (Page publishPage : publishPages) {
            List<String> jdrqList = publishPage.getHtml().xpath("//div[@id='_container_punish']/div/div[1]/table[@class='table  companyInfo-table']//tr/td[1]/tidyText()").all();
            List<String> jdswhList = publishPage.getHtml().xpath("//div[@id='_container_punish']/div/div[1]/table[@class='table  companyInfo-table']//tr/td[2]/tidyText()").all();
            List<String> lxList = publishPage.getHtml().xpath("//div[@id='_container_punish']/div/div[1]/table[@class='table  companyInfo-table']//tr/td[3]/tidyText()").all();
            List<String> jdjgList = publishPage.getHtml().xpath("//div[@id='_container_punish']/div/div[1]/table[@class='table  companyInfo-table']//tr/td[4]/tidyText()").all();
            for (int i = 0; i < jdrqList.size(); i++) {
                Map map=new HashMap();
                map.put("决定日期",jdrqList.get(i));
                map.put("决定书文号",jdswhList.get(i));
                map.put("类型",lxList.get(i));
                map.put("决定机关",jdjgList.get(i));
                mapListPubsh.add(map);
            }
        }
        return mapListPubsh;
    }
}
