package org.realityforge.gwt.appcache.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.testng.annotations.Test;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ManifestServletTest
{
  static class TestManifestServlet
    extends AbstractManifestServlet
  {
    private ServletContext _servletContext;
    private boolean _unmatchedHandlerResponse;
    private boolean _unmatchedHandlerCalled;

    void setUnmatchedHandlerResponse( final boolean unmatchedHandlerResponse )
    {
      _unmatchedHandlerResponse = unmatchedHandlerResponse;
    }

    boolean hasUnmatchedHandlerBeenCalled()
    {
      return _unmatchedHandlerCalled;
    }

    @Override
    protected boolean handleUnmatchedRequest( final HttpServletRequest request,
                                              final HttpServletResponse response,
                                              final String moduleName,
                                              final String baseUrl,
                                              final List<BindingProperty> computedBindings )
      throws ServletException, IOException
    {
      _unmatchedHandlerCalled = true;
      return _unmatchedHandlerResponse;
    }

    @Override
    public ServletContext getServletContext()
    {
      if ( null == _servletContext )
      {
        _servletContext = mock( ServletContext.class );
      }
      return _servletContext;
    }
  }

  @Test
  public void doGet()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();
    servlet.addPropertyProvider( new TestPropertyProvider( "user.agent", "ie9" ) );

    final String strongPermutation = "C7D408F8EFA266A7F9A31209F8AA7446";
    final String permutationContent =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<permutations>\n" +
      "   <permutation name=\"" + strongPermutation + "\">\n" +
      "      <user.agent>ie8,ie9,safari,ie10,gecko1_8</user.agent>\n" +
      "   </permutation>\n" +
      "</permutations>\n";

    final File permutations = createFile( "permutations", "xml", permutationContent );
    final String manifestContent = "ZANG!\n";
    final File manifest = createFile( "manifest", "appcache", manifestContent );

    final HttpServletRequest request = mock( HttpServletRequest.class );
    final HttpServletResponse response = mock( HttpServletResponse.class );
    when( request.getServletPath() ).thenReturn( "/fgis.appcache" );
    when( servlet.getServletContext().getResource( "/fgis/permutations.xml" ) ).
      thenReturn( permutations.toURI().toURL() );
    configureManifest( servlet, "/fgis/" + strongPermutation + ".appcache", manifest );

    final ServletOutputStream output = mock( ServletOutputStream.class );
    when( response.getOutputStream() ).thenReturn( output );

    servlet.doGet( request, response );

    verify( output ).write( manifestContent.getBytes( "US-ASCII" ) );
  }

  @Test
  public void doGet_matchedMultiplePermutations()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();
    servlet.addPropertyProvider( new TestPropertyProvider( "user.agent", "ie8" ) );
    servlet.addClientSideSelectionProperty( "screen.size" );

    final String permutationContent =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<permutations>\n" +
      "   <permutation name=\"P1\">\n" +
      "      <user.agent>ie8</user.agent>\n" +
      "      <screen.size>big</screen.size>\n" +
      "   </permutation>\n" +
      "   <permutation name=\"P2\">\n" +
      "      <user.agent>ie8</user.agent>\n" +
      "      <screen.size>small</screen.size>\n" +
      "   </permutation>\n" +
      "</permutations>\n";

    final File permutations = createFile( "permutations", "xml", permutationContent );
    final String manifest1Content = "CACHE MANIFEST\n\nCACHE:\n1\n2\n\n\nNETWORK:\nA\nB\n";
    final String manifest2Content = "CACHE MANIFEST\n\nCACHE:\n1\n3\n\n\nNETWORK:\nA\nC\n";
    final File manifest1 = createFile( "manifest", "appcache", manifest1Content );
    final File manifest2 = createFile( "manifest", "appcache", manifest2Content );

    final HttpServletRequest request = mock( HttpServletRequest.class );
    final HttpServletResponse response = mock( HttpServletResponse.class );
    when( request.getServletPath() ).thenReturn( "/fgis.appcache" );
    when( servlet.getServletContext().getResource( "/fgis/permutations.xml" ) ).
      thenReturn( permutations.toURI().toURL() );
    configureManifest( servlet, "/fgis/P1.appcache", manifest1 );
    configureManifest( servlet, "/fgis/P2.appcache", manifest2 );

    final ServletOutputStream output = mock( ServletOutputStream.class );
    when( response.getOutputStream() ).thenReturn( output );

    servlet.doGet( request, response );

    final String manifestContent =
      "CACHE MANIFEST\n" +
      "\n" +
      "CACHE:\n" +
      "1\n" +
      "2\n" +
      "3\n" +
      "\n" +
      "\n" +
      "NETWORK:\n" +
      "A\n" +
      "B\n" +
      "C\n";
    verify( output ).write( manifestContent.getBytes( "US-ASCII" ) );
  }

  @Test
  public void doGet_notFound()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();
    servlet.addPropertyProvider( new TestPropertyProvider( "user.agent", "ie9" ) );

    final String permutationContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><permutations></permutations>\n";

    final File permutations = createFile( "permutations", "xml", permutationContent );

    final HttpServletRequest request = mock( HttpServletRequest.class );
    final HttpServletResponse response = mock( HttpServletResponse.class );
    when( request.getServletPath() ).thenReturn( "/fgis.appcache" );
    when( servlet.getServletContext().getResource( "/fgis/permutations.xml" ) ).
      thenReturn( permutations.toURI().toURL() );

    servlet.doGet( request, response );

    verify( response ).sendError( HttpServletResponse.SC_NOT_FOUND );
    assertTrue( servlet.hasUnmatchedHandlerBeenCalled() );
  }

  @Test
  public void doGet_unmatchedHandlerResponds()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();
    servlet.addPropertyProvider( new TestPropertyProvider( "user.agent", "ie9" ) );

    final String permutationContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><permutations></permutations>\n";

    final File permutations = createFile( "permutations", "xml", permutationContent );

    final HttpServletRequest request = mock( HttpServletRequest.class );
    final HttpServletResponse response = mock( HttpServletResponse.class );
    when( request.getServletPath() ).thenReturn( "/fgis.appcache" );
    when( servlet.getServletContext().getResource( "/fgis/permutations.xml" ) ).
      thenReturn( permutations.toURI().toURL() );

    servlet.setUnmatchedHandlerResponse( true );

    servlet.doGet( request, response );

    verify( response, never() ).sendError( HttpServletResponse.SC_NOT_FOUND );
    assertTrue( servlet.hasUnmatchedHandlerBeenCalled() );
  }

  @Test
  public void doGet_whenDisabled()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();
    servlet.addPropertyProvider( new TestPropertyProvider( "user.agent", "ie9" ) );

    final HttpServletRequest request = mock( HttpServletRequest.class );
    final Cookie[] cookies =
      {
        new Cookie( AbstractManifestServlet.DISABLE_MANIFEST_COOKIE_NAME,
                    AbstractManifestServlet.DISABLE_MANIFEST_COOKIE_VALUE )
      };
    when( request.getCookies() ).thenReturn( cookies );
    final HttpServletResponse response = mock( HttpServletResponse.class );

    servlet.doGet( request, response );

    verify( response ).sendError( HttpServletResponse.SC_GONE );
  }

  @Test
  public void isAppCacheDisabled()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    {
      final HttpServletRequest request = mock( HttpServletRequest.class );
      when( request.getCookies() ).thenReturn( null );
      assertEquals( servlet.isAppCacheDisabled( request ), false );
    }

    {
      final HttpServletRequest request = mock( HttpServletRequest.class );
      final Cookie[] cookies = { new Cookie( "X", "y" ), new Cookie( "X1", "y2" ) };
      when( request.getCookies() ).thenReturn( cookies );
      assertEquals( servlet.isAppCacheDisabled( request ), false );
    }

    {
      final HttpServletRequest request = mock( HttpServletRequest.class );
      final Cookie[] cookies = { new Cookie( AbstractManifestServlet.DISABLE_MANIFEST_COOKIE_NAME, "someValue" ) };
      when( request.getCookies() ).thenReturn( cookies );
      assertEquals( servlet.isAppCacheDisabled( request ), false );
    }

    {
      final HttpServletRequest request = mock( HttpServletRequest.class );
      final Cookie[] cookies =
        {
          new Cookie( "X1", "y2" ),
          new Cookie( AbstractManifestServlet.DISABLE_MANIFEST_COOKIE_NAME,
                      AbstractManifestServlet.DISABLE_MANIFEST_COOKIE_VALUE )
        };
      when( request.getCookies() ).thenReturn( cookies );
      assertEquals( servlet.isAppCacheDisabled( request ), true );
    }
  }

  @Test
  public void getBaseUrl()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getServletPath() ).thenReturn( "/fgis.appcache" );
    assertEquals( servlet.getBaseUrl( request ), "/" );
  }

  @Test
  public void serveStringManifest()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final HttpServletResponse response = mock( HttpServletResponse.class );
    final ServletOutputStream output = mock( ServletOutputStream.class );
    when( response.getOutputStream() ).thenReturn( output );

    servlet.serveStringManifest( response, "DD" );

    verify( response ).setDateHeader( eq( "Date" ), anyLong() );
    verify( response ).setDateHeader( eq( "Last-Modified" ), anyLong() );
    verify( response ).setDateHeader( "Expires", 0 );
    verify( response ).setHeader( "Cache-control", "no-cache, must-revalidate, pre-check=0, post-check=0" );
    verify( response ).setHeader( "Pragma", "no-cache" );
    verify( response ).setContentType( "text/cache-manifest; charset=utf-8" );

    verify( output ).write( "DD".getBytes( "US-ASCII" ) );
  }

  @SuppressWarnings( "StringEquality" )
  @Test
  public void loadManifest()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final String expectedManifest = "XXXX\n";
    final File manifestFile = createFile( "manifest", "appcache", expectedManifest );

    configureManifest( servlet, "/foo/myapp/12345.appcache", manifestFile );
    when( servlet.getServletContext().getResourceAsStream( "/foo/myapp/12345.appcache" ) ).
      thenReturn( new FileInputStream( manifestFile ),
                  new FileInputStream( manifestFile ) );

    final String manifest = servlet.loadManifest( "/foo/", "myapp", "12345" );
    assertEquals( manifest, expectedManifest );

    final String manifest2 = servlet.loadManifest( "/foo/", "myapp", "12345" );
    assertTrue( manifest2 == manifest );

    verify( servlet.getServletContext(), times( 1 ) ).getResourceAsStream( "/foo/myapp/12345.appcache" );

    servlet.disableCache();

    final String manifest3 = servlet.loadManifest( "/foo/", "myapp", "12345" );
    assertTrue( manifest3 != manifest );
    assertEquals( manifest3, manifest );

    verify( servlet.getServletContext(), times( 2 ) ).getResourceAsStream( "/foo/myapp/12345.appcache" );
  }

  private void configureManifest( final TestManifestServlet servlet, final String path, final File manifestFile )
    throws FileNotFoundException
  {
    when( servlet.getServletContext().getResourceAsStream( path ) ).
      thenReturn( new FileInputStream( manifestFile ) );
  }

  @SuppressWarnings( "StringEquality" )
  @Test
  public void loadAndMergeManifests()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final String manifest1 = "CACHE MANIFEST\n\nCACHE:\n1\n2\n\n\nNETWORK:\nA\nB\n";
    final String manifest2 = "CACHE MANIFEST\n\nCACHE:\n1\n3\n\n\nNETWORK:\nA\nC\n";

    final File manifestFile1 = createFile( "manifest", "appcache", manifest1 );
    final File manifestFile2 = createFile( "manifest", "appcache", manifest2 );

    when( servlet.getServletContext().getResourceAsStream( "/foo/myapp/m1.appcache" ) ).
      thenReturn( new FileInputStream( manifestFile1 ),
                  new FileInputStream( manifestFile1 ) );
    when( servlet.getServletContext().getResourceAsStream( "/foo/myapp/m2.appcache" ) ).
      thenReturn( new FileInputStream( manifestFile2 ),
                  new FileInputStream( manifestFile2 ) );

    final String manifest = servlet.loadAndMergeManifests( "/foo/", "myapp", "m1", "m2" );
    final ManifestDescriptor result = ManifestDescriptor.parse( manifest );
    final List<String> cachedResources = result.getCachedResources();
    assertEquals( cachedResources.size(), 3 );
    assertTrue( cachedResources.contains( "1" ) );
    assertTrue( cachedResources.contains( "2" ) );
    assertTrue( cachedResources.contains( "3" ) );
    final List<String> networkResources = result.getNetworkResources();
    assertEquals( networkResources.size(), 3 );
    assertTrue( networkResources.contains( "A" ) );
    assertTrue( networkResources.contains( "B" ) );
    assertTrue( networkResources.contains( "C" ) );

    final String manifestResult2 = servlet.loadAndMergeManifests( "/foo/", "myapp", "m1", "m2" );
    assertTrue( manifestResult2 == manifest );

    verify( servlet.getServletContext(), times( 1 ) ).getResourceAsStream( "/foo/myapp/m2.appcache" );

    servlet.disableCache();

    final String manifestResult3 = servlet.loadAndMergeManifests( "/foo/", "myapp", "m1", "m2" );
    assertTrue( manifestResult3 != manifest );
    assertEquals( manifestResult3, manifest );

    verify( servlet.getServletContext(), times( 2 ) ).getResourceAsStream( "/foo/myapp/m2.appcache" );
  }

  @Test
  public void calculateBindingPropertiesForClient()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();
    servlet.addPropertyProvider( new TestPropertyProvider( "X", "1" ) );
    servlet.addPropertyProvider( new TestPropertyProvider( "Y", "2" ) );
    final HttpServletRequest request = mock( HttpServletRequest.class );
    final List<BindingProperty> properties = servlet.calculateBindingPropertiesForClient( request );
    assertEquals( properties.size(), 2 );
    final BindingProperty property1 = properties.get( 0 );
    final BindingProperty property2 = properties.get( 1 );
    assertEquals( property1.getName(), "X" );
    assertEquals( property1.getValue(), "1" );
    assertEquals( property2.getName(), "Y" );
    assertEquals( property2.getValue(), "2" );
  }

  @Test
  public void reduceToMatchingDescriptors_withSingleMatch()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final ArrayList<SelectionDescriptor> descriptors = new ArrayList<>();

    {
      final ArrayList<BindingProperty> properties = new ArrayList<>();
      bp( properties, "user.agent", "ie9" );
      descriptors.add( new SelectionDescriptor( "P1", properties ) );
    }

    {
      final ArrayList<BindingProperty> properties = new ArrayList<>();
      bp( properties, "user.agent", "ie8" );
      descriptors.add( new SelectionDescriptor( "P2", properties ) );
    }

    final ArrayList<BindingProperty> computedBindings = new ArrayList<>();
    bp( computedBindings, "user.agent", "ie9" );

    servlet.reduceToMatchingDescriptors( computedBindings, descriptors );

    assertEquals( computedBindings.size(), 0 );

    assertEquals( descriptors.size(), 1 );
    assertTrue( isPermutationPresent( descriptors, "P1" ) );
  }

  @Test
  public void reduceToMatchingDescriptors_withNoMatch()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final ArrayList<SelectionDescriptor> descriptors = new ArrayList<>();

    {
      final ArrayList<BindingProperty> properties = new ArrayList<>();
      bp( properties, "user.agent", "ie9" );
      descriptors.add( new SelectionDescriptor( "P1", properties ) );
    }

    {
      final ArrayList<BindingProperty> properties = new ArrayList<>();
      bp( properties, "user.agent", "ie8" );
      descriptors.add( new SelectionDescriptor( "P2", properties ) );
    }

    final ArrayList<BindingProperty> computedBindings = new ArrayList<>();
    bp( computedBindings, "user.agent", "ie10" );

    servlet.reduceToMatchingDescriptors( computedBindings, descriptors );

    assertEquals( computedBindings.size(), 0 );
    assertEquals( descriptors.size(), 0 );
  }

  @Test
  public void reduceToMatchingDescriptors_withPartialMatch()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final ArrayList<SelectionDescriptor> descriptors = new ArrayList<>();

    {
      final ArrayList<BindingProperty> properties = new ArrayList<>();
      bp( properties, "user.agent", "ie9" );
      bp( properties, "screen.size", "large" );
      descriptors.add( new SelectionDescriptor( "P1", properties ) );
    }

    {
      final ArrayList<BindingProperty> properties = new ArrayList<>();
      bp( properties, "user.agent", "ie9" );
      bp( properties, "screen.size", "small" );
      descriptors.add( new SelectionDescriptor( "P2", properties ) );
    }

    {
      final ArrayList<BindingProperty> properties = new ArrayList<>();
      bp( properties, "user.agent", "ie10" );
      bp( properties, "screen.size", "large" );
      descriptors.add( new SelectionDescriptor( "P3", properties ) );
    }
    {
      final ArrayList<BindingProperty> properties = new ArrayList<>();
      bp( properties, "user.agent", "ie10" );
      bp( properties, "screen.size", "small" );
      descriptors.add( new SelectionDescriptor( "P4", properties ) );
    }

    final ArrayList<BindingProperty> computedBindings = new ArrayList<>();
    bp( computedBindings, "user.agent", "ie10" );

    servlet.reduceToMatchingDescriptors( computedBindings, descriptors );

    assertEquals( computedBindings.size(), 0 );

    assertEquals( descriptors.size(), 2 );
    assertTrue( isPermutationPresent( descriptors, "P3" ) );
    assertTrue( isPermutationPresent( descriptors, "P4" ) );
  }

  @Test
  public void reduceToMatchingDescriptors_withIncompleteMatch()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final ArrayList<SelectionDescriptor> descriptors = new ArrayList<>();

    {
      final ArrayList<BindingProperty> properties = new ArrayList<>();
      bp( properties, "user.agent", "ie9" );
      bp( properties, "screen.size", "large" );
      descriptors.add( new SelectionDescriptor( "P1", properties ) );
    }

    {
      final ArrayList<BindingProperty> properties = new ArrayList<>();
      bp( properties, "user.agent", "ie9" );
      bp( properties, "screen.size", "small" );
      descriptors.add( new SelectionDescriptor( "P2", properties ) );
    }

    {
      final ArrayList<BindingProperty> properties = new ArrayList<>();
      bp( properties, "user.agent", "ie10" );
      bp( properties, "screen.size", "large" );
      descriptors.add( new SelectionDescriptor( "P3", properties ) );
    }
    {
      final ArrayList<BindingProperty> properties = new ArrayList<>();
      bp( properties, "user.agent", "ie10" );
      bp( properties, "screen.size", "small" );
      descriptors.add( new SelectionDescriptor( "P4", properties ) );
    }

    final ArrayList<BindingProperty> computedBindings = new ArrayList<>();
    bp( computedBindings, "user.agent", "ie10" );
    bp( computedBindings, "os.name", "ms" );

    servlet.reduceToMatchingDescriptors( computedBindings, descriptors );

    assertEquals( computedBindings.size(), 1 );
    assertTrue( isPropertyPresent( computedBindings, "os.name" ) );

    assertEquals( descriptors.size(), 2 );
    assertTrue( isPermutationPresent( descriptors, "P3" ) );
    assertTrue( isPermutationPresent( descriptors, "P4" ) );
  }

  @Test
  public void selectPermutations_withClientSidePropertiesPresent()
    throws Exception
  {
    final String permutationContent =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<permutations>\n" +
      "   <permutation name=\"P1\">\n" +
      "      <user.agent>ie8</user.agent>\n" +
      "      <screen.size>big</screen.size>\n" +
      "   </permutation>\n" +
      "   <permutation name=\"P2\">\n" +
      "      <user.agent>ie8</user.agent>\n" +
      "      <screen.size>small</screen.size>\n" +
      "   </permutation>\n" +
      "   <permutation name=\"P3\">\n" +
      "      <user.agent>ie8</user.agent>\n" +
      "      <screen.size>medium</screen.size>\n" +
      "   </permutation>\n" +
      "   <permutation name=\"P4\">\n" +
      "      <user.agent>ie9</user.agent>\n" +
      "      <screen.size>medium</screen.size>\n" +
      "   </permutation>\n" +
      "</permutations>\n";

    final String[] clientSideProperties = { "screen.size" };

    // Complete match
    {
      final ArrayList<BindingProperty> computedBindings = new ArrayList<>();
      bp( computedBindings, "user.agent", "ie8" );
      bp( computedBindings, "screen.size", "big" );
      ensurePermutationsSelected( permutationContent, clientSideProperties, computedBindings, "P1" );
    }

    // Partial match
    {
      final ArrayList<BindingProperty> computedBindings = new ArrayList<>();
      bp( computedBindings, "user.agent", "ie8" );
      ensurePermutationsSelected( permutationContent, clientSideProperties, computedBindings, "P1", "P2", "P3" );
    }

    // No match
    {
      final ArrayList<BindingProperty> computedBindings = new ArrayList<>();
      bp( computedBindings, "user.agent", "ie9" );
      bp( computedBindings, "screen.size", "big" );
      ensurePermutationsSelected( permutationContent, clientSideProperties, computedBindings, (String[]) null );
    }
  }

  private void ensurePermutationsSelected( final String permutationContent,
                                           final String[] clientSideProperties,
                                           final List<BindingProperty> computedBindings,
                                           final String... expected )
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();
    for ( final String property : clientSideProperties )
    {
      servlet.addClientSideSelectionProperty( property );
    }

    final File permutations = createFile( "permutations", "xml", permutationContent );
    configurePermutationsFile( servlet, "/foo/myapp/permutations.xml", permutations );

    final String[] selected = servlet.selectPermutations( "/foo/", "myapp", computedBindings );

    assertEquals( selected, expected );
  }

  private void configurePermutationsFile( final TestManifestServlet servlet,
                                          final String path,
                                          final File permutations )
    throws Exception
  {
    when( servlet.getServletContext().getResource( path ) ).thenReturn( permutations.toURI().toURL() );
    configureManifest( servlet, path, permutations );
  }

  @Test
  public void getModuleName()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final HttpServletRequest mock = mock( HttpServletRequest.class );
    when( mock.getServletPath() ).thenReturn( "/myapp.appcache" );
    assertEquals( servlet.getModuleName( mock ), "myapp" );
  }

  @Test( expectedExceptions = ServletException.class )
  public void getModuleName_missingMapping()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final HttpServletRequest mock = mock( HttpServletRequest.class );
    when( mock.getServletPath() ).thenReturn( null );
    servlet.getModuleName( mock );
  }

  @Test( expectedExceptions = ServletException.class )
  public void getModuleName_badMapping()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final HttpServletRequest mock = mock( HttpServletRequest.class );
    when( mock.getServletPath() ).thenReturn( "/XXXX.cache" );
    servlet.getModuleName( mock );
  }

  @SuppressWarnings( "StringEquality" )
  @Test
  public void getBindingMap()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final String permutationContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><permutations></permutations>\n";
    final File permutations = createFile( "permutations", "xml", permutationContent );
    final File manifestFile = createFile( "manifest", "appcache", "XXXX\n" );
    when( servlet.getServletContext().getResourceAsStream( "/foo/myapp/12345.appcache" ) ).
      thenReturn( new FileInputStream( manifestFile ),
                  new FileInputStream( manifestFile ),
                  new FileInputStream( manifestFile ) );

    assertTrue( permutations.setLastModified( 0 ) );

    configurePermutationsFile( servlet, "/foo/myapp/permutations.xml", permutations );

    final List<SelectionDescriptor> descriptors = servlet.getPermutationDescriptors( "/foo/", "myapp" );
    assertNotNull( descriptors );
    final String manifest = servlet.loadManifest( "/foo/", "myapp", "12345" );

    assertTrue( descriptors == servlet.getPermutationDescriptors( "/foo/", "myapp" ) );

    final String manifest2 = servlet.loadManifest( "/foo/", "myapp", "12345" );
    assertTrue( manifest2 == manifest );
    verify( servlet.getServletContext(), times( 1 ) ).getResourceAsStream( "/foo/myapp/12345.appcache" );

    assertTrue( permutations.setLastModified( Long.MAX_VALUE ) );

    assertFalse( descriptors == servlet.getPermutationDescriptors( "/foo/", "myapp" ) );

    final String manifest3 = servlet.loadManifest( "/foo/", "myapp", "12345" );
    assertTrue( manifest3 != manifest );
    verify( servlet.getServletContext(), times( 2 ) ).getResourceAsStream( "/foo/myapp/12345.appcache" );

    assertTrue( permutations.delete() );
  }

  @Test
  public void getPermutationStrongName_simpleMultiValued()
    throws Exception
  {
    final String strongPermutation = "C7D408F8EFA266A7F9A31209F8AA7446";
    final String permutationContent =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<permutations>\n" +
      "   <permutation name=\"" + strongPermutation + "\">\n" +
      "      <user.agent>ie8,ie9,safari,ie10,gecko1_8</user.agent>\n" +
      "   </permutation>\n" +
      "</permutations>\n";

    final ArrayList<BindingProperty> computedBindings = new ArrayList<>();
    bp( computedBindings, "user.agent", "ie9" );

    ensurePermutationsSelected( permutationContent, new String[ 0 ], computedBindings, strongPermutation );
  }

  @Test
  public void getPermutationStrongName_multiplePermutations()
    throws Exception
  {
    final String strongPermutation = "C7D408F8EFA266A7F9A31209F8AA7446";
    final String permutationContent =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<permutations>\n" +
      "   <permutation name=\"" + strongPermutation + "\">\n" +
      "      <user.agent>ie8,ie9,safari,ie10,gecko1_8</user.agent>\n" +
      "   </permutation>\n" +
      "   <permutation name=\"Other\">\n" +
      "      <user.agent>ie8,ie9,safari,ie10,gecko1_8</user.agent>\n" +
      "      <screen.size>biggo</screen.size>\n" +
      "   </permutation>\n" +
      "</permutations>\n";

    final ArrayList<BindingProperty> computedBindings = new ArrayList<>();
    bp( computedBindings, "user.agent", "ie9" );

    ensurePermutationsSelected( permutationContent, new String[ 0 ], computedBindings, strongPermutation );
  }

  @Test
  public void getPermutationStrongName_multipleMatchesForSinglePermutation()
    throws Exception
  {
    final String strongPermutation = "C7D408F8EFA266A7F9A31209F8AA7446";
    final String permutationContent =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<permutations>\n" +
      "   <permutation name=\"" + strongPermutation + "\">\n" +
      "      <user.agent>ie8</user.agent>\n" +
      "   </permutation>\n" +
      "   <permutation name=\"" + strongPermutation + "\">\n" +
      "      <user.agent>ie9</user.agent>\n" +
      "   </permutation>\n" +
      "</permutations>\n";

    final ArrayList<BindingProperty> computedBindings = new ArrayList<>();
    bp( computedBindings, "user.agent", "ie9" );

    ensurePermutationsSelected( permutationContent, new String[ 0 ], computedBindings, strongPermutation );
  }

  @Test
  public void getPermutationStrongName_multiplePermutationsAndSelectMostSpecific()
    throws Exception
  {
    final String strongPermutation = "C7D408F8EFA266A7F9A31209F8AA7446";
    final String permutationContent =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<permutations>\n" +
      "   <permutation name=\"" + strongPermutation + "\">\n" +
      "      <user.agent>ie8,ie9,safari,ie10,gecko1_8</user.agent>\n" +
      "      <screen.size>biggo</screen.size>\n" +
      "      <color.depth>much</color.depth>\n" +
      "   </permutation>\n" +
      "   <permutation name=\"Other\">\n" +
      "      <user.agent>ie8,ie9,safari,ie10,gecko1_8</user.agent>\n" +
      "   </permutation>\n" +
      "   <permutation name=\"Other2\">\n" +
      "      <user.agent>ie8,ie9,safari,ie10,gecko1_8</user.agent>\n" +
      "      <screen.size>biggo</screen.size>\n" +
      "   </permutation>\n" +
      "</permutations>\n";

    final ArrayList<BindingProperty> computedBindings = new ArrayList<>();
    bp( computedBindings, "user.agent", "ie9" );
    bp( computedBindings, "screen.size", "biggo" );
    bp( computedBindings, "color.depth", "much" );

    ensurePermutationsSelected( permutationContent, new String[ 0 ], computedBindings, strongPermutation );
  }


  private File createFile( final String prefix, final String extension, final String content )
    throws IOException
  {
    final File permutations = File.createTempFile( prefix, extension );
    permutations.deleteOnExit();
    final FileOutputStream outputStream = new FileOutputStream( permutations );
    outputStream.write( content.getBytes() );
    outputStream.close();
    return permutations;
  }

  private void bp( final ArrayList<BindingProperty> properties, final String key, final String value )
  {
    properties.add( new BindingProperty( key, value ) );
  }

  private boolean isPermutationPresent( final ArrayList<SelectionDescriptor> descriptors, final String permutationName )
  {
    for ( final SelectionDescriptor descriptor : descriptors )
    {
      if ( descriptor.getPermutationName().equals( permutationName ) )
      {
        return true;
      }
    }
    return false;
  }

  private boolean isPropertyPresent( final ArrayList<BindingProperty> properties, final String propertyName )
  {
    for ( final BindingProperty property : properties )
    {
      if ( property.getName().equals( propertyName ) )
      {
        return true;
      }
    }
    return false;
  }
}
