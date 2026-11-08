import subprocess, pathlib, sys
p = pathlib.Path(__file__).resolve().parent
out = p / "build"
out.mkdir(exist_ok=True)
tests = sorted(p.glob("*Test.java"))
sources = sorted((p / "src").glob("*.java"))
subprocess.run(["javac", "-d", str(out), *map(str, sources + tests)], check=True)
for test in tests:
    subprocess.run(["java", "-cp", str(out), test.stem], check=True)
print(f"PASS {len(tests)} test classes", flush=True)
