package org.realityforge.gwt.appcache.server.propertyprovider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

public class UserAgentPropertyProvider
  implements PropertyProvider
{
  private final Pattern _geckoRevisionPattern = Pattern.compile( "rv:([0-9]+)\\.([0-9]+)" );

  @Override
  @Nullable
  public String getPropertyValue( @Nonnull final HttpServletRequest request )
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
    else if ( userAgent.contains( "msie 8." ) || userAgent.contains( "msie 7." ) )
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
      if ( parseGeckoVersion( userAgent ) >= 1008 )
      {
        return "gecko1_8";
      }
      else
      {
        return "gecko";
      }
    }
    else
    {
      throw new IllegalStateException( "Can not find user agent property for userAgent: '" + userAgent + "'" );
    }
  }

  @Nonnull
  @Override
  public String getPropertyName()
  {
    return "user.agent";
  }

  private int parseGeckoVersion( final String userAgent )
  {
    final Matcher matcher = _geckoRevisionPattern.matcher( userAgent );
    if ( matcher.find() )
    {
      final int major = Integer.parseInt( matcher.group( 1 ) );
      final int minor = Integer.parseInt( matcher.group( 2 ) );

      return major * 1000 + minor;
    }
    else
    {
      return 0;
    }
  }
}
