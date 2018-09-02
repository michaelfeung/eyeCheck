package com.crawler.industry.skysearch;

import com.crawler.pipeline.ConsoleJsonPipeline;
import com.crawler.selenium.*;
import org.apache.commons.io.FileUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.PriorityScheduler;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenshengju on 2017/9/24 0024.
 */
public class SkySearchIndustry implements PageProcessor{
    private Site site=new Site().setRetryTimes(3).setSleepTime(100);
    private static List<String>  companyList=new ArrayList<>();
    @Override
    public void process(Page page) {
        if(page.getRequest().getUrl().startsWith("https://www.tianyancha.com/search"))
        {
            List<String> urls = page.getHtml().xpath("////a[@class='query_name sv-search-company f18 in-block vertical-middle']").links().all();
            System.out.println(urls);
            String url = urls.get(0);
            Request request = new Request(url);
            request.setPriority(3);
            page.addTargetRequest(request);
//            for (String url : urls) {
//                Request request = new Request(url);
//                request.setPriority(3);
////                request.putExtra(SeleniumDownloaderService.ACTION_HTMLS, new SeleniumHtmlAction[]{new PubshHtmlAction(),new AbnormalHtmlAction()});
//                page.addTargetRequest(request);
//            }
            page.setSkip(true);
        }
        if(page.getRequest().getUrl().startsWith("https://www.tianyancha.com/company/"))
        {
            List<Page> pageList=page.getResultItems().get("publishPages");
            Map result=new HashMap();
            Map mapBaseInfo=new HashMap();
            mapBaseInfo.put("工商注册号",page.getHtml().xpath("//table[@class='table companyInfo-table f14']//tr[1]/td[2]/allText()").get());
            mapBaseInfo.put("统一信用代码",page.getHtml().xpath("//table[@class='table companyInfo-table f14']//tr[2]/td[2]/allText()").get());
            mapBaseInfo.put("统一信用代码",page.getHtml().xpath("//table[@class='table companyInfo-table f14']//tr[2]/td[2]/allText()").get());
            mapBaseInfo.put("企业名称",page.getHtml().xpath("//span[@class='f18 in-block vertival-middle sec-c2']/allText()").get());
            mapBaseInfo.put("法人",page.getHtml().xpath("//div[@class='f18 overflow-width sec-c3']/allText()").get());
            mapBaseInfo.put("企业营业至",page.getHtml().xpath("//div[@class='baseinfo-module-content-value statusType1']/allText()").get());
            mapBaseInfo.put("注册资本",page.getHtml().xpath("//div[@class='new-border-bottom']/div[@class='pb10']/div[@class='baseinfo-module-content-value']/allText()").get());
            mapBaseInfo.put("住所",page.getHtml().xpath("//span[@class='in-block overflow-width vertical-top emailWidth']/allText()").get());
            mapBaseInfo.put("经营范围",page.getHtml().xpath("//span[@class='js-split-container']/allText()").get());
//            result.put("基本信息",mapBaseInfo);
//            result.put("行政处罚",PubshHtmlAction.getPubsh(page));
            String jyyc = page.getHtml().xpath("/html/body/div[2]/div[@id='web-content']/div/div[@class='company_top_fix top_container_new b-c-gray']/div[@class='container company_container']/div[@class='row position-rel']/div[@class='col-9 company-main pl0 pr10 company_new_2017']/div[@class='b-c-white new-border-bottom new-border-left new-border-right position-rel']/div[1]/div[@class='companypage_2017']/div[@class='navigation new-border-top new-border-right new-c3 js-company-navigation']/div[@class='over-hide']/div[@class='float-left f14 text-center nav_item_Box'][4]/div[@class='nav-item-p pt15 pl30 text-left nav-item-border']/div[1]/tidyText()").get();
            String yzwf = page.getHtml().xpath("/html/body/div[2]/div[@id='web-content']/div/div[@class='company_top_fix top_container_new b-c-gray']/div[@class='container company_container']/div[@class='row position-rel']/div[@class='col-9 company-main pl0 pr10 company_new_2017']/div[@class='b-c-white new-border-bottom new-border-left new-border-right position-rel']/div[1]/div[@class='companypage_2017']/div[@class='navigation new-border-top new-border-right new-c3 js-company-navigation']/div[@class='over-hide']/div[@class='float-left f14 text-center nav_item_Box'][4]/div[@class='nav-item-p pt15 pl30 text-left nav-item-border']/div[2]/tidyText()").get();


            mapBaseInfo.put("是否列入经营异常名录",jyyc.matches(".*\\d.*")?"是":"否");
            mapBaseInfo.put("是否列入严重违法失信企业名单（黑名单）",yzwf.matches(".*\\d.*")?"是":"否");
//            result.put("行政处罚",PubshHtmlAction.getPubsh(page));
//            result.put("经营异常",AbnormalHtmlAction.getAbnormal(page));
            page.putField("company",mapBaseInfo);
        }
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    public static void main(String[] args) throws IOException {
        long start=System.currentTimeMillis();
        List<String> list1 = FileUtils.readLines(new File("e:\\dataAll.txt"), "utf-8");
        List<String> list = list1.subList(0, 500);
        for (String s : list) {
            companyList.add(URLEncoder.encode(s.split("\\^_\\^")[0],"utf-8"));
        }
//        companyList.add(URLEncoder.encode("佛山宝玛特窑炉装备有限公司","utf-8"));
        WebDriverPool webDriverPool = new WebPhantomJsDriverPool(8);
        SeleniumDownloader seleniumDownloader = new SeleniumDownloader(webDriverPool);
//        seleniumDownloader.setProxyProvider(new OnceProxyProvider(new Data5uProxyHelper()));
//        WebDriverPool webDriverPool = new WebChromeDriverPool();
        List<String> urls=new ArrayList<>();
        for (String s : companyList) {
            urls.add("https://www.tianyancha.com/search?key="+s+"&checkFrom=searchBox");
        }
        urls.subList(1,100);
        String[] urlss=new String[urls.size()];
        urls.toArray(urlss);
        String[] ss={"https://www.tianyancha.com/search?key=%E6%9C%89%E9%99%90&checkFrom=searchBox","https://www.tianyancha.com/search/p2?key=%E6%9C%89%E9%99%90","https://www.tianyancha.com/search/p3?key=%E6%9C%89%E9%99%90","https://www.tianyancha.com/search/p4?key=%E6%9C%89%E9%99%90","https://www.tianyancha.com/search/p5?key=%E6%9C%89%E9%99%90"};
//        String[] ss={"https://www.tianyancha.com/company/1163301921"};
        Spider spider = Spider.create(new SkySearchIndustry())
                .addUrl(urlss)
                .setDownloader(seleniumDownloader)
//                .addPipeline(new ConsoleJsonSkyPipeline())
                .addPipeline(new JsonFilePipeline("e:\\cc"))
                .addPipeline(new ConsoleJsonPipeline())
//                .addPipeline(new FoSanPipeline())
                .setScheduler(new PriorityScheduler())
                .thread(8);
        spider.run();
        webDriverPool.shutdown();
        long end=System.currentTimeMillis();
        System.out.println("一共用了"+(end-start)/1000+"秒");

    }
}
