# HashLisp Specification

DRAFT

## Values and Heap

There are four types of _value_ in HashLisp:

1. **ShortInt** — 31-bit signed integer
2. **Symbol** — uniquely identified by an _identifier_ (string)
3. **nil** — a unit used as list sentinel
4. **ConsRef** — a reference to a _cons cell_ in the _heap_

All values manipulated by a HashLisp program is one of the four types.

The _heap_ is an array of _cons cells_.  Each **cons cell** contains:

* **fst** — a value (not mutable)
* **snd** — a value (not mutable)
* **hashCode** — a hash calculated over fst and snd _only_
* **memEval** — a mutable value used to memoise the evaluation of this cell


