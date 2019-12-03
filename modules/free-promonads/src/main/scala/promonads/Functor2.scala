package promonads

// cats.Functor couldn't be used for this unless we were passing [A] => cats.Functor[F[A, ?]]
trait Functor2[F[_, _]] {

  def map[A, B, C](fab: F[A, B])(f: B => C): F[A, C]
}
object Functor2 {
  def apply[F[_, _]](implicit functor2: Functor2[F]): Functor2[F] = functor2
}
