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
    final ArrayList<PermutationArtifact> permutationArtifacts = getPermutationArtifacts( artifacts );
    if ( 0 == permutationArtifacts.size() )
    {
      // hosted mode
      return new ArtifactSet( artifacts );
    }

    final Set<String> externalFiles = getConfigurationValues( context, STATIC_FILES_CONFIGURATION_PROPERTY_NAME );
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
      final String maniFest = writeManifest( logger, externalFiles, filesForCurrentPermutation );
      final String filename =
        permutation.getPermutation().getPermutationName() + Permutation.PERMUTATION_MANIFEST_FILE_ENDING;
      results.add( emitString( logger, maniFest, filename ) );
    }

    results.add( createPermutationMap( logger, context, permutationArtifacts ) );
    return results;
  }

  final ArtifactSet perPermutationLink( final TreeLogger logger,
                                        final LinkerContext context,
                                        final ArtifactSet artifacts )
    throws UnableToCompleteException
  {
    final Permutation permutation = calculatePermutation( context, artifacts );
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
  final String writeManifest( final TreeLogger logger, @Nonnull final Set<String> staticResources, @Nonnull final Set<String> cacheResources )
    throws UnableToCompleteException
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
    for ( final String resource : staticResources )
    {
      sb.append( urlEncode( logger, resource ) ).append( "\n" );
    }

    sb.append( "\n# GWT compiled files\n" );
    for ( final String resource : cacheResources )
    {
      sb.append( urlEncode( logger, resource ) ).append( "\n" );
    }

    sb.append( "\n\n" );
    sb.append( "# All other resources require the client to be online.\n" );
    sb.append( "NETWORK:\n" );
    sb.append( "*\n" );
    return sb.toString();
  }

  private String urlEncode( final TreeLogger logger, final String path )
    throws UnableToCompleteException
  {
    final int length = path.length();
    final StringBuilder sb = new StringBuilder( length );
    for ( int i = 0; i != length; ++i )
    {
      if ( path.codePointAt( i ) > 255 )
      {
        logger.log( TreeLogger.Type.ERROR, "Manifest entry '" + path + "' contains illegal character at index " + i );
        throw new UnableToCompleteException();
      }
      final char ch = path.charAt( i );
      if ( ( ch >= '0' && ch <= '9' ) ||
           ( ch >= 'A' && ch <= 'Z' ) ||
           ( ch >= 'a' && ch < 'z' ) ||
           '.' == ch ||
           '-' == ch ||
           '_' == ch )
      {
        sb.append( ch );
      }
      else if ( '/' == ch || '\\' == ch )
      {
        sb.append( '/' );
      }
      else
      {
        sb.append( '%' ).append( Integer.toHexString( ch ).toUpperCase() );
      }
    }
    return sb.toString();
  }

  final Set<String> getConfigurationValues( final LinkerContext context, final String propertyName )
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
                                              final LinkerContext context,
                                              final Collection<PermutationArtifact> artifacts )
    throws UnableToCompleteException
  {
    try
    {
      final String string = PermutationsIO.serialize( collectPermutationSelectors( context, artifacts ) );
      return emitString( logger, string, PermutationsIO.PERMUTATIONS_DESCRIPTOR_FILE_NAME );
    }
    catch ( final Exception e )
    {
      logger.log( Type.ERROR, "can not build manifest map", e );
      throw new UnableToCompleteException();
    }
  }

  private Map<String, Set<BindingProperty>> collectPermutationSelectors( final LinkerContext context,
                                                                         final Collection<PermutationArtifact> artifacts )
  {
    final Set<String> ignoreConfigs =
      getConfigurationValues( context, IGNORE_CONFIGURATIONS_CONFIGURATION_PROPERTY_NAME );
    return collectPermutationSelectors( artifacts, ignoreConfigs );
  }

  final Map<String, Set<BindingProperty>> collectPermutationSelectors( final Collection<PermutationArtifact> artifacts,
                                                                       final Set<String> ignoreConfigs )
  {
    final Map<String, Set<BindingProperty>> permutationBindings = new HashMap<String, Set<BindingProperty>>();
    for ( final PermutationArtifact artifact : artifacts )
    {
      final Permutation permutation = artifact.getPermutation();
      final HashSet<BindingProperty> calculatedBindings = new HashSet<BindingProperty>();
      final HashSet<String> completed = new HashSet<String>();
      permutationBindings.put( permutation.getPermutationName(), calculatedBindings );

      final Map<Integer, Set<BindingProperty>> bindings = permutation.getBindingProperties();
      final Set<BindingProperty> firstBindingProperties = bindings.values().iterator().next();
      for ( final BindingProperty p : firstBindingProperties )
      {
        final String key = p.getName();
        if ( !ignoreConfigs.contains( key ) && !completed.contains( key ) )
        {
          final HashSet<String> values = collectValuesForKey( bindings, key );
          if ( 1 == bindings.size() || values.size() > 1 )
          {
            calculatedBindings.add( new BindingProperty( key, joinValues( values ) ) );
          }
          completed.add( key );
        }
      }
    }
    return permutationBindings;
  }

  final HashSet<String> collectValuesForKey( final Map<Integer, Set<BindingProperty>> bindings, final String key )
  {
    final HashSet<String> values = new HashSet<String>();
    for ( final Set<BindingProperty> properties : bindings.values() )
    {
      for ( final BindingProperty property : properties )
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
  final Permutation calculatePermutation( final LinkerContext context, final ArtifactSet artifacts )
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
    return permutation;
  }
}
