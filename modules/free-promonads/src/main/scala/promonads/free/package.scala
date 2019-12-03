package promonads

package object free {

  implicit final class ToFreeOps[S[_, _], A, B](val fab: S[A, B]) {

    def asFreePro[T[A1, B1] >: S[A1, B1]]: FreePro[T, A, B] = FreePro.suspend(fab)
  }
}
