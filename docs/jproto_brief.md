# Brief for HashLisp jproto

jproto is a prototype of HashLisp in Java.

The detail of the hash table doesn't need to be implemented: indexing a HashMap by cons cell hashVaue is sufficient.

It should be structured into three main components, each developed with TDD and unit tests:

1. hash-cons heap and values
2. reader for s-expr syntax
3. evaluator with memoisation

It should follow the [specification](spec/spec.md).
It can have a simple front-end that uses the reader to parse stdin, runs the evaluator, and prints output to stdout.
It would be useful to ensure that values and the heap can be dumped to the console.

*Extensions*:

* List test cases for the hash-cons language and their expected evaluations in a file separatedly from the java tests (eg in CSV) so they can be reused as a language validation suite.