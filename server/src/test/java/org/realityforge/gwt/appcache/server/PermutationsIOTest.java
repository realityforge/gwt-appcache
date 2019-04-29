package org.realityforge.gwt.appcache.server;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class PermutationsIOTest
{
  @Test
  public void deserialize()
    throws Exception
  {
    final String permutationContent =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<permutations>\n" +
      "   <permutation name=\"C7D408F8EFA266A7F9A31209F8AA7446\">\n" +
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

    final List<SelectionDescriptor> descriptors =
      PermutationsIO.deserialize( new ByteArrayInputStream( permutationContent.getBytes( "US-ASCII" ) ) );
    assertEquals( descriptors.size(), 3 );
    final List<BindingProperty> binding1 = ensureBinding( descriptors, "C7D408F8EFA266A7F9A31209F8AA7446" );
    assertEquals( binding1.size(), 3 );
    assertBinding( binding1, "user.agent", "ie8,ie9,safari,ie10,gecko1_8" );
    assertBinding( binding1, "screen.size", "biggo" );
    assertBinding( binding1, "color.depth", "much" );

    final List<BindingProperty> binding2 = ensureBinding( descriptors, "Other2" );
    assertEquals( binding2.size(), 2 );
    assertBinding( binding2, "user.agent", "ie8,ie9,safari,ie10,gecko1_8" );
    assertBinding( binding2, "screen.size", "biggo" );

    final List<BindingProperty> binding3 = ensureBinding( descriptors, "Other" );
    assertEquals( binding3.size(), 1 );
    assertBinding( binding3, "user.agent", "ie8,ie9,safari,ie10,gecko1_8" );
  }

  @Test
  public void serialize()
    throws Exception
  {
    final String permutation1 = "C7D408F8EFA266A7F9A31209F8AA7446";
    final String permutation2 = "C7D408F8EFA2AAAAAAAAAAAAAAAAAAAA";
    final String permutation3 = "C7D408F8EFA2AAAAAABBBBBBBBBBBBBB";
    final String key1 = "user.agent";
    final String key2 = "screen.size";
    final String key3 = "color.depth";
    final String value11 = "ie8,ie9,safari,ie10,gecko1_8";
    final String value12 = "biggo";
    final String value13 = "much";
    final String value21 = "ie8,ie9,safari,ie10";
    final String value22 = "smallish";
    final String value31 = "ie8,ie9,ie10,gecko1_8";

    final ArrayList<SelectionDescriptor> input = new ArrayList<>();
    final ArrayList<BindingProperty> input1 = new ArrayList<>();
    input1.add( new BindingProperty( key1, value11 ) );
    input1.add( new BindingProperty( key2, value12 ) );
    input1.add( new BindingProperty( key3, value13 ) );
    input.add( new SelectionDescriptor( permutation1, input1 ) );

    final ArrayList<BindingProperty> input2 = new ArrayList<>();
    input2.add( new BindingProperty( key1, value21 ) );
    input2.add( new BindingProperty( key2, value22 ) );
    input.add( new SelectionDescriptor( permutation2, input2 ) );

    final ArrayList<BindingProperty> input3 = new ArrayList<>();
    input3.add( new BindingProperty( key1, value31 ) );
    input.add( new SelectionDescriptor( permutation3, input3 ) );

    final String output = PermutationsIO.serialize( input );
    final List<SelectionDescriptor> descriptors =
      PermutationsIO.deserialize( new ByteArrayInputStream( output.getBytes( "US-ASCII" ) ) );

    assertEquals( descriptors.size(), 3 );
    final List<BindingProperty> binding1 = ensureBinding( descriptors, permutation1 );
    assertEquals( binding1.size(), 3 );
    assertBinding( binding1, key1, value11 );
    assertBinding( binding1, key2, value12 );
    assertBinding( binding1, key3, value13 );

    final List<BindingProperty> binding2 = ensureBinding( descriptors, permutation2 );
    assertEquals( binding2.size(), 2 );
    assertBinding( binding2, key1, value21 );
    assertBinding( binding2, key2, value22 );

    final List<BindingProperty> binding3 = ensureBinding( descriptors, permutation3 );
    assertEquals( binding3.size(), 1 );
    assertBinding( binding3, key1, value31 );
  }

  private void assertBinding( final List<BindingProperty> binding1, final String key, final String value )
  {
    assertEquals( getProperty( binding1, key ).getValue(), value );
  }

  private BindingProperty getProperty( final List<BindingProperty> binding, final String key )
  {
    for ( final BindingProperty bindingProperty : binding )
    {
      if ( bindingProperty.getName().equals( key ) )
      {
        return bindingProperty;
      }
    }
    fail( "Failed to find property " + key );
    return null;
  }

  private List<BindingProperty> ensureBinding( final List<SelectionDescriptor> descriptors,
                                               final String permutationName )
  {
    for ( final SelectionDescriptor descriptor : descriptors )
    {
      if ( descriptor.getPermutationName().equals( permutationName ) )
      {
        return descriptor.getBindingProperties();
      }
    }
    fail( "Unable to locate permutation: " + permutationName );
    return null;
  }
}
