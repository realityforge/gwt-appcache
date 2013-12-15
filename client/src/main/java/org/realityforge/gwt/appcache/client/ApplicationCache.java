package org.realityforge.gwt.appcache.client;

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

public interface ApplicationCache
{
  @Nonnull
  ApplicationCacheStatus getStatus();

  void swapCache();

  void update();

  @Nonnull
  HandlerRegistration addCheckingHandler( @Nonnull CheckingEvent.Handler handler );

  @Nonnull
  HandlerRegistration addCachedHandler( @Nonnull CachedEvent.Handler handler );

  @Nonnull
  HandlerRegistration addDownloadingHandler( @Nonnull DownloadingEvent.Handler handler );

  @Nonnull
  HandlerRegistration addErrorHandler( @Nonnull ErrorEvent.Handler handler );

  @Nonnull
  HandlerRegistration addNoUpdateHandler( @Nonnull NoUpdateEvent.Handler handler );

  @Nonnull
  HandlerRegistration addObsoleteHandler( @Nonnull ObsoleteEvent.Handler handler );

  @Nonnull
  HandlerRegistration addProgressHandler( @Nonnull ProgressEvent.Handler handler );

  @Nonnull
  HandlerRegistration addUpdateReadyHandler( @Nonnull UpdateReadyEvent.Handler handler );
}
