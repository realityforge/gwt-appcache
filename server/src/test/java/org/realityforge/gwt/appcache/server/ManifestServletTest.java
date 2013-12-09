package org.realityforge.gwt.appcache.server;

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
}
