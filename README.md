# High Level Mindustry Logic

High Level Mindustry Logic (hlml) is a programming language that compiles to
Mindustry Logic (mlog), which are instructions that can be run by the processors
in [Mindustry](https://github.com/Anuken/Mindustry). It is a lot more productive
than directly writing instructions.

## Implementation

The reference implementation is in Java, called
[hlml.java](https://github.com/calestialgem/hlml.java).

## Design

Since hlml is a high level language, it removes the need for `set`, `op` and
`jump` instructions completely. These can be done using mutating statements,
expressions, control flow statements and procedures. Other instructions can be
invoked as procedures from the built-in scope `mlog`. For example,
`read result cell1 0` becomes `mlog::read(result, cell1, 0)`.

Furthermore, the variables and constants that are embedded in the processor
(like `@counter`) are accessed through `mlog`. The variables and constants have
`-`s in their name replaced with `_`s, otherwise they would not be valid hlml
identifiers. For example, `@phase-fabric` becomes `mlog::phase_fabric`.
Similarly, instructions with a different structure depending on the first
argument are identified with a `_` between the instruction name and the argument
name. For example, `draw lineRect x y w h` becomes
`mlog::draw_lineRect(x, y, w, h)`.

Instructions with filters (`radar`, `uradar`) have all the combinations
available. Filters that are not there (`any`) are not written in hlml. For
example, `radar enemy any any distance message1 1 unit` becomes
`mlog::radar_enemy_distance(message1, 1, unit)`.

Standard library is the scope `hlml` but it only has some constants for now.

See [`builtin.variable.hlml`](builtin.variable.hlml) and
[`builtin.procedure.hlml`](builtin.procedure.hlml) for a list of all the symbols
in the `mlog` scope and their counter-parts in Mindustry logic.

## Declarations

Declarations go to the top-level scope in a source file.

### Entrypoint

`entrypoint` keyword can be used to declare what the processor would do when
this program is loaded. It is the same thing as the main function in a C
program.

```hlml
link message1;
entrypoint { mlog::print("Hello, Mindustry!"); mlog::printflush(message1); }
```

## Definitions

Declarations that create a new symbol.

### Link

`link` keyword can be used to define an identifier to represent the link between
the processor and a building.

```hlml
link message1;
entrypoint { mlog::print("Hello, Mindustry!"); mlog::printflush(message1); }
```

`as` keyword can be used to rename this link to another identifier. The compiled
instructions still use the correct link name, the hlml code only knows the
identifier given by `as`.

```hlml
link message1 as logger;
entrypoint { mlog::print("Hello, Mindustry!"); mlog::printflush(logger); }
```

### Using

`using` keyword can be used to define an identifier to represent another symbol.
Aliases them selves are symbols in their own right.

```hlml
using mlog::printflush as flush;
link message1;
entrypoint { mlog::print("Hello, Mindustry!"); flush(message1); }
```

`as` keyword can be omitted, which means the definition will have the same
identifier as the aliased symbol.

```hlml
using mlog::printflush;
link message1;
entrypoint { mlog::print("Hello, Mindustry!"); printflush(message1); }
```

### Const

`const` keyword can be used to define an identifier to represent a compile-time
known value. These can have values that are numeric, color or string constants,
built-in variables and constants from `mlog` scope.

```hlml
link message1;
const text = "Hello, Mindustry!";
entrypoint { mlog::print(text); mlog::printflush(message1); }
```

`const` can declare constants with expressions that have known operands. Such
expressions can be evaluated in compile time as everything necessary to evaluate
them are available. Currently, that does not cover invocations.

```hlml
link cell1;
const answer = 40 + 2;
const index = 0;
entrypoint { mlog::write(answer, cell1, index); }
```

### Var

`var` keyword can be used to define an identifier to hold a value.

```hlml
link cell1;
entrypoint {
  var value;
  mlog::read(value, cell1, 0);
  value += 10;
  mlog::write(value, cell1, 0);
}
```

Variables can have initial values. Global variables can only have constant
initial values, otherwise initialization order would create nondeterministic
results.

```hlml
link cell1;
var value = 0;
entrypoint {
  var i = 0;
  while i < 16; i++ { mlog::write(value, cell1, i); value++; }
}
```

### Proc

`proc` keyword can be used to define an identifier to hold a parametrized,
arbitrary computation.

```hlml
proc set_all(cell, value) {
  while var i = 0; i < 16; i++ { mlog::write(value, cell, i); }
}
link cell1;
entrypoint { set_all(cell1, 0); }
```

Procedures have a return value. The ones that do not explicitly return anything
return `mlog::null` implicitly.

```hlml
proc double(a) { return a * 2; }
entrypoint { var value = 17; value = double(value); }
```

When invoking a procedure, parameters from the end might not be fulfilled. In
this case, those get `mlog::null` as argument.

```hlml
proc double(a) { return a * 2; }
entrypoint { var value = double(); }
```

Procedures can have output parameters, which are marked with a trailing `&`.
After the procedure ends, the values of these parameters are assigned to the
passed arguments at the invocation site. These conceptually map to the
instructions that have output parameters. For example, `read result cell1 0`
becomes `mlog::read(result, cell1, 0)`.

```hlml
proc double(a&) { a *= 2; }
entrypoint { var value = 17; double(value); }
```

### Visibility

Definitions can have the `public` keyword leading them. Which means that the
definition is visible from other source files. Otherwise, all definitions are
only visible in the source file they are declared in.

## Statements

Language constructs that denote instructions to be executed.

### Block

Using `{}` as deliminator, statements can be combined together.

```hlml
entrypoint {
  {}
  {
    {}
  }
}
```

Blocks form a lexical scope where the local variables do not escape.

```hlml
link cell1;
entrypoint {
  {
    var local = 17;
    mlog::write(local, cell1, 0);
  }
  var local = 67;
  mlog::write(local, cell1, 0);
}
```

### If Branch

Using the `if` keyword, control flow can branch depending on a condition. The
`if` branch is executed if the condition is not `mlog::false` using `jump`
instruction's semantics, otherwise the `else` branch is executed.

```hlml
link cell1;
entrypoint {
  var value;
  mlog::read(value, cell1, 0);
  if value < 1000 { value *= 56; }
  else { value *= 4; }
  mlog::write(value, cell1, 1);
}
```

`else` might be omitted. In that case, it works as if the `else` had an empty
block.

```hlml
link cell1;
entrypoint {
  var value;
  mlog::read(value, cell1, 0);
  if value < 1000 { value *= 56; }
  mlog::write(value, cell1, 1);
}
```

There could be inner variable declarations after the `if`. Such variables are
only available inside the statement.

```hlml
proc read(cell, index) {
  var value;
  mlog::read(value, cell, index);
  return value;
}
link cell1;
entrypoint {
  if var value = read(cell1, 0); value < 1000 {
    mlog::write(value * 56, cell1, 1);
  }
  else {
    mlog::write(value * 4, cell1, 1);
  }
}
```

### While Loop

Using the `while` keyword, control flow can loop depending on a condition. The
`while` branch is executed if the condition is not `mlog::false` using `jump`
instruction's semantics. Otherwise the `else` branch is executed. If the `while`
branch is executed, the interleaved statement is executed. Then, the condition
is checked again and the `while` branch is executed again if the condition is
not `mlog::false`. This loops while the condition is not `mlog::false` using
`jump` instruction's semantics.

```hlml
link cell1;
entrypoint {
  var value;
  var i = 1;
  while i <= 1000; i++ {
    value = i;
  }
  else {
    value = 0;
  }
  mlog::write(value, cell1, 0);
}
```

`else` might be omitted. In that case, it works as if the `else` had an empty
block.

```hlml
link cell1;
entrypoint {
  var value = 0;
  while value < 1000; value++ {}
  mlog::write(value + 1, cell1, 0);
}
```

The interleaved statement might be omitted. In that case, it works as if the
interleaved statement was an empty block.

```hlml
link cell1;
entrypoint {
  var value = 0;
  while value < 1000 { value++; }
  mlog::write(value + 1, cell1, 0);
}
```

There could be inner variable declarations after the `while`. Such variables are
only available inside the statement.

```hlml
link cell1;
entrypoint {
  var value = 0;
  while var i = 0; i < 1000; i++ { value = i; }
  mlog::write(value, cell1, 0);
}
```

### Break Jump

Using the `break` keyword, a `while` loop might be exited early.

```hlml
link cell1;
entrypoint {
  var i = 0;
  while i < 15; i++ {
    var value;
    mlog::read(value, cell1, i);
    if value > 0 { break; }
  }
  mlog::write(i, cell1, 15);
}
```

Loops can be labeled. Then, the `break` can have a label to set which loop it
targets.

```hlml
link cell1;
link cell2;
link cell3;
entrypoint {
  outer: while var i = 0; i < 16; i++ {
    var v1;
    mlog::read(v1, cell1, i);
    while var j = 0; j < 16; j++ {
      var v2;
      mlog::read(v2, cell2, j);
      if v2 < 0 { break outer; }
      mlog::write(v1 + v2, cell3, i);
    }
  }
}
```

### Continue Jump

Using the `continue` keyword, a `while` loop might be looped early.

```hlml
link cell1;
entrypoint {
  while var i = 0; i < 16; i++ {
    var value;
    mlog::read(value, cell1, i);
    if value >= 0 { continue; }
    mlog::write(-value, cell1, i);
  }
}
```

Loops can be labeled. Then, the `continue` can have a label to set which loop it
targets.

```hlml
link cell1;
link cell2;
link cell3;
entrypoint {
  outer: while var i = 0; i < 16; i++ {
    var v1;
    mlog::read(v1, cell1, i);
    while var j = 0; j < 16; j++ {
      var v2;
      mlog::read(v2, cell2, j);
      if v2 >= 0 { continue outer; }
      mlog::write(v1 + v2, cell3, i);
    }
  }
}
```

### Return Jump

Using the `return` keyword, a `proc` might be exited early. Then, the
procedure's return value is the value given in the `return`.

```hlml
proc find_first(cell, searched) {
  while var i = 0; i < 16; i++ {
    var value;
    mlog::read(value, cell, i);
    if value == searched { return i; }
  }
}
link cell1;
link cell2;
entrypoint { mlog::write(find_first(cell1, 3), cell2, 0); }
```

The value might be omitted. In that case, it works as if the value was
`mlog::null`.

```hlml
proc square_root(a) {
  if a < 0 { return; }
  var result;
  mlog::op_sqrt(result, a);
  return result;
}
link cell1;
entrypoint { mlog::write(square_root(5), cell1, 0); }
```

### Affect

Affect statements are executed solely for their effect on the program's context.
These include mutate, assign and discard statements.

#### Mutate

These are syntactic sugar for `op` instruction. Using `++` and `--` operators, a
variable can be incremented or decremented.

```hlml
link cell1;
entrypoint {
  var value;
  mlog::read(value, cell1, 0);
  if value < 0 { value++; }
  else { value--; }
  mlog::write(value, cell1, 1);
}
```

#### Assign

These are syntactic sugar for `set` and `op` instructions. Using `=`, and
compounding it with binary operators other than comparison and logical ones
(`*=`, `/=`, `//=`, `%=`, `+=`, `-=`, `<<=`, `>>=`, `&=`, `^=`, `|=`), a
variable can be mutated in place.

```hlml
link cell1;
entrypoint {
  var value;
  mlog::read(value, cell1, 0);
  if value < 0 { value *= 9; }
  else { value /= 9; }
  mlog::write(value, cell1, 1);
}
```

#### Discard

An expression can be used as a statement. In that case, the value denoted by the
expression is discarded. Such expressions are only meaningful when they have
procedure invocations.

```hlml
proc double(a&) { a *= 2; }
entrypoint { var value = 17; double(value); }
```

## Expressions

Language constructs that denote a value.

### Constants

Expressions that denote compile-time known values.

#### Number Constants

Numbers are formed as the base, the whole part, the fraction and the exponent or
precision. The base might be omitted, in which case it is decimal. Decimals can
have an exponent with `e` or `E` while non decimals have precision with `p` or
`P`. Bases are shown with `0b`, `0o`, `0d` and `0x` which are for binary, octal,
decimal and hexadecimal numbers, respectively. Digits can have `_` in between,
for readability.

```hlml
link cell1;
entrypoint {
  while var i = 0; i < 16; i++ {
    mlog::write(0x1.ffff_ffff_ffff_fP+1023, cell1, i);
  }
}
```

#### Color Constants

These are packed colors, which are denoted like `%ff00ff` in Mindustry logic. In
HLML, these are separated from hexadecimal numbers using `0p` in the beginning.
Color constants must have 6 or 8 hexadecimal digits: every two digit is a byte
for red, green, blue and alpha channels, respectively. If the color is in 6
digits, the alpha channel is assumed to be all set. Digits can have `_` in
between, for readability.

```hlml
link display1;
const turquoise = 0p00_ef_ff;
entrypoint {
  mlog::clear(0, 0, 0);
  mlog::draw_col(turquoise);
  mlog::draw_rect(20, 20, 40, 40);
  mlog::drawflush(display1);
}
```

#### String Constants

These are just bunch of `""` delimitated characters in the source file. (Source
file's are all handled in Unicode, but non-ASCII characters are only allowed in
comment sand strings.) There are no escape characters in HLML but Mindustry
itself understands `\n` and `[[`: former is used for new lines and the latter is
used for escaping color specifiers in printing (which are like
`"[red]some red text"`).

```hlml
link message1;
const text = "Hello, Mindustry!";
entrypoint { mlog::print(text); mlog::printflush(message1); }
```

### Member-Access

This is syntactic sugar for the `sensor` instruction. The "property" (which is
just a variable that is passed to `sensor`) must be in the current scope! Create
an alias in this scope to access a property from another scope.

```hlml
using mlog::copper as what_im_looking_for;
link container1 as container;
link cell1 as cell;
entrypoint { mlog::write(container.what_im_looking_for, cell, 0); }
```

### Call

Invoke a procedure by passing `()` delimitated arguments after it. Denotes the
procedure's return value.

```hlml
proc double(a) { return a * 2; }
entrypoint { var value = 17; value = double(value); }
```

### Member-Call

This is syntactic sugar for calling procedures, but the first argument is before
the procedure name. It can only call procedures in the current scope! Create
alias in this scope to call a procedure from another scope as member.

```hlml
proc double(a&) { a *= 2; }
entrypoint { var value = 17; value.double(); }
```

### Operations

These are syntactic sugar for the `op` instruction.

#### Unary

Unary operations are promotion `+`, negation `-`, bitwise not`~`, logical not
`!`. Bitwise not has a direct representation in the `op` instruction, while
other unary operations are declared as if they were in a binary expression of
the same kind where the left operand was zero (or `mlog::false`).

```hlml
link cell1;
entrypoint {
  var a;
  mlog::read(a, cell1, 0);
  mlog::write(~a, cell1, 1);
}
```

#### Binary

Binary operations are multiplication `*`, division `/`, integer division `//`,
modulus `%`, addition `+`, subtraction `-`, left shift `<<`, right shift `>>`,
bitwise and `&`, bitwise xor `^`, bitwise or `|`, less than `<`, less than or
equal to `<=`, greater than `>`, greater than or equal to `>=`, equal to `==`,
not equal to `!=`, strictly equal to `===`, logical and `&&`, logical or `||`.
Other than short-circuiting logical operators, rest have direct representation
in the `op` instruction. Although `op` has a logical and operation, the only
difference that has from bitwise and is the return being `mlog::true` or
`mlog::false` instead of non-zero or zero. (Instructions that do not jump cannot
be short-circuiting by nature.) The hlml binary logical operators use jumps to
short-circuit and do not evaluate the right operand if the left operand's value
is enough the find the result. This might help to side step performance heavy
calculations or unsafe code after the left operand checks the safety.

```hlml
link cell1;
entrypoint {
  var a;
  var b;
  mlog::read(a, cell1, 0);
  mlog::read(b, cell1, 1);
  mlog::write(a // b, cell1, 2);
}
```

## Tools

You can use
[hlml.vscode](https://marketplace.visualstudio.com/items?itemName=calestialgem.hlml)
for highlighting in VS Code.

## License

Licensed under GPL 3.0 or later.

---

Copyright (C) 2023 Cem Geçgel <gecgelcem@outlook.com>
