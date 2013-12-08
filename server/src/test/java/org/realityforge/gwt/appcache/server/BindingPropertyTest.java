package org.realityforge.gwt.appcache.server;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class BindingPropertyTest
{
  @Test
  public void basicTest()
  {
    final String key = "MyKey";
    final String value = "MyValue";
    final BindingProperty property = new BindingProperty( key, value );
    assertEquals( property.getName(), key );
    assertEquals( property.getValue(), value );
    assertTrue( property.matches( value ) );
    assertFalse( property.matches( "XXX" + value + "XXX" ) );
  }

  @Test
  public void testMultiValued()
  {
    final String key = "MyKey";
    final String value = "1,2,3";
    final BindingProperty property = new BindingProperty( key, value );
    assertEquals( property.getName(), key );
    assertEquals( property.getValue(), value );
    assertTrue( property.matches( "1" ) );
    assertTrue( property.matches( "2" ) );
    assertTrue( property.matches( "3" ) );
    assertFalse( property.matches( "X" ) );
  }
}
