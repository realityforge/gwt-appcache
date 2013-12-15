package org.realityforge.gwt.appcache.client;

import com.google.gwt.core.shared.GWT;
import javax.annotation.Nonnull;
import org.realityforge.gwt.appcache.client.html5.Html5ApplicationCache;

public final class ApplicationCacheFactory
{
  private static ApplicationCache _cache;

  public static ApplicationCache get()
  {
    if ( null == _cache )
    {
      if ( GWT.isClient() && Html5ApplicationCache.isSupported() )
      {
        register( new Html5ApplicationCache() );
      }
    }
    return _cache;
  }

  public static void register( @Nonnull final ApplicationCache applicationCache )
  {
    _cache = applicationCache;
  }

  public static boolean deregister( @Nonnull final ApplicationCache applicationCache )
  {
    if ( _cache != applicationCache )
    {
      return false;
    }
    else
    {
      _cache = null;
      return true;
    }
  }
}
