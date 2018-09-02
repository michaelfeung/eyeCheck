package com.crawler.proxyutils;

import com.crawler.HttpsUtils;
import com.crawler.proxy.ProxyExpire;
import com.crawler.proxy.ProxyHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenshengju on 2017/10/6 0006.
 */
public class Data5uProxyHelper implements ProxyHelper {
    @Override
    public List<ProxyExpire> getProxy(int i) {
        List<ProxyExpire> proxyExpires=new ArrayList<>();
        ProxyExpire proxy=null;
        try {
            String ipAndPort= HttpsUtils.get("http://api.ip.data5u.com/dynamic/get.html?order=51447991e57ff2fb9e32580a01be0e40&sep=3").trim();
            String[] ss = ipAndPort.split(":");
            proxy=new ProxyExpire(ss[0],Integer.parseInt(ss[1]),null,null,0);
            proxyExpires.add(proxy);
        } catch (Exception e) {

            System.out.println("得到代理ip失败"+e.getMessage());
        }
        return proxyExpires;
    }
}
