package org.realityforge.gwt.appcache.server;

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

  @Override
  public String getPropertyName()
  {
    return _key;
  }

  @Override
  public String getPropertyValue( final HttpServletRequest request )
    throws Exception
  {
    return _value;
  }
}
