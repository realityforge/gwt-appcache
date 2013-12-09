package org.realityforge.gwt.appcache.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
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

    final HashSet<BindingProperty> computedBindings = new HashSet<BindingProperty>();
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

    final HashSet<BindingProperty> computedBindings = new HashSet<BindingProperty>();
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

    final HashSet<BindingProperty> computedBindings = new HashSet<BindingProperty>();
    computedBindings.add( new BindingProperty( "user.agent", "ie9" ) );
    computedBindings.add( new BindingProperty( "screen.size", "biggo" ) );
    computedBindings.add( new BindingProperty( "color.depth", "much" ) );

    ensureStrongPermutationReturned( permutationContent, computedBindings, strongPermutation );
  }

  private void ensureStrongPermutationReturned( final String permutationContent,
                                                final HashSet<BindingProperty> computedBindings,
                                                final String expected )
    throws IOException, ServletException
  {
    final TestManifestServlet servlet = new TestManifestServlet();

    final ServletContext servletContext = servlet.getServletContext();
    final File permutations = createPermutationsXML( permutationContent );
    when( servletContext.getRealPath( "/foo/myapp/permutations.xml" ) ).thenReturn( permutations.getAbsolutePath() );

    final String permutationStrongName = servlet.getPermutationStrongName( "/foo/", "myapp", computedBindings );

    assertEquals( permutationStrongName, expected );
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
