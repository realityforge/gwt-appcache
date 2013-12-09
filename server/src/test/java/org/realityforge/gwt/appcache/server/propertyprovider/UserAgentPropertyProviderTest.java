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
      { "opera", "opera" },
      { "Gecko and some other text?", "gecko1_8" },
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
