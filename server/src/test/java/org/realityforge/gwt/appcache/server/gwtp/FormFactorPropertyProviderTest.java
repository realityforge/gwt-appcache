package org.realityforge.gwt.appcache.server.gwtp;

import javax.servlet.http.HttpServletRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public final class FormFactorPropertyProviderTest
{
  @DataProvider( name = "AgentMapping" )
  public Object[][] getAgentMapping()
  {
    return new Object[][]
      {
        { UserAgents.FENNEC_USER_AGENT, "mobile" },
        { UserAgents.OPERA_USER_AGENT, "mobile" },
        { UserAgents.ANDROID_TABLET_USER_AGENT, "tablet" },
        { UserAgents.DESKTOP_USER_AGENT_SAFARI, "desktop" },
        { UserAgents.DESKTOP_USER_AGENT_CHROME, "desktop" }
      };
  }

  @Test( dataProvider = "AgentMapping" )
  public void userAgentExtraction( final String userAgent, final String value )
  {
    final FormFactorPropertyProvider provider = new FormFactorPropertyProvider();
    assertEquals( provider.getPropertyName(), "gwtp.formfactor" );
    final HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( "User-Agent" ) ).thenReturn( userAgent );
    assertEquals( provider.getPropertyValue( request ), value );
  }
}
