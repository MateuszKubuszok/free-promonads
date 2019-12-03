package promonads

// binatural transformation
trait Function2K[F[_, _], G[_, _]] {

  def apply[A, B](fab: F[A, B]): G[A, B]
}
