package org.realityforge.gwt.appcache.server.mgwt;

import javax.servlet.http.HttpServletRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class MobileUserAgentPropertyProviderTest
{
  @DataProvider( name = "AgentMapping" )
  public Object[][] getAgentMapping()
  {
    return new Object[][]{
      { UserAgents.IPHONE_IOS5_USER_AGENT, "mobilesafari" },
      { UserAgents.IPAD_IOS5_USER_AGENT, "mobilesafari" },
      { UserAgents.ANDROID_PHONE_2x_USER_AGENT, "mobilesafari" },
      { UserAgents.BLACKBERRY_USER_AGENT, "not_mobile" },
      { UserAgents.DESKTOP_USER_AGENT_CHROME, "not_mobile" },
    };
  }

  @Test( dataProvider = "AgentMapping" )
  public void userAgentExtraction( final String userAgent, final String value )
  {
    final MobileUserAgentProvider provider = new MobileUserAgentProvider();

    assertEquals( provider.getPropertyName(), "mobile.user.agent" );
    final HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( "User-Agent" ) ).thenReturn( userAgent );
    assertEquals( provider.getPropertyValue( request ), value );
  }
}
