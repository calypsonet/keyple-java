

# Current issues

## Library ones
- Bad exception handling logic
  - We have too many of them
  - No use of inheritance
  - Most of them contain zero added information compared to the default `Exception`
  - Lots of them should be renamed, e.g. `TimeoutReaderException` --> `ReaderTimeoutException`
  - We should probably avoid to impose catching  (`throws` ...) so many (or any ?) of them
  - All the ones that are about possible IO issues (channel closed, timeout, etc.) should inherit `IOException`. Which
    is probably the only kind of Exception we should impose (with an inherited exception like `CalypsoIOException`).
  - Some unit tests are not passing anymore

- The code is WRONG
  - Tries to conform with all the most painful things of the java eco-system
  - Lots of absurd

## Other
- Unit tests are not compatible with the code anymore (this means continuous integration was disabled on ixxi side at some point)
- Integrating the ZIP changes are quite painful because they were done mannually:
  - ISO-8859-1 encoding
  - packages got moved without updating the rest of the code
  - classes got deleted without updating the calling code
- This means we should most probably spend some time just to see how we can take advantage of an IDE
