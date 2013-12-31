package org.realityforge.gwt.appcache.server;

import java.util.ArrayList;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class SelectionDescriptorTest
{
  @Test
  public void basicTest()
  {
    final String permutationName = "P1";
    final BindingProperty p1 = new BindingProperty( "A", "1" );
    final BindingProperty p2 = new BindingProperty( "B", "2" );
    final ArrayList<BindingProperty> bindingProperties = new ArrayList<BindingProperty>();
    bindingProperties.add( p1 );
    bindingProperties.add( p2 );
    final SelectionDescriptor descriptor = new SelectionDescriptor( permutationName, bindingProperties );
    assertEquals( descriptor.getPermutationName(), permutationName );
    assertEquals( descriptor.getBindingProperties().size(), 2 );
    assertTrue( descriptor.getBindingProperties().contains( p1 ) );
    assertTrue( descriptor.getBindingProperties().contains( p2 ) );
    assertTrue( descriptor.getBindingProperties().get( 0 ) == p1 );
    assertTrue( descriptor.getBindingProperties().get( 1 ) == p2 );
  }

  @Test
  public void equalsAndHashcode()
  {
    final String permutationName1 = "P1";
    final String permutationName2 = "P2";
    final BindingProperty bp1 = new BindingProperty( "A", "1" );
    final BindingProperty bp2 = new BindingProperty( "B", "2" );
    final BindingProperty bp3 = new BindingProperty( "C", "3" );

    final ArrayList<BindingProperty> bindingProperties1 = new ArrayList<BindingProperty>();
    bindingProperties1.add( bp1 );
    bindingProperties1.add( bp2 );

    // Different order from bindingProperties1
    final ArrayList<BindingProperty> bindingProperties2 = new ArrayList<BindingProperty>();
    bindingProperties2.add( bp2 );
    bindingProperties2.add( bp1 );

    // Different properties
    final ArrayList<BindingProperty> bindingProperties3 = new ArrayList<BindingProperty>();
    bindingProperties3.add( bp1 );
    bindingProperties3.add( bp3 );

    // Different properties
    final ArrayList<BindingProperty> bindingProperties4 = new ArrayList<BindingProperty>();
    bindingProperties4.add( bp1 );
    bindingProperties4.add( bp2 );
    bindingProperties4.add( bp3 );

    final SelectionDescriptor descriptor1 = new SelectionDescriptor( permutationName1, bindingProperties1 );
    final SelectionDescriptor descriptor2 = new SelectionDescriptor( permutationName1, bindingProperties2 );
    final SelectionDescriptor descriptor3 = new SelectionDescriptor( permutationName1, bindingProperties3 );
    final SelectionDescriptor descriptor4 = new SelectionDescriptor( permutationName1, bindingProperties4 );
    final SelectionDescriptor descriptor5 = new SelectionDescriptor( permutationName2, bindingProperties1 );

    assertTrue( descriptor1.equals( descriptor1 ) );
    assertTrue( descriptor1.equals( descriptor2 ) );
    assertFalse( descriptor1.equals( descriptor3 ) );
    assertFalse( descriptor1.equals( descriptor4 ) );
    assertFalse( descriptor1.equals( descriptor5 ) );

    assertTrue( descriptor2.equals( descriptor1 ) );
    assertTrue( descriptor2.equals( descriptor2 ) );
    assertFalse( descriptor2.equals( descriptor3 ) );
    assertFalse( descriptor2.equals( descriptor4 ) );
    assertFalse( descriptor2.equals( descriptor5 ) );

    assertFalse( descriptor3.equals( descriptor1 ) );
    assertFalse( descriptor3.equals( descriptor2 ) );
    assertTrue( descriptor3.equals( descriptor3 ) );
    assertFalse( descriptor3.equals( descriptor4 ) );
    assertFalse( descriptor3.equals( descriptor5 ) );

    assertFalse( descriptor4.equals( descriptor1 ) );
    assertFalse( descriptor4.equals( descriptor2 ) );
    assertFalse( descriptor4.equals( descriptor3 ) );
    assertTrue( descriptor4.equals( descriptor4 ) );
    assertFalse( descriptor4.equals( descriptor5 ) );

    assertFalse( descriptor5.equals( descriptor1 ) );
    assertFalse( descriptor5.equals( descriptor2 ) );
    assertFalse( descriptor5.equals( descriptor3 ) );
    assertFalse( descriptor5.equals( descriptor4 ) );
    assertTrue( descriptor5.equals( descriptor5 ) );

    assertEquals( descriptor1.hashCode(), descriptor1.hashCode() );
    assertEquals( descriptor1.hashCode(), descriptor2.hashCode() );
    assertNotEquals( descriptor1.hashCode(), descriptor3.hashCode() );
    assertNotEquals( descriptor1.hashCode(), descriptor4.hashCode() );
    assertNotEquals( descriptor1.hashCode(), descriptor5.hashCode() );

    assertEquals( descriptor2.hashCode(), descriptor1.hashCode() );
    assertEquals( descriptor2.hashCode(), descriptor2.hashCode() );
    assertNotEquals( descriptor2.hashCode(), descriptor3.hashCode() );
    assertNotEquals( descriptor2.hashCode(), descriptor4.hashCode() );
    assertNotEquals( descriptor2.hashCode(), descriptor5.hashCode() );

    assertNotEquals( descriptor3.hashCode(), descriptor1.hashCode() );
    assertNotEquals( descriptor3.hashCode(), descriptor2.hashCode() );
    assertEquals( descriptor3.hashCode(), descriptor3.hashCode() );
    assertNotEquals( descriptor3.hashCode(), descriptor4.hashCode() );
    assertNotEquals( descriptor3.hashCode(), descriptor5.hashCode() );

    assertNotEquals( descriptor4.hashCode(), descriptor1.hashCode() );
    assertNotEquals( descriptor4.hashCode(), descriptor2.hashCode() );
    assertNotEquals( descriptor4.hashCode(), descriptor3.hashCode() );
    assertEquals( descriptor4.hashCode(), descriptor4.hashCode() );
    assertNotEquals( descriptor4.hashCode(), descriptor5.hashCode() );

    assertNotEquals( descriptor5.hashCode(), descriptor1.hashCode() );
    assertNotEquals( descriptor5.hashCode(), descriptor2.hashCode() );
    assertNotEquals( descriptor5.hashCode(), descriptor3.hashCode() );
    assertNotEquals( descriptor5.hashCode(), descriptor4.hashCode() );
    assertEquals( descriptor5.hashCode(), descriptor5.hashCode() );
  }
}
