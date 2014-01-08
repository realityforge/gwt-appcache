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
  private static SupportDetector g_supportDetector;

  private final EventBus _eventBus;

  public static ApplicationCache getApplicationCacheIfSupported()
  {
    if ( null == g_cache )
    {
      if ( GWT.isClient() && getSupportDetector().isSupported() && getSupportDetector().hasManifest() )
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

  /**
   * Cancels the application cache download process.
   * The call is ignored if no download in progress.
   */
  public abstract void abort();

  /**
   * Request that the browser swap in the new application cache.
   * This may fail if there is no update or no application cache.
   *
   * @return false if unable swap in cache.
   */
  public abstract boolean swapCache();

  /**
   * Request that the browser update the application cache.
   * This may fail if the application is not cached.
   *
   * @return false if unable to request update.
   */
  public abstract boolean requestUpdate();

  /**
   * Attempt to remove the application from the cache.
   * This is achieved by forcing the server to return a 404 or 410 when updating the manifest.
   *
   * @return true if request succeeded.
   */
  public abstract boolean removeCache();

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

  /**
   * Fire a Checking event.
   */
  protected final void onChecking()
  {
    _eventBus.fireEventFromSource( new CheckingEvent(), this );
  }

  /**
   * Fire an Error event.
   */
  protected final void onError()
  {
    _eventBus.fireEventFromSource( new ErrorEvent(), this );
  }

  /**
   * Fire a NoUpdate event.
   */
  protected final void onNoUpdate()
  {
    _eventBus.fireEventFromSource( new NoUpdateEvent(), this );
  }

  /**
   * Fire a Downloading event.
   */
  protected final void onDownloading()
  {
    _eventBus.fireEventFromSource( new DownloadingEvent(), this );
  }

  /**
   * Fire a Progress event.
   */
  protected final void onProgress( final int loaded, final int total )
  {
    _eventBus.fireEventFromSource( new ProgressEvent( loaded, total ), this );
  }

  /**
   * Fire a UpdateReady event.
   */
  protected final void onUpdateReady()
  {
    _eventBus.fireEventFromSource( new UpdateReadyEvent(), this );
  }

  /**
   * Fire an Obsolete event.
   */
  protected final void onCached()
  {
    _eventBus.fireEventFromSource( new CachedEvent(), this );
  }

  /**
   * Fire an Obsolete event.
   */
  protected final void onObsolete()
  {
    _eventBus.fireEventFromSource( new ObsoleteEvent(), this );
  }

  /**
   * Detector for browser support of Appcache.
   */
  private static class SupportDetector
  {
    public boolean isSupported()
    {
      return Html5ApplicationCache.isSupported();
    }

    public boolean hasManifest()
    {
      return Html5ApplicationCache.hasManifest();
    }
  }

  /**
   * Detector for browsers that do not support Appcache.
   */
  @SuppressWarnings( "unused" )
  private static class NoSupportDetector
    extends SupportDetector
  {
    @Override
    public boolean isSupported()
    {
      return false;
    }

    @Override
    public boolean hasManifest()
    {
      return false;
    }
  }

  private static SupportDetector getSupportDetector()
  {
    if ( null == g_supportDetector )
    {
      g_supportDetector = com.google.gwt.core.shared.GWT.create( SupportDetector.class );
    }
    return g_supportDetector;
  }
}
