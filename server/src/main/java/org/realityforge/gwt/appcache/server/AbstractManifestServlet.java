package org.realityforge.gwt.appcache.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.realityforge.gwt.appcache.linker.AppcacheLinker;
import org.realityforge.gwt.appcache.linker.XMLPermutationProvider;
import org.realityforge.gwt.appcache.server.propertyprovider.PropertyProvider;

public abstract class AbstractManifestServlet
  extends HttpServlet
{
  private static final long serialVersionUID = -2540671294104865306L;

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
    final Set<BindingProperty> computedBindings = calculateBindingPropertiesForClient( request );
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

  private String loadManifest( final String baseUrl, final String moduleName, final String strongName )
    throws ServletException
  {
    final String filePath = baseUrl + moduleName + "/" + strongName + AppcacheLinker.PERMUTATION_MANIFEST_FILE_ENDING;
    final String realPath = getServletContext().getRealPath( filePath );
    assert null != realPath;
    return readFile( new File( realPath ) );
  }

  private String getBaseUrl( final HttpServletRequest request )
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

  private Set<BindingProperty> calculateBindingPropertiesForClient( final HttpServletRequest request )
    throws ServletException
  {
    try
    {
      final Set<BindingProperty> computedBindings = new HashSet<BindingProperty>();
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

  private void serveStringManifest( final HttpServletResponse response, final String manifest )
    throws ServletException
  {
    configureForNoCaching( response );

    response.setContentType( "text/cache-manifest" );
    try
    {
      final byte[] data = manifest.getBytes( "UTF-8" );
      response.getOutputStream().write( data, 0, data.length );
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

  private String getPermutationStrongName( @Nonnull final String baseUrl,
                                           @Nonnull final String moduleName,
                                           @Nonnull final Set<BindingProperty> computedBindings )
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
              final String[] values = requirement.getValue().split( "," );
              for ( final String value : values )
              {
                if ( value.equals( candidate.getValue() ) )
                {
                  propertyMatched = true;
                  break;
                }
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

  private Map<String, List<BindingProperty>> getBindingMap( @Nonnull final String baseUrl,
                                                            @Nonnull final String moduleName )
    throws Exception
  {
    final String realPath =
      getServletContext().getRealPath( baseUrl + moduleName + "/" + AppcacheLinker.PERMUTATIONS_DESCRIPTOR_FILE_NAME );
    final File permutationDescriptor = new File( realPath );
    final long lastModified = permutationDescriptor.lastModified();
    if ( null == _bindingMap || _permutationDescriptorLastModified < lastModified )
    {
      _bindingMap = XMLPermutationProvider.deserialize( new FileInputStream( realPath ) );
      _permutationDescriptorLastModified = lastModified;
    }
    return _bindingMap;
  }

  @Nonnull
  private String getModuleName( @Nonnull final HttpServletRequest request )
    throws ServletException
  {
    // request url should be something like .../modulename.appcache" within
    // the same folder of your host page...
    final Pattern pattern =
      Pattern.compile( "/([a-zA-Z0-9]+)\\" + AppcacheLinker.PERMUTATION_MANIFEST_FILE_ENDING + "$" );
    final Matcher matcher = pattern.matcher( request.getServletPath() );
    if ( !matcher.find() )
    {
      throw new ServletException( "can not calculate module base from url: '" + request.getServletPath() + "'" );
    }

    return matcher.group( 1 );
  }
}
