package org.realityforge.gwt.appcache.server.mgwt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.realityforge.gwt.appcache.server.BindingProperty;
import org.realityforge.gwt.appcache.server.propertyprovider.PropertyProvider;

public final class MgwtOsPropertyProvider
  implements PropertyProvider
{
  static final BindingProperty iPhone = new BindingProperty( "mgwt.os", "iphone" );
  static final BindingProperty retina = new BindingProperty( "mgwt.os", "retina" );
  static final BindingProperty iPhone_undefined = new BindingProperty( "mgwt.os", "iphone_undefined" );

  static final BindingProperty iPad = new BindingProperty( "mgwt.os", "ipad" );
  static final BindingProperty iPad_retina = new BindingProperty( "mgwt.os", "ipad_retina" );
  static final BindingProperty iPad_undefined = new BindingProperty( "mgwt.os", "ipad_undefined" );

  @Nonnull
  @Override
  public String getPropertyName()
  {
    return "mgwt.os";
  }

  @Override
  @Nullable
  public String getPropertyValue( @Nonnull HttpServletRequest request )
  {
    final String userAgent = request.getHeader( "User-Agent" ).toLowerCase();

    if ( userAgent.contains( "android" ) )
    {
      if ( userAgent.contains( "mobile" ) )
      {
        return "android";
      }
      else
      {
        return "android_tablet";
      }
    }
    else if ( userAgent.contains( "ipad" ) )
    {
      final String value = getRetinaCookieValue( request );
      if ( "0".equals( value ) )
      {
        return "ipad";
      }
      else if ( "1".equals( value ) )
      {
        return "ipad_retina";
      }
      else
      {
        return "ipad_undefined";
      }
    }
    else if ( userAgent.contains( "iphone" ) )
    {
      final String value = getRetinaCookieValue( request );
      if ( "0".equals( value ) )
      {
        return "iphone";
      }
      else if ( "1".equals( value ) )
      {
        return "retina";
      }
      else
      {
        return "iphone_undefined";
      }
    }
    else if ( userAgent.contains( "blackberry" ) )
    {
      return "blackberry";
    }
    else
    {
      return "desktop";
    }
  }

  final String getRetinaCookieValue( final HttpServletRequest request )
  {
    final Cookie[] cookies = request.getCookies();
    if ( null != cookies )
    {
      for ( final Cookie cookie : cookies )
      {
        if ( "mgwt_ios_retina".equals( cookie.getName() ) )
        {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
}
