package org.realityforge.gwt.appcache.client;

import com.google.web.bindery.event.shared.EventBus;
import javax.annotation.Nonnull;

final class TestApplicationCache
  extends ApplicationCache
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
