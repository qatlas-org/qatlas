# Build QAtlas on your own Windows PC - the same steps as the GitHub Action.
# Use it as a backup when GitHub Actions is not available.
#
# Usage (PowerShell, from anywhere inside the repo):
#   .\scripts\publish-local.ps1                     # build only
#   .\scripts\publish-local.ps1 -Push               # build + push Docker images
#   .\scripts\publish-local.ps1 -Push -Latest -Client
#
#   -Push     push qatlas/backend, qatlas/frontend, qatlas/qatlas-ui to Docker Hub
#   -Latest   also tag the images as "latest" (only for a real release)
#   -Client   publish api-client (qatlas-client) to Maven Central
#
# Needs: Java 21, Docker Desktop, (for -Push) "docker login",
#        (for -Client) %USERPROFILE%\.m2\settings.xml + GPG key. See docs\BUILD_AND_RELEASE.md
param(
    [switch]$Push,
    [switch]$Latest,
    [switch]$Client
)
$ErrorActionPreference = "Stop"

function Run {
    param([string]$Exe, [string[]]$Arguments)
    & $Exe @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Command failed: $Exe $($Arguments -join ' ')" }
}

Set-Location (git rev-parse --show-toplevel)
$Ns = "qatlas"
$config = Get-Content ".mvn/maven.config" -Raw
if ($config -notmatch '-Drevision=(\S+)') { throw "Could not read version from .mvn/maven.config" }
$Version = $Matches[1]

$Mvn = if (Get-Command mvn -ErrorAction SilentlyContinue) { "mvn" } else { ".\mvnw.cmd" }

Write-Host "==> QAtlas version $Version (push=$Push latest=$Latest client=$Client)"

Write-Host "==> Maven build (frontend + api-client)"
Run $Mvn @("-B", "-ntp", "-DskipTests", "-pl", "frontend,api-client", "package")

$Platforms = "linux/amd64,linux/arm64"
$Builder = "qatlas-multiarch"

if ($Push) {
    # Multi-arch push needs a buildx builder with the docker-container driver.
    docker buildx inspect $Builder *> $null
    if ($LASTEXITCODE -ne 0) { Run "docker" @("buildx", "create", "--name", $Builder, "--driver", "docker-container") }
}

function Build-Image {
    param([string]$Img, [string[]]$DockerArgs)
    $tags = @("-t", "$Ns/${Img}:$Version")
    if ($Latest) { $tags += @("-t", "$Ns/${Img}:latest") }
    if ($Push) {
        # Same as GitHub: one tag with amd64 + arm64, pushed directly.
        # (A plain "docker push" would replace it with an amd64-only image.)
        Write-Host "==> build + push $Ns/${Img}:$Version ($Platforms)"
        Run "docker" (@("buildx", "build", "--builder", $Builder, "--platform", $Platforms, "--push") + $tags + $DockerArgs)
    } else {
        Write-Host "==> docker build $Ns/${Img}:$Version (this PC only, not pushed)"
        Run "docker" (@("build") + $tags + $DockerArgs)
    }
}

Build-Image "backend"   @("-f", "backend/Dockerfile", ".")
Build-Image "frontend"  @("frontend")
Build-Image "qatlas-ui" @("--build-arg", "QATLAS_UI_VERSION=$Version",
                          "--build-arg", "QATLAS_API_CLIENT_VERSION=$Version", "qatlas-ui")

if ($Client) {
    Write-Host "==> Publishing qatlas-client $Version to Maven Central"
    Run $Mvn @("-B", "-ntp", "-DskipTests", "-pl", "api-client", "deploy")
    if (-not $Version.EndsWith("-SNAPSHOT")) {
        Write-Host "Now open https://central.sonatype.com/publishing/deployments and press Publish."
    }
}

Write-Host "==> Done"
