package org.realityforge.gwt.appcache.server;

import javax.annotation.Nonnull;

public final class BindingProperty
{
  private final String _name;
  private final String _value;

  public BindingProperty( @Nonnull final String name, @Nonnull final String value )
  {
    _name = name;
    _value = value;
  }

  @Nonnull
  public String getName()
  {
    return _name;
  }

  @Nonnull
  public String getValue()
  {
    return _value;
  }

  @Override
  public String toString()
  {
    return "BindingProperty[name=" + _name + ", value=" + _value + "]";
  }
}
