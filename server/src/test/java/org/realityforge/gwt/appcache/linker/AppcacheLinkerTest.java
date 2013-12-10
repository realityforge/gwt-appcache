package org.realityforge.gwt.appcache.linker;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.linker.ConfigurationProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
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
  public void writeManifest()
  {
    final AppcacheLinker linker = new AppcacheLinker();
    final HashSet<String> staticResources = new HashSet<String>();
    staticResources.add( "index.html" );
    final HashSet<String> cacheResources = new HashSet<String>();
    staticResources.add( "5435435435435435FDEC.js" );
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
