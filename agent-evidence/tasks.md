# Tasks

- [x] 1. Add and execute a failing test for repeat and conflicting amounts.
- [x] 2. Implement minimal Payment class in Java 17.
- [x] 3. Re-run tests and record command and output.

## Test evidence (Java 17.0.8)

Before implementation, `javac --release 17 PaymentTest.java` succeeded and `java PaymentTest` exited 1:

```text
Exception in thread "main" java.lang.AssertionError: Payment implementation is missing
	at PaymentTest.main(PaymentTest.java:10)
Caused by: java.lang.ClassNotFoundException: Payment
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:641)
	at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:188)
	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:520)
	at java.base/java.lang.Class.forName0(Native Method)
	at java.base/java.lang.Class.forName(Class.java:375)
	at PaymentTest.main(PaymentTest.java:8)
```

After implementation, `javac --release 17 Payment.java PaymentTest.java` succeeded and `java PaymentTest` exited 0:

```text
PaymentTest: PASS
```
