package org.realityforge.gwt.appcache.server;

import java.io.File;
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
    when( servlet.getServletContext().getRealPath( "/fgis/permutations.xml" ) ).
      thenReturn( permutations.getAbsolutePath() );
    when( servlet.getServletContext().getRealPath( "/fgis/" + strongPermutation + ".appcache" ) ).
      thenReturn( manifest.getAbsolutePath() );

    final ServletOutputStream output = mock( ServletOutputStream.class );
    when( response.getOutputStream() ).thenReturn( output );

    servlet.doGet( request, response );

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
    when( servlet.getServletContext().getRealPath( "/fgis/permutations.xml" ) ).
      thenReturn( permutations.getAbsolutePath() );

    servlet.doGet( request, response );

    verify( response ).sendError( HttpServletResponse.SC_NOT_FOUND );
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
          new Cookie( AbstractManifestServlet.DISABLE_MANIFEST_COOKIE_NAME, AbstractManifestServlet.DISABLE_MANIFEST_COOKIE_VALUE )
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
    verify( response ).setHeader( "Cache-control", "no-cache, no-store, must-revalidate, pre-check=0, post-check=0" );
    verify( response ).setHeader( "Pragma", "no-cache" );
    verify( response ).setContentType( "text/cache-manifest" );

    verify( output ).write( "DD".getBytes( "US-ASCII" ) );
  }

  @Test
  public void loadManifest()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final String expectedManifest = "XXXX\n";
    final File manifestFile = createFile( "manifest", "appcache", expectedManifest );

    when( servlet.getServletContext().getRealPath( "/foo/myapp/12345.appcache" ) ).
      thenReturn( manifestFile.getAbsolutePath() );

    final String manifest = servlet.loadManifest( "/foo/", "myapp", "12345" );
    assertEquals( manifest, expectedManifest );
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

  @Test
  public void getBindingMap()
    throws Exception
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final ServletContext servletContext = servlet.getServletContext();
    final String permutationContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><permutations></permutations>\n";
    final File permutations = createFile( "permutations", "xml", permutationContent );
    assertTrue( permutations.setLastModified( 0 ) );

    when( servletContext.getRealPath( "/foo/myapp/permutations.xml" ) ).thenReturn( permutations.getAbsolutePath() );

    final List<PermutationDescriptor> descriptors = servlet.getPermutationDescriptors( "/foo/", "myapp" );
    assertNotNull( descriptors );

    assertTrue( descriptors == servlet.getPermutationDescriptors( "/foo/", "myapp" ) );

    assertTrue( permutations.setLastModified( Long.MAX_VALUE ) );

    assertFalse( descriptors == servlet.getPermutationDescriptors( "/foo/", "myapp" ) );

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

    final ArrayList<BindingProperty> computedBindings = new ArrayList<BindingProperty>();
    computedBindings.add( new BindingProperty( "user.agent", "ie9" ) );

    ensureStrongPermutationReturned( permutationContent, computedBindings, strongPermutation );
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

    final ArrayList<BindingProperty> computedBindings = new ArrayList<BindingProperty>();
    computedBindings.add( new BindingProperty( "user.agent", "ie9" ) );

    ensureStrongPermutationReturned( permutationContent, computedBindings, strongPermutation );
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

    final ArrayList<BindingProperty> computedBindings = new ArrayList<BindingProperty>();
    computedBindings.add( new BindingProperty( "user.agent", "ie9" ) );

    ensureStrongPermutationReturned( permutationContent, computedBindings, strongPermutation );
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

    final ArrayList<BindingProperty> computedBindings = new ArrayList<BindingProperty>();
    computedBindings.add( new BindingProperty( "user.agent", "ie9" ) );
    computedBindings.add( new BindingProperty( "screen.size", "biggo" ) );
    computedBindings.add( new BindingProperty( "color.depth", "much" ) );

    ensureStrongPermutationReturned( permutationContent, computedBindings, strongPermutation );
  }

  private void ensureStrongPermutationReturned( final String permutationContent,
                                                final List<BindingProperty> computedBindings,
                                                final String expected )
    throws IOException, ServletException
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final ServletContext servletContext = servlet.getServletContext();
    final File permutations = createFile( "permutations", "xml", permutationContent );
    when( servletContext.getRealPath( "/foo/myapp/permutations.xml" ) ).thenReturn( permutations.getAbsolutePath() );

    final String permutationStrongName = servlet.getPermutationStrongName( "/foo/", "myapp", computedBindings );

    assertEquals( permutationStrongName, expected );
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
}
