package promonads

// to be honest I am not sure that flatMap belongs here... but Haskell's arrow has it
// and Haskell's arrow is virtually promonad, so...
trait Promonad[F[_, _]] extends cats.arrow.Arrow[F] with FlatMap2[F] with Contravariant2[F] {
  final def pure[A, B](value:       B): F[A, B] = lift(_ => value)
  final def contramap[A, B, C](fab: F[A, B])(f: C => A): F[C, B] = lmap(fab)(f)
  final def first[A, B, C](fa:      F[A, B]): F[(A, C), (B, C)] = flatMap(id[(A, C)]) { ac =>
    map(contramap[A, B, (A, C)](fa)(_._1)) { b =>
      b -> ac._2
    }
  }
}
object Promonad {
  def apply[F[_, _]](implicit promonad: Promonad[F]): Promonad[F] = promonad

  implicit def fromArrowAndProFlatMap[F[_, _]](implicit Arrow: cats.arrow.Arrow[F],
                                               ProFlatMap:     FlatMap2[F]): Promonad[F] = new Promonad[F] {
    def lift[A, B](f:         A => B): F[A, B] = Arrow.lift(f)
    def flatMap[A, B, C](fab: F[A, B])(f: B => F[A, C]): F[A, C] = ProFlatMap.flatMap(fab)(f)
    def compose[A, B, C](f:   F[B, C], g: F[A, B]): F[A, C] = Arrow.compose(f, g)
  }

  implicit val forFunction: Promonad[Function] = new Promonad[Function] {
    def lift[A, B](f:         A => B): A => B = f
    def compose[A, B, C](f:   Function[B, C], g: Function[A, B]): A => C = f compose g
    def flatMap[A, B, C](fab: Function[A, B])(f: B => Function[A, C]): A => C = a => f(fab(a))(a)
  }
}
