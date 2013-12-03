package org.realityforge.gwt.appcache.linker;

import com.google.gwt.core.ext.Linker;
import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.Transferable;

@Transferable
public class PermutationArtifact
  extends Artifact<PermutationArtifact>
{
  private static final long serialVersionUID = -2097933260935878782L;

  private final Permutation _permutation;

  public PermutationArtifact( final Class<? extends Linker> linker, final Permutation permutation )
  {
    super( linker );
    _permutation = permutation;
  }

  @Override
  public int hashCode()
  {
    return getPermutation().hashCode();
  }

  @Override
  protected int compareToComparableArtifact( final PermutationArtifact o )
  {
    return getPermutation().getPermutationName().compareTo( o.getPermutation().getPermutationName() );
  }

  @Override
  protected Class<PermutationArtifact> getComparableArtifactType()
  {
    return PermutationArtifact.class;
  }

  public Permutation getPermutation()
  {
    return _permutation;
  }
}
