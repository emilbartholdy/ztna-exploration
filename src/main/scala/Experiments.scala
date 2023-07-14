import cats.effect.{IO, IOApp}
import scala.concurrent.duration._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.client.Client
import cats.Functor
import cats.syntax.functor._
import cats.effect.std.Random
import clients.zscaler.ZScalerApiClient
import cats.MonadThrow
import cats.syntax.all._
import scala.util._

case class Random(seed: Int):
  def nextInt: (Int, Random) =
    val rng = scala.util.Random(seed)
    val res = rng.nextInt
    val newSeed = Random(seed+1)
    (res, newSeed)

object RandomNumberPrinterApp extends IOApp.Simple:

  def printRandomNumberFact(client: Client[IO])(number: Int): IO[Unit] =
    client
      .expect[String](s"http://numbersapi.com/$number")
      .flatMap(IO.println)
  
  val run =
    EmberClientBuilder
      .default[IO]
      .build
      .use { client =>
        for {
          _ <- printRandomNumberFact(client)(33)
          _ <- printRandomNumberFact(client)(42)
          _ <- printRandomNumberFact(client)(11)
        } yield ()
    }

object FizzBuzzApp extends IOApp.Simple {
  val run =
    for {
      counter <- IO.ref(0)

      wait = IO.sleep(1.second)
      poll = wait *> counter.get

      _ <- poll.flatMap(IO.println(_)).foreverM.start
      _ <- poll.map(_ % 3 == 0).ifM(IO.println("fizz"), IO.unit).foreverM.start
      _ <- poll.map(_ % 5 == 0).ifM(IO.println("buzz"), IO.unit).foreverM.start

      _ <- (wait *> counter.update(_ + 1)).foreverM.void
    } yield ()
}

extension [A](a: A) {
  inline def |>[B](inline f: A => B): B = f(a)
}

val triple = (x: Int) => 3 * x
val half = (x: Int) => x / 2
val sum = (x: Int) =>  (y: Int) => x + y