package org.realityforge.gwt.appcache.linker;

import java.util.HashSet;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class AppcacheLinkerTest
{
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
