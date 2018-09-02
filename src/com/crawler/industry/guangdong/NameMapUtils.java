package com.crawler.industry.guangdong;

import com.crawler.industry.config.CrawlerConfig;

import java.util.Map;

/**
 * Created by chenshengju on 2017/10/16 0016.
 * 用于提取字段的工具类
 */
public class NameMapUtils {
    public static String[] regNums= CrawlerConfig.regNums;
    public static String[] companyNames=CrawlerConfig.companyNames;
    public static String[] legalPersons=CrawlerConfig.legalPersons;
    public static String[] businessTos=CrawlerConfig.businessTos;
    public static String[] regMoneys=CrawlerConfig.regMoneys;
    public static String[] addresss=CrawlerConfig.addresss;
    public static String[] businessScopes=CrawlerConfig.businessScopes;
    public enum Name{regNum,companyName,legalPerson,businessTo,regMoney,address,businessScope};
    public static String map(Map<String,String> mapBaseInfoAll,Name name)
    {
        if(name==Name.regNum)
        {
            for (String regNum : regNums) {
                if(mapBaseInfoAll.get(regNum)!=null)
                {
                    return mapBaseInfoAll.get(regNum);
                }
            }
        }
        if(name==Name.companyName)
        {
            for (String companyName : companyNames) {
                if(mapBaseInfoAll.get(companyName)!=null)
                {
                    return mapBaseInfoAll.get(companyName);
                }
            }
        }
        if(name==Name.legalPerson)
        {
            for (String legalPerson : legalPersons) {
                if(mapBaseInfoAll.get(legalPerson)!=null)
                {
                    return mapBaseInfoAll.get(legalPerson);
                }
            }
        }
        if(name==Name.businessTo)
        {
            for (String businessTo : businessTos) {
                if(mapBaseInfoAll.get(businessTo)!=null)
                {
                    return mapBaseInfoAll.get(businessTo);
                }
            }
            return CrawlerConfig.businessTo;
        }
        if(name==Name.regMoney)
        {
            for (String regMoney : regMoneys) {
                if(mapBaseInfoAll.get(regMoney)!=null)
                {
                    return mapBaseInfoAll.get(regMoney);
                }
            }
        }
        if(name==Name.address)
        {
            for (String address : addresss) {
                if(mapBaseInfoAll.get(address)!=null)
                {
                    return mapBaseInfoAll.get(address);
                }
            }
        }
        if(name==Name.businessScope)
        {
            for (String businessScope : businessScopes) {
                if(mapBaseInfoAll.get(businessScope)!=null)
                {
                    return mapBaseInfoAll.get(businessScope);
                }
            }
        }
        return null;
    }

}
