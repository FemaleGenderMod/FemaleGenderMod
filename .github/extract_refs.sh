#!/usr/bin/env bash

# Extract the branch name from $GITHUB_REF, replacing any slash characters with dashes
# https://github.com/CaffeineMC/sodium-fabric/blob/435a6bd7ecfa58499b64f1a73b61309070929d86/.github/workflows/build-commit.yml#L15
ref="${GITHUB_REF#refs/heads/}" && echo "branch=${ref////-}" >> $GITHUB_OUTPUT
