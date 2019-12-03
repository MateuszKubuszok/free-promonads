package promonads

// cats.FlatMap couldn't be used for this unless we were passing [A] => cats.FlatMap[F[A, ?]]
trait FlatMap2[F[_, _]] {

  def flatMap[A, B, C](fab: F[A, B])(f: B => F[A, C]): F[A, C]
  def pure[A, B](value:     B): F[A, B]

  final def map[A, B, C](fab: F[A, B])(f: B => C): F[A, C] = flatMap(fab)(f andThen pure)
}
object FlatMap2 {
  def apply[F[_, _]](implicit proFlatMap: FlatMap2[F]): FlatMap2[F] = proFlatMap
}
