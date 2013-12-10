package org.realityforge.gwt.appcache.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.realityforge.gwt.appcache.server.propertyprovider.PropertyProvider;

public abstract class AbstractManifestServlet
  extends HttpServlet
{
  private static final long serialVersionUID = -2540671294104865306L;

  // request url should be something like .../modulename.appcache" within
  // the same folder of your host page...
  private static final Pattern MODULE_PATTERN =
    Pattern.compile( "/([a-zA-Z0-9]+)\\" + Permutation.PERMUTATION_MANIFEST_FILE_ENDING + "$" );

  private transient ArrayList<PropertyProvider> _providers = new ArrayList<PropertyProvider>();
  private transient long _permutationDescriptorLastModified = Long.MIN_VALUE;
  private transient Map<String, List<BindingProperty>> _bindingMap;

  protected final void addPropertyProvider( final PropertyProvider propertyProvider )
  {
    _providers.add( propertyProvider );
  }

  @Override
  protected void doGet( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException
  {
    final String moduleName = getModuleName( request );
    final String baseUrl = getBaseUrl( request );
    final List<BindingProperty> computedBindings = calculateBindingPropertiesForClient( request );
    final String strongName = getPermutationStrongName( baseUrl, moduleName, computedBindings );
    if ( null != strongName )
    {
      final String manifest = loadManifest( baseUrl, moduleName, strongName );
      serveStringManifest( response, manifest );
    }
    else
    {
      throw new ServletException( "unknown device" );
    }
  }

  final String loadManifest( final String baseUrl, final String moduleName, final String strongName )
    throws ServletException
  {
    final String filePath = baseUrl + moduleName + "/" + strongName + Permutation.PERMUTATION_MANIFEST_FILE_ENDING;
    final String realPath = getServletContext().getRealPath( filePath );
    assert null != realPath;
    return readFile( new File( realPath ) );
  }

  final String getBaseUrl( final HttpServletRequest request )
  {
    final String base = request.getServletPath();
    // cut off module
    return base.substring( 0, base.lastIndexOf( "/" ) + 1 );
  }

  private String readFile( final File file )
    throws ServletException
  {
    final StringWriter sw = new StringWriter();
    FileReader fileReader = null;
    try
    {
      fileReader = new FileReader( file );
      final BufferedReader br = new BufferedReader( fileReader );
      String line;
      while ( null != ( line = br.readLine() ) )
      {
        sw.append( line ).append( "\n" );
      }

      return sw.toString();
    }
    catch ( final Exception e )
    {
      throw new ServletException( "error while reading manifest file", e );
    }
    finally
    {
      if ( null != fileReader )
      {
        try
        {
          fileReader.close();
        }
        catch ( final IOException ioe )
        {
          //Ignored
        }
      }
    }
  }

  final List<BindingProperty> calculateBindingPropertiesForClient( final HttpServletRequest request )
    throws ServletException
  {
    try
    {
      final List<BindingProperty> computedBindings = new ArrayList<BindingProperty>( _providers.size() );
      for ( final PropertyProvider provider : _providers )
      {
        computedBindings.add( new BindingProperty( provider.getPropertyName(), provider.getPropertyValue( request ) ) );
      }
      return computedBindings;
    }
    catch ( final Exception e )
    {
      throw new ServletException( "can not calculate properties for client", e );
    }
  }

  final void serveStringManifest( final HttpServletResponse response, final String manifest )
    throws ServletException
  {
    configureForNoCaching( response );

    response.setContentType( "text/cache-manifest" );
    try
    {
      final byte[] data = manifest.getBytes( "UTF-8" );
      response.getOutputStream().write( data );
    }
    catch ( final Exception e )
    {
      throw new ServletException( "can not write manifest to output stream", e );
    }
  }

  private void configureForNoCaching( final HttpServletResponse response )
  {
    final Date now = new Date();
    // set create date to current timestamp
    response.setDateHeader( "Date", now.getTime() );
    // set modify date to current timestamp
    response.setDateHeader( "Last-Modified", now.getTime() );
    // set expiry to back in the past (makes us a bad candidate for caching)
    response.setDateHeader( "Expires", 0 );
    // HTTP 1.1 (disable caching of any kind)
    // HTTP 1.1 'pre-check=0, post-check=0' => (Internet Explorer should always check)
    response.setHeader( "Cache-control", "no-cache, no-store, must-revalidate, pre-check=0, post-check=0" );
    response.setHeader( "Pragma", "no-cache" );
  }

  final String getPermutationStrongName( @Nonnull final String baseUrl,
                                         @Nonnull final String moduleName,
                                         @Nonnull final List<BindingProperty> computedBindings )
    throws ServletException
  {
    try
    {
      String selectedPermutation = null;
      int selectedMatchStrength = 0;
      final Map<String, List<BindingProperty>> map = getBindingMap( baseUrl, moduleName );
      for ( final Entry<String, List<BindingProperty>> permutationEntry : map.entrySet() )
      {
        int matchStrength = 0;
        boolean matched = true;
        final List<BindingProperty> requiredBindings = permutationEntry.getValue();
        for ( final BindingProperty requirement : requiredBindings )
        {
          boolean propertyMatched = false;
          for ( final BindingProperty candidate : computedBindings )
          {
            if ( requirement.getName().equals( candidate.getName() ) )
            {
              if ( requirement.matches( candidate.getValue() ) )
              {
                propertyMatched = true;
                break;
              }
            }
          }
          if ( !propertyMatched )
          {
            matched = false;
            break;
          }
          else
          {
            matchStrength = requiredBindings.size();
          }
        }
        if ( matched && matchStrength > selectedMatchStrength )
        {
          selectedPermutation = permutationEntry.getKey();
          selectedMatchStrength = matchStrength;
        }
      }
      return selectedPermutation;
    }
    catch ( final Exception e )
    {
      throw new ServletException( "can not read permutation information", e );
    }
  }

  final Map<String, List<BindingProperty>> getBindingMap( @Nonnull final String baseUrl,
                                                          @Nonnull final String moduleName )
    throws Exception
  {
    final String realPath =
      getServletContext().getRealPath( baseUrl + moduleName + "/" + PermutationsIO.PERMUTATIONS_DESCRIPTOR_FILE_NAME );
    final File permutationDescriptor = new File( realPath );
    final long lastModified = permutationDescriptor.lastModified();
    if ( null == _bindingMap || _permutationDescriptorLastModified < lastModified )
    {
      _bindingMap = PermutationsIO.deserialize( new FileInputStream( realPath ) );
      _permutationDescriptorLastModified = lastModified;
    }
    return _bindingMap;
  }

  @Nonnull
  final String getModuleName( @Nonnull final HttpServletRequest request )
    throws ServletException
  {
    final String servletPath = request.getServletPath();
    if ( null == servletPath )
    {
      throw new ServletException( "Unable to determine the servlet path." );
    }
    final Matcher matcher = MODULE_PATTERN.matcher( servletPath );
    if ( !matcher.find() )
    {
      throw new ServletException( "Unable to determine the module base from url: '" + servletPath + "'" );
    }
    return matcher.group( 1 );
  }
}
