# Build and release QAtlas

QAtlas has 3 Docker images and 1 Java library:

| What | Where it goes | Built from |
|---|---|---|
| `qatlas/backend` | Docker Hub | `backend/Dockerfile` (context = repo root) |
| `qatlas/frontend` (classic UI) | Docker Hub | `frontend/Dockerfile` (after `mvn -pl frontend package`) |
| `qatlas/qatlas-ui` (React UI) | Docker Hub | `qatlas-ui/Dockerfile` |
| `org.qatlas:qatlas-client` | Maven Central | `api-client/` |

The version for **all** of them is in `.mvn/maven.config`:

```
-Drevision=1.1.0-SNAPSHOT
```

Change only this line when you start a new version.

---

## Part 1 – GitHub Actions (main way)

Workflow file: `.github/workflows/build-and-publish.yml`

| You do | What happens |
|---|---|
| Push a commit / open a PR | Everything is built. Nothing is pushed. |
| **Actions → Build and publish → Run workflow**, pick a branch | Build + push images with the version tag (e.g. `1.1.0-SNAPSHOT`). Tick boxes to also tag `latest` or publish the api-client. |
| Push a git tag equal to the version (e.g. `1.1.0`) | Build + push images + `latest` + publish api-client. The tag must match `.mvn/maven.config`, or the run fails. |

> The "Run workflow" button is shown only when the workflow file is on the
> **default branch (`main`)**. Merge it to `main` once, and also keep it on
> the branch you release from.

### One-time setup: secrets

Go to **GitHub → qatlas-org/qatlas → Settings → Secrets and variables → Actions → New repository secret**
and add:

| Secret | Value | Where to get it |
|---|---|---|
| `DOCKERHUB_USERNAME` | Docker Hub user that can push to `qatlas/*` | – |
| `DOCKERHUB_TOKEN` | Access token, permission **Read & Write** | hub.docker.com → Account settings → Personal access tokens |
| `CENTRAL_USERNAME` | User token – username part | central.sonatype.com → your account → Generate User Token |
| `CENTRAL_PASSWORD` | User token – password part | same place |
| `GPG_PRIVATE_KEY` | Full output of `gpg --armor --export-secret-keys KEYID` (including the BEGIN/END lines) | your GPG key (see Part 2, step 4) |
| `GPG_PASSPHRASE` | Passphrase of that key | – |

### Release a version (example 1.1.0)

1. In `.mvn/maven.config` set `-Drevision=1.1.0` and commit.
2. Create and push the tag:
   ```
   git tag 1.1.0
   git push origin 1.1.0
   ```
3. Wait for the workflow to finish (Actions tab).
4. `autoPublish` is `false` in `api-client/pom.xml`, so open
   https://central.sonatype.com/publishing/deployments and press **Publish**.
5. Set the next version, e.g. `-Drevision=1.1.1-SNAPSHOT`, and commit.

---

## Part 2 – Local PC (backup way)

Use this if GitHub Actions is down, or to test before pushing.

### 1. Install

- Git
- Java 21 (e.g. Eclipse Temurin 21)
- Docker Desktop (Windows) or Docker Engine (Linux)
- GPG – only for publishing the api-client (Windows: Gpg4win)

Maven is optional: the repo has `mvnw` / `mvnw.cmd`, which download Maven 3.9 by themselves.

### 2. Clone

```
git clone https://github.com/qatlas-org/qatlas.git
cd qatlas
git checkout 1.1.0-SNAPSHOT
```

### 3. Docker Hub login

```
docker login -u <your-docker-hub-user>
```

Use the **access token** as the password.

### 4. GPG key (only for api-client)

Check if you still have the key (for example from a backup):

```
gpg --list-secret-keys --keyid-format=long
```

- **You have a backup file:** `gpg --import my-key.asc`
- **The key was lost with the old PC:** make a new one and publish it. Maven Central accepts a new key.
  ```
  gpg --full-generate-key        (choose RSA 4096, add a passphrase)
  gpg --list-secret-keys --keyid-format=long
  gpg --keyserver keyserver.ubuntu.com --send-keys <KEYID>
  gpg --keyserver keys.openpgp.org --send-keys <KEYID>
  ```

**Make a backup now** so this never happens again, and put the same text in the
`GPG_PRIVATE_KEY` GitHub secret:

```
gpg --armor --export-secret-keys <KEYID> > qatlas-gpg-private.asc
```

Keep this file in a password manager, not in git.

### 5. Maven settings

Copy `docs/local-setup/settings.xml.example` to
`%USERPROFILE%\.m2\settings.xml` (Windows) or `~/.m2/settings.xml` (Linux)
and put in your Central user token.

### 6. Build and push

Windows (PowerShell):

```
.\scripts\publish-local.ps1                    # build only
.\scripts\publish-local.ps1 -Push              # build + push images
.\scripts\publish-local.ps1 -Push -Client      # + publish api-client
```

Linux / Git Bash:

```
scripts/publish-local.sh                       # build only
scripts/publish-local.sh --push                # build + push images
scripts/publish-local.sh --push --client       # + publish api-client
```

Add `-Latest` / `--latest` only for a real release.

---

## What NOT to keep only on your PC

- Code changes → always push the branch to GitHub.
- GPG key → backup file in a password manager + GitHub secret.
- Tokens (Docker Hub, Central) → GitHub secrets. You can create new tokens any time.
- Server `.env` files with real passwords → password manager (they are git-ignored).
