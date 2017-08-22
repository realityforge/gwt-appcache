package org.realityforge.gwt.appcache.server.gwtp;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import org.realityforge.gwt.appcache.server.propertyprovider.PropertyProvider;

public final class FormFactorPropertyProvider
  implements PropertyProvider
{
  @Nonnull
  @Override
  public String getPropertyName()
  {
    return "gwtp.formfactor";
  }

  @Override
  public String getPropertyValue( @Nonnull final HttpServletRequest request )
  {
    final String userAgent = request.getHeader( "User-Agent" );
    if ( isSupportedMobileAgent( userAgent ) && !isUnsupportedMobileAgent( userAgent ) )
    {
      return "mobile";
    }
    else if ( isSupportedTabletAgent( userAgent ) )
    {
      return "tablet";
    }
    return "desktop";
  }

  private boolean isSupportedMobileAgent( final String userAgent )
  {
    return ( userAgent.contains( "Mobi" ) ||
             userAgent.contains( "DoCoMo" ) ||
             userAgent.contains( "Mini" ) ||
             userAgent.contains( "Puffin" ) ||
             userAgent.contains( "Fennec" ) ||
             userAgent.contains( "j2me" ) ||
             userAgent.contains( "Palm" ) ||
             userAgent.contains( "portalmmm" ) ||
             userAgent.contains( "Symbian ?OS" ) ||
             userAgent.contains( "BOLT" ) ||
             userAgent.contains( "webOS" ) ||
             userAgent.contains( "UP" ) ||
             userAgent.contains( "\\.(Link|Browser)" ) ||
             userAgent.contains( "MIDP" ) ||
             userAgent.contains( "PSP" ) ||
             userAgent.contains( "WP" ) ||
             userAgent.contains( "SonyEricsson" ) ||
             userAgent.contains( "Windows ?CE" ) ||
             userAgent.contains( "Android 0" ) ||
             userAgent.contains( "Novarra-Vision" ) ||
             userAgent.contains( "Nokia" ) ||
             userAgent.contains( "uZardWeb" ) );
  }

  private boolean isUnsupportedMobileAgent( final String userAgent )
  {
    return ( userAgent.contains( "Silk-Accelerated" ) ||
             userAgent.contains( "Pad" ) ||
             userAgent.contains( "GT-P1000M" ) ||
             userAgent.contains( "Xoom" ) ||
             userAgent.contains( "SCH-I800" ) );
  }

  private boolean isSupportedTabletAgent( final String userAgent )
  {
    return ( userAgent.contains( "Pad" ) ||
             userAgent.contains( "Android" ) ||
             userAgent.contains( "Kindle" ) ||
             userAgent.contains( "Silk-Accelerated" ) ||
             userAgent.contains( "nook" ) ||
             userAgent.contains( "PlayBook" ) );
  }
}
