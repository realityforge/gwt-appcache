package org.realityforge.gwt.appcache.linker;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.ConfigurationProperty;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.linker.EmittedArtifact.Visibility;
import com.google.gwt.core.ext.linker.SelectionProperty;
import com.google.gwt.core.ext.linker.impl.SelectionInformation;
import com.google.gwt.core.ext.linker.impl.StandardGeneratedResource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.realityforge.gwt.appcache.server.BindingProperty;
import org.realityforge.gwt.appcache.server.Permutation;
import org.realityforge.gwt.appcache.server.SelectionDescriptor;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class AppcacheLinkerTest
{
  @Test
  public void getConfigurationValues()
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final TreeSet<ConfigurationProperty> properties = new TreeSet<>();
    properties.add( new TestConfigurationProperty( "ba", new ArrayList<>() ) );
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
    final TreeSet<String> strings = new TreeSet<>();
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
    final ArrayList<SelectionDescriptor> bindings = new ArrayList<>();
    final ArrayList<BindingProperty> binding0 = new ArrayList<>();
    bp( binding0, "a", "z1" );
    bp( binding0, "b", "z2" );
    bindings.add( new SelectionDescriptor( "X", binding0 ) );
    final HashSet<String> values0 = linker.collectValuesForKey( bindings, "a" );
    assertEquals( values0.size(), 1 );
    assertTrue( values0.contains( "z1" ) );

    final ArrayList<BindingProperty> binding1 = new ArrayList<>();
    bp( binding1, "a", "w1" );
    bp( binding1, "b", "w2" );
    bindings.add( new SelectionDescriptor( "X", binding1 ) );

    final HashSet<String> values1 = linker.collectValuesForKey( bindings, "a" );
    assertEquals( values1.size(), 2 );
    assertTrue( values1.contains( "z1" ) );
    assertTrue( values1.contains( "w1" ) );
  }

  @Test
  public void collectPermutationSelectors()
  {
    final AppcacheLinker linker = new AppcacheLinker();

    final ArrayList<PermutationArtifact> artifacts = new ArrayList<>();
    final ArrayList<BindingProperty> binding0 = new ArrayList<>();
    bp( binding0, "user.agent", "ie8" );
    bp( binding0, "some.key", "blah" );
    bp( binding0, "other.key", "blee" );
    final Permutation permutation = addPermutation( artifacts, "X", binding0, new HashSet<>() );
    final ArrayList<BindingProperty> binding01 = new ArrayList<>();
    bp( binding01, "user.agent", "ie9" );
    bp( binding01, "some.key", "blah" );
    bp( binding01, "other.key", "blee" );
    addSoftPermutation( permutation, binding01, new HashSet<>() );
    final ArrayList<BindingProperty> binding02 = new ArrayList<>();
    bp( binding02, "user.agent", "ie10" );
    bp( binding02, "some.key", "blah" );
    bp( binding02, "other.key", "blee" );
    addSoftPermutation( permutation, binding02, new HashSet<>() );

    final ArrayList<BindingProperty> binding1 = new ArrayList<>();
    bp( binding1, "user.agent", "safari" );
    addPermutation( artifacts, "Y", binding1, new HashSet<>() );

    final ArrayList<BindingProperty> binding2 = new ArrayList<>();
    bp( binding2, "user.agent", "gecko_16" );
    addPermutation( artifacts, "Z", binding2, new HashSet<>() );

    final List<SelectionDescriptor> values = linker.collectPermutationSelectors( TreeLogger.NULL, artifacts );

    assertEquals( values.size(), 3 );

    final List<BindingProperty> x = ensureBinding( values, "X", 1 );
    assertProperty( x.get( 0 ), "user.agent", "ie10,ie9,ie8" );

    final List<BindingProperty> z = ensureBinding( values, "Z", 1 );
    assertProperty( z.get( 0 ), "user.agent", "gecko_16" );

    final List<BindingProperty> y = ensureBinding( values, "Y", 1 );
    assertProperty( y.get( 0 ), "user.agent", "safari" );
  }

  private List<BindingProperty> ensureBinding( final List<SelectionDescriptor> values,
                                               final String permutationName,
                                               final int propertyCount )
  {
    final List<BindingProperty> properties = ensureBinding( values, permutationName );
    assertEquals( properties.size(), propertyCount );
    return properties;
  }

  private void assertProperty( final BindingProperty property, final String key, final String value )
  {
    assertEquals( property.getName(), key );
    assertEquals( property.getValue(), value );
  }

  private void bp( final List<BindingProperty> properties, final String key, final String value )
  {
    properties.add( new BindingProperty( key, value ) );
  }

  private List<BindingProperty> ensureBinding( final List<SelectionDescriptor> descriptors,
                                               final String permutationName )
  {
    for ( final SelectionDescriptor descriptor : descriptors )
    {
      if ( descriptor.getPermutationName().equals( permutationName ) )
      {
        return descriptor.getBindingProperties();
      }
    }
    fail( "Unable to locate permutation: " + permutationName );
    return null;
  }

  private Permutation addPermutation( final ArrayList<PermutationArtifact> artifacts,
                                      final String permutationName,
                                      final List<BindingProperty> bindings,
                                      final HashSet<String> files )
  {
    final Permutation permutation = new Permutation( permutationName );
    addSoftPermutation( permutation, bindings, files );
    artifacts.add( new PermutationArtifact( AppcacheLinker.class, permutation ) );
    return permutation;
  }

  private void addSoftPermutation( final Permutation permutation,
                                   final List<BindingProperty> bindings,
                                   final HashSet<String> files )
  {
    permutation.getSelectors().add( new SelectionDescriptor( permutation.getPermutationName(), bindings ) );
    permutation.getPermutationFiles().addAll( files );
  }

  @Test
  public void getAllPermutationFiles()
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final ArrayList<PermutationArtifact> artifacts = new ArrayList<>();
    final HashSet<String> files1 = new HashSet<>();
    files1.add( "File1.txt" );
    addPermutation( artifacts, "X", new ArrayList<>(), files1 );
    final HashSet<String> files2 = new HashSet<>();
    files2.add( "File2.txt" );
    addPermutation( artifacts, "X", new ArrayList<>(), files2 );
    final Set<String> files = linker.getAllPermutationFiles( artifacts );
    assertEquals( files.size(), 2 );
    assertTrue( files.contains( "File1.txt" ) );
    assertTrue( files.contains( "File2.txt" ) );
  }

  @Test
  public void calculatePermutation()
    throws UnableToCompleteException
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final ArtifactSet artifacts1 = new ArtifactSet();
    artifacts1.add( new PermutationArtifact( AppcacheLinker.class, new Permutation( "1" ) ) );
    artifacts1.add( new StandardGeneratedResource( "myapp.devmode.js", new byte[ 0 ] ) );
    artifacts1.add( new StandardGeneratedResource( "file1.txt", new byte[ 0 ] ) );
    final TreeMap<String, String> configs2 = new TreeMap<>();
    configs2.put( "user.agent", "ie9" );
    configs2.put( "screen.size", "large" );
    configs2.put( "geolocationSupport", "maybe" );
    artifacts1.add( new SelectionInformation( "S2", 0, configs2 ) );
    final TreeMap<String, String> configs3 = new TreeMap<>();
    configs3.put( "user.agent", "ie9" );
    configs3.put( "screen.size", "small" );
    configs3.put( "geolocationSupport", "maybe" );
    artifacts1.add( new SelectionInformation( "S2", 1, configs3 ) );

    final LinkerContext linkerContext = mock( LinkerContext.class );
    when( linkerContext.getModuleName() ).thenReturn( "myapp" );

    final TreeSet<SelectionProperty> properties = new TreeSet<>();
    properties.add( new TestSelectionProperty( "user.agent", false ) );
    properties.add( new TestSelectionProperty( "screen.size", false ) );
    properties.add( new TestSelectionProperty( "geolocationSupport", true ) );

    when( linkerContext.getProperties() ).thenReturn( properties );

    final Permutation permutation = linker.calculatePermutation( TreeLogger.NULL, linkerContext, artifacts1 );

    assertEquals( permutation.getPermutationName(), "S2" );
    final Set<String> files = permutation.getPermutationFiles();
    assertEquals( files.size(), 1 );
    assertTrue( files.contains( "myapp/file1.txt" ) );

    final List<SelectionDescriptor> softPermutations = permutation.getSelectors();
    assertEquals( softPermutations.size(), 2 );
    final SelectionDescriptor s0 = softPermutations.get( 0 );
    final List<BindingProperty> bp0 = s0.getBindingProperties();
    final BindingProperty property01 = findProperty( "user.agent", bp0 );
    assertNotNull( property01 );
    assertEquals( property01.getValue(), "ie9" );

    final BindingProperty property02 = findProperty( "screen.size", bp0 );
    assertNotNull( property02 );
    assertEquals( property02.getValue(), "large" );

    final SelectionDescriptor s1 = softPermutations.get( 1 );
    final List<BindingProperty> bp1 = s1.getBindingProperties();
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
    artifacts1.add( new StandardGeneratedResource( "myapp.devmode.js", new byte[ 0 ] ) );
    artifacts1.add( new StandardGeneratedResource( "file1.txt", new byte[ 0 ] ) );
    final TreeMap<String, String> configs2 = new TreeMap<>();
    configs2.put( "user.agent", "ie9" );
    artifacts1.add( new SelectionInformation( "S2", 0, configs2 ) );

    final LinkerContext linkerContext = mock( LinkerContext.class );
    when( linkerContext.getModuleName() ).thenReturn( "myapp" );

    {
      final ArtifactSet artifactSet = linker.perPermutationLink( TreeLogger.NULL, linkerContext, artifacts1 );
      final SortedSet<PermutationArtifact> permutationArtifacts = artifactSet.find( PermutationArtifact.class );
      assertEquals( permutationArtifacts.size(), 1 );
      final PermutationArtifact permutationArtifact = permutationArtifacts.iterator().next();
      final Permutation permutation = permutationArtifact.getPermutation();
      assertEquals( permutation.getPermutationName(), "S2" );
      assertTrue( permutation.getPermutationFiles().contains( "myapp/file1.txt" ) );
      assertEquals( permutation.getSelectors().size(), 1 );
    }

    {
      final ArtifactSet artifactSet = linker.link( TreeLogger.NULL, linkerContext, artifacts1, true );
      final SortedSet<PermutationArtifact> permutationArtifacts = artifactSet.find( PermutationArtifact.class );
      assertEquals( permutationArtifacts.size(), 1 );
      final PermutationArtifact permutationArtifact = permutationArtifacts.iterator().next();
      final Permutation permutation = permutationArtifact.getPermutation();
      assertEquals( permutation.getPermutationName(), "S2" );
      assertTrue( permutation.getPermutationFiles().contains( "myapp/file1.txt" ) );
      assertEquals( permutation.getSelectors().size(), 1 );
    }
  }

  @Test
  public void createPermutationMap()
    throws UnableToCompleteException, IOException
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final ArrayList<PermutationArtifact> artifacts1 = new ArrayList<>();
    final Permutation permutation = new Permutation( "X" );

    artifacts1.add( new PermutationArtifact( AppcacheLinker.class, permutation ) );
    final List<BindingProperty> configs2 = new ArrayList<>();
    bp( configs2, "user.agent", "ie9" );
    permutation.getSelectors().add( new SelectionDescriptor( "X", configs2 ) );

    final LinkerContext linkerContext = mock( LinkerContext.class );
    when( linkerContext.getModuleName() ).thenReturn( "myapp" );

    final EmittedArtifact artifacts = linker.createPermutationMap( TreeLogger.NULL, artifacts1 );
    assertEquals( artifacts.getVisibility(), Visibility.Public );
    assertEquals( artifacts.getPartialPath(), "permutations.xml" );
    assertTrue( ( artifacts.getLastModified() - System.currentTimeMillis() < 1000L ) );
    final String content = toContents( artifacts );
    assertEquals( content, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                           "<permutations>\n" +
                           "<permutation name=\"X\">\n" +
                           "<user.agent>ie9</user.agent>\n" +
                           "</permutation>\n" +
                           "</permutations>\n" );
  }

  private String toContents( final EmittedArtifact artifacts )
    throws UnableToCompleteException, IOException
  {
    final InputStream contents = artifacts.getContents( TreeLogger.NULL );
    final StringBuilder sb = new StringBuilder();
    int ch;
    while ( -1 != ( ch = contents.read() ) )
    {
      sb.append( (char) ch );
    }
    return sb.toString();
  }

  private BindingProperty findProperty( final String key, final List<BindingProperty> bindingProperties )
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
    artifacts1.add( new StandardGeneratedResource( "path1", new byte[ 0 ] ) );
    artifacts1.add( new StandardGeneratedResource( "path2", new byte[ 0 ] ) );
    final StandardGeneratedResource resource =
      new StandardGeneratedResource( "path3", new byte[ 0 ] );
    resource.setVisibility( Visibility.Private );
    artifacts1.add( resource );
    artifacts1.add( new StandardGeneratedResource( "compilation-mappings.txt", new byte[ 0 ] ) );
    artifacts1.add( new StandardGeneratedResource( "myapp.devmode.js", new byte[ 0 ] ) );
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
  public void parseFallbackResources()
    throws Exception
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final HashSet<String> fallbackResources = new HashSet<>();
    fallbackResources.add( "a b" );
    fallbackResources.add( "c   d" );
    fallbackResources.add( " e   f" );
    fallbackResources.add( "g h " );

    final Map<String, String> map = linker.parseFallbackResources( TreeLogger.NULL, fallbackResources );
    assertEquals( map.get( "a" ), "b" );
    assertEquals( map.get( "c" ), "d" );
    assertEquals( map.get( "e" ), "f" );
    assertEquals( map.get( "g" ), "h" );
  }

  @Test
  public void writeManifest()
    throws Exception
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final HashSet<String> staticResources = new HashSet<>();
    staticResources.add( "index.html" );
    staticResources.add( "file with space.html" );
    final HashSet<String> cacheResources = new HashSet<>();
    cacheResources.add( "mydir/file_with_$.js" );
    cacheResources.add( "5435435435435435FDEC.js" );
    final HashMap<String, String> fallbackResources = new HashMap<>();
    fallbackResources.put( "online.png", "offline.png" );
    final String manifest = linker.writeManifest( TreeLogger.NULL, staticResources, fallbackResources, cacheResources );
    final String[] lines = manifest.split( "\n" );
    assertEquals( lines[ 0 ], "CACHE MANIFEST" );

    final int cacheSectionStart = findLine( lines, 0, lines.length, "CACHE:" );
    final int networkSectionStart = findLine( lines, cacheSectionStart + 1, lines.length, "NETWORK:" );
    final int fallbackSectionStart = findLine( lines, networkSectionStart + 1, lines.length, "FALLBACK:" );

    assertNotEquals( findLine( lines, networkSectionStart + 1, lines.length, "*" ), -1 );
    assertNotEquals( findLine( lines, cacheSectionStart + 1, networkSectionStart, "index.html" ), -1 );
    assertNotEquals( findLine( lines, cacheSectionStart + 1, networkSectionStart, "file%20with%20space.html" ), -1 );
    assertNotEquals( findLine( lines, cacheSectionStart + 1, networkSectionStart, "5435435435435435FDEC.js" ), -1 );
    assertNotEquals( findLine( lines, cacheSectionStart + 1, networkSectionStart, "mydir/file_with_%24.js" ), -1 );
    assertNotEquals( findLine( lines, fallbackSectionStart + 1, lines.length, "online.png offline.png" ), -1 );
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
