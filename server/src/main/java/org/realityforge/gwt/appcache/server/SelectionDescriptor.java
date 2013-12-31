package org.realityforge.gwt.appcache.server;

import java.util.Collections;
import java.util.List;

/**
 * A description of a selection rule.
 * A client must match the specified binding properties and the permutation will be selected.
 */
public final class SelectionDescriptor
{
  private final String _permutationName;
  private final List<BindingProperty> _bindingProperties;

  public SelectionDescriptor( final String permutationName, final List<BindingProperty> bindingProperties )
  {
    _permutationName = permutationName;
    _bindingProperties = Collections.unmodifiableList( bindingProperties );
  }

  public String getPermutationName()
  {
    return _permutationName;
  }

  public List<BindingProperty> getBindingProperties()
  {
    return _bindingProperties;
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

    final SelectionDescriptor that = (SelectionDescriptor) o;
    return _permutationName.equals( that._permutationName ) && equalProperties( that );
  }

  private boolean equalProperties( final SelectionDescriptor that )
  {
    if ( _bindingProperties.size() != that._bindingProperties.size() )
    {
      return false;
    }
    for ( final BindingProperty property : _bindingProperties )
    {
      if ( !that._bindingProperties.contains( property ) )
      {
        return false;
      }
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = _permutationName.hashCode();
    for ( final BindingProperty property : _bindingProperties )
    {
      result = 31 * result + property.hashCode();
    }
    return result;
  }
}
