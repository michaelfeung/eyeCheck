package com.crawler.industry.skysearch;

import com.crawler.selenium.SeleniumHtmlAction;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenshengju on 2017/10/3 0003.
 */
public class AbnormalHtmlAction implements SeleniumHtmlAction {
    @Override
    public void execute(WebDriver driver, Page page) throws Exception {
        List<Page> pageList=new ArrayList<>();
//        String text = driver.findElement(By.xpath("//div[21]/div[@id='_container_punish']/div/div[@class='company_pager']/div[@class='total']")).getText();
//        //TODO 待优化
//        Pattern pattern=Pattern.compile("\\d");
//        Matcher matcher = pattern.matcher(text);
//        matcher.find();
//        text=matcher.group();
//        int pageCount=Integer.parseInt(text);
        Page pageThis=new Page();
        pageThis.setRequest(new Request(driver.getCurrentUrl()));
        pageThis.setRawText(driver.getPageSource());
//        pageList.add(pageThis);
//        for(int i=0;i<pageCount-1;i++)
//        {
//            WebElement element = driver.findElement(By.xpath("//div[@id='_container_punish']/div/div[@class='company_pager']/ul[@class='pagination-sm pagination']/li[@class='pagination-next  ']/a"));
//            element.click();
//            Thread.sleep(SkyConfig.clickPagesleepTime);
//            Page pageNow=new Page();
//            pageNow.setRequest(new Request(driver.getCurrentUrl()));
//            pageNow.setRawText(driver.getPageSource());
//            pageList.add(pageNow);
//        }
        String text = null;
        try {
            text = driver.findElement(By.xpath("//div[@id='nav-main-abnormalCount']/span[@class='intro-count']")).getText();
        } catch (Exception e) {
            System.out.println("没有想想经营异常的内容");
            return;
        }
        page.getRequest().putExtra("abnormalPagesCount",Integer.parseInt(text));
        page.getRequest().putExtra("abnormalPages",pageList);
    }
    public static List<Map> getAbnormal(Page page)
    {
        //行政处罚
        Object obj = page.getRequest().getExtra("abnormalPages");
        if(obj==null)
        {
            return null;
        }
        int count=0;
        List<Map> mapListAbnormal=new ArrayList<>();
        List<Page> abnormalPages = (List<Page>)obj ;
        for (Page abnormalPage : abnormalPages) {
            List<String> lrrqList = abnormalPage.getHtml().xpath("//div[@id='_container_abnormal']/div/div/table[@class='table  companyInfo-table']//tr/td[1]").all();
            List<String> lryyList = abnormalPage.getHtml().xpath("//div[@id='_container_abnormal']/div/div/table[@class='table  companyInfo-table']//tr/td[2]").all();
            List<String> jdjgList = abnormalPage.getHtml().xpath("//div[@id='_container_abnormal']/div/div/table[@class='table  companyInfo-table']//tr/td[3]").all();
            List<String> ycrqList = abnormalPage.getHtml().xpath("//div[@id='_container_abnormal']/div/div/table[@class='table  companyInfo-table']//tr/td[4]").all();
            List<String> ycyyList = abnormalPage.getHtml().xpath("//div[@id='_container_abnormal']/div/div/table[@class='table  companyInfo-table']//tr/td[5]").all();
            List<String> ycjgList = abnormalPage.getHtml().xpath("//div[@id='_container_abnormal']/div/div/table[@class='table  companyInfo-table']//tr/td[6]").all();
            for (int i = 0; i < lrrqList.size(); i++) {
                count++;
                Map map=new HashMap();
                map.put("列入日期",lrrqList.get(i));
                map.put("列入原因",lryyList.get(i));
                map.put("决定机关",jdjgList.get(i));
                map.put("移除日期",ycrqList.get(i));
                map.put("移除原因",ycyyList.get(i));
                map.put("移除机关",ycjgList.get(i));
                mapListAbnormal.add(map);
            }
        }
        //用于寻找翻页逻辑
        int realCount= (int) page.getRequest().getExtra("abnormalPagesCount");
        if(realCount!=count)
        {
            String msg="真正数量数量"+realCount+"实际数量"+count+"url"+page.getRequest().getUrl();
            try {
                FileUtils.write(new File("寻找分页.txt"),msg,"utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //用于寻找翻页逻辑
        return mapListAbnormal;
    }
}
