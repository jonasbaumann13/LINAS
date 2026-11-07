param([string]$JdkHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
if (!$JdkHome) {
    $compiler = Get-Command javac.exe -ErrorAction SilentlyContinue
    if ($compiler) { $JdkHome = Split-Path (Split-Path $compiler.Source) }
}
if (!$JdkHome) {
    $jdk = Get-ChildItem (Join-Path $env:USERPROFILE '.jdks') -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName 'bin/jpackage.exe') } |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($jdk) { $JdkHome = $jdk.FullName }
}
foreach ($tool in @('javac', 'jar', 'jpackage')) {
    if (!$JdkHome -or !(Test-Path (Join-Path $JdkHome "bin/$tool.exe"))) {
        throw 'A complete JDK (17+) is required. Set JAVA_HOME or pass -JdkHome (IntelliJ: Project Structure > SDKs).'
    }
}
# Unique output directories preserve previous builds.
$buildRoot = Join-Path $PSScriptRoot ('dist/build-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff'))
$classes = Join-Path $buildRoot 'classes'
$inputDir = Join-Path $buildRoot 'input'
$outputDir = Join-Path $buildRoot 'windows'
New-Item -ItemType Directory -Path $classes, $inputDir | Out-Null
$sources = @(Get-ChildItem (Join-Path $PSScriptRoot 'Gen26') -Filter '*.java' | ForEach-Object FullName)
Write-Host 'Compiling LINAS...'
& (Join-Path $JdkHome 'bin/javac.exe') -encoding windows-1252 --release 17 -d $classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
& (Join-Path $JdkHome 'bin/jar.exe') --create --file (Join-Path $inputDir 'LINAS.jar') --main-class Linas -C $classes .
if ($LASTEXITCODE -ne 0) { throw 'JAR creation failed.' }
foreach ($folder in @('Pics', 'Units', 'ReadMe', 'tmp')) {
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot "Gen26/$folder") -Destination $inputDir -Recurse
}
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'LICENSE') -Destination $inputDir
Write-Host 'Packaging Windows application and Java runtime...'
& (Join-Path $JdkHome 'bin/jpackage.exe') --type app-image --name LINAS --input $inputDir --main-jar LINAS.jar --main-class Linas --dest $outputDir
if ($LASTEXITCODE -ne 0) { throw 'Windows packaging failed.' }
Write-Host "Ready: $outputDir\LINAS\LINAS.exe"
Write-Host "To distribute LINAS, zip the entire folder: $outputDir\LINAS"
