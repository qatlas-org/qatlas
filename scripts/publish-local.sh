#!/usr/bin/env bash
# Build QAtlas on your own PC - the same steps as the GitHub Action.
# Use it as a backup when GitHub Actions is not available.
#
# Usage (run from anywhere inside the repo):
#   scripts/publish-local.sh              # build only
#   scripts/publish-local.sh --push       # build + push Docker images
#   scripts/publish-local.sh --push --latest --client
#
#   --push     push qatlas/backend, qatlas/frontend, qatlas/qatlas-ui to Docker Hub
#   --latest   also tag the images as "latest" (only for a real release)
#   --client   publish api-client (qatlas-client) to Maven Central
#
# Needs: Java 21, Docker, (for --push) "docker login", (for --client) ~/.m2/settings.xml + GPG key.
# See docs/BUILD_AND_RELEASE.md
set -euo pipefail

PUSH=false; LATEST=false; CLIENT=false
for arg in "$@"; do
  case "$arg" in
    --push) PUSH=true ;;
    --latest) LATEST=true ;;
    --client) CLIENT=true ;;
    -h|--help) sed -n '2,16p' "$0"; exit 0 ;;
    *) echo "Unknown option: $arg" >&2; exit 1 ;;
  esac
done

cd "$(git rev-parse --show-toplevel)"
NS=qatlas
VERSION=$(sed -n 's/.*-Drevision=\([^ ]*\).*/\1/p' .mvn/maven.config | tr -d '\r')
[ -n "$VERSION" ] || { echo "Could not read version from .mvn/maven.config" >&2; exit 1; }

if command -v mvn >/dev/null 2>&1; then MVN=mvn; else MVN=./mvnw; fi

echo "==> QAtlas version $VERSION (push=$PUSH latest=$LATEST client=$CLIENT)"

echo "==> Maven build (frontend + api-client)"
$MVN -B -ntp -DskipTests -pl frontend,api-client package

build() { # $1=image name, rest = docker build args
  local img=$1; shift
  echo "==> docker build $NS/$img:$VERSION"
  docker build -t "$NS/$img:$VERSION" "$@"
  if [ "$LATEST" = true ]; then docker tag "$NS/$img:$VERSION" "$NS/$img:latest"; fi
}

build backend   -f backend/Dockerfile .
build frontend  frontend
build qatlas-ui --build-arg "QATLAS_UI_VERSION=$VERSION" \
                --build-arg "QATLAS_API_CLIENT_VERSION=$VERSION" qatlas-ui

if [ "$PUSH" = true ]; then
  for img in backend frontend qatlas-ui; do
    docker push "$NS/$img:$VERSION"
    if [ "$LATEST" = true ]; then docker push "$NS/$img:latest"; fi
  done
fi

if [ "$CLIENT" = true ]; then
  echo "==> Publishing qatlas-client $VERSION to Maven Central"
  $MVN -B -ntp -DskipTests -pl api-client deploy
  case "$VERSION" in
    *-SNAPSHOT) ;;
    *) echo "Now open https://central.sonatype.com/publishing/deployments and press Publish." ;;
  esac
fi

echo "==> Done"
