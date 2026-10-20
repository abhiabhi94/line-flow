# Releasing to the Play Store

Two workflows build the app:

| Workflow | Runs on | Produces |
|----------|---------|----------|
| **CI** (`.github/workflows/ci.yml`) | every push to `main` and every pull request | debug APK, uploaded as the `lineflow-debug-apk` artifact and linked in a PR comment |
| **Release** (`.github/workflows/release.yml`) | every `v*` tag push, or by hand from the Actions tab | signed `.aab` + `.apk` + R8 mapping, uploaded as an artifact and attached to a GitHub Release |

The release build is signed with the upload key stored in repository secrets.
Nothing secret is ever committed: `keystore.properties` and the keystore are
written on the runner from secrets and deleted at the end of the job.

## One-time setup

### 1. The upload keystore

The existing key is `release-key.jks` at the repo root (ignored by git), with
its passwords in `keystore.properties` (also ignored). To create a fresh one:

```bash
keytool -genkey -v -keystore release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

Keep the `.jks` and the passwords somewhere safe and backed up. With Play App
Signing, Google holds the *app* signing key and this is only the *upload*
key, so it can be reset through Play Console support if lost, but that is a
slow process.

### 2. Store it in the `release` environment's secrets

The workflow's job runs in the GitHub environment named **`release`**, so the
secrets go there, not in the repository-wide list: Settings → Environments →
`release` (create it if missing) → Environment secrets. Or with the `gh` CLI
(macOS `base64` takes `-i`, not `-w0`):

```bash
gh secret set --env release ANDROID_UPLOAD_KEYSTORE_BASE64 --body "$(base64 -i release-key.jks)"
gh secret set --env release ANDROID_UPLOAD_KEYSTORE_PASSWORD   # prompts for the value
gh secret set --env release ANDROID_UPLOAD_KEY_ALIAS           # the keyAlias from keystore.properties
gh secret set --env release ANDROID_UPLOAD_KEY_PASSWORD        # optional, see below
```

`ANDROID_UPLOAD_KEY_PASSWORD` is only needed when the key's password differs
from the keystore's. `keytool` creates PKCS12 keystores by default, and those
have a single password for both, so most setups can skip it.

An environment also lets you add protection rules: "Required reviewers" makes
every release build wait for an approval click, and "Deployment branches and
tags" can restrict it to `main` and `v*` tags.

The workflow fails fast with a clear message if a required secret is missing,
and it opens the keystore before building, so a wrong password, a bad alias
or a mangled base64 upload fails in a second rather than after the compile.
That step prints the certificate's SHA-256 fingerprint; compare it with

```bash
keytool -list -v -keystore release-key.jks -alias <keyAlias>
```

on the machine that holds the keystore.

## Cutting a release

1. Bump `versionCode` (always +1, Play requires it strictly higher than any
   build ever uploaded) and `versionName` in `app/build.gradle.kts`. Commit
   as "Bump version" and get it onto `main`.
2. Tag that commit and push the tag. **A tag is `v` + `versionName`**, and the
   workflow refuses to build if they disagree:

   ```bash
   git checkout main && git pull
   git tag v1.1.0 && git push origin v1.1.0
   ```

The tag push runs the Release workflow: unit tests, lint, ktlint and detekt,
then a signed `.aab` and `.apk`, uploaded as the workflow artifact
`lineflow-<name>-<code>` and attached to a GitHub Release named
"LineFlow 1.1.0 (8)".

To build without tagging (a dry run, or an internal-testing upload), use
**Actions → Release → Run workflow** on any branch.

The artifact contains:

| File | Use |
|------|-----|
| `lineflow-<v>.aab` | Upload this to Play Console |
| `lineflow-<v>.apk` | Same signed build as an APK, for a device check |
| `lineflow-<v>-mapping.txt` | R8 mapping. Upload under App bundle explorer → Downloads → "Upload ReTrace mapping file" so crash reports are readable |

In Play Console: Release → Testing (internal) or Production → Create new
release → upload the `.aab` → release notes → review → roll out.

## Building locally instead

`./gradlew bundleRelease` works on any machine that has `keystore.properties`
at the repo root pointing at the `.jks`. Without that file the release build
is **unsigned**, which Play rejects, so the workflow above is the recommended
path.
