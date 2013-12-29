package org.realityforge.gwt.appcache.server;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ManifestDescriptorTest
{
  @Test
  public void writeManifest()
    throws Exception
  {
    final ManifestDescriptor descriptor = new ManifestDescriptor();
    descriptor.getCachedResources().add( "index.html" );
    descriptor.getCachedResources().add( "file with space.html" );
    descriptor.getCachedResources().add( "mydir/file_with_$.js" );
    descriptor.getCachedResources().add( "5435435435435435FDEC.js" );
    descriptor.getNetworkResources().add( "some/backend service.json" );
    descriptor.getNetworkResources().add( "*" );

    final String manifest = descriptor.toString();

    final String[] lines = manifest.split( "\n" );
    assertEquals( lines[ 0 ], "CACHE MANIFEST" );

    final int cacheSectionStart = findLine( lines, 0, lines.length, "CACHE:" );
    final int networkSectionStart = findLine( lines, cacheSectionStart + 1, lines.length, "NETWORK:" );

    assertNotEquals( findLine( lines, networkSectionStart + 1, lines.length, "*" ), -1 );
    assertNotEquals( findLine( lines, networkSectionStart + 1, lines.length, "some/backend%20service.json" ), -1 );
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
