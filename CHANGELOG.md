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
