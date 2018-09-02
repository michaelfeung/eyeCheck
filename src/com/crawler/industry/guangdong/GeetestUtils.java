package com.crawler.industry.guangdong;

import com.crawler.industry.config.CrawlerConfig;
import com.crawler.selenium.*;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenshengju on 2017/9/24 0024.
 * 用于获取关键的三个参数
 */
public class GeetestUtils {
    private  static final Logger log= LoggerFactory.getLogger(GeetestUtils.class);
    public  static Map getGeetestKey() throws Exception {
        int failCount=0;
        WebPhantomJsDriverPool webPhantomJsDriverPool=new WebPhantomJsDriverPool(5);

        webPhantomJsDriverPool.setSeleniumAction(new ChangeOfficeJs());
        WebDriver driver = webPhantomJsDriverPool.get();
        //如果有代理，那么加代理
        if(CrawlerConfig.ipProxyList!=null)
        {
            webPhantomJsDriverPool.setProxy(driver,CrawlerConfig.ipProxyList.get(0));
        }
        String url="http://gd.gsxt.gov.cn/aiccips";
//        String url="http://www.aklajfa.com/";
        //将js重定向到自己的js中
        driver.get(url);
        if("<html><head></head><body></body></html>".equals(driver.getPageSource()))
        {
            log.info("请求返回的是空，可能网络有问题，尝试次数为{}",failCount);
            failCount++;
            if(failCount>15)
            {
                log.error("该网站已经不能访问了，尝试了{}（如果你的浏览器还能访问，那么清除你浏览器的cookie，也许就不能访问了）",failCount);
                CrawlerConfig.canCrawler=false;
                return null;
            }
        }
        driver.manage().window().maximize();
        BufferedImage bufferedImage1=null;
        BufferedImage bufferedImage2=null;
            log.info("进入到首页");
            while (true)
            {
                try {
//                    String name = URLDecoder.decode(driver.getCurrentUrl().split("\\?")[1].split("=")[1],"utf-8");
                    String name ="广东佛山";
                    log.debug("开始输入公司名",name);
                    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
                    Actions actionMy = new Actions(driver);
                    //通过action模拟键盘输入java关键字到 输入框，只有使用了perform方法才会输入进去
                    actionMy.sendKeys(driver.findElement(By.id("content")), name).perform();//content keyword
                    WebElement gt_popup_wrap_e = driver.findElement(By.id("search"));// search btn_query
                    Thread.sleep(2000);
                    gt_popup_wrap_e.click();
                    //todo
//                    Thread.sleep(2000);
//                    File file3=((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
//                    FileUtils.copyFile(file3,new File("e:/test1.jpg"));
                    //todo
                    (new WebDriverWait(driver, 3))
                            .until(new ExpectedCondition<Boolean>() {
                                @Override
                                public Boolean apply(WebDriver d) {
                                    return d.findElement(By.xpath("//div[@class='gt_slider_knob gt_show']")).isDisplayed();
                                }
                            });

                    (new WebDriverWait(driver, 3))
                            .until(new ExpectedCondition<Boolean>() {
                                @Override
                                public Boolean apply(WebDriver d) {
                                    return d.findElement(By.xpath("//div[@class='gt_cut_bg gt_show']")).isDisplayed();
                                }
                            });

                    (new WebDriverWait(driver, 3))
                            .until(new ExpectedCondition<Boolean>() {
                                @Override
                                public Boolean apply(WebDriver d) {
                                    return d.findElement(By.xpath("//div[@class='gt_cut_fullbg gt_show']")).isDisplayed();
                                }
                            });

                    PhantomJSDriver phantomJSDriver= (PhantomJSDriver) driver;
                    phantomJSDriver.executePhantomJS("var page=this;" +
                            "page.onResourceRequested = function(requestData, networkRequest) {" +
                            "if(requestData.url=='http://gd.gsxt.gov.cn/aiccips/verify/sec.html'){"+
                            "var path = 'geetest.txt';"+
                            "var fs = require('fs');"+
                            "fs.write(path, requestData.postData, 'w');"+
                            "}};");
                    //找到滑动的圆球
                    WebElement element = driver.findElement(By.xpath("//div[@class='gt_slider_knob gt_show']"));
                    //获取缺口位置
                    int index = 100;
                    if(!CrawlerConfig.isAllGeetestPass)
                    {
                        bufferedImage1 = Utils.getImages(driver, "//div[@class='gt_cut_bg gt_show']/div");
                        bufferedImage2 = Utils.getImages(driver, "//div[@class='gt_cut_fullbg gt_show']/div");
                        index = Utils.get_diff_location(bufferedImage1, bufferedImage2);
                    }
//

//                    //todo
//                    File file = new File("e:/hhh" + File.separator + new Date().getTime() + "-1" + ".jpg");
//                    File file2 = new File("e:/hhh" + File.separator + new Date().getTime() + "-2" + ".jpg");
//                    if(file.exists())
//                    {
//                        file.mkdirs();
//                        file2.mkdirs();
//                    }
//                    ImageIO.write(bufferedImage1, "jpg", file);
//                    ImageIO.write(bufferedImage2, "jpg", file2);
                    //todo
                    System.out.println(index);
                    List<Integer> track_list = Utils.get_diff_location(index);
                    //生成轨迹
                    Point point = element.getLocation();
                    int y = point.getY();
                    System.out.println("y:"+y);
                    //模拟鼠标的移动
                    System.out.println("第一步,点击元素");
                    Actions action = new Actions(driver);
                    action.clickAndHold(element).perform();
                    Thread.sleep(1500L);
                    int a=track_list.size()/ RandomUtils.nextInt(10, 12);
                    int b=track_list.size()/RandomUtils.nextInt(4, 7);
                    int c=track_list.size()/RandomUtils.nextInt(2, 3);
                    for (int i = 0; i < track_list.size(); i++) {

                        int track = track_list.get(i);
                        new Actions(driver).moveToElement(element, track+22, 22).perform();
                        Thread.sleep(RandomUtils.nextInt(2, 7));

                    }
                    new Actions(driver).moveToElement(element, 21, 22).perform();
                    Thread.sleep(100);
                    new Actions(driver).moveToElement(element, 21, 22).perform();
                    Thread.sleep(100);
                    new Actions(driver).moveToElement(element, 21,22).perform();
                    Thread.sleep(100);
                    new Actions(driver).moveToElement(element, 21, 22).perform();
                    Thread.sleep(100);
                    new Actions(driver).moveToElement(element, 21, 22).perform();
                    Thread.sleep(100);
                    new Actions(driver).moveToElement(element, 21, 22).perform();
                    new Actions(driver).release(element).perform();
                    //todo
//                    File file4=((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
//                    FileUtils.copyFile(file4,new File("e:/test.jpg"));
                    //todo
                    log.debug("等待滑块验证结果");
                    By gtTypeBy = By.cssSelector(".gt_info_type");
                    By gtInfoBy = By.cssSelector(".gt_info_content");
                    SeleniumUtils.waitForLoad(driver, gtTypeBy,10);
                    SeleniumUtils.waitForLoad(driver, gtInfoBy,10);
                    String gtType = driver.findElement(gtTypeBy).getText();
                    String gtInfo = driver.findElement(gtInfoBy).getText();
                    System.out.println(gtType + "---" + gtInfo);
                    if(!gtType.equals("再来一次:") && !gtType.equals("验证失败:")&&!gtInfo.equals("拖动滑块将悬浮图像正确拼合")){
                        log.info("滑块验证成功"+driver.toString());
                        String geetest = FileUtils.readFileToString(new File("geetest.txt"), "utf-8");
                        log.info("等待查询结果页加载完毕");
                        //等待目录页加载出来
                        SeleniumUtils.waitForLoad(driver, By.xpath("//div[@class='mianBodyStyle']"),20);
                        log.info("跳转到查询结果页成功");
                        Map geetestMap=new HashMap();
                        String[] nameAndValue = geetest.split("&");
                        geetestMap.put(nameAndValue[1].split("=")[0],nameAndValue[1].split("=")[1]);
                        geetestMap.put(nameAndValue[2].split("=")[0],nameAndValue[2].split("=")[1]);
                        geetestMap.put(nameAndValue[3].split("=")[0],nameAndValue[3].split("=")[1]);
                        geetestMap.put("JSESSIONID",driver.manage().getCookieNamed("JSESSIONID").getValue());
                        geetestMap.put("WEB",driver.manage().getCookieNamed("WEB").getValue());
                        webPhantomJsDriverPool.close(driver);
                        log.info("自动获取得到了三个参数，可以绕过滑块了");
                        return geetestMap;
                    }
                    driver.navigate().to(url);
                    log.info("滑块验证失败重新尝试");
                } catch (Exception e) {
                    failCount++;
                    e.printStackTrace();
                    log.info("失败了{}次，滑块验证失败，或验证成功但卡住在跳转阶段"+driver.getCurrentUrl(),failCount);
                    driver.navigate().to(url);
                    if(failCount>30)
                    {
                        return null;
                    }

                }

            }
    }
    public static void main(String[] args) {
        try {
            getGeetestKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
