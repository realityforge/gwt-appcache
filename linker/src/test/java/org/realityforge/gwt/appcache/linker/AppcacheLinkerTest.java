package org.realityforge.gwt.appcache.linker;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.ConfigurationProperty;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.EmittedArtifact.Visibility;
import com.google.gwt.core.ext.linker.impl.SelectionInformation;
import com.google.gwt.core.ext.linker.impl.StandardGeneratedResource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
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
    binding0.add( new BindingProperty( "some.key", "blah" ) );
    binding0.add( new BindingProperty( "other.key", "blee" ) );
    final Permutation permutation = addPermutation( artifacts, "X", 0, binding0, new HashSet<String>() );
    final HashSet<BindingProperty> binding01 = new HashSet<BindingProperty>();
    binding01.add( new BindingProperty( "user.agent", "ie9" ) );
    binding01.add( new BindingProperty( "some.key", "blah" ) );
    binding01.add( new BindingProperty( "other.key", "blee" ) );
    addSoftPermutation( permutation, 1, binding01, new HashSet<String>() );
    final HashSet<BindingProperty> binding02 = new HashSet<BindingProperty>();
    binding02.add( new BindingProperty( "user.agent", "ie10" ) );
    binding02.add( new BindingProperty( "some.key", "blah" ) );
    binding02.add( new BindingProperty( "other.key", "blee" ) );
    addSoftPermutation( permutation, 2, binding02, new HashSet<String>() );

    final HashSet<BindingProperty> binding1 = new HashSet<BindingProperty>();
    binding1.add( new BindingProperty( "user.agent", "safari" ) );
    addPermutation( artifacts, "Y", 0, binding1, new HashSet<String>() );

    final HashSet<BindingProperty> binding2 = new HashSet<BindingProperty>();
    binding2.add( new BindingProperty( "user.agent", "gecko_16" ) );
    addPermutation( artifacts, "Z", 0, binding2, new HashSet<String>() );

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

  private Permutation addPermutation( final ArrayList<PermutationArtifact> artifacts,
                               final String permutationName,
                               final int permutationIndex,
                               final HashSet<BindingProperty> bindings,
                               final HashSet<String> files )
  {
    final Permutation permutation = new Permutation( permutationName );
    addSoftPermutation( permutation, permutationIndex, bindings, files );
    artifacts.add( new PermutationArtifact( AppcacheLinker.class, permutation ) );
    return permutation;
  }

  private void addSoftPermutation( final Permutation permutation,
                                   final int permutationIndex,
                                   final HashSet<BindingProperty> bindings, final HashSet<String> files )
  {
    permutation.getBindingProperties().put( permutationIndex, bindings );
    permutation.getPermutationFiles().addAll( files );
  }

  @Test
  public void getAllPermutationFiles()
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final ArrayList<PermutationArtifact> artifacts = new ArrayList<PermutationArtifact>();
    final HashSet<String> files1 = new HashSet<String>();
    files1.add( "File1.txt" );
    addPermutation( artifacts, "X", 0, new HashSet<BindingProperty>(), files1 );
    final HashSet<String> files2 = new HashSet<String>();
    files2.add( "File2.txt" );
    addPermutation( artifacts, "X", 0, new HashSet<BindingProperty>(), files2 );
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
  public void calculatePermutation()
    throws UnableToCompleteException
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final ArtifactSet artifacts1 = new ArtifactSet();
    artifacts1.add( new PermutationArtifact( AppcacheLinker.class, new Permutation( "1" ) ) );
    artifacts1.add( new StandardGeneratedResource( Generator.class, "myapp.devmode.js", new byte[ 0 ] ) );
    artifacts1.add( new StandardGeneratedResource( Generator.class, "file1.txt", new byte[ 0 ] ) );
    final TreeMap<String, String> configs2 = new TreeMap<String, String>();
    configs2.put( "user.agent", "ie9" );
    configs2.put( "screen.size", "large" );
    artifacts1.add( new SelectionInformation( "S2", 0, configs2 ) );
    final TreeMap<String, String> configs3 = new TreeMap<String, String>();
    configs3.put( "user.agent", "ie9" );
    configs3.put( "screen.size", "small" );
    artifacts1.add( new SelectionInformation( "S2", 1, configs3 ) );

    final LinkerContext linkerContext = mock( LinkerContext.class );
    when( linkerContext.getModuleName() ).thenReturn( "myapp" );
    final Permutation permutation = linker.calculatePermutation( linkerContext, artifacts1 );

    assertEquals( permutation.getPermutationName(), "S2" );
    final Set<String> files = permutation.getPermutationFiles();
    assertEquals( files.size(), 1 );
    assertTrue( files.contains( "myapp/file1.txt" ) );

    final Map<Integer, Set<BindingProperty>> softPermutations = permutation.getBindingProperties();
    assertEquals( softPermutations.size(), 2 );
    final Set<BindingProperty> bp0 = softPermutations.get( 0 );
    final BindingProperty property01 = findProperty( "user.agent", bp0 );
    assertNotNull( property01 );
    assertEquals( property01.getValue(), "ie9" );

    final BindingProperty property02 = findProperty( "screen.size", bp0 );
    assertNotNull( property02 );
    assertEquals( property02.getValue(), "large" );

    final Set<BindingProperty> bp1 = softPermutations.get( 1 );
    final BindingProperty property11 = findProperty( "user.agent", bp1 );
    assertNotNull( property11 );
    assertEquals( property11.getValue(), "ie9" );

    final BindingProperty property12 = findProperty( "screen.size", bp1 );
    assertNotNull( property12 );
    assertEquals( property12.getValue(), "small" );
  }

  @Test
  public void perPermutationLink()
    throws UnableToCompleteException
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final ArtifactSet artifacts1 = new ArtifactSet();
    artifacts1.add( new StandardGeneratedResource( Generator.class, "myapp.devmode.js", new byte[ 0 ] ) );
    artifacts1.add( new StandardGeneratedResource( Generator.class, "file1.txt", new byte[ 0 ] ) );
    final TreeMap<String, String> configs2 = new TreeMap<String, String>();
    configs2.put( "user.agent", "ie9" );
    artifacts1.add( new SelectionInformation( "S2", 0, configs2 ) );

    final LinkerContext linkerContext = mock( LinkerContext.class );
    when( linkerContext.getModuleName() ).thenReturn( "myapp" );

    {
      final ArtifactSet artifactSet = linker.perPermutationLink( null, linkerContext, artifacts1 );
      final SortedSet<PermutationArtifact> permutationArtifacts = artifactSet.find( PermutationArtifact.class );
      assertEquals( permutationArtifacts.size(), 1 );
      final PermutationArtifact permutationArtifact = permutationArtifacts.iterator().next();
      final Permutation permutation = permutationArtifact.getPermutation();
      assertEquals( permutation.getPermutationName(), "S2" );
      assertTrue( permutation.getPermutationFiles().contains( "myapp/file1.txt" ) );
      assertEquals( permutation.getBindingProperties().size(), 1 );
    }

    {
      final ArtifactSet artifactSet = linker.link( null, linkerContext, artifacts1, true );
      final SortedSet<PermutationArtifact> permutationArtifacts = artifactSet.find( PermutationArtifact.class );
      assertEquals( permutationArtifacts.size(), 1 );
      final PermutationArtifact permutationArtifact = permutationArtifacts.iterator().next();
      final Permutation permutation = permutationArtifact.getPermutation();
      assertEquals( permutation.getPermutationName(), "S2" );
      assertTrue( permutation.getPermutationFiles().contains( "myapp/file1.txt" ) );
      assertEquals( permutation.getBindingProperties().size(), 1 );
    }
  }

  @Test
  public void createPermutationMap()
    throws UnableToCompleteException, IOException
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final ArrayList<PermutationArtifact> artifacts1 = new ArrayList<PermutationArtifact>();
    final Permutation permutation = new Permutation( "X" );

    artifacts1.add( new PermutationArtifact( AppcacheLinker.class, permutation ) );
    final HashSet<BindingProperty> configs2 = new HashSet<BindingProperty>();
    configs2.add( new BindingProperty( "user.agent", "ie9" ) );
    permutation.getBindingProperties().put( 0, configs2 );

    final LinkerContext linkerContext = mock( LinkerContext.class );
    when( linkerContext.getModuleName() ).thenReturn( "myapp" );

    final EmittedArtifact artifacts = linker.createPermutationMap( null, linkerContext, artifacts1 );
    assertEquals( artifacts.getVisibility(), Visibility.Public );
    assertEquals( artifacts.getPartialPath(), "permutations.xml" );
    assertTrue( ( artifacts.getLastModified() - System.currentTimeMillis() < 1000L ) );
    final String content = toContents( artifacts );
    assertEquals( content, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><permutations>\n" +
                          "<permutation name=\"X\">\n" +
                          "<user.agent>ie9</user.agent>\n" +
                          "</permutation>\n" +
                          "</permutations>\n" );
  }

  private String toContents( final EmittedArtifact artifacts )
    throws UnableToCompleteException, IOException
  {
    final InputStream contents = artifacts.getContents( TreeLogger.NULL );
    final StringBuilder sb = new StringBuilder(  );
    int ch;
    while(-1 != (ch=contents.read()))
    {
      sb.append( (char) ch );
    }
    return sb.toString();
  }

  private BindingProperty findProperty( final String key, final Set<BindingProperty> bindingProperties )
  {
    for ( final BindingProperty p : bindingProperties )
    {
      if ( p.getName().equals( key ) )
      {
        return p;
      }
    }
    return null;
  }

  @Test
  public void getArtifactsForCompilation()
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final ArtifactSet artifacts1 = new ArtifactSet();
    final PermutationArtifact artifact1 = new PermutationArtifact( AppcacheLinker.class, new Permutation( "1" ) );
    final PermutationArtifact artifact2 = new PermutationArtifact( AppcacheLinker.class, new Permutation( "2" ) );
    artifacts1.add( artifact1 );
    artifacts1.add( new StandardGeneratedResource( Generator.class, "path1", new byte[ 0 ] ) );
    artifacts1.add( new StandardGeneratedResource( Generator.class, "path2", new byte[ 0 ] ) );
    final StandardGeneratedResource resource =
      new StandardGeneratedResource( Generator.class, "path3", new byte[ 0 ] );
    resource.setVisibility( Visibility.Private );
    artifacts1.add( resource );
    artifacts1.add( new StandardGeneratedResource( Generator.class, "compilation-mappings.txt", new byte[ 0 ] ) );
    artifacts1.add( new StandardGeneratedResource( Generator.class, "myapp.devmode.js", new byte[ 0 ] ) );
    artifacts1.add( artifact2 );
    final LinkerContext linkerContext = mock( LinkerContext.class );
    when( linkerContext.getModuleName() ).thenReturn( "myapp" );
    final Set<String> files =
      linker.getArtifactsForCompilation( linkerContext, artifacts1 );
    assertEquals( files.size(), 2 );
    assertTrue( files.contains( "myapp/path1" ) );
    assertTrue( files.contains( "myapp/path2" ) );
  }

  @Test
  public void writeManifest()
    throws Exception
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final HashSet<String> staticResources = new HashSet<String>();
    staticResources.add( "index.html" );
    staticResources.add( "file with space.html" );
    final HashSet<String> cacheResources = new HashSet<String>();
    cacheResources.add( "mydir/file_with_$.js" );
    cacheResources.add( "5435435435435435FDEC.js" );
    final String manifest = linker.writeManifest( TreeLogger.NULL, staticResources, cacheResources );
    final String[] lines = manifest.split( "\n" );
    assertEquals( lines[ 0 ], "CACHE MANIFEST" );

    final int cacheSectionStart = findLine( lines, 0, lines.length, "CACHE:" );
    final int networkSectionStart = findLine( lines, cacheSectionStart + 1, lines.length, "NETWORK:" );

    assertNotEquals( findLine( lines, networkSectionStart + 1, lines.length, "*" ), -1 );
    assertNotEquals( findLine( lines, cacheSectionStart + 1, networkSectionStart, "index.html" ), -1 );
    assertNotEquals( findLine( lines, cacheSectionStart + 1, networkSectionStart, "file%20with%20space.html" ), -1 );
    assertNotEquals( findLine( lines, cacheSectionStart + 1, networkSectionStart, "5435435435435435FDEC.js" ), -1 );
    assertNotEquals( findLine( lines, cacheSectionStart + 1, networkSectionStart, "mydir/file_with_%24.js" ), -1 );
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
