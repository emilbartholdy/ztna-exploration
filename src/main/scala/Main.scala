import cats.effect.{IO, IOApp}
import scala.concurrent.duration._
import cats.Functor
import cats.syntax.functor._
import cats.MonadThrow
import cats.syntax.all._
import scala.util._
import models._
import validation._
import actionprocessing.ActionProcessor
import givens.{given Validator, given ActionProcessor}

object ZtnaServerApp extends IOApp.Simple:

  val request1 = SisoRequest(Fqdn("www.google1.com"), List(SinglePort(443)))
  val request2 = DefaultRequest(Fqdn("www.google.com"))
  
  def processRequest(request: Request)(using validator: Validator): ValidationFlowResult =
    request match
      case r: SisoRequest => validator.siso(r)
      case r: DefaultRequest => validator.default(r)

  def processResult(validationFlowResult: ValidationFlowResult)(using actionProcessor: ActionProcessor): IO[List[IO[Unit]]] =
    for
      result <- validationFlowResult
    yield
      result match
        case Left(error) => List(IO.println(s"Error: ${error}"))
        case Right(actions) => actionProcessor.process(actions)
  
  val run =
    for
      _ <- IO.println("Processing request...")
      result = processRequest(request1)
      _ <- IO.println("Processing actions...")
      actions <- processResult(result)
      _ <- IO.println("Executing actions...")
      _ <- actions.sequence
      _ <- IO.println("Request successfully processed!")
    yield ()
