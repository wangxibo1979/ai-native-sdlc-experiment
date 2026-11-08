import json, pathlib, subprocess, urllib.request, urllib.error
root=pathlib.Path(__file__).resolve().parent
contract=json.loads((root/'contract.json').read_text())
for v in [1,2]:
 d=root/('v'+str(v));(d/'build').mkdir(exist_ok=True)
 subprocess.run(['javac','--release','17','--add-modules','jdk.httpserver','-d',str(d/'build'),str(d/'Provider.java'),str(d/'Consumer.java')],check=True)
results=[]
for name,cv,pv,provider_mode,consumer_mode,expected in [
 ('old-old',1,1,'normal','normal',0),('old-new',1,2,'normal','normal',0),('new-new',2,2,'normal','normal',0),('new-old-supported-fallback',2,1,'normal','normal',0),('incompatible-request',2,2,'normal','break-request',0),('incompatible-response',1,2,'break-response','normal',1),('provider-failure',2,2,'normal','failure',0)]:
 server=subprocess.Popen(['java','--add-modules','jdk.httpserver','-cp',str(root/('v'+str(pv))/'build'),'Provider',provider_mode],stdout=subprocess.PIPE,stderr=subprocess.PIPE,text=True)
 try:
  port=server.stdout.readline().strip()
  assert port.isdigit(),'provider did not start'
  r=subprocess.run(['java','-cp',str(root/('v'+str(cv))/'build'),'Consumer',port,consumer_mode],capture_output=True,text=True,timeout=30)
  print(name,'provider_pid='+str(server.pid),'consumer_exit='+str(r.returncode),r.stdout.strip(),r.stderr.strip(),flush=True)
  assert r.returncode==expected,(name,'unexpected consumer outcome')
  if consumer_mode=='break-request': assert 'HTTP=400' in r.stdout
  if provider_mode=='break-response': assert 'paymentId missing' in r.stderr
  # Provider schema checks use actual HTTP payloads and the versioned fixture.
  if provider_mode=='normal':
   req=urllib.request.Request('http://127.0.0.1:'+port+'/payments',data=b'orderId=probe&amountFen=100',method='POST')
   with urllib.request.urlopen(req,timeout=5) as response:
    payload=json.load(response);assert response.status==contract['v'+str(pv)]['success'];assert set(contract['v'+str(pv)]['response_required'])<=set(payload)
    if pv==2:assert payload['status']=='PENDING'
   try:urllib.request.urlopen(urllib.request.Request('http://127.0.0.1:'+port+'/payments',data=b'orderId=bad&amountFen=0'),timeout=5)
   except urllib.error.HTTPError as e:assert e.code==400
   else:raise AssertionError('invalid input accepted')
  results.append({'case':name,'consumer_version':cv,'provider_version':pv,'provider_pid':server.pid,'consumer_exit':r.returncode,'expected_consumer_exit':expected,'stdout':r.stdout,'stderr':r.stderr})
 finally:
  server.terminate();server.wait(timeout=5)
pathlib.Path('outputs/evidence/v3-matrix-results.json').write_text(json.dumps(results,indent=2),encoding='utf-8')
print('MATRIX_CASES='+str(len(results))+' ALL_EXPECTATIONS_MET')
