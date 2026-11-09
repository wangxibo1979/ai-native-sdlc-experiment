from pathlib import Path
import subprocess,sys,json,hashlib,time
root=Path(__file__).resolve().parents[2]
base=root/'validation/v6';results=[]
for stage,source,b_expected in [('original',root/'outputs/evidence/snapshots/v6-original/PaymentLedger.java',0),('mutant',root/'outputs/evidence/snapshots/v6-mutant/PaymentLedger.java',1),('repaired',base/'candidate/PaymentLedger.java',0)]:
 out=base/'replay-build'/stage;out.mkdir(parents=True,exist_ok=True)
 cmd=['javac','--release','17','-encoding','UTF-8','-d',str(out),str(source),str(base/'A-tests/PaymentLedgerTest.java'),str(base/'B-tests/IndependentBoundaryTest.java'),str(base/'B-tests/MinimalReplayRepro.java')]
 r=subprocess.run(cmd,timeout=60);assert r.returncode==0,'compile failure is not a business RED'
 for cls,expected in [('PaymentLedgerTest',0),('IndependentBoundaryTest',b_expected),('MinimalReplayRepro',b_expected)]:
  start=time.monotonic();r=subprocess.run(['java','-cp',str(out),cls],capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=60)
  print(stage,cls,'EXIT',r.returncode,flush=True);print(r.stdout+r.stderr,flush=True)
  results.append({'stage':stage,'test':cls,'actual_exit':r.returncode,'expected_exit':expected,'seconds':time.monotonic()-start,'source_sha256':hashlib.sha256(source.read_bytes()).hexdigest()})
  assert r.returncode==expected,(stage,cls,'unexpected result')
Path('outputs/evidence/v6-replay-results.json').write_text(json.dumps(results,indent=2),encoding='utf-8')
print('V6_EXPECTED_A_PASS_B_DETECTION_AND_REPAIR_REPLAY_COMPLETE')
