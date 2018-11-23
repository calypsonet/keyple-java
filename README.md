# Keyple Java

This is the repository for the Java implementation of the [keyple](https://keyple.org/) API.
![global architecture](doc/20181123-Keyple-components.svg "keyple SDK global architecture")

## Supported platforms
- Java SE 1.6 compact2
- Android 4.4 KitKat API level 19

## Documentation
Function specification, Javadoc and compiled JARs are on [keyple-doc](https://calypsonet.github.io/keyple-doc/).

## keyple-java repositories structure

- Modules that are provide as artifacts
  - keyple-core: source and unit tests for the SeProxy module.
  - keyple-calypso: source and unit tests for the Calypso library.
  - keyple-plugin: source and unit tests for the different plugins: smartcard.io PC/SC, Stub, Android NFC, Android OMAPI, etc.
- developer support, testing
  - example: source for the generic and Calypso implementation examples.
  - integration: source for the integration code (SDK).

## Keyple packages' usages
The packages to import in order to implement a ticketing application, a reader plugin, or a SE library to manage a specific solution.
- generic to any SE solution
![generic packages](doc/SeProxyPackage.svg "Keyple generic packages")
- specific to Calypso
![Calypso packages](doc/CalypsoPackage.svg "Calypso packages")


