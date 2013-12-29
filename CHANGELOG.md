## 0.7:
* Return a 404 response if unable to determine the appropriate manifest to
  serve rather than a 500.

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
