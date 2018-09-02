package com.crawler.proxy;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.ProxyProvider;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chenshengju on 2017/10/6 0006.
 */
public class ExpireProxyProvider implements ProxyProvider {
    private final List<ProxyExpire> proxies;

    private final AtomicInteger pointer= new AtomicInteger(-1);
    private  int proxyCount=0;
    private ProxyHelper proxyHelper;
    private OfflineStrategy offlineStrategy;

    public ExpireProxyProvider(int proxyCount,ProxyHelper proxyHelper,OfflineStrategy offlineStrategy) {
        this.offlineStrategy=offlineStrategy;
        this.proxyCount=proxyCount;
        this.proxyHelper=proxyHelper;
        this.proxies = proxyHelper.getProxy(proxyCount);
    }


    @Override
    public void returnProxy(Proxy proxy, Page page, Task task) {
        if(offlineStrategy.needOfflineProxy(page))
        {
            proxy=proxyHelper.getProxy(1).get(0);
        }
    }

    @Override
    public Proxy getProxy(Task task) {
        ProxyExpire proxyExpire = proxies.get(incrForLoop());
        if(proxyExpire.isExpire())
        {
            proxyExpire=proxyHelper.getProxy(1).get(0);
        }
        return proxyExpire;
    }

    private int incrForLoop() {
        int p = pointer.incrementAndGet();
        int size = proxies.size();
        if (p < size) {
            return p;
        }
        while (!pointer.compareAndSet(p, p % size)) {
            p = pointer.get();
        }
        return p % size;
    }
}
