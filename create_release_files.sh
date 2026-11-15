#!/bin/bash

build_release() {
  platform=$1
  echo "Creating jar for $platform"
  mkdir -p build/releases/$platform
  rm -f build/releases/$platform/ChestShop.jar
  cp build/libs/ChestShop.jar build/releases/$platform/ChestShop.jar
  unzip -o build/releases/$platform/ChestShop.jar "META-INF/MANIFEST.MF" -d .
  sed -i -E "s/Distribution-Type: \w+/Distribution-Type: $platform/g" META-INF/MANIFEST.MF
  zip -m --update build/releases/$platform/ChestShop.jar META-INF/MANIFEST.MF
  rm -d META-INF
}

echo Current version:
version=$(grep '^chestshopVersion=' gradle.properties | cut -d= -f2)
echo $version
if [[ "$version" == *"-SNAPSHOT" ]] ; then
  echo "Not a release"
  exit 0;
fi

echo "Detected release. Building platform release jars..."

if [[ ! -f build/libs/ChestShop.jar ]]; then
  echo "No built jar file found at build/libs/ChestShop.jar! Aborting."
  exit 0;
fi

build_release 'spigot'
build_release 'devbukkit'
build_release 'modrinth'
build_release 'github'
build_release 'hangar'

echo "Release jar files created in build/releases/"