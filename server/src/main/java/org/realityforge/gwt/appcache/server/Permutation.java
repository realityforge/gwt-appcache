package org.realityforge.gwt.appcache.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Permutation
{
  public static final String PERMUTATION_MANIFEST_FILE_ENDING = ".appcache";
  private final String _permutationName;
  private final List<SelectionDescriptor> _selectors = new ArrayList<>();
  private final HashSet<String> _permutationFiles = new HashSet<>();
  private final HashMap<String, String> _fallbackFiles = new HashMap<>();

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
    if ( !( obj instanceof Permutation ) )
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

  public Map<String, String> getFallbackFiles()
  {
    return _fallbackFiles;
  }

  public List<SelectionDescriptor> getSelectors()
  {
    return _selectors;
  }
}
