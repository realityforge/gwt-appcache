package org.realityforge.gwt.appcache.server;

public final class BindingProperty
{
  private final String _name;
  private final String _value;

  public BindingProperty( String name, String value )
  {
    _name = name;
    _value = value;
  }

  public String getName()
  {
    return _name;
  }

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
