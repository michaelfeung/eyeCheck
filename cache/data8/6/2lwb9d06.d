   �         Ghttps://interface.sina.cn/dfz/outside/auto/getcityautocontent-p1.d.html     %��?��      %���1�         
     O K           �      Server   nginx/1.6.1   Date   Thu, 30 Aug 2018 11:44:47 GMT   Content-Type   	text/html   Vary   Accept-Encoding   Cache-Control   max-age=240   Expires   Thu, 30 Aug 2018 11:48:00 GMT   Last-Modified   Thu, 30 Aug 2018 11:48:00 GMT   X-Comos-Cost   0.003   debug   &web008-i.nwapcms.msina.gz.sinanode.com   Content-Encoding   gzip   SINA-LB   (aGEuMzYuZzEua3hjLmxiLnNpbmFub2RlLmNvbQ==   SINA-TS   NDk1M2MzNjggMCAwIDAgMyA2Cg== ;(function(){
var capitalHash = {"\u4e0a\u6d77":"\u4e0a\u6d77","\u4e91\u5357":"\u6606\u660e","\u5b81\u590f\u56de\u65cf":"\u94f6\u5ddd","\u5b89\u5fbd":"\u5408\u80a5","\u5c71\u4e1c":"\u6d4e\u5357","\u5c71\u897f":"\u592a\u539f","\u5e7f\u4e1c":"\u5e7f\u5dde","\u5e7f\u897f\u58ee\u65cf":"\u5357\u5b81","\u6c5f\u82cf":"\u5357\u4eac","\u6c5f\u897f":"\u5357\u660c","\u6cb3\u5317":"\u77f3\u5bb6\u5e84","\u6cb3\u5357":"\u90d1\u5dde","\u6d59\u6c5f":"\u676d\u5dde","\u6d77\u5357":"\u6d77\u53e3","\u6e56\u5317":"\u6b66\u6c49","\u6e56\u5357":"\u957f\u6c99","\u8d35\u5dde":"\u8d35\u9633","\u8fbd\u5b81":"\u6c88\u9633","\u9ed1\u9f99\u6c5f":"\u54c8\u5c14\u6ee8","\u53f0\u6e7e":"\u53f0\u5317","\u56db\u5ddd":"\u6210\u90fd","\u65b0\u7586\u7ef4\u543e\u5c14":"\u4e4c\u9c81\u6728\u9f50","\u91cd\u5e86":"\u91cd\u5e86","\u798f\u5efa":"\u798f\u5dde","\u897f\u85cf":"\u62c9\u8428","\u5185\u8499\u53e4":"\u547c\u548c\u6d69\u7279","\u5317\u4eac":"\u5317\u4eac","\u5409\u6797":"\u957f\u6625","\u5929\u6d25":"\u5929\u6d25","\u7518\u8083":"\u5170\u5dde","\u9655\u897f":"\u897f\u5b89","\u9752\u6d77":"\u897f\u5b81"};
var carMarketData = {"\u5e7f\u5dde":{"relate":[{"name":"\u5317\u4eac","home":"http://bj.auto.sina.com.cn/"},{"name":"\u4e0a\u6d77","home":"http://sh.auto.sina.com.cn/"},{"name":"\u5e7f\u5dde","home":"http://gz.auto.sina.com.cn/"},{"name":"\u91cd\u5e86","home":"http://cq.auto.sina.com.cn/"}],"focus":{"link":"http://cd.auto.sina.com.cn/","img":"http://sc.sinaimg.cn/2013/0528/U6977P841DT20130528090453.jpg","title":"\u56db\u5ddd\u4e00\u5468\u8f66\u5e02\u76d8\u70b9"},"home":"http://gz.auto.sina.com.cn","list":[{"link":["http://gz.auto.sina.com.cn/2018-07-18/detail-ihfnsvyz9302980.shtml","http://gz.auto.sina.com.cn/2018-07-18/detail-ihfnsvyz9285994.shtml","http://gz.auto.sina.com.cn/2018-07-18/detail-ihfnsvyz9241897.shtml"],"title":["\u6b27\u6d32\u7535\u52a8\u6c7d\u8f66\u653e\u7f13","\u5965\u8feaA5\u9650\u65f6\u4f18\u60e0","\u5954\u9a70C\u7ea7\u8ba9\u5229\u4fc3\u9500"]},{"link":["http://gz.auto.sina.com.cn/2018-07-18/detail-ihfnsvyz9213357.shtml","http://gz.auto.sina.com.cn/2018-07-18/detail-ihfnsvyz9169371.shtml","http://gz.auto.sina.com.cn/2018-07-18/detail-ihfnsvyz9148385.shtml"],"title":["\u7535\u52a8\u6717\u9038\u8c0d\u7167\u66dd\u5149","\u5927\u4f17\u63a2\u8363\u7533\u62a5\u56fe","\u5b9d\u9a8fSUV\u8c0d\u7167\u66dd\u5149"]},{"link":["http://gz.auto.sina.com.cn/2018-07-18/detail-ihfnsvyz9106499.shtml","http://gz.auto.sina.com.cn/2018-07-18/detail-ihfnsvyz9080839.shtml","http://gz.auto.sina.com.cn/2018-07-18/detail-ihfnsvyz9057611.shtml"],"title":["\u65b0\u900d\u5ba2\u7533\u62a5\u56fe","\u957f\u5b89CS85\u7533\u62a5\u56fe","MINI\u7eaf\u7535\u52a8\u7248\u8c0d\u7167"]}],"pic":"http://i2.sinaimg.cn/qc/2012/1225/chengdu.png"}};
var handle = function(city, capital){
	city = typeof(carMarketData[city]) == 'object' ? city : capital;
	var nav = SHM.E('SI_IP_Auto_Tab_Nav_1');
	var cont = SHM.E('SI_IP_Auto_Tab_Cont_1');
	var rel = SHM.E('SI_IP_Auto_City_Title');
	var data = carMarketData[city];

	if(!nav||!cont||!rel||typeof(data)!='object'){
		return;
	}

	var contHTML = '<ul tab-type="tab-cont" class="list-a">';
	for(var i = 0, len = data.list.length; i < len; i++){
		contHTML += '<li>';
		for(var j =0, l = data.list[i].title.length; j < l; j++){
			contHTML += '<a href="'+ data.list[i].link[j] +'" target="_blank">'+ data.list[i].title[j] +'</a> ';
		}
		contHTML += '</li>';
	}
	contHTML += '</ul>';

	var relHTML = '<a href="http://auto.sina.com.cn/city/" target="_blank">\u5168\u56fd</a>';
	var len = data.relate.length;
	len = len > 5 ? 5 : len;
	for(var i = 0; i < len; i++){
		relHTML += '|<a href="'+ data.relate[i].home +'" target="_blank">'+ data.relate[i].name +'</a>';
	}

	cont.innerHTML = contHTML;
	rel.innerHTML = relHTML;
	nav.innerHTML = city;
	nav.href = carMarketData[city].home;
	nav.target = '_blank';
	nav.style.display = '';
	rel.style.display = '';
	cont.style.display = '';
};
jsLoader({
	name: 'shm',
	callback: function() {
		//SHM.home.iplookup.load(function(info, city) {
		var capital = capitalHash["\u5e7f\u4e1c"];
        handle("\u5e7f\u5dde", capital);
		//});
	}
});
})();

