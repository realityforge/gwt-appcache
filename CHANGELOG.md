## 1.0.1:
* The deferred binding rule for disabling the appcache was tied to the storageSupport
  configuration property rather than the appcacheSupport configuration property. This
  has been fixed. This may have resulted in the code bloat with some older browser
  permutations.

## 1.0:
* Rename ApplicationCache.get to ApplicationCache.getApplicationCacheIfSupported
  for consistency with other frameworks.

## 0.9:
* Cache manifests rather than re-reading them from the filesystem and clear cache
  when permutations.xml is reloaded.
* Use deferred binding to control support for appcache capability.
* Reduce the set of module dependencies declared in the Appcache.gwt.xml file
  to the minimal set.
* Support the notion of client-side selection properties in AbstractManifestServlet.
  If one of these properties is not defined on the server-side then a merged
  manifest that covers all of the permutations that are matched is sent to the
  client and selection occurs on the client-side.
* Change PermutationsIO.deserialize() to return an unmodifiable list.

## 0.8:
* Support multiple selector properties when matching strong and weak permutations.
* Rename PermutationDescriptor to SelectionDescriptor to reflect actual semantics.
* Implement the ApplicationCache.abort() method for the sake of completeness.
* Clear the 'appcache_disable' cookie when instance of Html5ApplicationCache is
  initially created.

## 0.7:
* Remove no-store from Cache-control http header when returning manifest file
  to ensure Firefox can use the manifest file.
* Declare the character set in the content type of the manifest file to ensure
  file names are correctly decoded.
* Use the ProgressEvent.lengthComputable value to determine whether the browser
  has supplied the loaded and total fields on the event.
* Implement equals() and hashcode() on BindingProperty
* Add a utility method AbstractManifestServlet.loadAndMergeManifests that loads
  and merges the cache files for multiple permutations. This is useful when it
  is not possible to determine the exact permutation required for a client on
  the server. This defers the decision to the client but may result in extra
  files being downloaded.
* Extract a ManifestDescriptor class to make managing manifests easier.
* Add a template method AbstractManifestServlet.handleUnmatchedRequest to
  allow sub-classes the opportunity to intercept and handle the scenario
  where a permutation could not be matched.
* In AbstractManifestServlet, return a 404 response if unable to determine
  the appropriate manifest to serve rather than a 500.
* Promote several methods in AbstractManifestServlet from package access to
  protected access to make sub-classing easier.

## 0.6:

* Refactor ApplicationCache.swapCache to return a boolean value indicating
  whether the browser accepted request.
* Rename ApplicationCache.update to requestUpdate and make it return a
  boolean value indicating whether the browser accepted request.
* Remove "appcache_ignorable_permutation_properties" configuration property
  and use builtin mechanisms for determining selection properties.
* Generate a more complete Maven POM file.
* Compile against the last release of GWT 2.5.1 rather than the 2.6 RC1.
* Add javadoc packages to the build process.
* Update the client library to support the attempt to remove the local
  cache by re-requesting manifest with cookie disabling appcache set.
* Support disabling the application cache by setting a cookie named
  "appcache_disable" to "1".
* If the containing html page does not contain a manifest declaration
  then do not create a ApplicationCache in singleton.
* Add the file count and total file count fields to the ProgressEvent.
* Ensure that the file names declared in the manifest file are correctly
  encoded.
* Support pre 1.8 versions of gecko renderer when doing server side
  selection of the manifest file.

## 0.5:

* Convert ApplicationCache into an abstract class and merge in the code from
  the AbstractApplicationCache into ApplicationCache.
* Add tests to ensure event propagation behaviour and de-registration of
  handlers works correctly.

## 0.4:

* Split the server artifact into two so that the linker need not be included
  in the deployed artifact.
* Import the mGWT client code and start to refactor to make it easier to use
  within java based test frameworks.

## 0.3:

* Initial release
