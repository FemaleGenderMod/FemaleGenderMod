from __future__ import annotations

import json
from json.decoder import JSONDecodeError
from glob import glob
from pathlib import Path

# JSON decode errors in any of these files should cause a build to fail
critical = [
    "src/main/resources/fabric.mod.json",
    "src/main/resources/wildfire_gender.mixins.json",
    "src/main/resources/assets/wildfire_gender/sounds.json",
    "src/main/resources/assets/wildfire_gender/lang/en_us.json",
]
should_fail = False

jsons = [Path(x) for x in glob("src/main/resources/**/*.json", recursive=True)]

class Group:
    def __init__(self, name: str):
        self.name = name

    def __enter__(self):
        print(f"::group::{self.name}")

    def __exit__(self, *args):
        print(f"::endgroup::")

for file in jsons:
    with open(file) as f:
        try:
            json.load(f)
        except JSONDecodeError as e:
            print(f"::error file={file!s},line={e.lineno},col={e.colno}::{e!s}")
            if str(file) in critical:
                should_fail = True

if should_fail:
    exit(1)

with open("src/main/resources/assets/wildfire_gender/lang/en_us.json") as f:
    root_translations = json.load(f)

for translation in glob("src/main/resources/assets/wildfire_gender/lang/*.json"):
    file = Path(translation)
    if file.name == "en_us.json":
        continue

    try:
        with open(file) as f:
            strings = json.load(f)
    except JSONDecodeError:
        continue

    missing_from_root = {x for x in strings if x not in root_translations}
    missing_from_translation = {x for x in root_translations if x not in strings}

    with Group(file.name):
        if missing_from_root:
            print(f"::notice file={translation}::Has {len(missing_from_root)} extra translation strings")
            for key in missing_from_root:
                print(f"  - {key}")
        if missing_from_translation:
            print(f"::notice file={translation}::Missing {len(missing_from_translation)} translation strings")
            for key in missing_from_translation:
                print(f"  - {key}")
        if not missing_from_translation and not missing_from_root:
            print("No missing translations!")
