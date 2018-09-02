package com.crawler.proxy;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.ProxyProvider;

/**
 * Created by chenshengju on 2017/10/3 0003.
 */
public class OnceProxyProvider implements ProxyProvider {
    private ProxyHelper proxyHelper;
    public OnceProxyProvider(ProxyHelper proxyHelper) {
        this.proxyHelper=proxyHelper;
    }


    @Override
    public void returnProxy(Proxy proxy, Page page, Task task) {
        //Donothing
    }

    @Override
    public Proxy getProxy(Task task) {
        return  proxyHelper.getProxy(1).get(0);

    }


}
