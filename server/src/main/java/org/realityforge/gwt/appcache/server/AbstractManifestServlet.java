package org.realityforge.gwt.appcache.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.realityforge.gwt.appcache.server.propertyprovider.PropertyProvider;

/**
 * Base class for servlet the does server-side permutation selection and serves the correct appcache manifest.
 * <p/>
 * The servlet calls a chain of PropertyProvider instances to derive properties that are used to select the
 * appropriate manifest at runtime. Sometimes, certain properties can not be determined definitively except on
 * client-side and thus the servlet may need to serve up a manifest that merges appcache manifests for multiple
 * permutations so that the selection can occur on the client-side.
 */
public abstract class AbstractManifestServlet
  extends HttpServlet
{
  private static final Logger LOG = Logger.getLogger( AbstractManifestServlet.class.getName() );

  static final String DISABLE_MANIFEST_COOKIE_NAME = "appcache_disable";
  static final String DISABLE_MANIFEST_COOKIE_VALUE = "1";

  private static final long serialVersionUID = -2540671294104865306L;

  // request url should be something like .../modulename.appcache" within
  // the same folder of your host page...
  private static final Pattern MODULE_PATTERN =
    Pattern.compile( "/([a-zA-Z0-9]+)\\" + Permutation.PERMUTATION_MANIFEST_FILE_ENDING + "$" );

  private transient ArrayList<PropertyProvider> _providers = new ArrayList<PropertyProvider>();
  private transient ArrayList<String> _clientSideSelectionProperties = new ArrayList<String>();
  private transient long _permutationsDescriptorLastModified = Long.MIN_VALUE;
  private transient List<SelectionDescriptor> _selectionDescriptors;
  private transient Map<String, String> _cache;
  private transient boolean _enableCache;

  protected AbstractManifestServlet()
  {
    enableCache();
  }

  protected final void addPropertyProvider( final PropertyProvider propertyProvider )
  {
    _providers.add( propertyProvider );
  }

  protected final boolean isCacheEnabled()
  {
    return _enableCache;
  }

  protected final void enableCache()
  {
    if ( null == _cache )
    {
      _cache = new HashMap<String, String>();
    }
    _enableCache = true;
  }

  protected final void disableCache()
  {
    _enableCache = false;
    _cache = null;
  }

  /**
   * Specify that a property with the specified name may need to be ultimately determined on the
   * client-side.
   */
  protected final void addClientSideSelectionProperty( final String propertyName )
  {
    _clientSideSelectionProperties.add( propertyName );
  }

  @Override
  protected void doGet( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException
  {
    // If the application has sent a cookie to disable the application cache
    // then return an appropriate  gone response to flush the cache
    if ( isAppCacheDisabled( request ) )
    {
      // Maybe should be HttpServletResponse.SC_NOT_FOUND ?
      response.sendError( HttpServletResponse.SC_GONE );
      return;
    }

    try
    {
      final String moduleName = getModuleName( request );
      final String baseUrl = getBaseUrl( request );
      final List<BindingProperty> computedBindings = calculateBindingPropertiesForClient( request );
      final String[] permutations = selectPermutations( baseUrl, moduleName, computedBindings );
      if ( null != permutations )
      {
        final String manifest = 1 == permutations.length ?
                                loadManifest( baseUrl, moduleName, permutations[ 0 ] ) :
                                loadAndMergeManifests( baseUrl, moduleName, permutations );
        serveStringManifest( response, manifest );
      }
      else if ( !handleUnmatchedRequest( request, response, moduleName, baseUrl, computedBindings ) )
      {
        response.sendError( HttpServletResponse.SC_NOT_FOUND );
      }
    }
    catch ( final ServletException se )
    {
      LOG.log( Level.WARNING, "Error generating response for manifest", se );
      response.sendError( HttpServletResponse.SC_NOT_FOUND );
    }
  }

  /**
   * Sub-classes can override this method to perform handle the inability to find a matching permutation. In which
   * case the method should return true if the response has been handled.
   */
  protected boolean handleUnmatchedRequest( final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final String moduleName,
                                            final String baseUrl,
                                            final List<BindingProperty> computedBindings )
    throws ServletException, IOException
  {
    return false;
  }

  protected boolean isAppCacheDisabled( final HttpServletRequest request )
  {
    final Cookie[] cookies = request.getCookies();
    if ( null != cookies )
    {
      for ( final Cookie cookie : cookies )
      {
        if ( DISABLE_MANIFEST_COOKIE_NAME.equals( cookie.getName() ) &&
             DISABLE_MANIFEST_COOKIE_VALUE.equals( cookie.getValue() ) )
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Utility method that loads and merges the cache files for multiple permutations.
   * This is useful when it is not possible to determine the exact permutation required
   * for a client on the server. This defers the decision to the client but may result in
   * extra files being downloaded.
   */
  protected final String loadAndMergeManifests( final String baseUrl,
                                                final String moduleName,
                                                final String... permutationNames )
    throws ServletException
  {
    final String cacheKey = toCacheKey( baseUrl, moduleName, permutationNames );
    if ( isCacheEnabled() )
    {
      final String manifest = _cache.get( cacheKey );
      if ( null != manifest )
      {
        return manifest;
      }
    }

    final ManifestDescriptor descriptor = new ManifestDescriptor();
    for ( final String permutationName : permutationNames )
    {
      final String manifest = loadManifest( baseUrl, moduleName, permutationName );
      final ManifestDescriptor other = ManifestDescriptor.parse( manifest );
      descriptor.merge( other );
    }
    Collections.sort( descriptor.getCachedResources() );
    Collections.sort( descriptor.getNetworkResources() );
    final String manifest = descriptor.toString();
    if ( isCacheEnabled() )
    {
      _cache.put( cacheKey, manifest );
    }
    return manifest;
  }

  private String toCacheKey( final String baseUrl, final String moduleName, final String[] permutationNames )
  {
    Arrays.sort( permutationNames );
    final StringBuilder sb = new StringBuilder();
    sb.append( baseUrl );
    sb.append( moduleName );
    sb.append( "/" );
    for ( int i = 0; i < permutationNames.length; i++ )
    {
      if ( 0 != i )
      {
        sb.append( ',' );
      }
      sb.append( permutationNames[ i ] );
    }
    return sb.toString();
  }

  protected final String loadManifest( final String baseUrl, final String moduleName, final String strongName )
    throws ServletException
  {
    final String cacheKey = baseUrl + moduleName + "/" + strongName;
    final String filePath = cacheKey + Permutation.PERMUTATION_MANIFEST_FILE_ENDING;
    if ( isCacheEnabled() )
    {
      final String manifest = _cache.get( cacheKey );
      if ( null != manifest )
      {
        return manifest;
      }
    }
    final InputStream content = getServletContext().getResourceAsStream( filePath );
    if ( null == content )
    {
      throw new ServletException( "Unable to locate manifest file: " + filePath );
    }
    final String manifest = readFile( content );
    if ( isCacheEnabled() )
    {
      _cache.put( cacheKey, manifest );
    }
    return manifest;
  }

  final String getBaseUrl( final HttpServletRequest request )
  {
    final String base = request.getServletPath();
    // cut off module
    return base.substring( 0, base.lastIndexOf( "/" ) + 1 );
  }

  private String readFile( final InputStream content )
    throws ServletException
  {
    try
    {
      final StringWriter sw = new StringWriter();
      final InputStreamReader fileReader = new InputStreamReader( content );
      final BufferedReader br = new BufferedReader( fileReader );
      String line;
      while ( null != ( line = br.readLine() ) )
      {
        sw.append( line ).append( "\n" );
      }

      return sw.toString();
    }
    catch ( final IOException e )
    {
      throw new ServletException( "Error while reading manifest file", e );
    }
    finally
    {
      try
      {
        content.close();
      }
      catch ( final IOException ioe )
      {
        //Ignored
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

  protected final void serveStringManifest( final HttpServletResponse response, final String manifest )
    throws ServletException
  {
    configureForNoCaching( response );

    response.setContentType( "text/cache-manifest; charset=utf-8" );
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
    response.setHeader( "Cache-control", "no-cache, must-revalidate, pre-check=0, post-check=0" );
    response.setHeader( "Pragma", "no-cache" );
  }

  /**
   * This method will restrict the list of descriptors so that it only contains selections valid for specified
   * bindings. Bindings will be removed from the list as they are matched. Unmatched bindings will remain in the
   * bindings list after the method completes.
   */
  protected final void reduceToMatchingDescriptors( @Nonnull final List<BindingProperty> bindings,
                                                    @Nonnull final List<SelectionDescriptor> descriptors )
  {
    final Iterator<BindingProperty> iterator = bindings.iterator();
    while ( iterator.hasNext() )
    {
      final BindingProperty property = iterator.next();

      // Do any selectors match?
      boolean matched = false;
      for ( final SelectionDescriptor selection : descriptors )
      {
        final BindingProperty candidate =
          findMatchingBindingProperty( selection.getBindingProperties(), property );
        if ( null != candidate )
        {
          matched = true;
          break;
        }
      }
      if ( matched )
      {
        // if so we can remove binding property from candidates list
        iterator.remove();

        // and now remove any selections that don't match
        final Iterator<SelectionDescriptor> selections = descriptors.iterator();
        while ( selections.hasNext() )
        {
          final SelectionDescriptor selection = selections.next();
          if ( null == findSatisfiesBindingProperty( selection.getBindingProperties(), property ) )
          {
            selections.remove();
          }
        }
      }
    }
  }

  /**
   * Return the binding property in the bindings list that matches specified requirement.
   */
  private BindingProperty findMatchingBindingProperty( final List<BindingProperty> bindings,
                                                       final BindingProperty requirement )
  {
    for ( final BindingProperty candidate : bindings )
    {
      if ( requirement.getName().equals( candidate.getName() ) )
      {
        return candidate;
      }
    }
    return null;
  }

  /**
   * Return the binding property in the bindings list that satisfies specified requirement. Satisfies means that
   * the property name matches and the value matches.
   */
  private BindingProperty findSatisfiesBindingProperty( final List<BindingProperty> bindings,
                                                        final BindingProperty requirement )
  {
    final BindingProperty property = findMatchingBindingProperty( bindings, requirement );
    if ( null != property && property.matches( requirement.getValue() ) )
    {
      return property;
    }
    return null;
  }

  /**
   * Return an array of permutation names that are selected based on supplied properties.
   * <p/>
   * The array may be null if no permutation could be found. It may be a single value if
   * the bindings uniquely identify a permutation or it may be multiple values if client side
   * properties are not specified and the server side properties do not uniquely identify a
   * permutation.
   */
  protected final String[] selectPermutations( @Nonnull final String baseUrl,
                                               @Nonnull final String moduleName,
                                               @Nonnull final List<BindingProperty> computedBindings )
    throws ServletException
  {
    try
    {
      final List<SelectionDescriptor> descriptors = new ArrayList<SelectionDescriptor>();
      descriptors.addAll( getPermutationDescriptors( baseUrl, moduleName ) );
      final List<BindingProperty> bindings = new ArrayList<BindingProperty>();
      bindings.addAll( computedBindings );
      reduceToMatchingDescriptors( bindings, descriptors );
      if ( 0 == bindings.size() )
      {
        if ( 1 == descriptors.size() )
        {
          return new String[]{ descriptors.get( 0 ).getPermutationName() };
        }
        else
        {
          final ArrayList<String> permutations = new ArrayList<String>();
          for ( final SelectionDescriptor descriptor : descriptors )
          {
            if ( descriptor.getBindingProperties().size() == computedBindings.size() )
            {
              permutations.add( descriptor.getPermutationName() );
            }
            else
            {
              for ( final BindingProperty property : descriptor.getBindingProperties() )
              {
                // This is one of the bindings that we could not reduce on
                if ( null == findMatchingBindingProperty( computedBindings, property ) )
                {
                  if ( canMergeManifestForSelectionProperty( property.getName() ) )
                  {
                    permutations.add( descriptor.getPermutationName() );
                  }
                }
              }
            }
          }
          if ( 0 == permutations.size() )
          {
            return null;
          }
          else
          {
            return permutations.toArray( new String[ permutations.size() ] );
          }
        }
      }
      return null;
    }
    catch ( final Exception e )
    {
      throw new ServletException( "can not read permutation information", e );
    }
  }

  /**
   * Return true if specified property may be determined client-side and manifests should be merged to
   * allow client-side selection.
   */
  private boolean canMergeManifestForSelectionProperty( final String propertyName )
  {
    return _clientSideSelectionProperties.contains( propertyName );
  }

  final List<SelectionDescriptor> getPermutationDescriptors( @Nonnull final String baseUrl,
                                                             @Nonnull final String moduleName )
    throws Exception
  {
    final String filePath = baseUrl + moduleName + "/" + PermutationsIO.PERMUTATIONS_DESCRIPTOR_FILE_NAME;
    final URL content = getServletContext().getResource( filePath );
    if ( null == content )
    {
      throw new ServletException( "Unable to locate permutations file: " + filePath );
    }
    final URLConnection connection = content.openConnection();
    final long lastModified = connection.getLastModified();
    if ( null == _selectionDescriptors || _permutationsDescriptorLastModified < lastModified )
    {
      final InputStream inputStream = connection.getInputStream();
      try
      {
        _selectionDescriptors = PermutationsIO.deserialize( inputStream );
        _permutationsDescriptorLastModified = lastModified;
        if ( null != _cache )
        {
          _cache.clear();
        }
      }
      finally
      {
        inputStream.close();
      }
    }
    return _selectionDescriptors;
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
