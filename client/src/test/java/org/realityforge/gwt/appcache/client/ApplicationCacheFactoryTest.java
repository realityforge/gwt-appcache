package org.realityforge.gwt.appcache.client;

import com.google.gwt.event.shared.SimpleEventBus;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ApplicationCacheFactoryTest
{
  @Test
  public void basicOperation()
  {
    assertNull( ApplicationCacheFactory.get() );
    ApplicationCacheFactory.register( new TestApplicationCache( new SimpleEventBus() ) );
    assertNotNull( ApplicationCacheFactory.get() );
    assertNotNull( ApplicationCacheFactory.deregister( ApplicationCacheFactory.get() ) );
    assertNull( ApplicationCacheFactory.get() );
  }
}
