**Folder content**
---

* Two main branches to demonstrate the use of keyple
    - generic: examples of keyple-core implementation targeting all kinds of secure elements
    - calypso : examples of keyple-calypso implementation targeting specific functionalities of Calypso secure elements

**Build configuration**
---
### Building the examples and the artifacts
**The first time you build the project**, you need to build artifacts in order of their dependencies, each of those artifacts will be published in your mavenLocal repository. 

###Linux or Macos
Following commands will build the artifacts one by one. The first command is required to build the gradle wrapper.  

```
gradle wrapper --gradle-version 4.5.1
./gradlew :keyple-core:build  --info
./gradlew :keyple-calypso:build  --info
./gradlew :keyple-plugin:keyple-plugin-pcsc:build  --info
./gradlew :keyple-plugin:keyple-plugin-stub:build  --info
./gradlew :keyple-plugin:keyple-plugin-remotese:build --info
./gradlew :example:generic:example-generic-common:build  :example:generic:example-generic-pc:build
./gradlew :example:calypso:example-calypso-common:build  :example:calypso:example-calypso-pc:build
./gradlew :keyple-plugin:keyple-plugin-android-nfc:build --info
./gradlew :keyple-plugin:keyple-plugin-android-omapi:build --info
```
Once the artifacts are saved into your local maven repository (usually $USER_HOME/.m2) you can build any artifact in any order.  
Optionally if you want to build the application apk package.

```
./gradlew -b ./example/calypso/android/nfc/build.gradle assembleDebug 
./gradlew -b ./example/calypso/android/omapi/build.gradle assembleDebug
```

###Windows
Following commands will build the artifacts one by one. The first command is required to build the gradle wrapper.

```
gradle wrapper --gradle-version 4.5.1
.\gradlew.bat :keyple-core:build  --info
.\gradlew.bat :keyple-calypso:build  --info
.\gradlew.bat :keyple-plugin:keyple-plugin-pcsc:build  --info
.\gradlew.bat :keyple-plugin:keyple-plugin-stub:build  --info
.\gradlew.bat :keyple-plugin:keyple-plugin-remotese:build  --info
.\gradlew.bat :example:generic:example-generic-common:build  :example:generic:example-generic-pc:build
.\gradlew.bat :example:calypso:example-calypso-common:build  :example:calypso:example-calypso-pc:build
.\gradlew.bat :keyple-plugin:keyple-plugin-android-nfc:build --info
.\gradlew.bat :keyple-plugin:keyple-plugin-android-omapi:build --info
```

Once the artifacts are saved into your local maven repository (usually $USER_HOME/.m2) you can build any artifact in any order.  
Optionally if you want to build the application apk package.

```
.\gradlew.bat -b ./example/calypso/android/nfc/build.gradle assembleDebug 
.\gradlew.bat -b ./example/calypso/android/omapi/build.gradle assembleDebug
```


**Log configuration**
---
The application log output format is configurable in the properties files
`resources/simplelogger.properties`.
The user will mainly be interested in setting the log level with the `org.slf4j.simpleLogger.defaultLogLevel` field (see the documentation inside the file).
