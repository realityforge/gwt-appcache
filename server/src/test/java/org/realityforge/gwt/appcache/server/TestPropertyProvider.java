package org.realityforge.gwt.appcache.server;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import org.realityforge.gwt.appcache.server.propertyprovider.PropertyProvider;

public final class TestPropertyProvider
  implements PropertyProvider
{
  private final String _key;
  private final String _value;

  public TestPropertyProvider( final String key, final String value )
  {
    _key = key;
    _value = value;
  }

  @Nonnull
  @Override
  public String getPropertyName()
  {
    return _key;
  }

  @Override
  @Nullable
  public String getPropertyValue( @Nonnull final HttpServletRequest request )
    throws Exception
  {
    return _value;
  }
}
