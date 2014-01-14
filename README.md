gwt-appcache
============

[![Build Status](https://secure.travis-ci.org/realityforge/gwt-appcache.png?branch=master)](http://travis-ci.org/realityforge/gwt-appcache)

The HTML5 Appcache specification is a mechanism for enabling offline
HTML5 applications. This library provides a simple way to generate the
required cache manifests and serve a separate manifest for each separate
permutation. The library also provides support for the browser side aspects
of the appcache specification. See the appendix section includes further
references concerning the appcache spec.

Quick Start
-----------

The simplest way to appcache enable a GWT application is to;

* add the following dependencies into the build system. i.e.

```xml
<dependency>
   <groupId>org.realityforge.gwt.appcache</groupId>
   <artifactId>gwt-appcache-client</artifactId>
   <version>1.0.1</version>
   <scope>provided</scope>
</dependency>
<dependency>
   <groupId>org.realityforge.gwt.appcache</groupId>
   <artifactId>gwt-appcache-linker</artifactId>
   <version>1.0.1</version>
   <scope>provided</scope>
</dependency>
<dependency>
   <groupId>org.realityforge.gwt.appcache</groupId>
   <artifactId>gwt-appcache-server</artifactId>
   <version>1.0.1</version>
</dependency>
```

* add the following snippet into the .gwt.xml file.

```xml
<module rename-to='myapp'>
  ...

  <!-- Enable the client-side library -->
  <inherits name="org.realityforge.gwt.appcache.Appcache"/>

  <!-- Enable the linker -->
  <inherits name="org.realityforge.gwt.appcache.linker.Linker"/>

  <!-- enable the linker that generates the manifest -->
  <add-linker name="appcache"/>

  <!-- configure all the static files not managed by the GWT compiler -->
  <extend-configuration-property name="appcache_static_files" value="./"/>
  <extend-configuration-property name="appcache_static_files" value="index.html"/>
</module>
```

* configure html that launches the application to look for the manifest.

```xml
<!doctype html>
<html manifest="myapp.appcache">
   ...
</html>
```

* declare the servlet that serves the manifest.

```java
@WebServlet( urlPatterns = { "/myapp.manifest" } )
public class ManifestServlet
  extends AbstractManifestServlet
{
  public ManifestServlet()
  {
    addPropertyProvider( new UserAgentPropertyProvider() );
  }
}
```

* interact with the application from within the browser.

```java
final ApplicationCache cache = ApplicationCache.getApplicationCacheIfSupported();
if ( null != cache )
{
  cache.addUpdateReadyHandler( new UpdateReadyEvent.Handler()
  {
    @Override
    public void onUpdateReadyEvent( @Nonnull final UpdateReadyEvent event )
    {
      //Force a cache update if new version is available
      cache.swapCache();
    }
  } );

  // Ask the browser to recheck the cache
  cache.requestUpdate();

  ...
```


This should be sufficient to get your application using the appcache. If you
load the application in a modern browser you should see it making use of the
cache in the console.

A very simple example of this code is available in the
[gwt-appcache-example](https://github.com/realityforge/gwt-appcache-example)
project.

How does it work?
-----------------

For every permutation generated by the GWT compiler, a separate manifest file
is generated. The manifest includes almost all public resources generated by
GWT with the exception of some used during debugging and development (i.e.
`myapp.devmode.js` and `compilation-mappings.txt`). The manifest also includes
any additional files declared using the "`appcache_static_files`" configuration
setting.

After the GWT compiler has generated all the different permutations, a single
xml descriptor `permutations.xml` is generated that lists all the permutations
and the  deferred-binding properties that were used to uniquely identify the
permutations. Typically these include values of properties such as "`user.agent`".

If the compiler is using soft permutations then it is possible that multiple
deferred-binding properties will be served using a single permutation, in which
case the descriptor will have comma separated values in the `permutations.xml`
for that permutation.

The manifest servlet is then responsible for reading the `permutations.xml` and
inspecting the incoming request and generating properties that enable it to select
the correct permutation and thus the correct manifest file. The selected manifest
file is returned to the requester.

How To: Define a new Selection Configuration
--------------------------------------------

Sometimes it is useful to define a new configuration property in the gwt module
descriptors that will define new permutations. A fairly typical example would
be to define a configuration property that defines different view modalities.
i.e. Is the device phone-like, tablet-like or a desktop. This would drive the
ui and workflow in the application.

Step 1 is to define the configuration in the gwt module descriptor. i.e.

```xml
<define-property name="ui.modality" values="phone, tablet, desktop"/>
  <property-provider name="ui.modality"><![CDATA[
  {
    var ua = window.navigator.userAgent.toLowerCase();
    if ( ua.indexOf('android') != -1 ) { return 'phone'; }
    if ( ua.indexOf('iphone') != -1 ) { return 'phone'; }
    if ( ua.indexOf('ipad') != -1 ) { return 'tablet'; }
    return 'desktop';
  }
]]></property-provider>
```

Step 2 is to use the new configuration property to control the deferred binding
rules in gwt modules. For example, the following could be added to a .gwt.xml
module file;

```xml
<replace-with class="com.biz.client.gin.DesktopInjectorWrapper">
  <when-type-is class="com.biz.client.gin.InjectorWrapper"/>
  <when-property-is name="ui.modality" value="desktop"/>
</replace-with>

<replace-with class="com.biz.client.gin.TableInjectorWrapper">
  <when-type-is class="com.biz.client.gin.InjectorWrapper"/>
  <when-property-is name="ui.modality" value="tablet"/>
</replace-with>

<replace-with class="com.biz.client.gin.PhoneInjectorWrapper">
  <when-type-is class="com.biz.client.gin.InjectorWrapper"/>
  <when-property-is name="ui.modality" value="phone"/>
</replace-with>
```

Step 3 is to define a property provider for your new configuration property and
add it to the manifest servlet. i.e.

```java
public class UIModalityPropertyProvider
  implements PropertyProvider
{
  @Override
  public String getPropertyValue( final HttpServletRequest request )
  {
    final String ua = request.getHeader( "User-Agent" ).toLowerCase();
    if ( ua.contains( "android" ) || ua.contains( "phone" ) ) { return "phone"; }
    else if ( ua.contains( "ipad" ) ) { return "tablet"; }
    else { return "desktop"; }
  }

  @Override
  public String getPropertyName()
  {
    return "ui.modality";
  }
}
```

```java
@WebServlet( urlPatterns = { "/myapp.manifest" } )
public class ManifestServlet
  extends AbstractManifestServlet
{
  public ManifestServlet()
  {
    addPropertyProvider( new UIModalityPropertyProvider() );
    addPropertyProvider( new UserAgentPropertyProvider() );
  }
}
```

This example demonstrates a simple mechanism for supporting server-side derivable
configuration properties to select a permutation. In some cases, the selection
property can only be determined on the client. This scenario is more complex and
requires a combination of cookies and dynamic host pages to address.

How To: Define a new client-side selection Configuration
--------------------------------------------------------

Sometimes configuration properties can only be determined on the client. A good
example is the device pixel density that can be determined by inspecting the
"window.devicePixelRatio" property in the browser.

```xml
<define-property name="pixel.density" values="high, low"/>
  <property-provider name="pixel.density"><![CDATA[
  {
  if(window.devicePixelRatio >= 2) { return 'high'; }
  return 'low';
  }
]]></property-provider>
```

The gwt-appcache library can defer the selection of the property to the client-side
by merging the manifests of the high and low density permutations and returning
the merged manifest to the client. This is done by marking the "pixel.density"
 property as client-side via;

```java
@WebServlet( urlPatterns = { "/myapp.manifest" } )
public class ManifestServlet
  extends AbstractManifestServlet
{
  public ManifestServlet()
  {
    addPropertyProvider( new UserAgentPropertyProvider() );
    ...
    addClientSideSelectionProperty( "pixel.density" );
  }
}
```

This will mean that the client ultimately caches extra data that may not be used
by the client. This may be acceptable for small applications but a better approach
is to detect the pixel density and set a cookie prior to navigating to the page
that hosts the application. The server can then attempt to determine the value of
the configuration property using the cookie name like;


```java
public class PixelDensityPropertyProvider
  implements PropertyProvider
{
  @Override
  public String getPropertyName() { return "pixel.density"; }

  @Override
  public String getPropertyValue( HttpServletRequest request )
  {
    final Cookie[] cookies = request.getCookies();
    if ( null != cookies )
    {
      for ( final Cookie cookie : cookies )
      {
        if ( "pixel.density".equals( cookie.getName() ) )
        {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
}
```

```java
@WebServlet( urlPatterns = { "/myapp.manifest" } )
public class ManifestServlet
  extends AbstractManifestServlet
{
  public ManifestServlet()
  {
    addPropertyProvider( new UserAgentPropertyProvider() );
    addPropertyProvider( new PixelDensityPropertyProvider() );
    ...
    addClientSideSelectionProperty( "pixel.density" );
  }
}
```

How To: Integrate into existing framework
-----------------------------------------

The gwt-appcache library was designed to be easy to integrate into any other
gwt framework. A good example is the wonderful [MGWT](https://github.com/dankurka/mgwt)
library from which this project was initially derived. MGWT selects the permutation
based on the following configuration properties;

* `mgwt.os` - `iphone`, `iphone_retina`, `ipad`, `ipad_retina`, `android`, `android_tablet`, `blackberry` etc.
* `mobile.user.agent` - `mobilesafari` vs `not_mobile`.
* `user.agent` - A standard gwt configuration property.
* `phonegap.env` - Always `no` for web applications.

It is important to the MGWT framework to distinguish between retina and non-retina versions of
the iphone and ipad variants. The retina versions inspect the `window.devicePixelRatio` browser property
similarly to the above `pixel.density` example. Rather than making this a separate configuration
property, MGWT conflates this with operating system. As a result it uses a custom strategy to
merge the multiple permutations manifests as can be observed at [Html5ManifestServletBase](https://github.com/realityforge/mgwt/blob/use_gwt_appcache/src/main/java/com/googlecode/mgwt/linker/server/Html5ManifestServletBase.java#L16-L63).
MGWT also defines several [property providers](https://github.com/realityforge/mgwt/tree/use_gwt_appcache/src/main/java/com/googlecode/mgwt/linker/server/propertyprovider).
There is a [pull request](https://github.com/dankurka/mgwt/pull/37) where you can look at the
work required to re-integrate the functionality back into the MGWT framework. This is a good
example of complex integration of `gwt-appcache`.

Appendix
--------

* [A Beginner's Guide to Using the Application Cache](http://www.html5rocks.com/en/tutorials/appcache/beginner/)
* [Appcache Facts](http://appcachefacts.info/)
* [Offline Web Application Standard](http://www.whatwg.org/specs/web-apps/current-work/multipage/offline.html)

Credit
------

This library began as a enhancement of similar functionality in the
[MGWT](https://github.com/dankurka/mgwt) project by Daniel Kurka. All
credit goes to Daniel for the initial code and idea.
