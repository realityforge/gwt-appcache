package org.realityforge.gwt.appcache.client;

import com.google.web.bindery.event.shared.EventBus;
import javax.annotation.Nonnull;
import org.realityforge.gwt.appcache.client.html5.AbstractApplicationCache;

final class TestApplicationCache
  extends AbstractApplicationCache
{
  TestApplicationCache( final EventBus eventBus )
  {
    super( eventBus );
  }

  @Nonnull
  @Override
  public Status getStatus()
  {
    return Status.IDLE;
  }

  @Override
  public void swapCache()
  {
  }

  @Override
  public void update()
  {
  }
}
