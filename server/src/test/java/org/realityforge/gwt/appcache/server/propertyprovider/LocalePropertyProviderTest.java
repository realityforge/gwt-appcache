package org.realityforge.gwt.appcache.server.propertyprovider;

import javax.servlet.http.HttpServletRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class LocalePropertyProviderTest
{
  @DataProvider( name = "LocaleMapping" )
  public Object[][] getLocaleMapping()
  {
    return new Object[][]{
      { null, null, "default" },
      { null, "en-US,en;q=0.8", "en-US" },
      { null, "en;q=0.8", "en" },
      { "locale=en-US", "en;q=0.8", "en-US" },
      { "color=red&locale=en-US&width=wide", "en;q=0.8", "en-US" },
    };
  }

  @Test( dataProvider = "LocaleMapping" )
  public void localeExtraction( final String queryParam, final String acceptLanguage, final String locale )
  {
    final LocalePropertyProvider provider = new LocalePropertyProvider();

    assertEquals( provider.getPropertyName(), "locale" );
    final HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getQueryString() ).thenReturn( queryParam );
    when( request.getHeader( "Accept-Language" ) ).thenReturn( acceptLanguage );
    assertEquals( provider.getPropertyValue( request ), locale );
  }
}
