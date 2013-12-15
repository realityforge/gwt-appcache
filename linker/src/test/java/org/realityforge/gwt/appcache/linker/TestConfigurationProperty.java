package org.realityforge.gwt.appcache.linker;

import com.google.gwt.core.ext.linker.ConfigurationProperty;
import java.util.List;

final class TestConfigurationProperty
  implements ConfigurationProperty, Comparable<TestConfigurationProperty>
{

  private final String _name;
  private final List<String> _values;

  TestConfigurationProperty( final String name, final List<String> values )
  {
    _name = name;
    _values = values;
  }

  @Override
  public int compareTo( final TestConfigurationProperty o )
  {
    return _name.compareTo( o.getName() );
  }

  @Override
  public String getName()
  {
    return _name;
  }

  @Override
  public String getValue()
  {
    throw new IllegalStateException();
  }

  @Override
  public List<String> getValues()
  {
    return _values;
  }

  @Override
  public boolean hasMultipleValues()
  {
    return _values.size() > 1;
  }
}
