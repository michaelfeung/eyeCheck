package com.crawler.industry.guangdong;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crawler.industry.config.CrawlerConfig;
import com.crawler.industry.config.FileNameConfig;
import com.crawler.pipeline.ConsoleJsonPipeline;
import com.crawler.pipeline.FastFosanPipeline;
import com.crawler.pipeline.UrlSavePipeline;

import frcbRep.common.utils.FileUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.scheduler.PriorityScheduler;
import us.codecraft.webmagic.utils.HttpConstant;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by chenshengju on 2017/9/24 0024.
 * 抓取url的类
 */
public class GuangDongIndustryFast implements PageProcessor {
    private static final Logger log = LoggerFactory.getLogger(GuangDongIndustryFast.class);
    //bank文件行的集合（非公司名）
    private static List<String> companyList = new ArrayList<>();
    private Site site = new Site().setTimeOut(30000).setRetryTimes(5).setSleepTime(100)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
    //三个参数
    private Map parmMap=new HashMap();


    //所带的3个参数是否能快速查询
    private boolean canFast = true;

    public boolean getCanFast() {
        return canFast;
    }
    //这个函数有点乱，我自己也晕了
    @Override
    public void process(Page page) {
        //如果当前的url是http://gd.gsxt.gov.cn/aiccips/verify/sec.html
        if (page.getRequest().getUrl().contains("/aiccips/verify/sec.html")) {
            String rawText = page.getRawText();
            JSONObject jsonObject = JSON.parseObject(rawText);
            String textfield = jsonObject.getString("textfield");
            String status = jsonObject.getString("status");
            if ("fail".equals(status)) {
                log.info("您的geetest_challenge,geetest_validate,geetest_seccode,JSESSIONID,WEB这几个参数失效，请重新配置，否则查询速度将会慢数十倍");
                this.canFast = false;
                return;
            }
            Map<String, Object> parmMap = new HashMap<>();
            parmMap.put("textfield", textfield);
            parmMap.put("type", "nomal");
            Request request = new Request();
            request.setExtras(page.getRequest().getExtras());
            request.setPriority(3);
            request.setMethod(HttpConstant.Method.POST);
            request.setRequestBody(HttpRequestBody.form(parmMap, "utf-8"));
            request.setUrl("http://" + CrawlerConfig.gdDomain + "/aiccips/CheckEntContext/showCheck.html");
            page.addTargetRequest(request);
            page.setSkip(true);
        }


        //如果当前的url是http://gd.gsxt.gov.cn/aiccips/CheckEntContext/showCheck.html
        if (page.getRequest().getUrl().contains("aiccips/CheckEntContext/showCheck.html")) {
            String regNum = (String) page.getRequest().getExtra("regNum");
            String name = (String) page.getRequest().getExtra("name");
            String s = page.getHtml().xpath("//div[@class='mianBodyStyle']/div[4]/tidyText()").get().trim();
            if ("暂无数据".equals(s)) {
//                if(name.equals(CrawlerConfig.testCompanyName))
//                {
//                    this.canFast = false;
//                    log.error("这个网站的搜索引擎出问题了，本次不再搜索详情页");
//                    return;
//                }
                //查公司名显示没有数据，那么开始查询公司的注册号,这里判断是否进来的是查注册号进来的。
                if (page.getRequest().getExtra("regSearch") == null) {
                    log.info("查询公司{}名称显示没有数据，开始查询它的注册号号{}", name, regNum);
                    Request request = new Request("http://" + CrawlerConfig.gdDomain + "/aiccips/verify/sec.html");
                    request.setMethod(HttpConstant.Method.POST);
                    parmMap.put("textfield", regNum);
                    Map<String, Object> extras = page.getRequest().getExtras();
                    extras.put("regSearch", true);
                    request.setExtras(page.getRequest().getExtras());
                    request.setRequestBody(HttpRequestBody.form(parmMap, "utf-8"));
                    request.setPriority(3);
                    page.addTargetRequest(request);
                    page.setSkip(true);
                    return;
                } else {
                    //该公司是否有详情url(没有)
                    page.putField("isHaveUrl", false);
                    page.putField("name", name);
                    page.putField("regNum", regNum);
                    return;
                }
            } else {
                //如果是通过注册号搜索来的。则不用完全匹配名字
                if (page.getRequest().getExtra("regSearch") != null) {
                    List<String> urls = page.getHtml().css(".mianBodyStyle .clickStyle div a").links().all();

                    String companyUrl = urls.get(0);
                    //该公司是否有详情url(没有)
                    page.putField("isHaveUrl", true);
                    page.putField("name", name);
                    page.putField("regNum", regNum);
                    page.putField("companyUrl", companyUrl);
//            page.addTargetRequest(request);
                    return;
                } else//如果是通过公司名搜索出来的，则需要完全匹配名字
                {
                    String firstName = page.getHtml().xpath("//div[@class='clickStyle']//span[@class='rsfont']/allText()").all().get(0).replaceAll("(（|）|\\(|\\))", "");
                    ;
                    if (!firstName.equals(name.replaceAll("(（|）|\\(|\\))", ""))) {
                        log.info("查到了但没有匹配的数据项：{}，结果{}", name, firstName);
                        //该公司是否有详情url(没有)
                        page.putField("isHaveUrl", false);
                        page.putField("name", name);
                        page.putField("regNum", regNum);
                        return;
                    } else {
                        List<String> urls = page.getHtml().css(".mianBodyStyle .clickStyle div a").links().all();
                        //如果大于1还要判断是否注册号匹配，只有这样，
                        int index=0;
                        if(urls.size()>1)
                        {
                            List<String> nameList = page.getHtml().xpath("//div[@class='mianBodyStyle']/div[@class='clickStyle']/span/span[@class='rsfont']/allText()").all();
                            List<String> regList=page.getHtml().xpath("//div[@class='mianBodyStyle']/div[@class='clickStyle']/div[2]/table[@class='textStyle']//tr/td[1]/span[@class='dataTextStyle']/allText()").all();
                            for (int i = 0; i < nameList.size(); i++) {
                                if(firstName.equals(nameList.get(i).replaceAll("(（|）|\\(|\\))", ""))&&regList.get(i).equals(regNum))
                                {
                                    index=i;
                                }
                            }
                        }

                        String companyUrl = urls.get(index);
                        //该公司是否有详情url(没有)
                        page.putField("isHaveUrl", true);
                        page.putField("name", name);
                        page.putField("regNum", regNum);
                        page.putField("companyUrl", companyUrl);
//            page.addTargetRequest(request);
                    }
                }
            }
        }
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    public void startFast(String filePath) {
        if(!CrawlerConfig.canCrawler)
        {
            //如果该网站出现了异常，那么下个流程不再执行
            log.error("该网站有异常本次终止爬取");
            return ;
        }
        //得到关键参数
        /*Map<String,String> geetestKey;
        try {
            geetestKey=GeetestUtils.getGeetestKey();
            parmMap.put("geetest_challenge", geetestKey.get("geetest_challenge"));
            parmMap.put("geetest_validate", geetestKey.get("geetest_validate"));
            parmMap.put("geetest_seccode", geetestKey.get("geetest_seccode"));
            this.getSite().addCookie(CrawlerConfig.gdDomain, "JSESSIONID", geetestKey.get("JSESSIONID")) .addCookie(CrawlerConfig.gdDomain,"WEB", geetestKey.get("WEB"));
            log.info("三个参数获取成功");
        } catch (Exception e) {
            e.printStackTrace();
           log.info("三个参数自动获取失败");
        }*/
     long start = System.currentTimeMillis();
        String startDate = DateFormatUtils.format(new Date(), "YYYY-MM-dd HH:mm");
        log.info(startDate + "开始启动爬虫");
        putNoUrl();
        try {
            companyList = FileUtils.readLines(new File(filePath), CrawlerConfig.encode);
        } catch (IOException e) {
            log.error("银行文件未找到");
            return;
        }
        //将缓存url放入set集合中，使保存url的时候可以对其进行去重处理
        putCacheUrlSet();
        //排除已经缓存好的url,和查不出的的公司
        excludeOkUrlAndNoUrl(companyList);
        List<String> urls = new ArrayList<>();
        List<Request> requestList = new ArrayList<>();
        for (String s : companyList) {
            try {
                String[] companyLine = s.split("\\^_\\^");
                String name = companyLine[0];
                String regNum = companyLine[1];
                parmMap.put("textfield", name);
                Request request = new Request("http://" + CrawlerConfig.gdDomain + "/aiccips/verify/sec.html");
                request.setMethod(HttpConstant.Method.POST);
                request.putExtra("name", name);
                request.putExtra("regNum", regNum);
                request.setRequestBody(HttpRequestBody.form(parmMap, "utf-8"));
                requestList.add(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Request[] requests = new Request[requestList.size()];
        requestList.toArray(requests);
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        //是否配置代理
        //todo 代理
        if(CrawlerConfig.ipProxyList!=null)
        {
            SimpleProxyProvider simpleProxyProvider = new SimpleProxyProvider(CrawlerConfig.ipProxyList);
            httpClientDownloader.setProxyProvider(simpleProxyProvider);
            log.info("您设置了代理{}",CrawlerConfig.ipProxyList);
        }
        // todo 代理
        String[] urlss = new String[urls.size()];
        urls.toArray(urlss);
        List<SpiderListener> spiderListeners = new ArrayList<>();
        spiderListeners.add(new GdSpiderListener());
        if(requests.length<1)
        {
            return;
        }
        //测试所带的参数是否有效,顺便测试一下是不是网站的搜索引擎出问题了
        Request testRequest = new Request("http://" + CrawlerConfig.gdDomain + "/aiccips/verify/sec.html");
        testRequest.setMethod(HttpConstant.Method.POST);
        testRequest.putExtra("name", requests[0].getExtra("name"));
        testRequest.putExtra("regNum", requests[0].getExtra("regNum"));
        parmMap.put("textfield",requests[0].getExtra("name"));
        testRequest.setRequestBody(HttpRequestBody.form(parmMap, "utf-8"));
        Spider spiderTestGeetest = Spider.create(this)
                .addRequest(testRequest)
                .setDownloader(httpClientDownloader)
                .thread(1);
        spiderTestGeetest.run();

        //如果这几个参数是无效的，那么将不会执行爬取详情页url模块
        if (this.getCanFast()) {
            log.info("当前的参数是有效的");
            spiderTestGeetest.run();
            UrlSavePipeline urlSavePipeline = new UrlSavePipeline();
            Spider spider = Spider.create(this)
                    .setScheduler(new PriorityScheduler())
                    .addRequest(requests)
                    .setDownloader(httpClientDownloader)
                    .addPipeline(urlSavePipeline)
                    .setScheduler(new PriorityScheduler())
                    .thread(8);
            spider.run();
            long end = System.currentTimeMillis();
            System.out.println("一共用了" + (end - start) / 1000 + "秒");
            log.info(startDate + "开始爬取详情页爬虫，" + DateFormatUtils.format(new Date(), "YYYY-MM-dd HH:mm") + "结束爬虫，" + companyList.size() + "条数据，一共用了" + (end - start) / 1000 + "秒" + urlSavePipeline.getSuccessCount() + "成功查出" + urlSavePipeline.getNoSearchCount() + "查不出来" + (companyList.size() - urlSavePipeline.getSuccessCount().get() - urlSavePipeline.getNoSearchCount().get()) + "条失败");
        }
    }

    private void putNoUrl() {
        WebmagicValueHelp.noUrlNameSet=new HashSet<>();
        try {
            List<String> noUrlList = FileUtils.readLines(new File(CrawlerConfig.filePath + File.separator + FileNameConfig.noUrlCompany), FileNameConfig.noUrlCompanyEncode);
            WebmagicValueHelp.noUrlNameSet=new HashSet<>(noUrlList);
        } catch (IOException e) {
            log.info("没有{}缓存文件，即将建立",FileNameConfig.noUrlCompany);
        }

    }

    private void putCacheUrlSet() {
        List<String> cacheUrlLines= new ArrayList<>();
        try {
            cacheUrlLines = FileUtils.readLines(new File(CrawlerConfig.filePath+ File.separator+ FileNameConfig.urlCompany),FileNameConfig.urlCompanyEncode);
        } catch (IOException e) {
           log.info("url缓存文件未找到，本次重新生成");
        }
        WebmagicValueHelp.cacheUrlSet=new HashSet<>(cacheUrlLines);
    }

    //排除已经缓存好的url,和查不出的的公司
    private void excludeOkUrlAndNoUrl(List<String> companyList) {
        try {
            //缓存里已经存在的公司url
            List<String> cacheNameList=new ArrayList<>();
            List<String> cacheUrlLines=FileUtils.readLines(new File(CrawlerConfig.filePath+ File.separator+FileNameConfig.urlCompany),FileNameConfig.urlCompanyEncode);
            for (String s : cacheUrlLines) {
                cacheNameList.add(s.split(CrawlerConfig.readSpilt)[0]);
            }
            //查不出来的公司名url;
            List<String> noUrlNameList=FileUtils.readLines(new File(CrawlerConfig.filePath+ File.separator+FileNameConfig.noUrlCompany),FileNameConfig.noUrlCompanyEncode);
            WebmagicValueHelp.noUrlNameSet=new HashSet<>(noUrlNameList);
            //合并
            cacheNameList.addAll(noUrlNameList);
            Iterator<String> iterator = companyList.iterator();
            int count=0;
            while(iterator.hasNext())
            {
                String nameLine = iterator.next();
                if(cacheNameList.contains(nameLine.split(CrawlerConfig.readSpilt)[0]))
                {
                    iterator.remove();
                    count++;
                }
            }
            log.info("已经已经下载好的公司{}个",count);
        } catch (IOException e) {
            log.info("url缓存文件未找到，本次重新爬取");
        }
    }
}
