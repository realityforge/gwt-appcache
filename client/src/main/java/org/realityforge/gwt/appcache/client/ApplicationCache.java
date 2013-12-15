package org.realityforge.gwt.appcache.client;

import com.google.gwt.core.shared.GWT;
import com.google.web.bindery.event.shared.HandlerRegistration;
import javax.annotation.Nonnull;
import org.realityforge.gwt.appcache.client.event.CachedEvent;
import org.realityforge.gwt.appcache.client.event.CheckingEvent;
import org.realityforge.gwt.appcache.client.event.DownloadingEvent;
import org.realityforge.gwt.appcache.client.event.ErrorEvent;
import org.realityforge.gwt.appcache.client.event.NoUpdateEvent;
import org.realityforge.gwt.appcache.client.event.ObsoleteEvent;
import org.realityforge.gwt.appcache.client.event.ProgressEvent;
import org.realityforge.gwt.appcache.client.event.UpdateReadyEvent;
import org.realityforge.gwt.appcache.client.html5.Html5ApplicationCache;

public abstract class ApplicationCache
{
  public static enum Status
  {
    UNCACHED,
    IDLE,
    CHECKING,
    DOWNLOADING,
    UPDATEREADY,
    OBSOLETE
  }

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

  @Nonnull
  public abstract Status getStatus();

  public abstract void swapCache();

  public abstract void update();

  @Nonnull
  public abstract HandlerRegistration addCheckingHandler( @Nonnull CheckingEvent.Handler handler );

  @Nonnull
  public abstract HandlerRegistration addCachedHandler( @Nonnull CachedEvent.Handler handler );

  @Nonnull
  public abstract HandlerRegistration addDownloadingHandler( @Nonnull DownloadingEvent.Handler handler );

  @Nonnull
  public abstract HandlerRegistration addErrorHandler( @Nonnull ErrorEvent.Handler handler );

  @Nonnull
  public abstract HandlerRegistration addNoUpdateHandler( @Nonnull NoUpdateEvent.Handler handler );

  @Nonnull
  public abstract HandlerRegistration addObsoleteHandler( @Nonnull ObsoleteEvent.Handler handler );

  @Nonnull
  public abstract HandlerRegistration addProgressHandler( @Nonnull ProgressEvent.Handler handler );

  @Nonnull
  public abstract HandlerRegistration addUpdateReadyHandler( @Nonnull UpdateReadyEvent.Handler handler );
}
