package validation

import cats.effect.{IO, IOApp}
import scala.concurrent.duration._
import cats.Functor
import cats.syntax.functor._
import cats.MonadThrow
import cats.syntax.all._
import scala.util._
import cats.data.Validated
import cats.data.NonEmptyList
import models._
import clients.zscaler.ZScalerApiClient
import clients.infrastructure.InfrastructureClient
import scala.util.matching.Regex
import givens.{given ZScalerApiClient, given InfrastructureClient}
import _root_.rules._ // Why

/**
  * The ValidationFlowResult interface. All validationflows returns a ValidationFlowResult.
  */
type ValidationFlowResult = IO[Either[DomainError, List[Action]]]

// ValidationFlows interface

class Validator(
  val siso: SisoRequest => ValidationFlowResult,
  val default: DefaultRequest => ValidationFlowResult
)

// ValidationFlows instances

object Validator:
  def live: Validator =
    Validator(
      sisoValidationFlow,
      defaultValidationFlow
    )
  def failing: Validator =
    Validator(
      _ => IO.raiseError(Exception("Error in validation flows happened")),
      _ => IO.raiseError(Exception("Error in validation flows happened"))
    )

// Validation Flow implementations

def sisoValidationFlow
  (request: SisoRequest)
  (using infrastructureClient: InfrastructureClient)
  (using zscalerApiClient: ZScalerApiClient) : ValidationFlowResult =
    for
      r1 <- FqdnMustNotExistInExistingInfrastructureConfiguration.live.rule(request.fqdn)(infrastructureClient)
      r2 <- FqdnMustNotExistInZScaler.live.rule(request.fqdn)(zscalerApiClient)
    yield for 
        _ <- r1
        _ <- r2
    yield
      List(AddFqdnToDb(request.fqdn))
    
def defaultValidationFlow
  (request: DefaultRequest)
  (using infrastructureClient: InfrastructureClient)
  (using zscalerApiClient: ZScalerApiClient) : ValidationFlowResult =
    for
      r1 <- FqdnMustNotExistInExistingInfrastructureConfiguration.live.rule(request.fqdn)(infrastructureClient)
      r2 <- FqdnMustNotExistInZScaler.live.rule(request.fqdn)(zscalerApiClient)
      r3 <- FqdnMustMatchAnyDomainPattern.live.rule(request.fqdn)(List(".*.novo.com".r))
    yield for
      _ <- r1
      _ <- r2
      _ <- r3
    yield 
      List(AddFqdnToDb(request.fqdn))