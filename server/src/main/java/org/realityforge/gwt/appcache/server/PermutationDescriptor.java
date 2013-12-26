package org.realityforge.gwt.appcache.server;

import java.util.Collections;
import java.util.List;

public final class PermutationDescriptor
{
  private final String _permutationName;
  private final List<BindingProperty> _bindingProperties;

  public PermutationDescriptor( final String permutationName, final List<BindingProperty> bindingProperties )
  {
    _permutationName = permutationName;
    _bindingProperties = Collections.unmodifiableList( bindingProperties );
  }

  public int hashCode()
  {
    return _permutationName.hashCode();
  }

  public String getPermutationName()
  {
    return _permutationName;
  }

  public List<BindingProperty> getBindingProperties()
  {
    return _bindingProperties;
  }
}
