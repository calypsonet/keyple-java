#!/bin/bash

. .circleci/functions.sh

mkdir -p ~/artifacts/jars ~/pages/jars
find . -name "keyple-*.jar" \
  -exec cp  -rv {} ~/artifacts/jars \; \
  -exec cp  -rv {} ~/pages/jars \;

save_directory keyple-core
save_directory keyple-calypso
save_directory keyple-plugin/pcsc
save_directory keyple-plugin/stub
save_directory keyple-plugin/remotese

cp .build/web/* ~/pages/

echo "End of save artifacts java"



