$ErrorActionPreference = 'Stop'
$base = Split-Path -Parent $PSScriptRoot
$testDir = Join-Path $base 'B-tests'
$classes = Join-Path $testDir 'experiment1-classes'
$compileLog = Join-Path $testDir 'experiment1-compile.log'
$testLog = Join-Path $testDir 'experiment1-test.log'
$javaVersionLog = Join-Path $testDir 'experiment1-java-version.log'
$javacVersionLog = Join-Path $testDir 'experiment1-javac-version.log'
$resultFile = Join-Path $base 'B_EXPERIMENT1_RESULTS.json'
$candidate = Join-Path $base 'candidate\PaymentLedger.java'
$aSource = Join-Path $base 'A-tests\PaymentLedgerTest.java'
$bSource = Join-Path $testDir 'IndependentBoundaryTest.java'
$sourcePaths = @(
    (Join-Path $base 'CONFIRMED_SPEC.md'),
    (Join-Path $base 'B_SPEC_FIRST_REVIEW.md'),
    $candidate,
    $aSource,
    $bSource,
    $PSCommandPath
)
function SourceHashes {
    param([string[]]$Paths)
    foreach ($sourcePath in $Paths) {
        $hash = Get-FileHash -LiteralPath $sourcePath -Algorithm SHA256
        [ordered]@{ path = $sourcePath; sha256 = $hash.Hash }
    }
}
$started = [DateTimeOffset]::UtcNow.ToString('o')
$beforeHashes = @(SourceHashes -Paths $sourcePaths)
$java = (Get-Command java -ErrorAction Stop).Source
$javac = (Get-Command javac -ErrorAction Stop).Source
New-Item -ItemType Directory -Path $classes -Force | Out-Null
# Native stderr is captured as evidence; exit codes, not stderr presence, decide status.
$ErrorActionPreference = 'Continue'
& $java '-version' *> $javaVersionLog
$javaVersionExit = $LASTEXITCODE
& $javac '-version' *> $javacVersionLog
$javacVersionExit = $LASTEXITCODE
$compileArgs = @('--release', '17', '-encoding', 'UTF-8', '-d', $classes, $candidate, $bSource)
$compileStarted = [DateTimeOffset]::UtcNow.ToString('o')
& $javac @compileArgs *> $compileLog
$compileExit = $LASTEXITCODE
$compileEnded = [DateTimeOffset]::UtcNow.ToString('o')
$runArgs = @('-cp', $classes, 'IndependentBoundaryTest')
$testStarted = $null
$testEnded = $null
$testExit = $null
if ($compileExit -eq 0) {
    $testStarted = [DateTimeOffset]::UtcNow.ToString('o')
    & $java @runArgs *> $testLog
    $testExit = $LASTEXITCODE
    $testEnded = [DateTimeOffset]::UtcNow.ToString('o')
} else {
    'NOT RUN: compilation failed' | Set-Content -LiteralPath $testLog -Encoding UTF8
}
$ErrorActionPreference = 'Stop'
$afterHashes = @(SourceHashes -Paths $sourcePaths)
$unchanged = $true
for ($i = 0; $i -lt $beforeHashes.Count; $i++) {
    if ($beforeHashes[$i].sha256 -ne $afterHashes[$i].sha256) { $unchanged = $false }
}
$compileOutput = Get-Content -LiteralPath $compileLog -Raw -ErrorAction SilentlyContinue
$testOutput = Get-Content -LiteralPath $testLog -Raw
$result = [ordered]@{
    experiment = 'V6 B phase 2 experiment one'
    startedUtc = $started
    endedUtc = [DateTimeOffset]::UtcNow.ToString('o')
    runnerCommand = "powershell -NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`""
    workingDirectory = (Get-Location).Path
    javaExecutable = $java
    javaVersionExit = $javaVersionExit
    javaVersionOutput = (Get-Content -LiteralPath $javaVersionLog -Raw)
    javacExecutable = $javac
    javacVersionExit = $javacVersionExit
    javacVersionOutput = (Get-Content -LiteralPath $javacVersionLog -Raw)
    sourceHashesBefore = $beforeHashes
    sourceHashesAfter = $afterHashes
    allObservedSourcesUnchanged = $unchanged
    compile = [ordered]@{
        executable = $javac
        arguments = $compileArgs
        startedUtc = $compileStarted
        endedUtc = $compileEnded
        exit = $compileExit
        output = $compileOutput
        log = $compileLog
    }
    test = [ordered]@{
        executable = $java
        arguments = $runArgs
        startedUtc = $testStarted
        endedUtc = $testEnded
        exit = $testExit
        output = $testOutput
        log = $testLog
    }
    aTestsExecuted = $false
    candidateModified = $false
    cases = 'B01-B26 from B_SPEC_FIRST_REVIEW.md'
}
$result | ConvertTo-Json -Depth 9 | Set-Content -LiteralPath $resultFile -Encoding UTF8
Write-Output "RESULT_FILE=$resultFile"
Write-Output "COMPILE_STARTED_UTC=$compileStarted COMPILE_EXIT=$compileExit"
Write-Output "TEST_STARTED_UTC=$testStarted TEST_EXIT=$testExit"
Write-Output "ALL_OBSERVED_SOURCES_UNCHANGED=$unchanged"
Write-Output $testOutput
if ($compileExit -ne 0 -or $null -eq $testExit -or $testExit -ne 0 -or -not $unchanged) { exit 1 }
exit 0
