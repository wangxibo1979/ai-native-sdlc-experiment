$ErrorActionPreference = 'Stop'
$base = Split-Path -Parent $PSScriptRoot
$candidate = Join-Path $base 'candidate\PaymentLedger.java'
$aSource = Join-Path $base 'A-tests\PaymentLedgerTest.java'
$bSource = Join-Path $PSScriptRoot 'IndependentBoundaryTest.java'
$classes = Join-Path $PSScriptRoot 'experiment2-classes'
$sources = @((Join-Path $base 'CONFIRMED_SPEC.md'), (Join-Path $base 'B_SPEC_FIRST_REVIEW.md'), $candidate, $aSource, $bSource, $PSCommandPath)
function Hashes {
    foreach ($file in $sources) {
        [ordered]@{path=$file; sha256=(Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash}
    }
}
$before = @(Hashes)
if ($before[4].sha256 -ne 'CA9DCCC57D301DF35E6F8879BB20AE5E8613F500F2865B309653CEF8A7E1BE21') {
    throw 'Frozen B source hash changed; no candidate test run.'
}
$java = (Get-Command java).Source
$javac = (Get-Command javac).Source
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$compileLog = Join-Path $PSScriptRoot 'experiment2-compile.log'
$testLog = Join-Path $PSScriptRoot 'experiment2-test.log'
$compileArgs = @('--release','17','-encoding','UTF-8','-d',$classes,$candidate,$bSource)
$testArgs = @('-cp',$classes,'IndependentBoundaryTest')
$ErrorActionPreference = 'Continue'
$cs = [DateTimeOffset]::UtcNow.ToString('o')
& $javac @compileArgs *> $compileLog
$cexit = $LASTEXITCODE
$ce = [DateTimeOffset]::UtcNow.ToString('o')
$ts = $null; $te = $null; $texit = $null
if ($cexit -eq 0) {
    $ts = [DateTimeOffset]::UtcNow.ToString('o')
    & $java @testArgs *> $testLog
    $texit = $LASTEXITCODE
    $te = [DateTimeOffset]::UtcNow.ToString('o')
} else { 'NOT RUN: compile failure' | Set-Content -LiteralPath $testLog -Encoding UTF8 }
$ErrorActionPreference = 'Stop'
$after = @(Hashes)
$unchanged = $true
for ($i=0; $i -lt $before.Count; $i++) { if ($before[$i].sha256 -ne $after[$i].sha256) { $unchanged = $false } }
$record = [ordered]@{
    experiment='V6 B phase 3 experiment two; frozen suite before candidate inspection'
    actualInvocation="& '$PSCommandPath'"
    workingDirectory=(Get-Location).Path
    recordedUtc=[DateTimeOffset]::UtcNow.ToString('o')
    sourceHashesBefore=$before
    sourceHashesAfter=$after
    allObservedSourcesUnchanged=$unchanged
    frozenBSourceMatchesExperiment1=$true
    compile=[ordered]@{executable=$javac;arguments=$compileArgs;startedUtc=$cs;endedUtc=$ce;exit=$cexit;output=[IO.File]::ReadAllText($compileLog);log=$compileLog}
    test=[ordered]@{executable=$java;arguments=$testArgs;startedUtc=$ts;endedUtc=$te;exit=$texit;output=[IO.File]::ReadAllText($testLog);log=$testLog}
    caseDefinitions='B_SPEC_FIRST_REVIEW.md B01-B26; executable inputs and assertions in unchanged B-tests/IndependentBoundaryTest.java'
}
$record | ConvertTo-Json -Depth 9 | Set-Content -LiteralPath (Join-Path $base 'B_EXPERIMENT2_RESULTS.json') -Encoding UTF8
Write-Output "COMPILE_EXIT=$cexit TEST_EXIT=$texit SOURCES_UNCHANGED=$unchanged"
Write-Output $record.test.output
if ($cexit -ne 0 -or $null -eq $texit -or $texit -ne 0 -or -not $unchanged) {exit 1}
exit 0
