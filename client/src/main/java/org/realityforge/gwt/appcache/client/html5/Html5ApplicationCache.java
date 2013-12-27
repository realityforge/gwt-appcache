package org.realityforge.gwt.appcache.client.html5;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Cookies;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import org.realityforge.gwt.appcache.client.ApplicationCache;
import org.realityforge.gwt.appcache.client.event.CachedEvent;
import org.realityforge.gwt.appcache.client.event.ErrorEvent;
import org.realityforge.gwt.appcache.client.event.NoUpdateEvent;
import org.realityforge.gwt.appcache.client.event.ObsoleteEvent;
import org.realityforge.gwt.appcache.client.event.UpdateReadyEvent;

public final class Html5ApplicationCache
  extends ApplicationCache
{
  private static final String DISABLE_MANIFEST_COOKIE_NAME = "appcache_disable";
  private static final String DISABLE_MANIFEST_COOKIE_VALUE = "1";

  public static native boolean isSupported()/*-{
    return typeof ($wnd.applicationCache) == "object";
  }-*/;

  /**
   * @return true if the document has an AppCache manifest attached
   */
  public static boolean hasManifest()
  {
    return Document.get().getDocumentElement().hasAttribute( "manifest" );
  }

  public Html5ApplicationCache()
  {
    this( new SimpleEventBus() );
  }

  public Html5ApplicationCache( final EventBus eventBus )
  {
    super( eventBus );
    registerListeners0();
  }

  @Nonnull
  @Override
  public Status getStatus()
  {
    return Status.values()[ getStatus0() ];
  }

  @Override
  public boolean swapCache()
  {
    try
    {
      swapCache0();
      return true;
    }
    catch ( final Throwable t )
    {
      return false;
    }
  }

  private native void swapCache0() /*-{
    $wnd.applicationCache.swapCache();
  }-*/;

  @Override
  public boolean requestUpdate()
  {
    enableAppCache();
    try
    {
      update0();
      return true;
    }
    catch ( final Throwable t )
    {
      return false;
    }
  }

  private native void update0() /*-{
    $wnd.applicationCache.update();
  }-*/;

  private native int getStatus0()/*-{
    return $wnd.applicationCache.status;
  }-*/;

  @Override
  public boolean removeCache()
  {
    Cookies.setCookie( DISABLE_MANIFEST_COOKIE_NAME, DISABLE_MANIFEST_COOKIE_VALUE );

    // Register handlers for every terminal event so we can ensure that we remove the cookie.
    // All of these may be required due to; network failure, intermediate cache not passing
    // back to server, overlapping requests etc.
    final ArrayList<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();
    registrations.add( addErrorHandler( new ErrorEvent.Handler()
    {
      @Override
      public void onErrorEvent( @Nonnull final ErrorEvent event )
      {
        cacheRemovalCleanup( registrations );
      }
    } ) );
    registrations.add( addObsoleteHandler( new ObsoleteEvent.Handler()
    {
      @Override
      public void onObsoleteEvent( @Nonnull final ObsoleteEvent event )
      {
        cacheRemovalCleanup( registrations );
      }
    } ) );
    registrations.add( addNoUpdateHandler( new NoUpdateEvent.Handler()
    {
      @Override
      public void onNoUpdateEvent( @Nonnull final NoUpdateEvent event )
      {
        cacheRemovalCleanup( registrations );
      }
    } ) );
    registrations.add( addUpdateReadyHandler( new UpdateReadyEvent.Handler()
    {
      @Override
      public void onUpdateReadyEvent( @Nonnull final UpdateReadyEvent event )
      {
        cacheRemovalCleanup( registrations );
      }
    } ) );
    registrations.add( addCachedHandler( new CachedEvent.Handler()
    {
      @Override
      public void onCachedEvent( @Nonnull final CachedEvent event )
      {
        cacheRemovalCleanup( registrations );
      }
    } ) );
    try
    {
      update0();
      return true;
    }
    catch ( final Throwable t )
    {
      cacheRemovalCleanup( registrations );
      return false;
    }
  }

  private void cacheRemovalCleanup( final ArrayList<HandlerRegistration> registrations )
  {
    for ( final HandlerRegistration registration : registrations )
    {
      registration.removeHandler();
    }
    enableAppCache();
  }

  private void enableAppCache()
  {
    Cookies.removeCookie( DISABLE_MANIFEST_COOKIE_NAME );
  }

  private native void registerListeners0() /*-{
    var that = this;

    var check = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.ApplicationCache::onChecking()();
    } );
    $wnd.applicationCache.addEventListener( "checking", check );

    var onError = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.ApplicationCache::onError()();
    } );
    $wnd.applicationCache.addEventListener( "error", onError );

    var onNoUpdate = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.ApplicationCache::onNoUpdate()();
    } );
    $wnd.applicationCache.addEventListener( "noupdate", onNoUpdate );

    var onDownloading = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.ApplicationCache::onDownloading()();
    } );
    $wnd.applicationCache.addEventListener( "downloading", onDownloading );

    var onProgress = $entry( function (event) {
      that.@org.realityforge.gwt.appcache.client.ApplicationCache::onProgress(II)(event.loaded, event.total);
    } );
    $wnd.applicationCache.addEventListener( "progress", onProgress );

    var onUpdateReady = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.ApplicationCache::onUpdateReady()();
    } );
    $wnd.applicationCache.addEventListener( "updateready", onUpdateReady );

    var onCached = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.ApplicationCache::onCached()();
    } );
    $wnd.applicationCache.addEventListener( "cached", onCached );

    var onObsolete = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.ApplicationCache::onObsolete()();
    } );
    $wnd.applicationCache.addEventListener( "obsolete", onObsolete );
  }-*/;
}
