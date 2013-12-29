package org.realityforge.gwt.appcache.server;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public final class ManifestDescriptor
{
  private static final String CATCH_ALL = "*";

  ///List of resources to cache
  private final List<String> _cachedResources = new ArrayList<String>();
  ///List of resources that require the client to be online
  private final List<String> _networkResources = new ArrayList<String>();

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

  @Override
  public String toString()
  {
    return emitManifest();
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

    sb.append( "\n\n" );
    sb.append( "# All other resources require the client to be online.\n" );
    sb.append( "NETWORK:\n" );
    for ( final String resource : _networkResources )
    {
      if( CATCH_ALL.equals( resource ))
      {
        sb.append( CATCH_ALL ).append( "\n" );
      }
      else
      {
      sb.append( urlEncode( resource ) ).append( "\n" );
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
}
