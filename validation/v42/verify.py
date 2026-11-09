"""Small local experiment entry. No remote operations or CI administration."""
import argparse, copy, hashlib, json, pathlib, re, shutil, subprocess, sys, time

ROOT = pathlib.Path(__file__).resolve().parent
FIELDS = ['来源需求', '可观察结果', '检查方式', '覆盖状态', '执行证据', '是否阻断']

def read(path):
    return json.loads((ROOT / path).read_text(encoding='utf-8-sig'))

def sha(path):
    return hashlib.sha256((ROOT / path).read_bytes()).hexdigest()

def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT, text=True).strip()

def main():
    p = argparse.ArgumentParser()
    p.add_argument('--case', required=True, choices=['positive','missing','failure','gap','skip','zero','environment','map-pending','policy-downgrade','stale'])
    p.add_argument('--label', required=True)
    p.add_argument('--reuse')
    a = p.parse_args()
    out = ROOT / 'outputs/evidence' / a.label
    out.mkdir(parents=True, exist_ok=False)
    contract = read('verification_contract.json')
    targets = read(contract['target_file'])
    name = 'legacy' if a.case == 'gap' else 'full'
    mapping = read('outputs/test_map_' + name + '.json')
    links = read('outputs/map_links.json')[name]
    if a.case == 'map-pending':
        mapping[0]['覆盖状态'] = 'PENDING'
    if a.case == 'policy-downgrade':
        mapping[0]['是否阻断'] = 'NO'
    head = git('rev-parse','HEAD')
    dirty = git('status','--porcelain','--', *contract['source_paths'])
    result = {'case':a.case,'commit':head,'source_dirty':dirty,'source_hashes':{f:sha(f) for f in contract['source_paths']},'checks':{},'reasons':[], 'map_state':'MAP READY','conclusion':'NOT QUALIFIED'}
    if dirty:
        result['reasons'].append('SOURCE_NOT_COMMITTED')
    if sha(contract['target_file']) != contract['reviewed_targets_sha256']:
        result['reasons'].append('REVIEWED_TARGET_VERSION_MISMATCH')
    for f, digest in contract['reviewed_source_sha256'].items():
        if not (ROOT/f).is_file() or sha(f) != digest:
            result['reasons'].append('REVIEWED_CHECK_SOURCE_CHANGED:' + f)
    if len(mapping) != len(targets):
        result['map_state'] = 'MAP NOT READY'
    required = ['T%02d' % (i+1) for i in range(len(targets))]
    if contract.get('required_targets') != required:
        result['map_state'] = 'MAP NOT READY'
        result['reasons'].append('REQUIRED_TARGET_POLICY_INCOMPLETE')
    for i, row in enumerate(mapping):
        if set(row) != set(FIELDS) or i >= len(targets):
            result['map_state'] = 'MAP NOT READY'; continue
        if any(row[k] != targets[i][k] for k in FIELDS[:2]):
            result['map_state'] = 'MAP NOT READY'
        if row['检查方式'] not in ['AUTO','STATIC','COMBINED','NONE'] or row['覆盖状态'] not in ['COVERED','PARTIAL','GAP','UNCONFIRMED'] or row['是否阻断'] not in ['YES','NO']:
            result['map_state'] = 'MAP NOT READY'
        if row['覆盖状态'] == 'COVERED' and not links.get('T%02d' % (i+1)):
            result['map_state'] = 'MAP NOT READY'
        if row['是否阻断'] != 'YES':
            result['map_state'] = 'MAP NOT READY'
            result['reasons'].append('BLOCKING_POLICY_MISMATCH_T%02d' % (i+1))
        if row['是否阻断'] == 'YES' and row['覆盖状态'] != 'COVERED':
            result['reasons'].append('BLOCKING_COVERAGE_' + str(i+1))
    if result['map_state'] != 'MAP READY':
        result['reasons'].append('MAP_INCOMPLETE')
    selected = contract['profiles'][name]
    for key in ['POLICY','COMPILE','A','B','C']:
        result['checks'][key] = {'status':'NOT RUN','command':None,'exit':None,'test_count':None}

    def execute(key, cmd, need_count=False):
        c = result['checks'][key]; c['command'] = cmd
        t = time.monotonic()
        try:
            r = subprocess.run(cmd,cwd=ROOT,capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=90)
            c.update(exit=r.returncode,seconds=time.monotonic()-t,status='PASS' if r.returncode==0 else 'FAIL')
            text = r.stdout+r.stderr
            (out/(key+'.log')).write_text(text,encoding='utf-8')
            if need_count:
                matches = re.findall(r'CASES=(\d+)\s+FAILURES=(\d+)',text)
                c['test_count'] = int(matches[-1][0]) if matches else None
                if not matches or int(matches[-1][0]) < contract['minimum_cases'][key] or int(matches[-1][1]) != 0:
                    c['status']='FAIL'; c['reason']='ZERO_OR_MISSING_OR_FAILED_CASES'
        except (OSError,subprocess.TimeoutExpired) as e:
            c.update(status='BLOCKED',reason=str(e),seconds=time.monotonic()-t)
        return c['status'] == 'PASS'

    if a.case == 'stale':
        if not a.reuse: raise ValueError('stale requires an actual prior result')
        old = read(a.reuse)
        result['reused_commit'] = old['commit']
        result['reused_source_hashes'] = old['source_hashes']
        result['reasons'].append('STALE_COMMIT_EVIDENCE' if old['commit'] != head else 'STALE_CASE_NOT_ESTABLISHED')
        result['conclusion']='BLOCKED'
    else:
        source = 'fixtures/broken/PaymentLedger.java' if a.case=='failure' else 'checks/PaymentLedger.java'
        files = [source,'checks/PaymentLedgerTest.java'] + (['checks/IndependentBoundaryTest.java'] if 'B' in selected else []) + (['checks/CollisionProbe.java'] if 'C' in selected else [])
        if a.case == 'missing': files[files.index('checks/IndependentBoundaryTest.java')]='fixtures/missing/IndependentBoundaryTest.java'
        missing=[f for f in files if not (ROOT/f).is_file()]
        build=ROOT/'build'/a.label; build.mkdir(parents=True,exist_ok=True)
        policy_cmd = [sys.executable,'-c',"import unittest,sys; s=unittest.defaultTestLoader.loadTestsFromName('test_verification_policy'); r=unittest.TextTestRunner(verbosity=2).run(s); print('CASES='+str(r.testsRun)+' FAILURES='+str(len(r.failures)+len(r.errors))); sys.exit(not r.wasSuccessful())"]
        if not execute('POLICY',policy_cmd,True):
            pass  # Acceptance-policy failure stops compile and business checks.
        elif missing:
            result['checks']['COMPILE'].update(status='FAIL',reason='REQUIRED_FILE_MISSING',missing=missing)
        else:
            javac='javac-not-available-v42' if a.case=='environment' else 'javac'
            if execute('COMPILE',[javac,'--release','17','-encoding','UTF-8','-d',str(build),*files]):
                for key in selected:
                    if a.case=='skip' and key=='B':
                        result['checks'][key].update(status='SKIPPED',reason='CONTROLLED_SKIP'); continue
                    cmd=['java','-cp',str(build),{'A':'PaymentLedgerTest','B':'IndependentBoundaryTest','C':'CollisionProbe'}[key]]
                    if a.case=='zero' and key=='B':cmd=[sys.executable,'-c',"import unittest,sys; s=unittest.defaultTestLoader.discover('fixtures/empty-tests'); r=unittest.TextTestRunner().run(s); print('CASES='+str(r.testsRun)+' FAILURES='+str(len(r.failures)+len(r.errors))); sys.exit(not r.wasSuccessful())"]
                    execute(key,cmd,True)
        # Compile/missing prerequisites stop tests; business failures still collect the other test's diagnostics.
        statuses=[result['checks'][k]['status'] for k in ['POLICY','COMPILE',*selected]]
        for k in ['A','B','C']:
            if k not in selected:result['checks'][k]['applicability']='NOT REQUIRED BY CONTROLLED LEGACY PROFILE'
        if all(s=='PASS' for s in statuses) and not result['reasons']:
            result['conclusion']='QUALIFIED'
        elif any(s=='BLOCKED' for s in statuses) or result['map_state']=='MAP NOT READY':
            result['conclusion']='BLOCKED'
        for i,row in enumerate(mapping):
            if row['覆盖状态']=='COVERED':
                for link in links.get('T%02d'%(i+1),[]):
                    if result['checks'].get(link['check'],{}).get('status')!='PASS':
                        result['reasons'].append('MAPPED_CHECK_NOT_PASS_T%02d'%(i+1))
                        if result['conclusion']=='QUALIFIED':result['conclusion']='NOT QUALIFIED'
    runtime=copy.deepcopy(mapping)
    for i,row in enumerate(runtime):
        row['执行证据']={'commit':head,'result':str((out/'result.json').relative_to(ROOT)),'checks':links.get('T%02d'%(i+1),[])}
    (out/'executed_map.json').write_text(json.dumps(runtime,ensure_ascii=False,indent=2),encoding='utf-8')
    (out/'result.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps({'case':a.case,'commit':head,'map':result['map_state'],'checks':{k:v['status'] for k,v in result['checks'].items()},'conclusion':result['conclusion'],'reasons':result['reasons']},ensure_ascii=False))
    return 0 if result['conclusion']=='QUALIFIED' else 2 if result['conclusion']=='BLOCKED' else 1

if __name__=='__main__':sys.exit(main())
