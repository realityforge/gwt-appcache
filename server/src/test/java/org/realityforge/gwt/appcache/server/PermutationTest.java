package org.realityforge.gwt.appcache.server;

import java.util.HashSet;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class PermutationTest
{
  @Test
  public void basicOperation()
    throws Exception
  {
    final Permutation permutation1 = new Permutation( "Foo" );
    final Permutation permutation2 = new Permutation( "Bar" );
    assertEquals( permutation1.getPermutationName(), "Foo" );
    assertEquals( permutation1.getBindingProperties().size(), 0 );
    permutation1.getBindingProperties().put( 0, new HashSet<BindingProperty>() );
    assertEquals( permutation1.getBindingProperties().size(), 1 );
    assertFalse( permutation1.equals( permutation2 ) );
    assertTrue( permutation1.equals( permutation1 ) );
  }
}
