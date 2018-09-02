package com.crawler.industry.guangdong;

import com.crawler.industry.config.CrawlerConfig;
import com.crawler.industry.config.FileNameConfig;
import com.crawler.pipeline.ConsoleJsonPipeline;
import com.crawler.pipeline.FastFosanPipeline;
import com.crawler.selenium.*;
import com.sun.org.apache.xerces.internal.impl.xpath.XPath;

import frcbRep.common.utils.FileUtil;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.message.BasicHeader;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.pipeline.ResultItemsCollectorPipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.ProxyProvider;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.scheduler.PriorityScheduler;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by chenshengju on 2017/9/24 0024.
 * 爬取广东企业信息网的核心类，
 */
/**
 * @author Administrator
 *
 */
public class GuangDongIndustry implements PageProcessor{
    private static final Logger log= LoggerFactory.getLogger(GuangDongIndustry.class);
    /**
     * 银行文件的名单
     */
    private  List<String>  companyList=new ArrayList<>();
    /**
     * 银行文件的公司名-注册号
     */
    private  Map<String,String> companyAndRegNum=new HashMap<>();
    /**
     * 公司名-详情url映射
     */
    private Map<String,String> urlMapName=new HashMap();
    
    private Set<Cookie> cookies;
    
    private Site site=new Site().setRetryTimes(CrawlerConfig.retryTimes)
            .addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
            .setSleepTime(CrawlerConfig.sleepTime)
            .setTimeOut(100000)
            .setDisableCookieManagement(true);
    public  AtomicInteger noSearchCount=new AtomicInteger(0);
    public static ProxyProvider proxyProvider;
    @Override
    public void process(Page page) {
        String timoutPic = page.getHtml().xpath("//body/div[2]/div/img[1]/@src").get();
        //如果网页中有提示操作太过频繁的图片
        if(timoutPic!=null&&timoutPic.contains("controlinfo_new2.gif"))
        {
            log.info("该网站已经提示你操作过于频繁");
            log.info("该网站已经锁定您的ip，休息{}分钟再进行爬取",CrawlerConfig.sleepIP/60000);
            try {
                Thread.sleep(CrawlerConfig.sleepIP);
            } catch (InterruptedException e) {
                log.info("在休息过程中出现中断异常");
            }
            page.setSkip(true);
            return;
        }
        //如果是通过浏览器来搜索过来的
        if(page.getRequest().getUrl().startsWith("http://"+CrawlerConfig.gdDomain+"/aiccips?"))
        {
            String companyName="";
            companyName= (String) page.getRequest().getExtra("name");
            String s = page.getHtml().xpath("//div[@class='f24 mb40 mt40 sec-c1 ']/text()").get();
            if(s!=null)
            {  
                log.info("{}在天眼查网站中无法查出",companyName);
                page.setSkip(true);
                return;
            }

            //判断网页是否能够显示
            /*String noShowPic = page.getHtml().xpath("//div[2]/div/img/@src").get();
            if(noShowPic !=null&&noShowPic.contains("errorinfo_new2.gif"))
            {
                log.info("网站提示“系统无法显示，请返回操作”");
            }*/
        	String url = page.getHtml().xpath("//div[@class='header']/a").links().all().get(0);
        	Request request=new Request();
        	request.setUrl(url);
            //todo
            request.setExtras(page.getRequest().getExtras());
            request.putExtra(SeleniumDownloaderService.FAST_DOWNLOAD,true);
            request.setPriority(3);
            page.addTargetRequest(request);
            page.setSkip(true);
        }
        else{
        	String websiteName = page.getHtml().xpath("////h1[@class='name']/text()").get();
        	String companyName = page.getRequest().getExtra("name").toString();
        	//比较文件中的企业名称与网站中的企业名称是否一致
        	if(!companyName.equals(websiteName))
            {     
            log.info("{}在天眼查网站中无法查出",companyName);
            page.setSkip(true);
            return;
            }
        	
            Map<String,String> mapBaseInfo=new HashMap();
            //获取法律诉讼数量
            String lawsuit = page.getHtml().xpath("//div[@class='item-container'][2]/div/div[2]/span/text()").get();
            if(lawsuit == null || lawsuit.equals("")){
            	lawsuit = "0";
            }
            //获取失信人信息数量
            String dishonest = page.getHtml().xpath("//div[@class='item-container'][2]/div/div[4]/span/text()").get();
            if(dishonest == null || dishonest.equals("")){
            	dishonest = "0";
            }
            //获取被执行人数量
            String legalperson = page.getHtml().xpath("//div[@class='item-container'][2]/div/div[5]/span/text()").get();
            if(legalperson == null || legalperson.equals("")){
            	legalperson = "0";
            }
            mapBaseInfo.put("企业名称", page.getRequest().getExtra("name").toString());
            mapBaseInfo.put("法律诉讼",lawsuit);
            mapBaseInfo.put("失信人信息",dishonest);
            mapBaseInfo.put("被执行人",legalperson);
            page.putField("baseInfo",mapBaseInfo);
        }
    }

    /**
     * 传入待爬取的银行文件
     * @param fileName
     * @return 返回查询失败的公司名称List
     */
    public CrawlerReslut startCrawler(String fileName)  {
    	//用户登录
    	login();
        //将传值工具的值全部清零，防止下次运行的时候出现问题
        WebmagicValueHelp.init();
        //该值为政府文件的文件名
        WebmagicValueHelp.fileName=FilenameUtils.getName(fileName).replaceFirst("bank","gov");
        long start=System.currentTimeMillis();
        int companyCount=0;
        String startDate=DateFormatUtils.format(new Date(),"YYYY-MM-dd HH:mm");
        log.info(startDate+"开始启动爬虫");
        int threadCount =CrawlerConfig.theadCount;
        log.info("爬虫的线程数为{}",threadCount);
        //银行文件里的公司名单
        companyList = getCompanyBankLineList(fileName);       
        companyCount=companyList.size();
        //排除已经下载好的
        List<String> lastCompanyList=excludeOk(companyList);
        log.info("还有{}条数据要抓",companyList.size());
        //缓存的详情页面url替换
        WebDriverPool webDriverPool=new WebPhantomJsDriverPool(threadCount);
        ((WebPhantomJsDriverPool)webDriverPool).setSeleniumAction(new ChangeOfficeJs());
//        WebDriverPool webDriverPool=new WebChromeDriverPool(threadCount);
        SeleniumDownloader seleniumDownloader = new SeleniumDownloader(webDriverPool);
        //是否配置代理
        //todo 代理
        if(CrawlerConfig.ipProxyList!=null)
        {
            SimpleProxyProvider simpleProxyProvider = new SimpleProxyProvider(CrawlerConfig.ipProxyList);
            seleniumDownloader.setProxyProvider(simpleProxyProvider);
            log.info("您设置了代理{}",CrawlerConfig.ipProxyList);
        }
        // todo 代理
        Request[] serarRequests = getSerarRequest(companyList);
        List<SpiderListener> spiderListeners = new ArrayList<>();
        spiderListeners.add(new GdSpiderListener());
        ResultItemsCollectorPipeline resultItems = new ResultItemsCollectorPipeline();
        FastFosanPipeline fastFosanPipeline = new FastFosanPipeline();
        Spider spider = Spider.create(this)
                .addRequest(serarRequests)
                .setDownloader(seleniumDownloader)
                .addPipeline(new ConsoleJsonPipeline())
                .addPipeline(fastFosanPipeline)
                .addPipeline(resultItems)
                .setSpiderListeners(spiderListeners)
                .setScheduler(new PriorityScheduler())
                .thread(threadCount);
        spider.run();
        webDriverPool.shutdown();
        long end=System.currentTimeMillis();
        log.info(startDate+"开始爬虫，"+DateFormatUtils.format(new Date(),"YYYY-MM-dd HH:mm")+"结束爬虫，"+companyList.size()+"条数据，一共用了"+(end-start)/1000+"秒");
        log.info("该文件有{}条数据",companyCount);
        log.info("找到之前爬取过的文件，,一共{}条之前已经查出",lastCompanyList.size());
        log.info("本次运行{}条查出来了",fastFosanPipeline.getSuccess());
        int failCount = companyCount - lastCompanyList.size()  - fastFosanPipeline.getSuccess().get();
        log.info("有{}条抓取失败", failCount);
        exportFail(fileName);
        //导出爬取失败的公司名称
        CrawlerReslut crawlerReslut=new CrawlerReslut(companyCount,fastFosanPipeline.getSuccess().get(),failCount);
        return crawlerReslut;
    }
    
    /**
     * 首先进行用户登录，以获取页面cookie，实现接下来页面的免登录抓取
     * @param 
     * @return
     */
    public void login(){
    	WebDriverPool webDriverPool=new WebPhantomJsDriverPool();
    	WebDriver driver = null;
        try {
        	driver = webDriverPool.get();
            if(site.isDisableCookieManagement())
            {
                driver.manage().deleteAllCookies();
            }
        } catch (InterruptedException e) {
            log.warn("interrupted", e);
        }
        //进入登录页面进行用户登录
        driver.get("https://www.tianyancha.com/login");
    	Actions actionMy = new Actions(driver);
    	//输入用户名
    	actionMy.sendKeys(driver.findElement(By.xpath("//div[@class='pb30 position-rel']/input[1]")),CrawlerConfig.username).perform();
    	//输入密码
    	actionMy.sendKeys(driver.findElement(By.xpath("//div[@class='pb40 position-rel']/input")),CrawlerConfig.password).perform();
    	WebElement login_element = driver.findElement(By.xpath("//div[@onclick='loginByPhone(event);']"));
    	login_element.click();
    	try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	//获取登录后的cookie
    	cookies = driver.manage().getCookies();
    	setSite();
    	//创建cookie字段，注入请求头部
    	String cookiestr = "";
    	Iterator<Cookie> it = cookies.iterator();
    	while(it.hasNext()){
    		Cookie cookie = it.next();
    		cookiestr = cookiestr+cookie.getName()+"="+cookie.getValue()+";";
    	}
    	//请求头部添加cookie,实现免登录
    	site.addHeader("Cookie", cookiestr);
    }
    
    /**
     * 得到银行文件的行
     * @param fileName 银行文件的路径
     * @return
     */
    private List<String> getCompanyBankLineList(String fileName)
    {
        //银行文件里的公司名单
        List<String> bankCompanyList = new ArrayList<>();
        try {
            bankCompanyList = FileUtils.readLines(new File(fileName), CrawlerConfig.encode);
        } catch (IOException e) {
            log.error("{}没有找到,结束本次运行",fileName);
        }
        return bankCompanyList;
    }

    /**
     * 得到银行文件政府名称
     * @param bankCompanyList 银行文件的行集合
     * @return
     */
    private List<String> getBankCompanyName(List<String> bankCompanyList)
    {
        //银行文件里的公司名单
        List<String> conpanyName=new ArrayList<>();
        for (String s : bankCompanyList) {
            conpanyName.add(s.split(CrawlerConfig.readSpilt)[0]);
        }
        return conpanyName;
    }

    /**
     * 返回查询失败的公司List
     * @return
     */
    private List<String> exportFail(String fileName) {
        List<String> companyBankLineList = getCompanyBankLineList(fileName);
        //银行文件里的公司名单
        List<String> companyNameList=getBankCompanyName(companyBankLineList);
        //排除已经下载好的
        List<String> lastCompanyList=excludeOk(companyNameList);
        try {
            FileUtils.writeLines(new File(CrawlerConfig.filePath+File.separator+FilenameUtils.getName(fileName).replaceFirst("bank","fail")),FileNameConfig.failCompanyEncode,companyNameList,true);
        } catch (IOException e) {
            log.error("写入{}查询失败公司名失败",FileNameConfig.failCompany);
        }
        return companyNameList;
    }

    private void checkRefresh(String fileName) {

        if(CrawlerConfig.refreshUrl)
        {
            log.info("开始新增缓存url");
            GuangDongIndustryFast guangDongIndustryFast = new GuangDongIndustryFast();
            guangDongIndustryFast.startFast(fileName);
            log.info("新增缓存url结束");
        }else
        {
            log.info("当前不新增缓存url");
        }
    }

    private  List<String> excludeNoUrlCompany(List<String> companyList) {
        List<String> noUrlName= new ArrayList<>();
        try {
            noUrlName = FileUtils.readLines(new File(CrawlerConfig.filePath+File.separator+ FileNameConfig.noUrlCompany), FileNameConfig.noUrlCompanyEncode);
            //得到查不出公司的名单;
            noUrlName=noUrlName.stream().map(a->a.split("\\^_\\^")[0]).collect(Collectors.toList());
            //排除查不出来的公司
            companyList.removeAll(noUrlName);
        } catch (IOException e) {
            log.info("读取{}失败",FileNameConfig.noUrlCompany);
        }
        return noUrlName;
    }


    /**
     * 将已经缓存好的url地址替换原来的初始url直接去查询而不用经过 查询->列表->详情 的过程
     * @param companyList
     */
    private List<String> replaceDetail(List<String> companyList) {
        List<String> cacheUrl = new ArrayList<>();

        File cacheFile=null;
        try {
            cacheFile = new File(CrawlerConfig.filePath + File.separator + FileNameConfig.urlCompany);
            cacheUrl = FileUtils.readLines(cacheFile, FileNameConfig.urlCompanyEncode);
            int countReplace=0;
            for (String s : cacheUrl) {
                String name = s.split("\\^_\\^")[0];
                String url = s.split("\\^_\\^")[1];
                //顺便把缓存url与name的映射关系给放进去;
                urlMapName.put(url,name);
                if(companyList.contains(name))
                {
                    companyList.remove(name);
                    companyList.add(url);
                    countReplace++;
                }
            }
            log.info("{}条数据被替换为详情页url,{}条数据没有替换为详情url",countReplace,companyList.size()-countReplace);
        } catch (IOException e) {
            log.info("{}文件未找到",FileNameConfig.urlCompany);
        }
        return cacheUrl;
    }

    /**
     * 排除已经下载好的公司
     * @param companyList 已经下载好的公司名单
     */
    private List<String> excludeOk(List<String> companyList) {
        List<String> isOkList=new ArrayList<>();
        List<String> lastCompany=new ArrayList<>();
        try {
            List<String> lastCompanyLine=new ArrayList<>();
            String filename = CrawlerConfig.filePath+File.separator+WebmagicValueHelp.fileName;
            lastCompanyLine= FileUtils.readLines(new File(filename),CrawlerConfig.encode);
            for (String company : lastCompanyLine) {
                String name = company.split(CrawlerConfig.readSpilt)[0];
                lastCompany.add(name);
            }
            log.info("找到之前爬取过的文件，本此断点续爬,一共{}条已经爬出",lastCompany.size());
            Iterator<String> iterator = companyList.iterator();
            while(iterator.hasNext())
            {
                String next = iterator.next();
                if(lastCompany.contains(next))
                {
                    isOkList.add(next);
                    iterator.remove();
                }
            }
//            companyList.removeAll(lastCompany);
        } catch (IOException e) {
            log.info("没有找到已经下载好的文件，本此非断点续爬");
        }
        return isOkList;
    }
    /**
     * 将公司名单替换，成可查询的url，如果是详情页，则优先下载
     * @param list
     * @return
     */
    public  Request[] getSerarRequest(List<String> list) {
        Request request = null;
        List<Request> requests = new ArrayList<>();
        for (String s : list) {
        	request = new Request("http://"+CrawlerConfig.gdDomain+"/aiccips?name=" + DigestUtils.md5Hex(s));
            request.putExtra("name",s);
            request.putExtra(SeleniumDownloaderService.ACTION_HTMLS, new SearchAction());
            requests.add(request);
        }
        Request[] requestArray = new Request[requests.size()];
        requests.toArray(requestArray);
        return requestArray;

    }
    @Override
    public Site getSite() {  	
        return this.site;
    }
    
    public void setSite(){
    	for (Cookie cookie : cookies) { 
            site.addCookie(cookie.getName().toString(),cookie.getValue().toString());
        }
    }
}
