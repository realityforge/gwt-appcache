package org.realityforge.gwt.appcache.server.propertyprovider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

public interface PropertyProvider
{
  @Nonnull
  String getPropertyName();

  @Nullable
  String getPropertyValue( @Nonnull HttpServletRequest request )
    throws Exception;
}
