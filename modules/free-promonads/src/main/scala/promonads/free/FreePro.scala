package promonads
package free

sealed trait FreePro[S[_, _], A, B] extends Product with Serializable {

  final def flatMap[C](f:       B => FreePro[S, A, C]): FreePro[S, A, C] = FreePro.FlatMap(this, f)
  final def map[C](f:           B => C):                FreePro[S, A, C] = flatMap(f andThen FreePro.pure)
  final def contramap[C](f:     C => A):                FreePro[S, C, B] = FreePro.Contramap(this, f)
  final def andThen[C](freePro: FreePro[S, B, C]):      FreePro[S, A, C] = FreePro.AndThen(this, freePro)
  final def compose[C](freePro: FreePro[S, C, A]):      FreePro[S, C, B] = FreePro.AndThen(freePro, this)

  // not stack safe - if this makes sense, we would have to rewrite it into sth better
  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  final def foldMap[T[_, _]: Promonad](f2k: S ~~> T): T[A, B] = this match {
    case FreePro.Lifted(f)                 => Promonad[T].lift(f)
    case FreePro.Suspend(sab)              => f2k(sab)
    case FreePro.FlatMap(fab, flatMap)     => Promonad[T].flatMap(fab.foldMap(f2k))(flatMap(_).foldMap(f2k))
    case FreePro.Contramap(fab, contramap) => Promonad[T].lmap(fab.foldMap(f2k))(contramap)
    case FreePro.AndThen(fab, fbc)         => Promonad[T].andThen(fab.foldMap(f2k), fbc.foldMap(f2k))
  }
}
object FreePro {

  final private[FreePro] case class Lifted[S[_, _], A, B](f:       A => B) extends FreePro[S, A, B]
  final private[FreePro] case class Suspend[S[_, _], A, B](sab:    S[A, B]) extends FreePro[S, A, B]
  final private[FreePro] case class FlatMap[S[_, _], A, B, C](fab: FreePro[S, A, B], flatMap: B => FreePro[S, A, C])
      extends FreePro[S, A, C]
  final private[FreePro] case class Contramap[S[_, _], A, B, C](fab: FreePro[S, A, B], contramap: C => A)
      extends FreePro[S, C, B]
  final private[FreePro] case class AndThen[S[_, _], A, B, C](fab: FreePro[S, A, B], fbc: FreePro[S, B, C])
      extends FreePro[S, A, C]

  final def id[S[_, _], A]: FreePro[S, A, A] = lift(identity)
  final def pure[S[_, _], A, B](value:        B):       FreePro[S, A, B] = Lifted(_ => value)
  final def lift[S[_, _], A, B](f:            A => B):  FreePro[S, A, B] = Lifted(f)
  final def suspend[S[_, _], A, B](suspended: S[A, B]): FreePro[S, A, B] = Suspend(suspended)

  implicit def promonadInstance[S[_, _]]: Promonad[FreePro[S, ?, ?]] = new Promonad[FreePro[S, ?, ?]] {
    def lift[A, B](f:         A => B): FreePro[S, A, B] = FreePro.lift(f)
    def compose[A, B, C](f:   FreePro[S, B, C], g: FreePro[S, A, B]): FreePro[S, A, C] = f compose g
    def flatMap[A, B, C](fab: FreePro[S, A, B])(f: B => FreePro[S, A, C]): FreePro[S, A, C] = fab.flatMap(f)
  }
}
