package org.realityforge.gwt.appcache.server.mgwt;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.realityforge.gwt.appcache.server.AbstractManifestServlet;
import org.realityforge.gwt.appcache.server.BindingProperty;

public class AbstractMgwtManifestServlet
  extends AbstractManifestServlet
{
  @Override
  protected boolean handleUnmatchedRequest( final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final String moduleName,
                                            final String baseUrl,
                                            final List<BindingProperty> computedBindings )
    throws ServletException
  {
    final boolean isIPhoneWithoutCookie = computedBindings.contains( MgwtOsPropertyProvider.iPhone_undefined );
    final boolean isIPadWithoutCookie = computedBindings.contains( MgwtOsPropertyProvider.iPad_undefined );
    if ( isIPhoneWithoutCookie || isIPadWithoutCookie )
    {
      final List<BindingProperty> nonRetinaMatch = new ArrayList<>();
      final List<BindingProperty> retinaMatch = new ArrayList<>();
      if ( isIPhoneWithoutCookie )
      {
        computedBindings.remove( MgwtOsPropertyProvider.iPhone_undefined );
        nonRetinaMatch.add( MgwtOsPropertyProvider.iPhone );
        retinaMatch.add( MgwtOsPropertyProvider.retina );
      }
      else //if ( isIPadWithoutCookie )
      {
        computedBindings.remove( MgwtOsPropertyProvider.iPad_undefined );
        nonRetinaMatch.add( MgwtOsPropertyProvider.iPad );
        retinaMatch.add( MgwtOsPropertyProvider.iPad_retina );
      }

      nonRetinaMatch.addAll( computedBindings );
      retinaMatch.addAll( computedBindings );

      final String[] nonRetinaPermutations = selectPermutations( baseUrl, moduleName, nonRetinaMatch );
      final String[] retinaPermutations = selectPermutations( baseUrl, moduleName, retinaMatch );

      if ( null != nonRetinaPermutations && null != retinaPermutations )
      {
        final String[] permutations = new String[ nonRetinaPermutations.length + retinaPermutations.length ];
        System.arraycopy( nonRetinaPermutations, 0, permutations, 0, nonRetinaPermutations.length );
        System.arraycopy( retinaPermutations,
                          0,
                          permutations,
                          nonRetinaPermutations.length,
                          retinaPermutations.length );
        final String manifest = loadAndMergeManifests( baseUrl, moduleName, permutations );
        serveStringManifest( response, manifest );
        return true;
      }
    }
    return false;
  }
}
