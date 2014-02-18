package org.realityforge.gwt.appcache.server.mgwt;

import org.realityforge.gwt.appcache.server.propertyprovider.UserAgentPropertyProvider;

public class MgwtManifestServlet
  extends AbstractMgwtManifestServlet
{
  public MgwtManifestServlet()
  {
    addPropertyProvider( new MgwtOsPropertyProvider() );
    addPropertyProvider( new UserAgentPropertyProvider() );
    addPropertyProvider( new MobileUserAgentProvider() );
  }
}
