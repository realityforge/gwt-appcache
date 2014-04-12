package org.realityforge.gwt.appcache.server;

import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ManifestDescriptorTest
{
  @Test
  public void merge()
    throws Exception
  {
    final ManifestDescriptor descriptor1 = new ManifestDescriptor();
    descriptor1.getCachedResources().add( "1" );
    descriptor1.getCachedResources().add( "2" );
    descriptor1.getNetworkResources().add( "A" );
    descriptor1.getNetworkResources().add( "B" );
    descriptor1.getFallbackResources().put( "W", "W2" );
    descriptor1.getFallbackResources().put( "X", "X2" );
    descriptor1.getFallbackResources().put( "Y", "Y2" );

    final ManifestDescriptor descriptor2 = new ManifestDescriptor();
    descriptor2.getCachedResources().add( "1" );
    descriptor2.getCachedResources().add( "3" );
    descriptor2.getNetworkResources().add( "A" );
    descriptor2.getNetworkResources().add( "C" );
    descriptor2.getFallbackResources().put( "X", "X3" );
    descriptor2.getFallbackResources().put( "Y", "Y2" );
    descriptor2.getFallbackResources().put( "Z", "Z2" );

    descriptor1.merge( descriptor2 );

    assertEquals( descriptor2.getCachedResources().size(), 2 );
    assertEquals( descriptor2.getNetworkResources().size(), 2 );

    final List<String> cachedResources = descriptor1.getCachedResources();
    assertEquals( cachedResources.size(), 3 );
    assertTrue( cachedResources.contains( "1" ) );
    assertTrue( cachedResources.contains( "2" ) );
    assertTrue( cachedResources.contains( "3" ) );
    final List<String> networkResources = descriptor1.getNetworkResources();
    assertEquals( networkResources.size(), 3 );
    assertTrue( networkResources.contains( "A" ) );
    assertTrue( networkResources.contains( "B" ) );
    assertTrue( networkResources.contains( "C" ) );
    final Map<String, String> fallbackResources = descriptor1.getFallbackResources();
    assertEquals( fallbackResources.size(), 4 );
    assertEquals( fallbackResources.get( "W" ), "W2" );
    assertEquals( fallbackResources.get( "X" ), "X3" );
    assertEquals( fallbackResources.get( "Y" ), "Y2" );
    assertEquals( fallbackResources.get( "Z" ), "Z2" );
  }

  @Test
  public void parse()
    throws Exception
  {
    final String manifest =
      "CACHE MANIFEST\n" +
      "# Unique id #1388291771388.0.1896967523302424\n" +
      "\n" +
      "CACHE:\n" +
      "# Static app files\n" +
      "index.html\n" +
      "./\n" +
      "\n" +
      "# GWT compiled files\n" +
      "example/7AA66459135045C9848EE97F9163D226.cache.js\n" +
      "example/example.nocache.js\n" +
      "example/bonsai%20tree.jpg\n" +
      "example/clear.cache.gif\n" +
      "\n" +
      "\n" +
      "NETWORK:\n" +
      "some/backend%20service.json\n" +
      "*\n" +
      "\n" +
      "\n" +
      "FALLBACK:\n" +
      "online%20image.png offline%20image.png\n" +
      "data.json     nodata.json\n";
    final ManifestDescriptor descriptor = ManifestDescriptor.parse( manifest );

    final List<String> networkResources = descriptor.getNetworkResources();
    assertEquals( networkResources.size(), 2 );
    assertTrue( networkResources.contains( "*" ) );
    assertTrue( networkResources.contains( "some/backend service.json" ) );

    final List<String> cachedResources = descriptor.getCachedResources();
    assertEquals( cachedResources.size(), 6 );
    assertTrue( cachedResources.contains( "index.html" ) );
    assertTrue( cachedResources.contains( "./" ) );
    assertTrue( cachedResources.contains( "example/7AA66459135045C9848EE97F9163D226.cache.js" ) );
    assertTrue( cachedResources.contains( "example/example.nocache.js" ) );
    assertTrue( cachedResources.contains( "example/bonsai tree.jpg" ) );
    assertTrue( cachedResources.contains( "example/clear.cache.gif" ) );

    final Map<String, String> fallbackResources = descriptor.getFallbackResources();
    assertEquals( fallbackResources.size(), 2 );
    assertEquals( fallbackResources.get( "online image.png" ), "offline image.png" );
    assertEquals( fallbackResources.get( "data.json" ), "nodata.json" );
  }

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

    descriptor.getFallbackResources().put( "foo.json", "foo-offline.json" );
    descriptor.getFallbackResources().put( "a/facncy $records.json", "a/no $records.json" );

    final String manifest = descriptor.toString();

    final String[] lines = manifest.split( "\n" );
    assertEquals( lines[ 0 ], "CACHE MANIFEST" );

    final int cacheSectionStart = findLine( lines, 0, lines.length, "CACHE:" );
    final int networkSectionStart = findLine( lines, cacheSectionStart + 1, lines.length, "NETWORK:" );
    final int fallbackSectionStart = findLine( lines, networkSectionStart + 1, lines.length, "FALLBACK:" );

    assertNotEquals( findLine( lines, networkSectionStart + 1, lines.length, "*" ), -1 );
    assertNotEquals( findLine( lines, networkSectionStart + 1, lines.length, "some/backend%20service.json" ), -1 );
    assertNotEquals( findLine( lines, cacheSectionStart + 1, networkSectionStart, "index.html" ), -1 );
    assertNotEquals( findLine( lines, cacheSectionStart + 1, networkSectionStart, "file%20with%20space.html" ), -1 );
    assertNotEquals( findLine( lines, cacheSectionStart + 1, networkSectionStart, "5435435435435435FDEC.js" ), -1 );
    assertNotEquals( findLine( lines, cacheSectionStart + 1, networkSectionStart, "mydir/file_with_%24.js" ), -1 );

    assertNotEquals( findLine( lines,
                               fallbackSectionStart + 1,
                               lines.length,
                               "a/facncy%20%24records.json a/no%20%24records.json" ),
                     -1 );
    assertNotEquals( findLine( lines, fallbackSectionStart + 1, lines.length, "foo.json foo-offline.json" ), -1 );
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
