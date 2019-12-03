package promonads
package free

import org.specs2.mutable.Specification

sealed trait MyDomain[A, B] extends Product with Serializable
object MyDomain {
  case object CreateData extends MyDomain[String, java.util.UUID]
  case object FetchData extends MyDomain[java.util.UUID, Option[String]]
  case object DeleteData extends MyDomain[java.util.UUID, Unit]

  // typical Free pain
  val freeCreateData = MyDomain.CreateData.asFreePro[MyDomain]
  val freeFetchData  = MyDomain.FetchData.asFreePro[MyDomain]
  val freeDeleteData = MyDomain.DeleteData.asFreePro[MyDomain]
}

class FreePromonadSpec extends Specification {

  val program = MyDomain.freeCreateData andThen (for {
    data <- MyDomain.freeFetchData
    _ <- MyDomain.freeDeleteData
  } yield s"data: $data")

  val storage = scala.collection.mutable.Map.empty[java.util.UUID, String]

  val interpreter = new (MyDomain ~~> Function) {
    def apply[A, B](fab: MyDomain[A, B]): Function[A, B] = fab match {
      case MyDomain.CreateData => { (data: String) =>
        val uuid = java.util.UUID.randomUUID
        storage.put(uuid, data)
        uuid
      }
      case MyDomain.FetchData => { (uuid: java.util.UUID) =>
        storage.get(uuid)
      }
      case MyDomain.DeleteData => { (uuid: java.util.UUID) =>
        storage.remove(uuid)
        ()
      }
    }
  }

  "example program" should {

    "run successfully" in {
      program.foldMap(interpreter).apply("example data") should_=== "data: Some(example data)"
      storage should beEmpty
    }
  }
}
