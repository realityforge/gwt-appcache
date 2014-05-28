package org.realityforge.gwt.appcache.server;

import java.util.ArrayList;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class PermutationTest
{
  @Test
  public void basicOperation()
    throws Exception
  {
    final Permutation permutation1 = new Permutation( "Foo" );
    assertEquals( permutation1.getPermutationName(), "Foo" );
    assertEquals( permutation1.getSelectors().size(), 0 );
    permutation1.getSelectors().add( new SelectionDescriptor( "Foo", new ArrayList<BindingProperty>() ) );
    assertEquals( permutation1.getSelectors().size(), 1 );
    assertEquals( permutation1.getPermutationFiles().size(), 0 );
    permutation1.getPermutationFiles().add( "foo.txt" );
    assertEquals( permutation1.getPermutationFiles().size(), 1 );
    assertFalse( permutation1.equals( new Permutation( "Bar" ) ) );
    assertEquals( permutation1.getFallbackFiles().size(), 0 );
    permutation1.getFallbackFiles().put( "online.png", "offline.png" );
    assertEquals( permutation1.getFallbackFiles().size(), 1 );
    assertTrue( permutation1.equals( permutation1 ) );
  }
}
