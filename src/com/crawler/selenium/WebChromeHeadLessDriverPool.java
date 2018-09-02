package com.crawler.selenium;

/**
 * Created by chenshengju on 2017/9/20 0020.
 */
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.proxy.Proxy;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author taojw
 */
public class WebChromeHeadLessDriverPool implements WebDriverPool{
    private Logger logger = LoggerFactory.getLogger(getClass());

    private int CAPACITY = 5;
    private AtomicInteger refCount = new AtomicInteger(0);

    /**
     * store webDrivers available
     */
    private BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<WebDriver>(
            CAPACITY);

    private static String CHROME_PATH;
    private static DesiredCapabilities caps = DesiredCapabilities.chrome();
    static {
        CHROME_PATH = WebChromeHeadLessDriverPool.class.getResource("/chromedriver.exe").getFile();
        System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY,CHROME_PATH);
        caps.setJavascriptEnabled(true);
        ChromeOptions chromeOptions = new ChromeOptions();
//        设置为 headless 模式 （必须）
        chromeOptions.addArguments("--headless");
//        设置浏览器窗口打开大小  （非必须）
        chromeOptions.addArguments("--window-size=1920,1080");
        caps.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

    }

    public WebChromeHeadLessDriverPool() {
    }

    public WebChromeHeadLessDriverPool(int poolsize) {
        this.CAPACITY = poolsize;
        innerQueue = new LinkedBlockingDeque<WebDriver>(poolsize);
    }

    public WebDriver get() throws InterruptedException {
        WebDriver poll = innerQueue.poll();
        if (poll != null) {
            return poll;
        }
        if (refCount.get() < CAPACITY) {
            synchronized (innerQueue) {
                if (refCount.get() < CAPACITY) {

                    WebDriver mDriver = new ChromeDriver(caps);
                    // 尝试性解决：https://github.com/ariya/phantomjs/issues/11526问题
                    mDriver.manage().timeouts()
                            .pageLoadTimeout(60, TimeUnit.SECONDS);
                    // mDriver.manage().window().setSize(new Dimension(1366,
                    // 768));
                    innerQueue.add(mDriver);
                    refCount.incrementAndGet();
                }
            }
        }
        return innerQueue.take();
    }

    public void returnToPool(WebDriver webDriver) {
        // webDriver.quit();
        // webDriver=null;
        innerQueue.add(webDriver);
    }

    public void close(WebDriver webDriver) {
        refCount.decrementAndGet();
        webDriver.close();
        webDriver.quit();
        webDriver = null;
    }

    public void shutdown() {
        try {
            for (WebDriver driver : innerQueue) {
                close(driver);
            }
            innerQueue.clear();
        } catch (Exception e) {
//            e.printStackTrace();
            logger.warn("webdriverpool关闭失败",e);
        }
    }

    @Override
    public void setProxy(WebDriver webDriver,Proxy proxy) {

    }


}