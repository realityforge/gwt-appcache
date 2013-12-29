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
    assertEquals( property.getComponents().length, 1 );
    assertTrue( property.matches( value ) );
    assertFalse( property.matches( "XXX" + value + "XXX" ) );
  }

  @Test
  public void equals()
  {
    final String key = "MyKey";
    final String value = "MyValue";
    final BindingProperty property1 = new BindingProperty( key, value );
    final BindingProperty property2 = new BindingProperty( key, value );
    final BindingProperty property3 = new BindingProperty( key, value + "X" );
    final BindingProperty property4 = new BindingProperty( key + "X", value );
    assertTrue( property1.equals( property1 ) );
    assertTrue( property1.equals( property2 ) );
    assertFalse( property1.equals( property3 ) );
    assertFalse( property1.equals( property4 ) );
  }

  @Test
  public void testMultiValued()
  {
    final String key = "MyKey";
    final String value = "1,2,3";
    final BindingProperty property = new BindingProperty( key, value );
    assertEquals( property.getName(), key );
    assertEquals( property.getValue(), value );
    assertEquals( property.getComponents().length, 3 );
    assertTrue( property.matches( "1" ) );
    assertTrue( property.matches( "2" ) );
    assertTrue( property.matches( "3" ) );
    assertFalse( property.matches( "X" ) );
  }
}
