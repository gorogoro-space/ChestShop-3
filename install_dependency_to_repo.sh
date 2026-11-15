#!/bin/bash
# Maven のリポジトリにない依存を、repo/ に Maven 形式(group/artifact/version)で置く。
# Gradle は repo/ を、そのままリポジトリとして読む(build.gradle.kts を参照)。

echo "[Dependency installer]"
read -p "What is your dependency's file name? " filename

if [ "$filename" = "" ]; then
    exit 1
fi

read -p "What is your dependency's group ID? " groupID
read -p "What is your dependency's artifact ID? " artifactID
read -p "What is your dependency's version? " version

dir="repo/$(echo "$groupID" | tr . /)/$artifactID/$version"
mkdir -p "$dir"
cp "$filename" "$dir/$artifactID-$version.jar"
sha1sum "$dir/$artifactID-$version.jar" | cut -d' ' -f1 > "$dir/$artifactID-$version.jar.sha1"
md5sum "$dir/$artifactID-$version.jar" | cut -d' ' -f1 > "$dir/$artifactID-$version.jar.md5"

echo "[Dependency installed to $dir]"
echo "Add it to plugin/build.gradle.kts as: \"$groupID:$artifactID:$version\""
