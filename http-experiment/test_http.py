import pathlib, socket, subprocess, time, urllib.request, urllib.error
p=pathlib.Path(__file__).resolve().parent
build=p/'build'; build.mkdir(exist_ok=True)
subprocess.run(['javac','--add-modules','jdk.httpserver','-d',str(build),*[str(x) for x in (p/'src').glob('*.java')]],check=True)
s=socket.socket(); s.bind(('127.0.0.1',0)); port=s.getsockname()[1]; s.close()
server=subprocess.Popen(['java','--add-modules','jdk.httpserver','-cp',str(build),'PaymentHttpServer',str(port)],stdout=subprocess.PIPE,stderr=subprocess.PIPE)
try:
    for i in range(50):
        try:
            with socket.create_connection(('127.0.0.1',port),timeout=.2): break
        except OSError: time.sleep(.1)
    else: raise AssertionError('payment service failed to start')
    def pay(order,amount):
        return subprocess.run(['java','-cp',str(build),'OrderHttpClient',str(port),order,str(amount)],capture_output=True,text=True,check=True).stdout.strip()
    first=pay('o1',100); repeat=pay('o1',100); conflict=pay('o1',200)
    assert first.startswith('201:'), f'expected HTTP 201, received {first}'
    assert repeat==first, f'retry changed payment response: {first} / {repeat}'
    assert conflict.startswith('409:'), f'expected HTTP 409 on conflict, received {conflict}'
    print('PASS: separate JVM HTTP payment, retry and conflict')
finally:
    server.terminate(); server.wait(timeout=5)
