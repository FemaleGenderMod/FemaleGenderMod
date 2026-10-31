#!/usr/bin/env bash

# Extract the branch name from $GITHUB_REF, replacing any slash characters with dashes
# https://github.com/CaffeineMC/sodium-fabric/blob/435a6bd7ecfa58499b64f1a73b61309070929d86/.github/workflows/build-commit.yml#L15
ref="${GITHUB_REF#refs/heads/}" && echo "branch=${ref////-}" >> $GITHUB_OUTPUT

# Extract the Minecraft version from gradle.properties
minecraft_version=$(grep 'minecraft_version=' gradle.properties --color=never) && echo $minecraft_version >> $GITHUB_OUTPUT

### Build version summary

neoforge_version=$(grep 'neo_version=' gradle.properties --color=never)
neoforge_version="${neoforge_version#neo_version=}"

echo "## Version summary" >> $GITHUB_STEP_SUMMARY
echo "" >> $GITHUB_STEP_SUMMARY
echo "- Targeting Minecraft version ${minecraft_version#minecraft_version=}" >> $GITHUB_STEP_SUMMARY
echo "- Using NeoForge version $neoforge_version" >> $GITHUB_STEP_SUMMARY
