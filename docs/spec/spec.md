# HashLisp Specification

DRAFT

## Values and Heap

There are four types of _value_ in HashLisp:

1. **SmallInt** — 31-bit signed integer
2. **Symbol** — uniquely identified by an _identifier_ (string)
3. **nil** — a unit used as list sentinel
4. **ConsRef** — a reference to a _cons cell_ in the _heap_

All values manipulated by a HashLisp program is one of the four types.

The _heap_ is an array of _cons cells_.  Each **cons cell** contains:

* **fst** — a value (not mutable)
* **snd** — a value (not mutable)
* **hashCode** — a hash calculated over fst and snd _only_
* **memEval** — a mutable value used to memoise the evaluation of this cell

## S-expr data model

Basic items:

1. `<SmallInt>` — literal SmallInt (eg. `123`)
2. `<Symbol>` — literal symbol (eg. `lambda`)
3. `nil` — literal nil 
4. `(<sexpr> . <sexpr>)` — cons pair (eg. `(1 . 2)`)

Extended items:

5. `()` — empty list which is exactly equivalent to nil
6. `(<sexpr>...)` — a list of one or more elements
7. `(<sexpr>... . <sexpr>)` — a list where the final element is used instead of a terminal nil

Lists are constructed with cons with each element of the list stored in fst, and the list singly-linked through snd.

For example,

* `(print 1 2 3)` is equivalent to `(print . (1 . (2 . (3 . nil))))`.
* `(1 2 . 3)` is equivalent to `(1 . (2 . 3))`.
* `(1 2 . (3 4))` is equivalent to `(1 . (2 . (3 . (4 . nil))))`.

In s-expr used for programs, there is no need to use the cons pair notation: everything is built out of lists.  The cons pair notation is included for completeness as these structures can be built in memory by a program, and including this notation allows memory states to be both printed and read.

## HashLisp programs

A HashLisp program is a s-expr that conforms to additional syntax rules.

* nil and SmallInts are valid HashLisp programs
* symbols are valid HashLisp programs 
    * see below for **bound** and **free** variables, and **primitives**
* a list of valid HashLisp programs is a valid HashLisp program
    * see below for **special forms**

TODO: Explain **bound** and **free** variables

TODO: Explain **primitives** (just that there is a table of primitives)

TODO: Explain **special forms** (just lambda)

## HashLisp evaluation (**eval** and **apply**)

There is one operation that can be performed on a HashLisp program which is to evaluate it.  A s-expr is evaluated by the **eval** procedure:

* nil, SmallInts and symbols all evaluate to themself
* cons cells are assumed to start lists, and evaluate in two steps:
    1. the fst is recursively evaluated
    2. the **apply** procedure is followed with the result of step 1 as the _head_, and the snd of the cons cell as _args_.

The **apply** procedure takes a _head_ value and an _args_ value.

1. If the head is a symbol, lookup the symbol in the **primitives** table.
    * If present in the table, follow the primitive's apply procedure.
    * If not present, this is an error.
2. If the head is a lambda expression, follow the **arg match** procedure, then the **subst** procedure.
3. Otherwise, this is an error.

TODO: explain **arg match**

The **arg match** procedure takes a

TODO: explain **subst**

## HashLisp primitives
