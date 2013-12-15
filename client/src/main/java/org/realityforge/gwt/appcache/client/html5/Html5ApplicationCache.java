package org.realityforge.gwt.appcache.client.html5;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import javax.annotation.Nonnull;

public final class Html5ApplicationCache
  extends AbstractApplicationCache
{
  public static native boolean isSupported()/*-{
    return typeof ($wnd.applicationCache) == "object";
  }-*/;

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
  public native void swapCache() /*-{
    $wnd.applicationCache.swapCache();
  }-*/;

  @Override
  public native void update() /*-{
    $wnd.applicationCache.update();
  }-*/;

  private native int getStatus0()/*-{
    return $wnd.applicationCache.status;
  }-*/;

  private native void registerListeners0() /*-{
    var that = this;

    var check = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.html5.AbstractApplicationCache::onChecking()();
    } );
    $wnd.applicationCache.addEventListener( "checking", check );

    var onError = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.html5.AbstractApplicationCache::onError()();
    } );
    $wnd.applicationCache.addEventListener( "error", onError );

    var onNoUpdate = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.html5.AbstractApplicationCache::onNoUpdate()();
    } );
    $wnd.applicationCache.addEventListener( "noupdate", onNoUpdate );

    var onDownloading = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.html5.AbstractApplicationCache::onDownloading()();
    } );
    $wnd.applicationCache.addEventListener( "downloading", onDownloading );

    var onProgress = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.html5.AbstractApplicationCache::onProgress()();
    } );
    $wnd.applicationCache.addEventListener( "progress", onProgress );

    var onUpdateReady = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.html5.AbstractApplicationCache::onUpdateReady()();
    } );
    $wnd.applicationCache.addEventListener( "updateready", onUpdateReady );

    var onCached = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.html5.AbstractApplicationCache::onCached()();
    } );
    $wnd.applicationCache.addEventListener( "cached", onCached );

    var onObsolete = $entry( function () {
      that.@org.realityforge.gwt.appcache.client.html5.AbstractApplicationCache::onObsolete()();
    } );
    $wnd.applicationCache.addEventListener( "obsolete", onObsolete );
  }-*/;
}
