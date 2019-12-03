package promonads

// basically Arrow[F] - Strong[F] + id + pure
trait Promonad[F[_, _]]
    extends cats.arrow.Category[F]
    with cats.arrow.Profunctor[F]
    with Functor2[F]
    with Contravariant2[F] {

  def lift[A, B](f:           A => B): F[A, B]
  def map[A, B, C](fab:       F[A, B])(f: B => C): F[A, C] = rmap(fab)(f)
  def contramap[A, B, C](fab: F[A, B])(f: C => A): F[C, B] = lmap(fab)(f)

  final def id[A]: F[A, A] = lift(identity)
  final def pure[A, B](value:      B): F[A, B] = lift(_ => value)
  final def dimap[A, B, C, D](fab: F[A, B])(f: C => A)(g: B => D): F[C, D] = compose(compose(lift(g), fab), lift(f))
}
object Promonad {
  def apply[F[_, _]](implicit promonad: Promonad[F]): Promonad[F] = promonad

  // remove String[F]
  implicit def fromArrow[F[_, _]](implicit Arrow: cats.arrow.Arrow[F]): Promonad[F] = new Promonad[F] {
    def lift[A, B](f:       A => B): F[A, B] = Arrow.lift(f)
    def compose[A, B, C](f: F[B, C], g: F[A, B]): F[A, C] = Arrow.compose(f, g)
  }

  implicit val forFunction: Promonad[Function] = new Promonad[Function] {
    def lift[A, B](f:       A => B): A => B = f
    def compose[A, B, C](f: B => C, g: A => B): A => C = f compose g
  }
}
