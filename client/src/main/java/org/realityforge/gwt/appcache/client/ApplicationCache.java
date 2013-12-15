package org.realityforge.gwt.appcache.client;

import com.google.gwt.core.shared.GWT;
import com.google.web.bindery.event.shared.EventBus;
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

  private static ApplicationCache g_cache;

  private final EventBus _eventBus;

  public static ApplicationCache get()
  {
    if ( null == g_cache )
    {
      if ( GWT.isClient() && Html5ApplicationCache.isSupported() )
      {
        register( new Html5ApplicationCache() );
      }
    }
    return g_cache;
  }

  public static void register( @Nonnull final ApplicationCache applicationCache )
  {
    g_cache = applicationCache;
  }

  public static boolean deregister( @Nonnull final ApplicationCache applicationCache )
  {
    if ( g_cache != applicationCache )
    {
      return false;
    }
    else
    {
      g_cache = null;
      return true;
    }
  }

  protected ApplicationCache( final EventBus eventBus )
  {
    _eventBus = eventBus;
  }

  @Nonnull
  public abstract Status getStatus();

  public abstract void swapCache();

  public abstract void update();

  @Nonnull
  public final HandlerRegistration addCheckingHandler( @Nonnull CheckingEvent.Handler handler )
  {
    return _eventBus.addHandler( CheckingEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addCachedHandler( @Nonnull CachedEvent.Handler handler )
  {
    return _eventBus.addHandler( CachedEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addDownloadingHandler( @Nonnull DownloadingEvent.Handler handler )
  {
    return _eventBus.addHandler( DownloadingEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addErrorHandler( @Nonnull ErrorEvent.Handler handler )
  {
    return _eventBus.addHandler( ErrorEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addNoUpdateHandler( @Nonnull final NoUpdateEvent.Handler handler )
  {
    return _eventBus.addHandler( NoUpdateEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addObsoleteHandler( @Nonnull ObsoleteEvent.Handler handler )
  {
    return _eventBus.addHandler( ObsoleteEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addProgressHandler( @Nonnull ProgressEvent.Handler handler )
  {
    return _eventBus.addHandler( ProgressEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addUpdateReadyHandler( @Nonnull UpdateReadyEvent.Handler handler )
  {
    return _eventBus.addHandler( UpdateReadyEvent.getType(), handler );
  }

  protected final void onChecking()
  {
    _eventBus.fireEventFromSource( new CheckingEvent(), this );
  }

  protected final void onError()
  {
    _eventBus.fireEventFromSource( new ErrorEvent(), this );
  }

  protected final void onNoUpdate()
  {
    _eventBus.fireEventFromSource( new NoUpdateEvent(), this );
  }

  protected final void onDownloading()
  {
    _eventBus.fireEventFromSource( new DownloadingEvent(), this );
  }

  protected final void onProgress()
  {
    _eventBus.fireEventFromSource( new ProgressEvent(), this );
  }

  protected final void onUpdateReady()
  {
    _eventBus.fireEventFromSource( new UpdateReadyEvent(), this );
  }

  protected final void onCached()
  {
    _eventBus.fireEventFromSource( new CachedEvent(), this );
  }

  protected final void onObsolete()
  {
    _eventBus.fireEventFromSource( new ObsoleteEvent(), this );
  }
}
