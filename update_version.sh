#!/bin/bash
# gradle.properties の chestshopVersion を書き換える
echo Current version:
grep '^chestshopVersion=' gradle.properties | cut -d= -f2
echo ""
echo Input new version:
read version
sed -i -E "s/^chestshopVersion=.*/chestshopVersion=${version}/" gradle.properties
echo "Updated gradle.properties"
