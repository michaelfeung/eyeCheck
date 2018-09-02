package com.crawler.industry.skysearch;

import com.crawler.selenium.*;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenshengju on 2017/9/24 0024.
 */
public class SkySearchIndustryTest implements PageProcessor{
    private Site site=new Site().setRetryTimes(3).setSleepTime(100);
    @Override
    public void process(Page page) {
        if(page.getRequest().getUrl().startsWith("https://www.tianyancha.com/search"))
        {
            List<String> urls = page.getHtml().xpath("////a[@class='query_name sv-search-company f18 in-block vertical-middle']").links().all();
            System.out.println(urls);
            for (String url : urls) {
                page.addTargetRequest(url);
                System.out.println(url);
            }
            page.setSkip(true);
        }
        if(page.getRequest().getUrl().startsWith("https://www.tianyancha.com/company/"))
        {

            List<Page> pageList=page.getResultItems().get("publishPages");
            Map result=new HashMap();
            Map mapBaseInfo=new HashMap();
            mapBaseInfo.put("工商注册号",page.getHtml().xpath("//table[@class='table companyInfo-table f14']//tr[1]/td[2]/text()"));
            mapBaseInfo.put("统一信用代码",page.getHtml().xpath("//table[@class='table companyInfo-table f14']//tr[2]/td[2]/text()"));
            mapBaseInfo.put("统一信用代码",page.getHtml().xpath("//table[@class='table companyInfo-table f14']//tr[2]/td[2]/text()"));
            mapBaseInfo.put("企业名称",page.getHtml().xpath("//span[@class='f18 in-block vertival-middle sec-c2']/text()"));
            mapBaseInfo.put("法人",page.getHtml().xpath("//div[@class='f18 overflow-width sec-c3']/text()"));
            mapBaseInfo.put("企业营业至",page.getHtml().xpath("//div[@class='baseinfo-module-content-value statusType1']/text()"));
            mapBaseInfo.put("注册资本",page.getHtml().xpath("//div[@class='new-border-bottom']/div[@class='pb10']/div[@class='baseinfo-module-content-value']/text()"));
            mapBaseInfo.put("住所",page.getHtml().xpath("//tr[2]/td[3]/div[@class='textJustFy changeHoverText']/text()"));
            mapBaseInfo.put("经营范围",page.getHtml().xpath("//span[@class='js-split-container']/text()"));
            result.put("基本信息",mapBaseInfo);
            result.put("行政处罚",PubshHtmlAction.getPubsh(page));
//            result.put("经营异常",AbnormalHtmlAction.getAbnormal(page));
            page.putField("company",result);
        }
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    public static void main(String[] args) {
        WebDriverPool webDriverPool = new WebPhantomJsDriverPool();
//        WebDriverPool webDriverPool = new WebChromeDriverPool();
        String[] ss={"https://www.tianyancha.com/company/22822"};
//        Request request=new Request("https://www.tianyancha.com/company/22822");
        Request request=new Request("https://www.tianyancha.com/company/2489994686");
        request.putExtra(SeleniumDownloaderService.ACTION_HTMLS,new SeleniumHtmlAction[]{new PubshHtmlAction(),new AbnormalHtmlAction()});
        Spider spider = Spider.create(new SkySearchIndustryTest()).addRequest(request).setDownloader(new SeleniumDownloader(webDriverPool)).addPipeline(new ConsoleJsonSkyPipeline()).addPipeline(new JsonFilePipeline("e:\\cc")).thread(1);
        spider.start();
        webDriverPool.shutdown();
    }

}
