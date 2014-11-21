package org.realityforge.gwt.appcache.server.propertyprovider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

public class LocalePropertyProvider
  implements PropertyProvider
{
  @Nonnull
  @Override
  public String getPropertyName()
  {
    return "locale";
  }

  @Override
  @Nullable
  public String getPropertyValue( @Nonnull final HttpServletRequest request )
  {
    final String queryString = request.getQueryString();
    if ( null != queryString )
    {
      final String[] parts = queryString.split( "&" );
      for ( final String part : parts )
      {
        final int index = part.indexOf( '=' );
        if ( -1 != index )
        {
          final String key = part.substring( 0, index );
          if ( "locale".equals( key ) )
          {
            return part.substring( index + 1 );
          }
        }
      }
    }
    final String language = request.getHeader( "Accept-Language" );
    if ( null != language )
    {
      final String[] parts = language.split( "[,\\;]" );
      return parts[ 0 ];
    }
    return "default";
  }
}
