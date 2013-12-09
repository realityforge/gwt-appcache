package org.realityforge.gwt.appcache.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.testng.annotations.Test;
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
    final File permutations = createPermutationsXML( permutationContent );
    assertTrue( permutations.setLastModified( 0 ) );

    when( servletContext.getRealPath( "/foo/myapp/permutations.xml" ) ).thenReturn( permutations.getAbsolutePath() );

    final Map<String,List<BindingProperty>> bindings = servlet.getBindingMap( "/foo/", "myapp" );
    assertNotNull( bindings );

    assertTrue( bindings == servlet.getBindingMap( "/foo/", "myapp" ) );

    assertTrue( permutations.setLastModified( Long.MAX_VALUE ) );

    assertFalse( bindings == servlet.getBindingMap( "/foo/", "myapp" ) );

    assertTrue( permutations.delete() );
  }

  private File createPermutationsXML( final String permutationContent )
    throws IOException
  {
    final File permutations = File.createTempFile( "permutations", "xml" );
    permutations.deleteOnExit();
    final FileOutputStream outputStream = new FileOutputStream( permutations );
    outputStream.write( permutationContent.getBytes() );
    outputStream.close();
    return permutations;
  }
}
