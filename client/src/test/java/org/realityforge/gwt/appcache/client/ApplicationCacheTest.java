package org.realityforge.gwt.appcache.client;

import com.google.gwt.event.shared.SimpleEventBus;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ApplicationCacheTest
{
  @Test
  public void registryTest()
  {
    assertNull( ApplicationCache.get() );
    ApplicationCache.register( new TestApplicationCache( new SimpleEventBus() ) );
    assertNotNull( ApplicationCache.get() );
    final ApplicationCache applicationCache = ApplicationCache.get();
    assertTrue( ApplicationCache.deregister( applicationCache ) );
    assertNull( ApplicationCache.get() );
    assertFalse( ApplicationCache.deregister( applicationCache ) );
  }
}
