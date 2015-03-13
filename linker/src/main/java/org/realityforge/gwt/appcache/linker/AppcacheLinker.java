package org.realityforge.gwt.appcache.linker;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.AbstractLinker;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.ConfigurationProperty;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.EmittedArtifact.Visibility;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.SelectionProperty;
import com.google.gwt.core.ext.linker.Shardable;
import com.google.gwt.core.ext.linker.impl.SelectionInformation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.realityforge.gwt.appcache.server.BindingProperty;
import org.realityforge.gwt.appcache.server.ManifestDescriptor;
import org.realityforge.gwt.appcache.server.Permutation;
import org.realityforge.gwt.appcache.server.PermutationsIO;
import org.realityforge.gwt.appcache.server.SelectionDescriptor;

@LinkerOrder(LinkerOrder.Order.POST)
@Shardable
public final class AppcacheLinker
  extends AbstractLinker
{
  public static final String STATIC_FILES_CONFIGURATION_PROPERTY_NAME = "appcache_static_files";
  public static final String FALLBACK_FILES_CONFIGURATION_PROPERTY_NAME = "appcache_fallback_files";

  @Override
  public String getDescription()
  {
    return "AppcacheLinker";
  }

  @Override
  public ArtifactSet link( final TreeLogger logger,
                           final LinkerContext context,
                           final ArtifactSet artifacts,
                           final boolean onePermutation )
    throws UnableToCompleteException
  {
    if ( onePermutation )
    {
      return perPermutationLink( logger, context, artifacts );
    }
    else
    {
      return perCompileLink( logger, context, artifacts );
    }
  }

  private ArtifactSet perCompileLink( final TreeLogger logger,
                                      final LinkerContext context,
                                      final ArtifactSet artifacts )
    throws UnableToCompleteException
  {
    final ArrayList<PermutationArtifact> permutationArtifacts = getPermutationArtifacts( artifacts );
    if ( 0 == permutationArtifacts.size() )
    {
      // hosted mode
      return new ArtifactSet( artifacts );
    }

    final Set<String> allPermutationFiles = getAllPermutationFiles( permutationArtifacts );

    // get all artifacts
    final Set<String> allArtifacts = getArtifactsForCompilation( context, artifacts );

    final ArtifactSet results = new ArtifactSet( artifacts );
    for ( final PermutationArtifact permutation : permutationArtifacts )
    {
      // make a copy of all artifacts
      final HashSet<String> filesForCurrentPermutation = new HashSet<String>( allArtifacts );
      // remove all permutations
      filesForCurrentPermutation.removeAll( allPermutationFiles );
      // add files of the one permutation we are interested in
      // leaving the common stuff for all permutations in...
      for ( final String file : permutation.getPermutation().getPermutationFiles() )
      {
        if ( allArtifacts.contains( file ) )
        {
          filesForCurrentPermutation.add( file );
        }
      }

      // build manifest
      final Set<String> externalFiles = getConfigurationValues( context, STATIC_FILES_CONFIGURATION_PROPERTY_NAME );
      final Map<String, String> fallbackFiles =
        parseFallbackResources( logger, getConfigurationValues( context, FALLBACK_FILES_CONFIGURATION_PROPERTY_NAME ) );
      final String maniFest = writeManifest( logger, externalFiles, fallbackFiles, filesForCurrentPermutation );
      final String filename =
        permutation.getPermutation().getPermutationName() + Permutation.PERMUTATION_MANIFEST_FILE_ENDING;
      results.add( emitString( logger, maniFest, filename ) );
    }

    results.add( createPermutationMap( logger, permutationArtifacts ) );
    return results;
  }

  @Nonnull
  final Map<String, String> parseFallbackResources( @Nonnull final TreeLogger logger,
                                                    @Nonnull final Set<String> values )
    throws UnableToCompleteException
  {
    final HashMap<String, String> fallbackFiles = new HashMap<String, String>();
    for ( final String line : values )
    {
      final String[] elements = line.trim().split( " +" );
      if ( 2 != elements.length )
      {
        final String message = FALLBACK_FILES_CONFIGURATION_PROPERTY_NAME + " property value '" +
                               line + "' should have two url paths separated by whitespace";
        logger.log( Type.ERROR, message );
        throw new UnableToCompleteException();
      }
      fallbackFiles.put( elements[ 0 ], elements[ 1 ] );
    }

    return fallbackFiles;
  }

  final ArtifactSet perPermutationLink( final TreeLogger logger,
                                        final LinkerContext context,
                                        final ArtifactSet artifacts )
    throws UnableToCompleteException
  {
    final Permutation permutation = calculatePermutation( logger, context, artifacts );
    if ( null == permutation )
    {
      logger.log( Type.ERROR, "Unable to calculate permutation " );
      throw new UnableToCompleteException();
    }

    final ArtifactSet results = new ArtifactSet( artifacts );
    results.add( new PermutationArtifact( AppcacheLinker.class, permutation ) );
    return results;
  }

  final Set<String> getAllPermutationFiles( final ArrayList<PermutationArtifact> artifacts )
  {
    final Set<String> files = new HashSet<String>();
    for ( final PermutationArtifact artifact : artifacts )
    {
      files.addAll( artifact.getPermutation().getPermutationFiles() );
    }
    return files;
  }

  final ArrayList<PermutationArtifact> getPermutationArtifacts( final ArtifactSet artifacts )
  {
    final ArrayList<PermutationArtifact> results = new ArrayList<PermutationArtifact>();
    for ( final PermutationArtifact permutationArtifact : artifacts.find( PermutationArtifact.class ) )
    {
      results.add( permutationArtifact );
    }
    return results;
  }

  final Set<String> getArtifactsForCompilation( final LinkerContext context, final ArtifactSet artifacts )
  {
    final Set<String> artifactNames = new HashSet<String>();
    for ( final EmittedArtifact artifact : artifacts.find( EmittedArtifact.class ) )
    {
      if ( Visibility.Public == artifact.getVisibility() && shouldAddToManifest( artifact.getPartialPath() ) )
      {
        artifactNames.add( context.getModuleName() + "/" + artifact.getPartialPath() );
      }
    }
    return artifactNames;
  }

  private boolean shouldAddToManifest( final String path )
  {
    return !( path.equals( "compilation-mappings.txt" ) || path.endsWith( ".devmode.js" ) ||
            path.endsWith( ".cache.js.gz" ));
  }

  /**
   * Write a manifest file for the given set of artifacts and return it as a
   * string
   *
   * @param staticResources - the static resources of the app, such as
   *                        index.html file
   * @param fallbackResources  the fall back files to add to the manifest.
   * @param cacheResources  the gwt output artifacts like cache.html files
   * @return the manifest as a string
   */
  final String writeManifest( final TreeLogger logger,
                              @Nonnull final Set<String> staticResources,
                              @Nonnull final Map<String, String> fallbackResources,
                              @Nonnull final Set<String> cacheResources )
    throws UnableToCompleteException
  {
    final ManifestDescriptor descriptor = new ManifestDescriptor();
    descriptor.getCachedResources().addAll( staticResources );
    descriptor.getCachedResources().addAll( cacheResources );
    descriptor.getFallbackResources().putAll( fallbackResources );
    Collections.sort( descriptor.getCachedResources() );
    descriptor.getNetworkResources().add( "*" );
    try
    {
      return descriptor.toString();
    }
    catch ( final Exception e )
    {
      logger.log( Type.ERROR, "Error generating manifest: " + e, e );
      throw new UnableToCompleteException();
    }
  }

  @Nonnull
  final Set<String> getConfigurationValues( @Nonnull final LinkerContext context, @Nonnull final String propertyName )
  {
    final HashSet<String> set = new HashSet<String>();
    final SortedSet<ConfigurationProperty> properties = context.getConfigurationProperties();
    for ( final ConfigurationProperty configurationProperty : properties )
    {
      if ( propertyName.equals( configurationProperty.getName() ) )
      {
        for ( final String value : configurationProperty.getValues() )
        {
          set.add( value );
        }
      }
    }

    return set;
  }

  final EmittedArtifact createPermutationMap( final TreeLogger logger,
                                              final Collection<PermutationArtifact> artifacts )
    throws UnableToCompleteException
  {
    try
    {
      final String string = PermutationsIO.serialize( collectPermutationSelectors( logger, artifacts ) );
      return emitString( logger, string, PermutationsIO.PERMUTATIONS_DESCRIPTOR_FILE_NAME );
    }
    catch ( final Exception e )
    {
      logger.log( Type.ERROR, "can not build manifest map", e );
      throw new UnableToCompleteException();
    }
  }

  final List<SelectionDescriptor> collectPermutationSelectors( final TreeLogger logger,
                                                                 final Collection<PermutationArtifact> artifacts )
  {
    final List<SelectionDescriptor> descriptors = new ArrayList<SelectionDescriptor>();
    for ( final PermutationArtifact artifact : artifacts )
    {
      final Permutation permutation = artifact.getPermutation();
      final List<BindingProperty> calculatedBindings = new ArrayList<BindingProperty>();
      final HashSet<String> completed = new HashSet<String>();

      final List<SelectionDescriptor> selectors = permutation.getSelectors();
      final SelectionDescriptor firstSelector = selectors.iterator().next();
      for ( final BindingProperty p : firstSelector.getBindingProperties() )
      {
        final String key = p.getName();
        if ( !completed.contains( key ) )
        {
          final HashSet<String> values = collectValuesForKey( selectors, key );
          if ( 1 == selectors.size() || values.size() > 1 )
          {
            calculatedBindings.add( new BindingProperty( key, joinValues( values ) ) );
          }
          completed.add( key );
        }
      }
      Collections.sort( calculatedBindings, new Comparator<BindingProperty>()
      {
        @Override
        public int compare( final BindingProperty o1, final BindingProperty o2 )
        {
          return o2.getComponents().length - o1.getComponents().length;
        }
      } );
      descriptors.add( new SelectionDescriptor( permutation.getPermutationName(), calculatedBindings ) );
    }
    logger.log( Type.DEBUG, "Permutation map created with " + descriptors.size() + " descriptors." );
    return descriptors;
  }

  final HashSet<String> collectValuesForKey( final List<SelectionDescriptor> selectors, final String key )
  {
    final HashSet<String> values = new HashSet<String>();
    for ( final SelectionDescriptor selector : selectors )
    {
      for ( final BindingProperty property : selector.getBindingProperties() )
      {
        if ( property.getName().equals( key ) )
        {
          values.add( property.getValue() );
        }
      }
    }
    return values;
  }

  final String joinValues( final Set<String> values )
  {
    final StringBuilder sb = new StringBuilder();
    for ( final String value : values )
    {
      if ( 0 != sb.length() )
      {
        sb.append( "," );
      }
      sb.append( value );
    }
    return sb.toString();
  }

  /**
   * Return the permutation for a single link step.
   */
  final Permutation calculatePermutation( final TreeLogger logger,
                                          final LinkerContext context,
                                          final ArtifactSet artifacts )
    throws UnableToCompleteException
  {
    Permutation permutation = null;

    for ( final SelectionInformation result : artifacts.find( SelectionInformation.class ) )
    {
      final String strongName = result.getStrongName();
      if ( null != permutation && !permutation.getPermutationName().equals( strongName ) )
      {
        throw new UnableToCompleteException();
      }
      if ( null == permutation )
      {
        permutation = new Permutation( strongName );
        final Set<String> artifactsForCompilation = getArtifactsForCompilation( context, artifacts );
        permutation.getPermutationFiles().addAll( artifactsForCompilation );
      }
      final List<BindingProperty> list = new ArrayList<BindingProperty>();
      for ( final SelectionProperty property : context.getProperties() )
      {
        if ( !property.isDerived() )
        {
          final String name = property.getName();
          final String value = result.getPropMap().get( name );
          if ( null != value )
          {
            list.add( new BindingProperty( name, value ) );
          }
        }
      }
      final SelectionDescriptor selection = new SelectionDescriptor( strongName, list );
      final List<SelectionDescriptor> selectors = permutation.getSelectors();
      if ( !selectors.contains( selection ) )
      {
        selectors.add( selection );
      }
    }
    if ( null != permutation )
    {
      logger.log( Type.DEBUG, "Calculated Permutation: " + permutation.getPermutationName() +
                              " Selectors: " + permutation.getSelectors() );
    }
    return permutation;
  }
}
