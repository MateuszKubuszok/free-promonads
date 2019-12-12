# Free Promonads

An implementation of a profunctor, promonad and free promonad basing on my limited and probably wrong understanding of what they are.

## Profunctor

The intuition is that profunctor `P[A, B]` is a wrapper/placeholder for a function `A => B`.
But it isn't any wrapper for `A => B`, it is a wrapper which maintains some of `A => B` properties:
it is covariant on `B`, and contravariant on `A`: 

```scala
type P[-A, +B]
val pab: P[A, B]

val b2c: B => C
pab.map(b2c) : P[A, C]

val a2d: A =>D
pab.contramap(a2d): P[D, B]

type A0 >: A
type B0 <: B
pab: P[A0, B0] // downcasting on A, upcasting on B is allowed
```

We can understand it intuitively that if `P[A, B]` is `A => B` in a box `P` - imagine sth like `P(f: A => B)` -
then `map(g)` would be `P(f andThen g)` and `contramap(h)` would be `P(f combine h)`.
(In practice `P` doesn't have to be contain some `f` literelly, it can be a placeholder
or something working as-if there was a function there, but I find this intuition to work for me).

`cats.arrow.Profunctor` implement exactly that, except it also provides `dimap` combining `map` and `contramap` into one function,
and calling `contramap` and `map` `lmap` and `rmap` respectively.

## Promonad

While we can _put function `f` in a box `P`_ and then have some `g`/`h` be _appended/prependd to function in a box_
we cannot combine 2 functions that were already put in a box:

```scala
val pab: P[A, B]
val pbc: P[B, C]

def combine[A, B, C](p1: P[A, B], p2: P[B, C]): P[A, C] = ??? // missing
combine(pab, pbc) // we cannot do it with profunctor
```

A promonad is something, that allows us to do exactly that: take fome `f` in a box `P` (`P(f: A => B)`),
take some `g` in a box `P` (`P(g: B => C)`) and returns a box with combined functions `f` and `g` (`P(f andThen g)`).

```scala
def combine[A, B, C](p1: P[A, B], p2: P[B, C]): P[A, C] // defined for promonad
```

Additionally, promonad let us put any function `A => B` into box `P`.

```scala
def lift[A, B](f: A => B): P[A, B]
```

Promonad is also a category, which means that there is an identity:

```scala
def id[A]: P[A, A]

combine(id[A], combine(pab, id[B])) === pab
```

All in all we get something like:

```scala
trait Promonad[P[_,_]] extends Category[P] with Profunctor[P] {

  def lift[A, B]: P[A, A]
  def combine[A, B, C](p1: P[A, B])(p2: P[B, C]): P[A,C]

  def id[A] = lift(identity)

  // implements Category's and Profunctor's methods using methods above
}
```

This, is even to `cats.arrow.Arrow`, *except it still misses `Strong[P]`*.
More of consequences of that later on.

## Free

Free algebra X (where X = e.g. monoid, semigroup, monad, ring, etc) is a way of taking some type `A` (not necssarily of kind `*`)
and - without maing any assumptions about `A` - lift it to some type `Lifted[A]` which implements operations and requirements
of algebra X. For instance:

 * free semigroup of `A`, would be some `FM[A]` that: defines associative `+: (F[A], F[A]) => F[A]`
 * free monoid of `A`, would be some `FM[A]` that: defines associative `+: (F[A], F[A]) => F[A]` AND defines empty `FM[A]`
 * free monad `Free[F]` takes some `F[_]` and turns in into `Free[F]` which is a monad - has `pure: A => Free[F, A]` and `flatMap: (Free[F, A], A => Free[F, B]) => Free[F, B]`

This free algebra cannot assume anything except what is assument about algebra it implements. In particular, they cannot perform any actual operations on its values.
So what they can do if they cannot calulate things? Well, they can remember what operations were performed, storing just enough information so that later on, when you would
learn how to actually e.g combine values in your semigroup, you could replay them.

That's what we can see in `FreePro` (free monad): it remembers how we combined, lifted `S[_,_]` (profunctor), so that later on, when we would provide some translation
from type `S[_,_] => T[_,_]` and the definition of promonadic operations for `T[_,_]` (`Promonad[T]`), it would replay operations as it would translate `S` into `T`.
(see: [example](https://github.com/MateuszKubuszok/free-promonads/blob/master/modules/free-promonads/src/test/scala/promonads/free/FreePromonadSpec.scala)).

## So, is it actually useful?

TL;DR - hardly.

Remeber that `Promonad[P]` is kind of like `Arrow[P]` but without `Strong[P]`? This makes some huge difference.

Let say you would like to compose things like this:

```scala
val p1: P[A, B]
val p2: P[(A, B), C]
val p3: P[(A, B, C), D]
val p4: P[A, D] = ??? // compose p1, p2, p3 by threading argument from p1, through p2 and p3 etc
```

You cannot. There is no way for you to actually implement this without `Strong[P]` meaning that you
cannot reuse values from previous stages of computation - if your value `p` isn't already threading it
like e.g. `P[A, (A, B)]`, then you cannot do anything to reuse that `A` later on.

More than that, it prevents you from branching your code! Any sort of circuit breaking, if-else picking one
value `P` or the other depending on received `A` (basically `flatMap`) is also impossible to implement.

This limits us to only combining functions that never reuse previous steps of computations and never branches.
It is a huuuge limitation, which explains why even in Haskell you could have trouble finding promonads other
than as parts of some Arrow.

I guess they could(?) be used as abstraction useful for handling optics... though even with optics you'd
rather just create promonad-based DSL without reifying the concept into actuall type-class.

Therefore it was a nice little experiment, but it only explained to me why nobody bothers with promonads
in practice.
