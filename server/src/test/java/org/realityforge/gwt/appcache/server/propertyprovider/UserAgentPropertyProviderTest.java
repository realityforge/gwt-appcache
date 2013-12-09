package org.realityforge.gwt.appcache.server.propertyprovider;

import javax.servlet.http.HttpServletRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class UserAgentPropertyProviderTest
{
  @DataProvider( name = "AgentMapping" )
  public Object[][] getAgentMapping()
  {
    return new Object[][] {
      { "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.57 Safari/537.36",
        "safari" },
      { " Mozilla/5.0 (iPad; U; CPU OS 3_2_1 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Mobile/7B405", "safari" },
      { "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)", "ie10" },
      { "Mozilla/5.0 (Windows; U; MSIE 9.0; WIndows NT 9.0; en-US))", "ie9" },
      { "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; GTB7.4; InfoPath.2; SV1; .NET CLR 3.3.69573; WOW64; en-US)", "ie8" },
      //Assume that anyone asking for IE7 is doing it due to compatibility mode. Blargh!
      { "Mozilla/5.0 (compatible; MSIE 7.0; Windows NT 5.0; Trident/4.0; FBSMTWB; .NET CLR 2.0.34861; .NET CLR 3.0.3746.3218; .NET CLR 3.5.33652; msn OptimizedIE8;ENUS)", "ie8" },
      { "Opera/9.80 (Windows NT 6.1; U; ko) Presto/2.7.62 Version/11.00", "opera" },
      { "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:18.0) Gecko/20100101 Firefox/18.0", "gecko1_8" },
    };
  }


  @Test( dataProvider = "AgentMapping" )
  public void userAgentExtraction( final String userAgent, final String value )
  {
    final UserAgentPropertyProvider provider = new UserAgentPropertyProvider();

    assertEquals( provider.getPropertyName(), "user.agent" );
    final HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( "User-Agent" ) ).thenReturn( userAgent );
    assertEquals( provider.getPropertyValue( request ), value );
  }

  @Test( expectedExceptions = IllegalStateException.class )
  public void badUserAgent()
  {
    final UserAgentPropertyProvider provider = new UserAgentPropertyProvider();

    assertEquals( provider.getPropertyName(), "user.agent" );
    final HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( "User-Agent" ) ).thenReturn( "SomethingElse" );
    provider.getPropertyValue( request );
  }
}
