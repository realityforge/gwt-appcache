/*
 * Copyright 2011 Daniel Kurka
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.realityforge.gwt.appcache.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import javax.annotation.Nonnull;
import org.realityforge.gwt.appcache.client.event.NoUpdateEvent.Handler;

/**
 * The manifest hadn't changed.
 *
 * @author Daniel Kurka
 */
public class NoUpdateEvent
  extends GwtEvent<Handler>
{
  public interface Handler
    extends EventHandler
  {
    public void onNoUpdateEvent( @Nonnull NoUpdateEvent event );
  }

  private static final GwtEvent.Type<Handler> TYPE = new Type<Handler>();

  public static GwtEvent.Type<Handler> getType()
  {
    return TYPE;
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType()
  {
    return NoUpdateEvent.getType();
  }

  @Override
  protected void dispatch( @Nonnull final Handler handler )
  {
    handler.onNoUpdateEvent( this );
  }
}

