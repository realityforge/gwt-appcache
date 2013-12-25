package org.realityforge.gwt.appcache.server.propertyprovider;

interface UserAgents
{
  interface Desktop
  {
    String FIREFOX = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:18.0) Gecko/20100101 Firefox/18.0";
    String FIREFOX_BOUNDARY = "Mozilla/5.0 (X11; U; OpenBSD sparc64; pl-PL; rv:1.8.0.2) Gecko/20060429 Firefox/1.5.0.2";
    String FIREFOX_OLD = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.7.10) Gecko/20050921 Firefox/1.5.0.2 Mandriva/1.0.6-15mdk (2006.0)";
    String OPERA = "Opera/9.80 (Windows NT 6.1; U; ko) Presto/2.7.62 Version/11.00";
    String CHROME =
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36";
    String IE10 = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)";
    String IE9 = "Mozilla/5.0 (Windows; U; MSIE 9.0; WIndows NT 9.0; en-US))";
    String IE8 =
      "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; GTB7.4; InfoPath.2; SV1; .NET CLR 3.3.69573; WOW64; en-US)";
    String IE7 =
      "Mozilla/5.0 (compatible; MSIE 7.0; Windows NT 5.0; Trident/4.0; FBSMTWB; .NET CLR 2.0.34861; .NET CLR 3.0.3746.3218; .NET CLR 3.5.33652; msn OptimizedIE8;ENUS)";
  }

  interface Mobile
  {
    String BLACKBERRY =
      "Mozilla/5.0 (BlackBerry; U; BlackBerry 9780; de) AppleWebKit/534.1+ (KHTML, like Gecko) Version/6.0.0.359 Mobile Safari/534.1+";
    String ANDROID_TABLET =
      "Mozilla/5.0 (Linux; U; Android 3.0; en-us; Xoom Build/HRI39) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13";
    String ANDROID_PHONE_2x =
      "Mozilla/5.0 (Linux; U; Android 2.3.6; de-de; Nexus S Build/GRK39F) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
    String IPAD_IOS5 =
      "Mozilla/5.0 (iPad; CPU OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3";
    String IPAD =
      "Mozilla/5.0 (iPad; U; CPU OS 3_2_1 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Mobile/7B405";
    String IPHONE_IOS5 =
      "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3";
  }
}
