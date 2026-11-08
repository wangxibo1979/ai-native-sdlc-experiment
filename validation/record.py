import datetime, hashlib, json, pathlib, subprocess, sys
root=pathlib.Path(__file__).resolve().parents[1]
label, expected, *cmd=sys.argv[1:]
out=root/'outputs/evidence'; out.mkdir(parents=True,exist_ok=True)
target=out/(label+'.json')
if target.exists(): raise SystemExit('Evidence already exists: '+label)
start=datetime.datetime.now(datetime.timezone.utc).isoformat()
r=subprocess.run(cmd,cwd=root,capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=180)
log=r.stdout+r.stderr
(out/(label+'.log')).write_text(log,encoding='utf-8')
target.write_text(json.dumps({'label':label,'command':cmd,'cwd':str(root),'start':start,'end':datetime.datetime.now(datetime.timezone.utc).isoformat(),'actual_exit':r.returncode,'expected_exit':int(expected),'log':label+'.log'},ensure_ascii=False,indent=2),encoding='utf-8')
print(log,end='');print('ACTUAL_EXIT='+str(r.returncode))
if r.returncode!=int(expected):sys.exit(2)
