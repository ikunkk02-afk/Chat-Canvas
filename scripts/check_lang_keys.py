#!/usr/bin/env python3
"""Validate Chat Canvas language JSON files and compare their key sets."""

import json
from pathlib import Path


LANG_DIR = Path(__file__).resolve().parents[1] / "src/main/resources/assets/chat_canvas/lang"
LANGUAGES = ("en_us", "zh_cn", "zh_tw")


def load_language(code: str) -> dict[str, str]:
    path = LANG_DIR / f"{code}.json"

    def reject_duplicates(pairs: list[tuple[str, str]]) -> dict[str, str]:
        result: dict[str, str] = {}
        for key, value in pairs:
            if key in result:
                raise ValueError(f"{path}: duplicate key: {key}")
            result[key] = value
        return result

    raw = path.read_bytes()
    if raw.startswith(b"\xef\xbb\xbf"):
        raise ValueError(f"{path}: UTF-8 BOM is not allowed")
    language = json.loads(raw.decode("utf-8"), object_pairs_hook=reject_duplicates)
    invalid = [key for key, value in language.items() if not isinstance(value, str)]
    if invalid:
        raise ValueError(f"{path}: non-string values: {', '.join(invalid)}")
    return language


def main() -> None:
    languages = {code: load_language(code) for code in LANGUAGES}
    reference = set(languages[LANGUAGES[0]])
    failed = False
    for code in LANGUAGES:
        keys = set(languages[code])
        missing = sorted(reference - keys)
        extra = sorted(keys - reference)
        print(f"{code}: {len(keys)} keys")
        if missing:
            failed = True
            print(f"  missing: {', '.join(missing)}")
        if extra:
            failed = True
            print(f"  extra: {', '.join(extra)}")
    if failed:
        raise SystemExit(1)
    print("Language key sets match; JSON is valid and contains no duplicate keys.")


if __name__ == "__main__":
    main()
