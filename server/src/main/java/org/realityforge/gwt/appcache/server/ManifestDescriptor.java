package org.realityforge.gwt.appcache.server;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public final class ManifestDescriptor
{
  private static final String CATCH_ALL = "*";

  ///List of resources to cache
  private final List<String> _cachedResources = new ArrayList<>();
  ///List of resources that require the client to be online
  private final List<String> _networkResources = new ArrayList<>();
  ///List of fallback resources
  private final Map<String, String> _fallbackResources = new HashMap<>();

  @Nonnull
  public List<String> getCachedResources()
  {
    return _cachedResources;
  }

  @Nonnull
  public List<String> getNetworkResources()
  {
    return _networkResources;
  }

  @Nonnull
  public Map<String, String> getFallbackResources()
  {
    return _fallbackResources;
  }

  public void merge( @Nonnull final ManifestDescriptor other )
  {
    for ( final String resource : other.getCachedResources() )
    {
      if ( !_cachedResources.contains( resource ) )
      {
        _cachedResources.add( resource );
      }
    }
    for ( final String resource : other.getNetworkResources() )
    {
      if ( !_networkResources.contains( resource ) )
      {
        _networkResources.add( resource );
      }
    }
    _fallbackResources.putAll( other.getFallbackResources() );
  }

  @Override
  public String toString()
  {
    return emitManifest();
  }

  public static ManifestDescriptor parse( final String manifest )
    throws IllegalArgumentException
  {
    final String[] lines = manifest.split( "\n" );
    if ( 0 == lines.length || !lines[ 0 ].equals( "CACHE MANIFEST" ) )
    {
      throw new IllegalArgumentException( "Manifest header not present" );
    }
    final ManifestDescriptor descriptor = new ManifestDescriptor();
    final int cacheMode = 1;
    final int networkMode = 2;
    final int fallbackMode = 3;
    int mode = cacheMode;
    for ( int i = 1; i < lines.length; i++ )
    {
      final String line = lines[ i ].trim();
      if ( line.startsWith( "#" ) || 0 == line.length() )
      {
        //noinspection UnnecessaryContinue
        continue;
      }
      else if ( "CACHE:".equals( line ) )
      {
        mode = cacheMode;
      }
      else if ( "NETWORK:".equals( line ) )
      {
        mode = networkMode;
      }
      else if ( "FALLBACK:".equals( line ) )
      {
        mode = fallbackMode;
      }
      else if ( networkMode == mode )
      {
        descriptor.getNetworkResources().add( urlDecode( line ) );
      }
      else if ( cacheMode == mode )
      {
        descriptor.getCachedResources().add( urlDecode( line ) );
      }
      else
      {
        final String[] elements = line.split( " +" );
        if ( 2 != elements.length )
        {
          final String message = "Fallback line '" + line + "' should have two url paths separated by whitespace";
          throw new IllegalArgumentException( message );
        }
        descriptor.getFallbackResources().put( urlDecode( elements[ 0 ] ), urlDecode( elements[ 1 ] ) );
      }
    }
    return descriptor;
  }

  private static String urlDecode( final String line )
    throws IllegalStateException
  {
    try
    {
      return URLDecoder.decode( line, "UTF-8" );
    }
    catch ( final UnsupportedEncodingException uee )
    {
      throw new IllegalStateException( uee.getMessage(), uee );
    }
  }

  private String emitManifest()
    throws IllegalStateException
  {
    final StringBuilder sb = new StringBuilder();
    sb.append( "CACHE MANIFEST\n" );

    // It is assumed that this file is used within the context of GWT. This implies that every compile
    // will result in a set of files named after the hash of their content. Thus this file will change on
    // every compile. If this ever ceases to be the case then we will need to add a line such as;
    // sb.append( "# Compiled at " ).append( System.currentTimeMillis() ).append( "\n" );

    sb.append( "\n" );
    sb.append( "CACHE:\n" );
    for ( final String resource : _cachedResources )
    {
      sb.append( urlEncode( resource ) ).append( "\n" );
    }

    if ( !_networkResources.isEmpty() )
    {
      sb.append( "\n\n" );
      sb.append( "NETWORK:\n" );
      for ( final String resource : _networkResources )
      {
        if ( CATCH_ALL.equals( resource ) )
        {
          sb.append( CATCH_ALL ).append( "\n" );
        }
        else
        {
          sb.append( urlEncode( resource ) ).append( "\n" );
        }
      }
    }

    if ( !_fallbackResources.isEmpty() )
    {
      sb.append( "\n\n" );
      sb.append( "FALLBACK:\n" );
      for ( final Entry<String, String> entry : _fallbackResources.entrySet() )
      {
        sb.append( urlEncode( entry.getKey() ) );
        sb.append( " " );
        sb.append( urlEncode( entry.getValue() ) );
        sb.append( "\n" );
      }
    }
    return sb.toString();
  }

  private String urlEncode( final String path )
    throws IllegalStateException
  {
    final int length = path.length();
    final StringBuilder sb = new StringBuilder( length );
    for ( int i = 0; i != length; ++i )
    {
      if ( path.codePointAt( i ) > 255 )
      {
        throw new IllegalStateException( "Manifest entry '" + path + "' contains illegal character at index " + i );
      }
      final char ch = path.charAt( i );
      if ( ( ch >= '0' && ch <= '9' ) ||
           ( ch >= 'A' && ch <= 'Z' ) ||
           ( ch >= 'a' && ch <= 'z' ) ||
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
}
