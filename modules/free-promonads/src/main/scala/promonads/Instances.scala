package promonads

object Instances extends Instances
trait Instances extends LowPriorityInstances
trait LowPriorityInstances {

  // while Functor and Contravariant cannot be generalized to Functor2 and Contravariant2 the opposite is true

  implicit def functorFromFunctor2[F[_, _], A0](implicit functor2: Functor2[F]): cats.Functor[F[A0, ?]] =
    new cats.Functor[F[A0, ?]] {
      def map[A, B](fa: F[A0, A])(f: A => B): F[A0, B] = functor2.map(fa)(f)
    }

  implicit def contravariantFromContravariant2[F[_, _], A0](
    implicit contravariant2: Contravariant2[F]
  ): cats.Contravariant[F[?, A0]] = new cats.Contravariant[F[?, A0]] {
    def contramap[A, B](fa: F[A, A0])(f: B => A): F[B, A0] = contravariant2.contramap(fa)(f)
  }
}
