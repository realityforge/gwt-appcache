package org.realityforge.gwt.appcache.linker;

import com.google.gwt.core.ext.Linker;
import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.Transferable;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.realityforge.gwt.appcache.server.Permutation;

@Transferable
public class PermutationArtifact
  extends Artifact<PermutationArtifact>
{
  private static final long serialVersionUID = -2097933260935878782L;

  @Nonnull
  private final Permutation _permutation;

  PermutationArtifact( @Nonnull final Class<? extends Linker> linker, @Nonnull final Permutation permutation )
  {
    super( linker );
    _permutation = Objects.requireNonNull( permutation );
  }

  @Override
  public int hashCode()
  {
    return getPermutation().hashCode();
  }

  @Override
  protected int compareToComparableArtifact( @Nonnull final PermutationArtifact o )
  {
    return getPermutation().getPermutationName().compareTo( o.getPermutation().getPermutationName() );
  }

  @Nonnull
  @Override
  protected Class<PermutationArtifact> getComparableArtifactType()
  {
    return PermutationArtifact.class;
  }

  @Nonnull
  Permutation getPermutation()
  {
    return _permutation;
  }
}
