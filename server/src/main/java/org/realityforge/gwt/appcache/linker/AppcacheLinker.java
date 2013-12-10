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
import com.google.gwt.core.ext.linker.Shardable;
import com.google.gwt.core.ext.linker.impl.SelectionInformation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.realityforge.gwt.appcache.server.BindingProperty;
import org.realityforge.gwt.appcache.server.Permutation;
import org.realityforge.gwt.appcache.server.PermutationsIO;

@LinkerOrder( LinkerOrder.Order.POST )
@Shardable
public final class AppcacheLinker
  extends AbstractLinker
{
  public static final String STATIC_FILES_CONFIGURATION_PROPERTY_NAME = "appcache_static_files";
  public static final String IGNORE_CONFIGURATIONS_CONFIGURATION_PROPERTY_NAME =
    "appcache_ignorable_permutation_properties";

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
    final ArtifactSet results = new ArtifactSet( artifacts );
    final Collection<Permutation> permutations = calculatePermutations( context, artifacts );

    if ( 0 == permutations.size() )
    {
      // hosted mode
      return results;
    }

    final ArrayList<PermutationArtifact> permutationArtifacts = getPermutationArtifacts( artifacts );
    final Set<String> externalFiles = getConfigurationValues( context, STATIC_FILES_CONFIGURATION_PROPERTY_NAME );
    final Set<String> allPermutationFiles = getAllPermutationFiles( permutationArtifacts );

    // get all artifacts
    final Set<String> allArtifacts = getArtifactsForCompilation( context, artifacts );

    for ( final PermutationArtifact permutation : permutationArtifacts )
    {
      // make a copy of all artifacts
      final HashSet<String> filesForCurrentPermutation = new HashSet<String>( allArtifacts );
      // remove all permutations
      filesForCurrentPermutation.removeAll( allPermutationFiles );
      // add files of the one permutation we are interested in
      // leaving the common stuff for all permutations in...
      final Set<String> permutationFiles = permutation.getPermutation().getPermutationFiles();
      for ( final String permutationFile : permutationFiles )
      {
        if ( allArtifacts.contains( permutationFile ) )
        {
          filesForCurrentPermutation.add( permutationFile );
        }
      }

      // build manifest
      final String maniFest = writeManifest( externalFiles, filesForCurrentPermutation );
      final String filename = permutation.getPermutation().getPermutationName() + Permutation.PERMUTATION_MANIFEST_FILE_ENDING;
      results.add( emitString( logger, maniFest, filename ) );
    }

    results.add( createPermutationMap( logger, context, permutationArtifacts ) );
    return results;
  }

  private ArtifactSet perPermutationLink( final TreeLogger logger,
                                          final LinkerContext context,
                                          final ArtifactSet artifacts )
    throws UnableToCompleteException
  {
    final Collection<Permutation> permutations = calculatePermutations( context, artifacts );

    // since we are in onePermutation there should be just one
    // strongName - better make sure..
    if ( 1 != permutations.size() )
    {
      logger.log( Type.ERROR,
                  "There should be only one permutation right now, but there were: '" + permutations.size() + "'" );
      throw new UnableToCompleteException();
    }

    final ArtifactSet results = new ArtifactSet( artifacts );
    results.add( new PermutationArtifact( AppcacheLinker.class, permutations.iterator().next() ) );
    return results;
  }

  private Set<String> getAllPermutationFiles( final ArrayList<PermutationArtifact> artifacts )
  {
    final Set<String> files = new HashSet<String>();
    for ( final PermutationArtifact artifact : artifacts )
    {
      files.addAll( artifact.getPermutation().getPermutationFiles() );
    }
    return files;
  }

  private ArrayList<PermutationArtifact> getPermutationArtifacts( final ArtifactSet artifacts )
  {
    final ArrayList<PermutationArtifact> results = new ArrayList<PermutationArtifact>();
    for ( final PermutationArtifact permutationArtifact : artifacts.find( PermutationArtifact.class ) )
    {
      results.add( permutationArtifact );
    }
    return results;
  }

  private Set<String> getArtifactsForCompilation( final LinkerContext context, final ArtifactSet artifacts )
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
    return !( path.equals( "compilation-mappings.txt" ) || path.endsWith( ".devmode.js" ) );
  }

  /**
   * Write a manifest file for the given set of artifacts and return it as a
   * string
   *
   * @param staticResources - the static resources of the app, such as
   *                        index.html file
   * @param cacheResources  the gwt output artifacts like cache.html files
   * @return the manifest as a string
   */
  private String writeManifest( @Nonnull final Set<String> staticResources, @Nonnull final Set<String> cacheResources )
  {
    final StringBuilder sb = new StringBuilder();
    sb.append( "CACHE MANIFEST\n" );
    //build something unique so that the manifest file changes on recompile
    sb.append( "# Unique id #" ).
      append( ( new Date() ).getTime() ).
      append( "." ).
      append( Math.random() ).
      append( "\n" );
    sb.append( "\n" );
    sb.append( "CACHE:\n" );
    sb.append( "# Static app files\n" );
    for ( final String resources : staticResources )
    {
      sb.append( resources ).append( "\n" );
    }

    sb.append( "\n# GWT compiled files\n" );
    for ( final String resources : cacheResources )
    {
      sb.append( resources ).append( "\n" );
    }

    sb.append( "\n\n" );
    sb.append( "# All other resources require the client to be online.\n" );
    sb.append( "NETWORK:\n" );
    sb.append( "*\n" );
    return sb.toString();
  }

  private Set<String> getConfigurationValues( final LinkerContext context, final String propertyName )
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

  private EmittedArtifact createPermutationMap( final TreeLogger logger,
                                                final LinkerContext context,
                                                final Collection<PermutationArtifact> permutationArtifacts )
    throws UnableToCompleteException
  {
    try
    {
      final String string = PermutationsIO.serialize( calculateBindings( context, permutationArtifacts ) );
      return emitString( logger, string, PermutationsIO.PERMUTATIONS_DESCRIPTOR_FILE_NAME );
    }
    catch ( final Exception e )
    {
      logger.log( Type.ERROR, "can not build manifest map", e );
      throw new UnableToCompleteException();
    }
  }

  private Map<String, Set<BindingProperty>> calculateBindings( final LinkerContext context,
                                                               final Collection<PermutationArtifact> permutationArtifacts )
  {
    final Set<String> ignoreConfigs =
      getConfigurationValues( context, IGNORE_CONFIGURATIONS_CONFIGURATION_PROPERTY_NAME );
    final Map<String, Set<BindingProperty>> permutationBindings = new HashMap<String, Set<BindingProperty>>();
    for ( final PermutationArtifact permutationArtifact : permutationArtifacts )
    {
      final Permutation permutation = permutationArtifact.getPermutation();
      final HashSet<BindingProperty> calculatedBindings = new HashSet<BindingProperty>();
      permutationBindings.put( permutation.getPermutationName(), calculatedBindings );

      final Map<Integer, Set<BindingProperty>> bindings = permutation.getBindingProperties();
      final Set<BindingProperty> firstBindingProperties = bindings.values().iterator().next();
      if ( 1 == bindings.size() )
      {
        // No soft permutations
        for ( final BindingProperty b : firstBindingProperties )
        {
          calculatedBindings.add( new BindingProperty( b.getName(), b.getValue() ) );
        }
      }
      else
      {
        for ( final BindingProperty p : firstBindingProperties )
        {
          if ( ignoreConfigs.contains( p.getName() ) )
          {
            continue;
          }
          final HashSet<String> values = new HashSet<String>();
          for ( final Set<BindingProperty> properties : bindings.values() )
          {
            for ( final BindingProperty property : properties )
            {
              if ( property.getName().equals( p.getName() ) )
              {
                values.add( property.getValue() );
              }
            }
          }
          if ( values.size() > 1 )
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
            calculatedBindings.add( new BindingProperty( p.getName(), sb.toString() ) );
          }
        }
      }
    }
    return permutationBindings;
  }

  private Collection<Permutation> calculatePermutations( final LinkerContext context, final ArtifactSet artifacts )
    throws UnableToCompleteException
  {
    final HashMap<String, Permutation> map = new HashMap<String, Permutation>();
    for ( final SelectionInformation result : artifacts.find( SelectionInformation.class ) )
    {
      final String strongName = result.getStrongName();
      Permutation permutation = map.get( strongName );
      if ( null == permutation )
      {
        permutation = new Permutation( strongName );
        final Set<String> artifactsForCompilation = getArtifactsForCompilation( context, artifacts );
        permutation.getPermutationFiles().addAll( artifactsForCompilation );
        map.put( strongName, permutation );
      }
      final Map<Integer, Set<BindingProperty>> bindings = permutation.getBindingProperties();
      final int softPermutationId = result.getSoftPermutationId();
      if ( bindings.containsKey( softPermutationId ) )
      {
        throw new UnableToCompleteException();
      }
      final Set<BindingProperty> list = new HashSet<BindingProperty>();
      bindings.put( softPermutationId, list );
      for ( final Entry<String, String> entry : result.getPropMap().entrySet() )
      {
        list.add( new BindingProperty( entry.getKey(), entry.getValue() ) );
      }
    }
    return map.values();
  }
}
