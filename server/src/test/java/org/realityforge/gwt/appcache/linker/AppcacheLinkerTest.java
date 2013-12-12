package org.realityforge.gwt.appcache.linker;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.ConfigurationProperty;
import com.google.gwt.core.ext.linker.impl.StandardGeneratedResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.realityforge.gwt.appcache.server.BindingProperty;
import org.realityforge.gwt.appcache.server.Permutation;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class AppcacheLinkerTest
{
  @Test
  public void getConfigurationValues()
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final TreeSet<ConfigurationProperty> properties = new TreeSet<ConfigurationProperty>();
    properties.add( new TestConfigurationProperty( "ba", new ArrayList<String>() ) );
    properties.add( new TestConfigurationProperty( "foo", Arrays.asList( "V1", "V2" ) ) );
    final LinkerContext context = mock( LinkerContext.class );
    when( context.getConfigurationProperties() ).thenReturn( properties );
    final Set<String> values = linker.getConfigurationValues( context, "foo" );
    assertTrue( values.contains( "V1" ) );
    assertTrue( values.contains( "V2" ) );
  }

  @Test
  public void joinValues()
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final TreeSet<String> strings = new TreeSet<String>();
    assertEquals( linker.joinValues( strings ), "" );
    strings.add( "a" );
    assertEquals( linker.joinValues( strings ), "a" );
    strings.add( "b" );
    strings.add( "c" );
    assertEquals( linker.joinValues( strings ), "a,b,c" );
  }

  @Test
  public void collectValuesForKey()
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final TreeMap<Integer, Set<BindingProperty>> bindings = new TreeMap<Integer, Set<BindingProperty>>();
    final HashSet<BindingProperty> binding0 = new HashSet<BindingProperty>();
    binding0.add( new BindingProperty( "a", "z1" ) );
    binding0.add( new BindingProperty( "b", "z2" ) );
    bindings.put( 0, binding0 );
    final HashSet<String> values0 = linker.collectValuesForKey( bindings, "a" );
    assertEquals( values0.size(), 1 );
    assertTrue( values0.contains( "z1" ) );

    final HashSet<BindingProperty> binding1 = new HashSet<BindingProperty>();
    binding1.add( new BindingProperty( "a", "w1" ) );
    binding1.add( new BindingProperty( "b", "w2" ) );
    bindings.put( 1, binding1 );

    final HashSet<String> values1 = linker.collectValuesForKey( bindings, "a" );
    assertEquals( values1.size(), 2 );
    assertTrue( values1.contains( "z1" ) );
    assertTrue( values1.contains( "w1" ) );
  }

  @Test
  public void collectPermutationSelectors()
  {
    final AppcacheLinker linker = new AppcacheLinker();

    final ArrayList<PermutationArtifact> artifacts = new ArrayList<PermutationArtifact>();
    final HashSet<BindingProperty> binding0 = new HashSet<BindingProperty>();
    binding0.add( new BindingProperty( "user.agent", "ie8" ) );
    binding0.add( new BindingProperty( "user.agent", "ie9" ) );
    binding0.add( new BindingProperty( "user.agent", "ie10" ) );
    addPermutation( artifacts, "X", binding0, new HashSet<String>() );

    final HashSet<BindingProperty> binding1 = new HashSet<BindingProperty>();
    binding1.add( new BindingProperty( "user.agent", "safari" ) );
    addPermutation( artifacts, "Y", binding1, new HashSet<String>() );

    final HashSet<BindingProperty> binding2 = new HashSet<BindingProperty>();
    binding2.add( new BindingProperty( "user.agent", "gecko_16" ) );
    addPermutation( artifacts, "Z", binding2, new HashSet<String>() );

    final HashSet<String> ignoreConfigs = new HashSet<String>();
    final Map<String, Set<BindingProperty>> values = linker.collectPermutationSelectors( artifacts, ignoreConfigs );

    assertEquals( values.size(), 3 );

    assertTrue( values.containsKey( "X" ) );
    final Set<BindingProperty> x = values.get( "X" );
    assertEquals( x.size(), 1 );
    final BindingProperty b_x = x.iterator().next();
    assertEquals( b_x.getName(), "user.agent" );
    assertEquals( b_x.getValue(), "ie8,ie9,ie10" );

    assertTrue( values.containsKey( "Z" ) );
    final Set<BindingProperty> z = values.get( "Z" );
    assertEquals( z.size(), 1 );
    final BindingProperty b_z = z.iterator().next();
    assertEquals( b_z.getName(), "user.agent" );
    assertEquals( b_z.getValue(), "gecko_16" );

    assertTrue( values.containsKey( "Y" ) );
    final Set<BindingProperty> y = values.get( "Y" );
    assertEquals( y.size(), 1 );
    final BindingProperty b_y = y.iterator().next();
    assertEquals( b_y.getName(), "user.agent" );
    assertEquals( b_y.getValue(), "safari" );
  }

  private void addPermutation( final ArrayList<PermutationArtifact> artifacts,
                               final String permutationName,
                               final HashSet<BindingProperty> bindings,
                               final HashSet<String> files )
  {
    final Permutation permutation = new Permutation( permutationName );
    permutation.getBindingProperties().put( 0, bindings );
    permutation.getPermutationFiles().addAll( files );
    artifacts.add( new PermutationArtifact( AppcacheLinker.class, permutation ) );
  }

  @Test
  public void getAllPermutationFiles()
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final ArrayList<PermutationArtifact> artifacts = new ArrayList<PermutationArtifact>();
    final HashSet<String> files1 = new HashSet<String>();
    files1.add( "File1.txt" );
    addPermutation( artifacts, "X", new HashSet<BindingProperty>(), files1 );
    final HashSet<String> files2 = new HashSet<String>();
    files2.add( "File2.txt" );
    addPermutation( artifacts, "X", new HashSet<BindingProperty>(), files2 );
    final Set<String> files = linker.getAllPermutationFiles( artifacts );
    assertEquals( files.size(), 2 );
    assertTrue( files.contains( "File1.txt" ) );
    assertTrue( files.contains( "File2.txt" ) );
  }

  @Test
  public void getPermutationArtifacts()
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final ArtifactSet artifacts1 = new ArtifactSet();
    final PermutationArtifact artifact1 = new PermutationArtifact( AppcacheLinker.class, new Permutation( "1" ) );
    final PermutationArtifact artifact2 = new PermutationArtifact( AppcacheLinker.class, new Permutation( "2" ) );
    artifacts1.add( artifact1 );
    artifacts1.add( new StandardGeneratedResource( Generator.class, "path1", new byte[ 0 ] ) );
    artifacts1.add( new StandardGeneratedResource( Generator.class, "path2", new byte[ 0 ] ) );
    artifacts1.add( artifact2 );
    final ArrayList<PermutationArtifact> permutationArtifacts = linker.getPermutationArtifacts( artifacts1 );
    assertEquals( permutationArtifacts.size(), 2 );
    assertTrue( permutationArtifacts.contains( artifact1 ) );
    assertTrue( permutationArtifacts.contains( artifact2 ) );
  }

  @Test
  public void writeManifest()
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final HashSet<String> staticResources = new HashSet<String>();
    staticResources.add( "index.html" );
    final HashSet<String> cacheResources = new HashSet<String>();
    cacheResources.add( "5435435435435435FDEC.js" );
    final String manifest = linker.writeManifest( staticResources, cacheResources );
    final String[] lines = manifest.split( "\n" );
    assertEquals( lines[ 0 ], "CACHE MANIFEST" );

    final int cacheSectionStart = findLine( lines, 0, lines.length, "CACHE:" );
    final int networkSectionStart = findLine( lines, cacheSectionStart + 1, lines.length, "NETWORK:" );

    assertNotEquals( findLine( lines, networkSectionStart + 1, lines.length, "*" ), -1 );
    assertNotEquals( findLine( lines, cacheSectionStart + 1, networkSectionStart, "index.html" ), -1 );
    assertNotEquals( findLine( lines, cacheSectionStart + 1, networkSectionStart, "5435435435435435FDEC.js" ), -1 );
  }

  private int findLine( final String[] lines, final int start, final int end, final String line )
  {
    for ( int i = start; i < end; i++ )
    {
      if ( lines[ i ].equals( line ) )
      {
        return i;
      }
    }
    return -1;
  }
}
