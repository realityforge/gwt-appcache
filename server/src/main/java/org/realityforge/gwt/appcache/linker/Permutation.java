package org.realityforge.gwt.appcache.linker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.realityforge.gwt.appcache.server.BindingProperty;

public class Permutation
{
  private final String _permutationName;
  private final Map<Integer, Set<BindingProperty>> _bindingProperties = new HashMap<Integer, Set<BindingProperty>>();
  private final HashSet<String> _permutationFiles = new HashSet<String>();

  public Permutation( final String permutationName )
  {
    _permutationName = permutationName;
  }

  public int hashCode()
  {
    return _permutationName.hashCode();
  }

  @Override
  public boolean equals( final Object obj )
  {
   if( !(obj instanceof Permutation) )
   {
     return false;
   }
    final Permutation other = (Permutation) obj;
    return other._permutationName.equals( _permutationName );
  }

  public String getPermutationName()
  {
    return _permutationName;
  }

  public Set<String> getPermutationFiles()
  {
    return _permutationFiles;
  }

  public Map<Integer, Set<BindingProperty>> getBindingProperties()
  {
    return _bindingProperties;
  }
}
