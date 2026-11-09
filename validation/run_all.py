import pathlib, subprocess, sys
root=pathlib.Path(__file__).resolve().parents[1]
def run(*cmd,expected=0):
 r=subprocess.run(list(cmd),cwd=root,timeout=240)
 if r.returncode!=expected:raise SystemExit('unexpected result '+str(cmd)+' got '+str(r.returncode))
# Reproduce original behavior RED, GREEN and REFACTOR without replacing history.
for stage,expected in [('v2-red',1),('v2-green',0),('v2-refactor',0)]:
 out=root/'validation/ci-build'/stage;out.mkdir(parents=True,exist_ok=True)
 source=root/'outputs/evidence/snapshots'/stage/'PaymentLedger.java'
 run('javac','--release','17','-d',str(out),str(source),str(root/'validation/v2/PaymentLedgerTest.java'))
 run('java','-cp',str(out),'PaymentLedgerTest',expected=expected)
run(sys.executable,'run_tests.py')
run(sys.executable,'validation/v3/run_matrix.py')
run(sys.executable,'validation/v4_run.py','ci')
run(sys.executable,'validation/v5/release_cases.py')
run(sys.executable,'validation/v6/replay.py')
print('V41_EXECUTED_V2_V3_V4_V5_V6_WITH_EXPECTED_NEGATIVE_CONTROLS')
