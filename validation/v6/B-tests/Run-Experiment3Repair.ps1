$ErrorActionPreference = 'Stop'
$base = Split-Path -Parent $PSScriptRoot
$candidate = Join-Path $base 'candidate\PaymentLedger.java'
$aSource = Join-Path $base 'A-tests\PaymentLedgerTest.java'
$bSource = Join-Path $PSScriptRoot 'IndependentBoundaryTest.java'
$minimalSource = Join-Path $PSScriptRoot 'MinimalReplayRepro.java'
$sources = @((Join-Path $base 'CONFIRMED_SPEC.md'), (Join-Path $base 'B_SPEC_FIRST_REVIEW.md'), $candidate, $aSource, $bSource, $minimalSource, $PSCommandPath)
function HashInputs {
    foreach ($file in $sources) { [ordered]@{path=$file;sha256=(Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash} }
}
$before = @(HashInputs)
if ($before[3].sha256 -ne 'B7E8C89BC725E47E06DC5E7E2E82ECFF137256AF2E94FF5BA2FD1762D61E32AB') { throw 'Original A source changed; stopped before tests.' }
if ($before[4].sha256 -ne 'CA9DCCC57D301DF35E6F8879BB20AE5E8613F500F2865B309653CEF8A7E1BE21') { throw 'Frozen B source changed; stopped before tests.' }
if ($before[5].sha256 -ne '414D51550CACAA6D6390515E1E385DEC55EBAD38B3339FAB151AC5E42C5514F1') { throw 'Minimal reproducer source changed; stopped before tests.' }
if ($before[0].sha256 -ne 'EDD5697CBB1F2FB08248906E6699D266A52A84A2D97EDD1E72CBA6E25FED9A3C') { throw 'Confirmed spec changed; stopped before tests.' }
if ($before[1].sha256 -ne 'B942DFB86EC2D875F8EFE62FD33ED81299B0C680C1FFF9F921DC8F2C54E41BD0') { throw 'Spec-first design changed; stopped before tests.' }
$java = (Get-Command java).Source
$javac = (Get-Command javac).Source
$classes = Join-Path $PSScriptRoot 'experiment3-repair-classes'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$compileLog = Join-Path $PSScriptRoot 'experiment3-repair-compile.log'
$compileArgs = @('--release','17','-encoding','UTF-8','-d',$classes,$candidate,$bSource,$minimalSource)
$ErrorActionPreference = 'Continue'
$cs = [DateTimeOffset]::UtcNow.ToString('o')
& $javac @compileArgs *> $compileLog
$compileExit = $LASTEXITCODE
$ce = [DateTimeOffset]::UtcNow.ToString('o')
Write-Output "COMPILE_EXIT=$compileExit START=$cs END=$ce"
$runs = [ordered]@{}
if ($compileExit -eq 0) {
    foreach ($entry in @(@{name='frozenSuite';main='IndependentBoundaryTest';file='experiment3-repair-frozen.log'}, @{name='minimalReproducer';main='MinimalReplayRepro';file='experiment3-repair-minimal.log'})) {
        $log = Join-Path $PSScriptRoot $entry.file
        $runArgs = @('-cp',$classes,$entry.main)
        $started = [DateTimeOffset]::UtcNow.ToString('o')
        & $java @runArgs *> $log
        $runExit = $LASTEXITCODE
        $ended = [DateTimeOffset]::UtcNow.ToString('o')
        $runs[$entry.name] = [ordered]@{executable=$java;arguments=$runArgs;startedUtc=$started;endedUtc=$ended;exit=$runExit;output=[IO.File]::ReadAllText($log);log=$log}
        Write-Output "$($entry.name)_EXIT=$runExit START=$started END=$ended"
        Write-Output $runs[$entry.name].output
    }
}
$ErrorActionPreference = 'Stop'
$after = @(HashInputs)
$unchanged = $true
for ($i=0; $i -lt $before.Count; $i++) {if ($before[$i].sha256 -ne $after[$i].sha256) {$unchanged=$false}}
$result = [ordered]@{
    experiment='V6 phase 4 B independent repair verification'
    actualInvocation="& '$PSCommandPath'"
    workingDirectory=(Get-Location).Path
    recordedUtc=[DateTimeOffset]::UtcNow.ToString('o')
    sourceHashesBefore=$before
    sourceHashesAfter=$after
    allObservedSourcesUnchanged=$unchanged
    originalAHashMatches=$true
    frozenBHashMatches=$true
    minimalReproducerHashMatches=$true
    confirmedSpecHashMatches=$true
    specFirstDesignHashMatches=$true
    aTestsExecuted=$false
    compile=[ordered]@{executable=$javac;arguments=$compileArgs;startedUtc=$cs;endedUtc=$ce;exit=$compileExit;output=[IO.File]::ReadAllText($compileLog);log=$compileLog}
    runs=$runs
    caseDefinitions='Unchanged B_SPEC_FIRST_REVIEW.md B01-B26 and frozen IndependentBoundaryTest.java; unchanged MinimalReplayRepro.java executes two calls at 2147483648L'
}
$result | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $base 'B_EXPERIMENT3_REPAIR_RESULTS.json') -Encoding UTF8
Write-Output "ALL_OBSERVED_SOURCES_UNCHANGED=$unchanged"
if ($compileExit -ne 0 -or $runs.Count -ne 2 -or $runs.frozenSuite.exit -ne 0 -or $runs.minimalReproducer.exit -ne 0 -or -not $unchanged) {exit 1}
exit 0
