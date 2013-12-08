package org.realityforge.gwt.appcache.server;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class BindingPropertyTest
{
  @Test
  public void basicTest()
  {
    final BindingProperty property = new BindingProperty( "X", "V" );
    assertEquals( property.getName(), "X" );
    assertEquals( property.getValue(), "V" );
  }
}
