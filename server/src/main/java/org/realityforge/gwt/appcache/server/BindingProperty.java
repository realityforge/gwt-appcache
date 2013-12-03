package org.realityforge.gwt.appcache.server;

import java.io.Serializable;

public class BindingProperty
  implements Serializable
{
  private static final long serialVersionUID = -4176373787349662615L;
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
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( _name == null ) ? 0 : _name.hashCode() );
    result = prime * result + ( ( _value == null ) ? 0 : _value.hashCode() );
    return result;
  }

  @Override
  public boolean equals( Object obj )
  {
    if ( this == obj )
    {
      return true;
    }
    if ( obj == null )
    {
      return false;
    }
    if ( getClass() != obj.getClass() )
    {
      return false;
    }
    BindingProperty other = (BindingProperty) obj;
    if ( _name == null )
    {
      if ( other._name != null )
      {
        return false;
      }
    }
    else if ( !_name.equals( other._name ) )
    {
      return false;
    }
    if ( _value == null )
    {
      if ( other._value != null )
      {
        return false;
      }
    }
    else if ( !_value.equals( other._value ) )
    {
      return false;
    }
    return true;
  }

  @Override
  public String toString()
  {
    return "BindingProperty [name=" + _name + ", value=" + _value + "]";
  }
}
