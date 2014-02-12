package org.realityforge.gwt.appcache.server.mgwt;

import javax.servlet.http.HttpServletRequest;
import org.realityforge.gwt.appcache.server.propertyprovider.PropertyProvider;

public final class PhoneGapPropertyProvider
  implements PropertyProvider
{
  @Override
  public String getPropertyName()
  {
    return "phonegap.env";
  }

  @Override
  public String getPropertyValue( final HttpServletRequest request )
  {
    return "no";
  }
}
