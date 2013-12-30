package org.realityforge.gwt.appcache.client.event;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ProgressEventTest
{
  @Test
  public void isLengthComputable()
  {
    assertFalse( new ProgressEvent( 0, 0 ).isLengthComputable() );
    assertTrue( new ProgressEvent( 1, 2 ).isLengthComputable() );
  }
}
