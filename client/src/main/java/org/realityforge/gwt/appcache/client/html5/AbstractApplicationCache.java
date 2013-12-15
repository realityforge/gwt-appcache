package org.realityforge.gwt.appcache.client.html5;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import javax.annotation.Nonnull;
import org.realityforge.gwt.appcache.client.ApplicationCache;
import org.realityforge.gwt.appcache.client.event.CachedEvent;
import org.realityforge.gwt.appcache.client.event.CheckingEvent;
import org.realityforge.gwt.appcache.client.event.DownloadingEvent;
import org.realityforge.gwt.appcache.client.event.ErrorEvent;
import org.realityforge.gwt.appcache.client.event.NoUpdateEvent;
import org.realityforge.gwt.appcache.client.event.ObsoleteEvent;
import org.realityforge.gwt.appcache.client.event.ProgressEvent;
import org.realityforge.gwt.appcache.client.event.UpdateReadyEvent;

public abstract class AbstractApplicationCache
  implements ApplicationCache
{
  private final EventBus _eventBus;

  protected AbstractApplicationCache( final EventBus eventBus )
  {
    _eventBus = eventBus;
  }

  @Nonnull
  @Override
  public final HandlerRegistration addCheckingHandler( @Nonnull CheckingEvent.Handler handler )
  {
    return _eventBus.addHandler( CheckingEvent.getType(), handler );
  }

  @Nonnull
  @Override
  public final HandlerRegistration addCachedHandler( @Nonnull CachedEvent.Handler handler )
  {
    return _eventBus.addHandler( CachedEvent.getType(), handler );
  }

  @Nonnull
  @Override
  public final HandlerRegistration addDownloadingHandler( @Nonnull DownloadingEvent.Handler handler )
  {
    return _eventBus.addHandler( DownloadingEvent.getType(), handler );
  }

  @Nonnull
  @Override
  public final HandlerRegistration addErrorHandler( @Nonnull ErrorEvent.Handler handler )
  {
    return _eventBus.addHandler( ErrorEvent.getType(), handler );
  }

  @Nonnull
  @Override
  public final HandlerRegistration addNoUpdateHandler( @Nonnull final NoUpdateEvent.Handler handler )
  {
    return _eventBus.addHandler( NoUpdateEvent.getType(), handler );
  }

  @Nonnull
  @Override
  public final HandlerRegistration addObsoleteHandler( @Nonnull ObsoleteEvent.Handler handler )
  {
    return _eventBus.addHandler( ObsoleteEvent.getType(), handler );
  }

  @Nonnull
  @Override
  public final HandlerRegistration addProgressHandler( @Nonnull ProgressEvent.Handler handler )
  {
    return _eventBus.addHandler( ProgressEvent.getType(), handler );
  }

  @Nonnull
  @Override
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
