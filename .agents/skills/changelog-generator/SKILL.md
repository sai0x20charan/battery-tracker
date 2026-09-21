---
name: changelog-generator
description: Generate user-facing changelog from git history since the last tag for Mobile and/or Wear OS modules and write to the respective CHANGELOG.md.
---

# Changelog Generator

Generate a user-facing changelog from the git commit history since the latest tag and write it to the respective `CHANGELOG.md` in `mobile/src/main/assets/CHANGELOG.md` and/or `wear/src/main/assets/CHANGELOG.md`.

## Instructions

### 1. Determine Target Module(s)

Identify whether the changelog is being generated for:
* **Mobile**: `mobile` module
* **Wear OS**: `wear` module
* **Both**: when unspecified or full release

### 2. Get the Latest Relevant Tag

Run:

```bash
git describe --tags --abbrev=0
```

Or find target-specific tags if applicable:
* For Mobile: `git tag -l "*mobile*" --sort=v:refname | tail -n 1` (or latest general tag: `git tag -l "v*" --sort=v:refname | tail -n 1`)
* For Wear OS: `git tag -l "*wear*" --sort=v:refname | tail -n 1` (or latest general tag: `git tag -l "v*" --sort=v:refname | tail -n 1`)

Use the tag as the starting point for analyzing commits.

### 3. Retrieve Commit History

Run:

* For Mobile:
```bash
git log <tag>..HEAD --pretty=format:"%h %s%n%b" --no-merges -- mobile/
```
* For Wear OS:
```bash
git log <tag>..HEAD --pretty=format:"%h %s%n%b" --no-merges -- wear/
```
* For Both / All:
```bash
git log <tag>..HEAD --pretty=format:"%h %s%n%b" --no-merges
```

Also inspect commit messages following conventional commit scopes (e.g. `feat(mobile):`, `feat(wear):`, `fix(mobile):`, `fix(wear):`).

### 4. Rewrite Commit Messages

Rewrite commits into clear, user-friendly descriptions:

* Begin each sentence with a capital letter.
* Use concise and easy-to-understand language.
* End each description with proper punctuation.
* Minimize technical jargon when possible.
* Do not include commit hashes.

### 5. Categorize Changes

Group the rewritten commits into the following sections:

* **New Features**
    * User-visible functionality and additions.

* **Improvements**
    * Enhancements, optimizations, UI refinements, performance improvements, and refactors.

* **Bug Fixes**
    * Fixes for crashes, incorrect behavior, and UI issues.

#### Exclude

Do not include:
* Chores
* CI/CD changes
* Build configuration changes
* Dependency updates
* Developer-only changes that do not affect users

### 6. Determine the App Version

Extract the current app version from `versionName`:
* For Mobile: `mobile/build.gradle.kts`
* For Wear OS: `wear/build.gradle.kts`

### 7. Generate the Changelog

Overwrite the target changelog file:
* Mobile: [mobile/src/main/assets/CHANGELOG.md](mobile/src/main/assets/CHANGELOG.md)
* Wear OS: [wear/src/main/assets/CHANGELOG.md](wear/src/main/assets/CHANGELOG.md)

Format:

```markdown
## New Features
- <feature 1>
- <feature 2>

## Improvements
- <improvement 1>
- <improvement 2>

## Bug Fixes
- <bug fix 1>
- <bug fix 2>
```

## Rules

* Remove any section that has no entries.
* Preserve the order of changes within each section.
* Keep the text concise to fit under Google Play's 500-character release notes limit.
* Overwrite the existing `CHANGELOG.md` file for the target module.
