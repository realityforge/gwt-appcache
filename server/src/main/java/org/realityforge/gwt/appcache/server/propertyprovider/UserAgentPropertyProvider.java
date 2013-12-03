package org.realityforge.gwt.appcache.server.propertyprovider;

import javax.servlet.http.HttpServletRequest;

public class UserAgentPropertyProvider
  implements PropertyProvider
{
  @Override
  public String getPropertyValue( final HttpServletRequest request )
  {
    final String userAgent = request.getHeader( "User-Agent" ).toLowerCase();
    if ( userAgent.contains( "opera" ) )
    {
      return "opera";
    }
    else if ( userAgent.contains( "safari" ) || userAgent.contains( "iphone" ) || userAgent.contains( "ipad" ) )
    {
      return "safari";
    }
    else if ( userAgent.contains( "gecko" ) )
    {
      return "gecko1_8";
    }
    else
    {
      throw new IllegalStateException( "Can not find user agent property for userAgent: '" + userAgent + "'" );
    }
  }

  @Override
  public String getPropertyName()
  {
    return "user.agent";
  }
}
