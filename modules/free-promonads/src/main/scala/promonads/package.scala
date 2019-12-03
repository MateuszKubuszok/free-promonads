package object promonads {

  type ~~>[F[_, _], G[_, _]] = Function2K[F, G]
}
