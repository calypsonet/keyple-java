#!/bin/bash

. .circleci/functions.sh

mkdir -p ~/artifacts/jars ~/pages/jars
find . -name "keyple-*.jar" \
  -exec cp {} ~/artifacts/jars \; \
  -exec cp {} ~/pages/jars \;

save_directory keyple-core
save_directory keyple-calypso
save_directory example/common
save_directory example/pc
save_directory keyple-plugin/pcsc
save_directory keyple-plugin/stub

cp .build/web/* ~/pages/

echo "End of save artifacts java"



