package com.crawler.industry.guangdong;

import com.crawler.selenium.SeleniumAction;
import com.crawler.selenium.SeleniumUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by chenshengju on 2017/10/31 0031.
 * 用于改变极验js的代码
 */
public class ChangeOfficeJs implements SeleniumAction {
    private  static final Logger log= LoggerFactory.getLogger(ChangeOfficeJs.class);

    @Override
    public void execute(WebDriver driver) throws Exception {
        //将http://static.geetest.com/static/js/offline.6.0.0.js转向到时本地的js文件中
        SeleniumUtils.changeUrl("http://static.geetest.com/static/js/offline.6.0.0.js","resources/offline.6.0.0.js", (PhantomJSDriver) driver);
    }
}
