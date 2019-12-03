package promonads
package free

import cats.syntax.all._

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

  val program1 = MyDomain.freeCreateData andThen MyDomain.freeFetchData
  val program2 = MyDomain.freeCreateData andThen MyDomain.freeDeleteData

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

  "example programs" should {

    "run successfully" in {
      storage should beEmpty
      program1.foldMap(interpreter).apply("example data") should_=== Option("example data")
      storage.size should_=== 1
      program2.foldMap(interpreter).apply("example data")
      storage.size should_=== 1
    }
  }
}
