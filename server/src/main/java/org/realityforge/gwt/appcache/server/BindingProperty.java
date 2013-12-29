package org.realityforge.gwt.appcache.server;

import javax.annotation.Nonnull;

public final class BindingProperty
{
  private final String _name;
  private final String _value;
  private final String[] _components;

  public BindingProperty( @Nonnull final String name, @Nonnull final String value )
  {
    _name = name;
    _value = value;
    _components = value.split( "," );
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

  @Nonnull
  public String[] getComponents()
  {
    return _components;
  }

  public boolean matches( @Nonnull final String value )
  {
    for ( final String component : _components )
    {
      if ( component.equals( value ) )
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals( final Object o )
  {
    if ( this == o )
    {
      return true;
    }
    else if ( o == null || getClass() != o.getClass() )
    {
      return false;
    }
    else
    {
      final BindingProperty that = (BindingProperty) o;
      return _name.equals( that._name ) && _value.equals( that._value );
    }
  }

  @Override
  public int hashCode()
  {
    int result = _name.hashCode();
    result = 31 * result + _value.hashCode();
    return result;
  }

  @Override
  public String toString()
  {
    return "BindingProperty[name=" + _name + ", value=" + _value + "]";
  }
}
