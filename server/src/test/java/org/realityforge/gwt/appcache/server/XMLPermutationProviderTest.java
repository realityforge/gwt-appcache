package org.realityforge.gwt.appcache.server;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class XMLPermutationProviderTest
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

    final Map<String, List<BindingProperty>> bindings =
      XMLPermutationProvider.deserialize( new ByteArrayInputStream( permutationContent.getBytes( "US-ASCII" ) ) );
    assertEquals( bindings.size(), 3 );
    final List<BindingProperty> binding1 = ensureBinding( bindings, "C7D408F8EFA266A7F9A31209F8AA7446" );
    assertEquals( binding1.size(), 3 );
    assertBinding( binding1, "user.agent", "ie8,ie9,safari,ie10,gecko1_8" );
    assertBinding( binding1, "screen.size", "biggo" );
    assertBinding( binding1, "color.depth", "much" );

    final List<BindingProperty> binding2 = ensureBinding( bindings, "Other2" );
    assertEquals( binding2.size(), 2 );
    assertBinding( binding2, "user.agent", "ie8,ie9,safari,ie10,gecko1_8" );
    assertBinding( binding2, "screen.size", "biggo" );

    final List<BindingProperty> binding3 = ensureBinding( bindings, "Other" );
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

    final HashMap<String, Set<BindingProperty>> input = new HashMap<String, Set<BindingProperty>>();
    final HashSet<BindingProperty> input1 = new HashSet<BindingProperty>();
    input1.add( new BindingProperty( key1, value11 ) );
    input1.add( new BindingProperty( key2, value12 ) );
    input1.add( new BindingProperty( key3, value13 ) );
    input.put( permutation1, input1 );

    final HashSet<BindingProperty> input2 = new HashSet<BindingProperty>();
    input2.add( new BindingProperty( key1, value21 ) );
    input2.add( new BindingProperty( key2, value22 ) );
    input.put( permutation2, input2 );

    final HashSet<BindingProperty> input3 = new HashSet<BindingProperty>();
    input3.add( new BindingProperty( key1, value31 ) );
    input.put( permutation3, input3 );

    final String output = XMLPermutationProvider.serialize( input );
    final Map<String, List<BindingProperty>> bindings =
      XMLPermutationProvider.deserialize( new ByteArrayInputStream( output.getBytes( "US-ASCII" ) ) );

    assertEquals( bindings.size(), 3 );
    final List<BindingProperty> binding1 = ensureBinding( bindings, permutation1 );
    assertEquals( binding1.size(), 3 );
    assertBinding( binding1, key1, value11 );
    assertBinding( binding1, key2, value12 );
    assertBinding( binding1, key3, value13 );

    final List<BindingProperty> binding2 = ensureBinding( bindings, permutation2 );
    assertEquals( binding2.size(), 2 );
    assertBinding( binding2, key1, value21 );
    assertBinding( binding2, key2, value22 );

    final List<BindingProperty> binding3 = ensureBinding( bindings, permutation3 );
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

  private List<BindingProperty> ensureBinding( final Map<String, List<BindingProperty>> bindings,
                                               final String permutationName )
  {
    final List<BindingProperty> binding1 = bindings.get( permutationName );
    assertNotNull( binding1 );
    return binding1;
  }
}
