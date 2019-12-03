package promonads

// cats.FlatMap couldn't be used for this unless we were passing [B] => cats.Contravariant[F[?, B]]
trait Contravariant2[F[_, _]] {

  def contramap[A, B, C](fab: F[A, B])(f: C => A): F[C, B]
}
object Contravariant2 {
  def apply[F[_, _]](implicit proContramap: Contravariant2[F]): Contravariant2[F] = proContramap
}
