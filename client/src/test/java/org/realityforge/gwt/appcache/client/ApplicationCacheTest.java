package org.realityforge.gwt.appcache.client;

import com.google.gwt.event.shared.SimpleEventBus;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ApplicationCacheTest
{
  @Test
  public void basicOperation()
  {
    assertNull( ApplicationCache.get() );
    ApplicationCache.register( new TestApplicationCache( new SimpleEventBus() ) );
    assertNotNull( ApplicationCache.get() );
    assertNotNull( ApplicationCache.deregister( ApplicationCache.get() ) );
    assertNull( ApplicationCache.get() );
  }
}
