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
      { UserAgents.Desktop.CHROME, "safari" },
      { UserAgents.Mobile.IPAD_IOS5, "safari" },
      { UserAgents.Mobile.IPAD, "safari" },
      { UserAgents.Mobile.IPHONE_IOS5, "safari" },
      { UserAgents.Mobile.ANDROID_PHONE_2x, "safari" },
      { UserAgents.Mobile.ANDROID_TABLET, "safari" },
      { UserAgents.Mobile.BLACKBERRY, "safari" },
      { UserAgents.Desktop.IE10, "ie10" },
      { UserAgents.Desktop.IE9, "ie9" },
      { UserAgents.Desktop.IE8, "ie8" },
      //Assume that anyone asking for IE7 is doing it due to compatibility mode. Blargh!
      { UserAgents.Desktop.IE7, "ie8" },
      { UserAgents.Desktop.OPERA, "opera" },
      { UserAgents.Desktop.FIREFOX, "gecko1_8" },
      { UserAgents.Desktop.FIREFOX_BOUNDARY, "gecko1_8" },
      { UserAgents.Desktop.FIREFOX_OLD, "gecko" },
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
