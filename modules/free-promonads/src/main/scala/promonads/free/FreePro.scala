package promonads
package free

import cats.syntax.all._

sealed trait FreePro[S[_, _], A, B] extends Product with Serializable {

  import FreePro._

  final def foldMap[T[_, _]: Promonad](f2k: S ~~> T): T[A, B] = {
    @scala.annotation.tailrec
    def rewrite[A1, B1, C1](head: T[A1, B1], tail: FreePro[S, B1, C1]): T[A1, C1] = tail match {
      case Lifted(f)                   => head.rmap(f)
      case Suspend(sab)                => head >>> f2k(sab)
      case Merge(Lifted(f), fbc)       => rewrite(head.rmap(f), fbc)
      case Merge(Suspend(sab), fbc)    => rewrite(head >>> f2k(sab), fbc)
      case Merge(Merge(fad, fdb), fbc) => rewrite(head, Merge(fad, Merge(fdb, fbc)))
    }
    rewrite(Promonad[T].id[A], this)
  }
}
object FreePro {

  final private[FreePro] case class Lifted[S[_, _], A, B](f:     A => B) extends FreePro[S, A, B]
  final private[FreePro] case class Suspend[S[_, _], A, B](sab:  S[A, B]) extends FreePro[S, A, B]
  final private[FreePro] case class Merge[S[_, _], A, B, C](fab: FreePro[S, A, B], fbc: FreePro[S, B, C])
      extends FreePro[S, A, C]

  final def id[S[_, _], A]: FreePro[S, A, A] = lift(identity)
  final def pure[S[_, _], A, B](value:        B):       FreePro[S, A, B] = Lifted(_ => value)
  final def lift[S[_, _], A, B](f:            A => B):  FreePro[S, A, B] = Lifted(f)
  final def suspend[S[_, _], A, B](suspended: S[A, B]): FreePro[S, A, B] = Suspend(suspended)

  implicit def promonadInstance[S[_, _]]: Promonad[FreePro[S, ?, ?]] = new Promonad[FreePro[S, ?, ?]] {
    def lift[A, B](f:       A => B): FreePro[S, A, B] = FreePro.lift(f)
    def compose[A, B, C](f: FreePro[S, B, C], g: FreePro[S, A, B]): FreePro[S, A, C] = FreePro.Merge(g, f)
  }
}
