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
import org.realityforge.gwt.appcache.client.event.ObsoleteEvent.Handler;

/**
 * The manifest was found to have become a 404 or 410 page, so the application
 * cache is being deleted.
 *
 * @author Daniel Kurka
 */
public class ObsoleteEvent
  extends GwtEvent<Handler>
{
  public interface Handler
    extends EventHandler
  {
    void onObsoleteEvent( @Nonnull ObsoleteEvent event );
  }

  private static final GwtEvent.Type<Handler> TYPE = new Type<Handler>();

  public static GwtEvent.Type<Handler> getType()
  {
    return TYPE;
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType()
  {
    return ObsoleteEvent.getType();
  }

  @Override
  protected void dispatch( @Nonnull final Handler handler )
  {
    handler.onObsoleteEvent( this );
  }
}
