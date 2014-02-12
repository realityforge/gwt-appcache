package org.realityforge.gwt.appcache.server.mgwt;

import javax.servlet.http.HttpServletRequest;
import org.realityforge.gwt.appcache.server.propertyprovider.PropertyProvider;

public final class MobileUserAgentProvider
  implements PropertyProvider
{
  @Override
  public String getPropertyName()
  {
    return "mobile.user.agent";
  }

  @Override
  public String getPropertyValue( final HttpServletRequest request )
  {
    final String userAgent = request.getHeader( "User-Agent" ).toLowerCase();
    if ( userAgent.contains( "android" ) || userAgent.contains( "iphone" ) || userAgent.contains( "ipad" ) )
    {
      return "mobilesafari";
    }
    else
    {
      return "not_mobile";
    }
  }
}
