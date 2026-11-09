# Experiment one: earlier tooling attempts

This note preserves partial evidence from execution-tool transcripts. It does not invent missing timestamps or subprocess exits. The authoritative complete later run is in `../B_EXPERIMENT1_RESULTS.json`.

1. Initial launch command:

   `powershell -NoProfile -ExecutionPolicy Bypass -File 'E:\标准化投标相关文件\AI机制实验\v41-validation\validation\v6\B-tests\Run-Experiment1.ps1'`

   Actual outer exit: 1. Observed error: `Resolve-Path : Cannot find path` for a mojibake rendering of the base directory plus `CONFIRMED_SPEC.md`; `PathNotFound,Microsoft.PowerShell.Commands.ResolvePathCommand`. Failure occurred while collecting source hashes, before any compiler/test invocation. No exact start timestamp was recorded for this setup failure. Cause: Windows PowerShell decoding the BOM-less UTF-8 script's Chinese literal. Fix: use `$base = Split-Path -Parent $PSScriptRoot`, avoiding the path literal. This changed only the B runner.

2. Second launch used the same command after that runner fix. Execution tool session: 30714. The captured version log showed Java/javac 17.0.8, and the test log recorded all B01–B26 PASS; first completed case at `2026-11-08T15:47:59.275254900Z`, final case and summary at `2026-11-08T15:47:59.301104300Z`, with `CASES=26 FAILURES=0`. No result JSON or compiler/test exit evidence was captured for this attempt. A Ctrl-C request ended the outer session with actual exit 1 and no final output. These PASS lines alone were not accepted as subprocess exit proof. Exact full timing is unknown. The common log paths were subsequently overwritten by the complete later run; the partial observations above are retained from the tool transcript, not presented as surviving raw logs.

3. Complete later invocation used the existing PowerShell shell directly:

   `& 'E:\标准化投标相关文件\AI机制实验\v41-validation\validation\v6\B-tests\Run-Experiment1.ps1'`

   Execution tool session: 15164. Output collection was delayed, prompting read-only log/process checks. A later Ctrl-C request to that session returned the already-produced complete runner output and outer exit **0**. Its result JSON had been finalized at `2026-11-08T15:49:45.0731228Z`, with actual compiler exit 0 and test exit 0 captured inside the runner. The report uses those recorded subprocess exits rather than interpreting the Ctrl-C request as evidence of test completion. Source SHA-256 was unchanged before/after. No test Java source changed between these attempts.
