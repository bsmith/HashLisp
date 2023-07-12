# Command-line interface

_HashLisp_ implementations should follow the cmdline argument specification in this document.
This will allow common tests to be used between multiple implementations of _HashLisp_.

## Overview:

```
hashlisp [-r|--read] [-E|--eval] [(-f|--file) 'filename.hl'] [(-e|--expr) '(code ...)'] [--] [args...]
```

`hashlisp` is the executable of the _HashLisp_ implementation.

The standard file extension for _HashLisp_ programs in `.hl`.

Argument parsing will stop at a `--` argument, and everything after that point will be available to the _HashLisp_ program.
If no `--` argument is present, any unparsed arguments will be passed to the _HashLisp_ program.

### `-r|--read`

If this flag is present, the program will be read and the program that would be passed to evaluation is printed to standard output without evaluation.

It is an error to specify both `-r|--read` and `-E|--eval`.

### `-E|--eval`

This is the default mode.  The program is read and is then passed to evaluation.

It is an error to specify both `-E|--eval` and `-r|--read`.

### `(-f|--file) 'filename.hl'`

The given file is opened and used as the source of reading the program.

It is an error to specify both `-f|--file` and `-e|--expr`.

### `(-e|--expr) '(code...)'`

The given argument is used as the source of reading the program.

It is an error to specify both `-e|--expr` and `-f|--file`.

### `args...`

Arguments provided after `--` or after arguments parsed by the _HashLisp_ implementation are provided to the program when it is evaluated.

If no arguments are provided, the empty list (`nil`) is passed to the program when it is evaluated.

## Reading a _HashLisp_ program

TODO: write this section

## Evaluating a _HashLisp_ program

TODO: write this section
