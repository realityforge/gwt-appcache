package org.realityforge.gwt.appcache.server.propertyprovider;

import javax.servlet.http.HttpServletRequest;

public class UserAgentPropertyProvider
  implements PropertyProvider
{
  @Override
  public String getPropertyValue( final HttpServletRequest request )
  {
    final String userAgent = request.getHeader( "User-Agent" ).toLowerCase();
    if ( userAgent.contains( "msie 10." ) )
    {
      return "ie10";
    }
    else if ( userAgent.contains( "msie 9." ) )
    {
      return "ie9";
    }
    // Assume that the 7 string indicates ie8 in compatibility mode
    else if ( userAgent.contains( "msie 8." ) || userAgent.contains( "msie 7." ))
    {
      return "ie8";
    }
    else if ( userAgent.contains( "opera" ) )
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
