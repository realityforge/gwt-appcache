package org.realityforge.gwt.appcache.client;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.mockito.Mockito;
import org.realityforge.gwt.appcache.client.event.CachedEvent;
import org.realityforge.gwt.appcache.client.event.CheckingEvent;
import org.realityforge.gwt.appcache.client.event.DownloadingEvent;
import org.realityforge.gwt.appcache.client.event.ErrorEvent;
import org.realityforge.gwt.appcache.client.event.NoUpdateEvent;
import org.realityforge.gwt.appcache.client.event.ObsoleteEvent;
import org.realityforge.gwt.appcache.client.event.ProgressEvent;
import org.realityforge.gwt.appcache.client.event.UpdateReadyEvent;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ApplicationCacheTest
{
  @Test
  public void registryTest()
  {
    assertNull( ApplicationCache.getApplicationCacheIfSupported() );
    ApplicationCache.register( new TestApplicationCache( new SimpleEventBus() ) );
    assertNotNull( ApplicationCache.getApplicationCacheIfSupported() );
    final ApplicationCache applicationCache = ApplicationCache.getApplicationCacheIfSupported();
    assertTrue( ApplicationCache.deregister( applicationCache ) );
    assertNull( ApplicationCache.getApplicationCacheIfSupported() );
    assertFalse( ApplicationCache.deregister( applicationCache ) );
  }

  @Test
  public void handlerInteractions()
  {
    final TestApplicationCache applicationCache = new TestApplicationCache( new SimpleEventBus() );

    {
      final UpdateReadyEvent.Handler handler = mock( UpdateReadyEvent.Handler.class );
      final HandlerRegistration registration = applicationCache.addUpdateReadyHandler( handler );
      applicationCache.onUpdateReady();
      verify( handler, only() ).onUpdateReadyEvent( Mockito.<UpdateReadyEvent>anyObject() );
      registration.removeHandler();
      applicationCache.onUpdateReady();
      verify( handler, atMost( 1 ) ).onUpdateReadyEvent( Mockito.<UpdateReadyEvent>anyObject() );
    }

    {
      final CachedEvent.Handler handler = mock( CachedEvent.Handler.class );
      final HandlerRegistration registration = applicationCache.addCachedHandler( handler );
      applicationCache.onCached();
      verify( handler, only() ).onCachedEvent( Mockito.<CachedEvent>anyObject() );
      registration.removeHandler();
      applicationCache.onCached();
      verify( handler, atMost( 1 ) ).onCachedEvent( Mockito.<CachedEvent>anyObject() );
    }

    {
      final CheckingEvent.Handler handler = mock( CheckingEvent.Handler.class );
      final HandlerRegistration registration = applicationCache.addCheckingHandler( handler );
      applicationCache.onChecking();
      verify( handler, only() ).onCheckingEvent( Mockito.<CheckingEvent>anyObject() );
      registration.removeHandler();
      applicationCache.onChecking();
      verify( handler, atMost( 1 ) ).onCheckingEvent( Mockito.<CheckingEvent>anyObject() );
    }

    {
      final DownloadingEvent.Handler handler = mock( DownloadingEvent.Handler.class );
      final HandlerRegistration registration = applicationCache.addDownloadingHandler( handler );
      applicationCache.onDownloading();
      verify( handler, only() ).onDownloadingEvent( Mockito.<DownloadingEvent>anyObject() );
      registration.removeHandler();
      applicationCache.onDownloading();
      verify( handler, atMost( 1 ) ).onDownloadingEvent( Mockito.<DownloadingEvent>anyObject() );
    }

    {
      final ErrorEvent.Handler handler = mock( ErrorEvent.Handler.class );
      final HandlerRegistration registration = applicationCache.addErrorHandler( handler );
      applicationCache.onError();
      verify( handler, only() ).onErrorEvent( Mockito.<ErrorEvent>anyObject() );
      registration.removeHandler();
      applicationCache.onError();
      verify( handler, atMost( 1 ) ).onErrorEvent( Mockito.<ErrorEvent>anyObject() );
    }

    {
      final NoUpdateEvent.Handler handler = mock( NoUpdateEvent.Handler.class );
      final HandlerRegistration registration = applicationCache.addNoUpdateHandler( handler );
      applicationCache.onNoUpdate();
      verify( handler, only() ).onNoUpdateEvent( Mockito.<NoUpdateEvent>anyObject() );
      registration.removeHandler();
      applicationCache.onNoUpdate();
      verify( handler, atMost( 1 ) ).onNoUpdateEvent( Mockito.<NoUpdateEvent>anyObject() );
    }

    {
      final ProgressEvent.Handler handler = mock( ProgressEvent.Handler.class );
      final HandlerRegistration registration = applicationCache.addProgressHandler( handler );
      applicationCache.onProgress( 2, 5 );
      verify( handler, only() ).onProgressEvent( refEq( new ProgressEvent( 2, 5 ), "source" ) );
      registration.removeHandler();
      applicationCache.onProgress( 3, 5 );
      verify( handler, atMost( 1 ) ).onProgressEvent( Mockito.<ProgressEvent>anyObject() );
    }

    {
      final ObsoleteEvent.Handler handler = mock( ObsoleteEvent.Handler.class );
      final HandlerRegistration registration = applicationCache.addObsoleteHandler( handler );
      applicationCache.onObsolete();
      verify( handler, only() ).onObsoleteEvent( Mockito.<ObsoleteEvent>anyObject() );
      registration.removeHandler();
      applicationCache.onObsolete();
      verify( handler, atMost( 1 ) ).onObsoleteEvent( Mockito.<ObsoleteEvent>anyObject() );
    }
  }
}
