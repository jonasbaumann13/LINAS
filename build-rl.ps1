param([string]$JdkHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
if (!$JdkHome) {
    $compiler = Get-Command javac.exe -ErrorAction SilentlyContinue
    if ($compiler) { $JdkHome = Split-Path (Split-Path $compiler.Source) }
}
if (!$JdkHome) {
    $jdk = Get-ChildItem (Join-Path $env:USERPROFILE '.jdks') -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName 'bin/javac.exe') } |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($jdk) { $JdkHome = $jdk.FullName }
}
if (!$JdkHome -or !(Test-Path (Join-Path $JdkHome 'bin/javac.exe'))) {
    throw 'A JDK 17+ is required. Set JAVA_HOME or pass -JdkHome.'
}
$classes = Join-Path $PSScriptRoot 'dist/rl/classes'
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sources = @(Get-ChildItem (Join-Path $PSScriptRoot 'Gen26') -Filter '*.java' | ForEach-Object FullName)
& (Join-Path $JdkHome 'bin/javac.exe') -encoding windows-1252 --release 17 -d $classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
Write-Host "Compiled: $classes"
Write-Host 'Run: python examples/python/linas_client.py'
