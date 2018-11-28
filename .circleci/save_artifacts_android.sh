#!/bin/bash

. .circleci/functions.sh

#save artifacts of keyple core projects (java)
/bin/bash .circleci/save_artifacts.sh

mkdir -p ~/artifacts/jars ~/pages/jars
find . -name "*.aar"  \
  -exec cp -rv {} ~/artifacts/jars \; \
  -exec cp -rv {} ~/pages/jars \;

save_directory keyple-plugin/android-nfc
save_directory keyple-plugin/android-omapi

echo "End of save artifacts android"

