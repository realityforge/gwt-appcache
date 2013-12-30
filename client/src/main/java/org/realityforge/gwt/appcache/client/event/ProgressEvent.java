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
import org.realityforge.gwt.appcache.client.event.ProgressEvent.Handler;

/**
 * The user agent is downloading resources listed by the manifest.
 *
 * @author Daniel Kurka
 */
public class ProgressEvent
  extends GwtEvent<Handler>
{
  public interface Handler
    extends EventHandler
  {
    void onProgressEvent( @Nonnull ProgressEvent event );
  }

  private static final GwtEvent.Type<Handler> TYPE = new Type<Handler>();

  public static GwtEvent.Type<Handler> getType()
  {
    return TYPE;
  }

  private final int _loaded;
  private final int _total;

  public ProgressEvent( final int loaded, final int total )
  {
    _loaded = loaded;
    _total = total;
  }

  public boolean isLengthComputable()
  {
    return 0 != _total;
  }

  public int getLoaded()
  {
    return _loaded;
  }

  public int getTotal()
  {
    return _total;
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType()
  {
    return ProgressEvent.getType();
  }

  @Override
  protected void dispatch( @Nonnull final Handler handler )
  {
    handler.onProgressEvent( this );
  }
}
