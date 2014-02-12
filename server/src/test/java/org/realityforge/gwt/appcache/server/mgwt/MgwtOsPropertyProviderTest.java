package org.realityforge.gwt.appcache.server.mgwt;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class MgwtOsPropertyProviderTest
{
  @DataProvider( name = "AgentMapping" )
  public Object[][] getAgentMapping()
  {
    return new Object[][]
      {
        { UserAgents.IPHONE_IOS5_USER_AGENT, "iphone", new Cookie[]{ new Cookie( "mgwt_ios_retina", "0" ) } },
        { UserAgents.IPHONE_IOS5_USER_AGENT, "retina", new Cookie[]{ new Cookie( "mgwt_ios_retina", "1" ) } },
        { UserAgents.IPHONE_IOS5_USER_AGENT, "iphone_undefined", new Cookie[]{ new Cookie( "mgwt_ios_retina", "X" ) } },
        { UserAgents.IPHONE_IOS5_USER_AGENT, "iphone_undefined", new Cookie[ 0 ] },
        { UserAgents.IPAD_IOS5_USER_AGENT, "ipad", new Cookie[]{ new Cookie( "mgwt_ios_retina", "0" ) } },
        { UserAgents.IPAD_IOS5_USER_AGENT, "ipad_retina", new Cookie[]{ new Cookie( "mgwt_ios_retina", "1" ) } },
        { UserAgents.IPAD_IOS5_USER_AGENT, "ipad_undefined", new Cookie[]{ new Cookie( "mgwt_ios_retina", "X" ) } },
        { UserAgents.IPAD_IOS5_USER_AGENT, "ipad_undefined", new Cookie[ 0 ] },
        { UserAgents.ANDROID_PHONE_2x_USER_AGENT, "android", new Cookie[ 0 ] },
        { UserAgents.ANDROID_TABLET_USER_AGENT, "android_tablet", new Cookie[ 0 ] },
        { UserAgents.DESKTOP_USER_AGENT_CHROME, "desktop", new Cookie[ 0 ] },
        { UserAgents.DESKTOP_USER_AGENT_SAFARI, "desktop", new Cookie[ 0 ] },
        { UserAgents.DESKTOP_USER_AGENT_FIREFOX, "desktop", new Cookie[ 0 ] },
        { "", "desktop", new Cookie[ 0 ] },
        { UserAgents.BLACKBERRY_USER_AGENT, "blackberry", new Cookie[ 0 ] },
      };
  }

  @Test( dataProvider = "AgentMapping" )
  public void osValueExtraction( final String userAgent, final String value, final Cookie[] cookies )
  {
    final MgwtOsPropertyProvider provider = new MgwtOsPropertyProvider();
    assertEquals( provider.getPropertyName(), "mgwt.os" );
    final HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( "User-Agent" ) ).thenReturn( userAgent );
    when( request.getCookies() ).thenReturn( cookies );
    assertEquals( provider.getPropertyValue( request ), value );
  }
}
