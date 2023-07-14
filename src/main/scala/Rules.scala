package rules

import cats.effect.{IO}
import cats.Functor
import cats.MonadThrow
import cats.syntax.all._
import scala.util._
import cats.data.Validated
import cats.data.NonEmptyList
import models._
import clients.zscaler.ZScalerApiClient
import clients.infrastructure.InfrastructureClient
import scala.util.matching.Regex


/**
  * The RuleResult interface. All rules returns a RuleResult.
  */
type RuleResult = IO[Either[DomainError, Unit]]

// First rule

/**
  * Fqdn must not exist in database.
  */
case class FqdnMustNotExistInExistingInfrastructureConfiguration(
  val rule: Fqdn => InfrastructureClient => RuleResult
)

object FqdnMustNotExistInExistingInfrastructureConfiguration:
  def live =
    FqdnMustNotExistInExistingInfrastructureConfiguration(
      liveImplementation
    )

  def succeeding =
    FqdnMustNotExistInExistingInfrastructureConfiguration(
      succeedingImplementation
    )
  
  def failing =
    FqdnMustNotExistInExistingInfrastructureConfiguration(
      failingImplementation
    )
  
  private def liveImplementation
    (fqdn: Fqdn)
    (client: InfrastructureClient): RuleResult = 
      for
        asgmtDomains <- client.readAllAsgmtDomains
      yield
        val fqdnExists = asgmtDomains.map(_.domain).contains(fqdn)
        
        fqdnExists match
          case true => Left(FqdnExistsInInfrastructure())
          case false => Right(())
  
  private def failingImplementation
    (fqdn: Fqdn)
    (client: InfrastructureClient): RuleResult = 
      Left(FqdnExistsInInfrastructure()).pure[IO]
  
  private def succeedingImplementation
    (fqdn: Fqdn)
    (client: InfrastructureClient): RuleResult = 
      Right(()).pure[IO]

// Second rule

/**
  * Fqdn must not exist in database.
  */
case class FqdnMustNotExistInZScaler(
  val rule: Fqdn => ZScalerApiClient => RuleResult
)

object FqdnMustNotExistInZScaler:
  def live =
    FqdnMustNotExistInZScaler(
      liveImplementation
    )

  def succeeding =
    FqdnMustNotExistInZScaler(
      succeedingImplementation
    )
  
  def failing =
    FqdnMustNotExistInZScaler(
      failingImplementation
    )
  
  private def liveImplementation
    (fqdn: Fqdn)
    (client: ZScalerApiClient): RuleResult =
      for
        zscalerFqdns <- client.readAllFqdn
      yield
        val fqdnExists = zscalerFqdns.contains(fqdn)
        
        fqdnExists match
          case true => Left(FqdnExistsInZscaler())
          case false => Right(())
      
  
  private def failingImplementation
    (fqdn: Fqdn)
    (client: ZScalerApiClient): RuleResult = 
      Left(FqdnExistsInZscaler()).pure[IO]
  
  private def succeedingImplementation
    (fqdn: Fqdn)
    (client: ZScalerApiClient): RuleResult = 
      Right(()).pure[IO]

// Third rule

/**
  * Fqdn must comply with domain pattern.
  */

case class FqdnMustMatchAnyDomainPattern(
  val rule: Fqdn => List[Regex] => RuleResult
)

object FqdnMustMatchAnyDomainPattern:
  def live =
    FqdnMustMatchAnyDomainPattern(
      liveImplementation
    )

  def succeeding =
    FqdnMustMatchAnyDomainPattern(
      succeedingImplementation
    )
  
  def failing =
    FqdnMustMatchAnyDomainPattern(
      failingImplementation
    )
  
  private def liveImplementation
    (fqdn: Fqdn)
    (patterns: List[Regex]): RuleResult = 
      var matches = patterns.map(_.matches(fqdn.value))
      val fqdnMatchesAtLeastOnePattern = matches.contains(true)
      
      fqdnMatchesAtLeastOnePattern match
        case true => IO.pure(Right(()))
        case false => IO.pure(Left(FqdnDoesNotMatchAnyValidDomainPatterns()))
  
  private def failingImplementation
    (fqdn: Fqdn)
    (patterns: List[Regex]): RuleResult = 
      Left(FqdnDoesNotMatchAnyValidDomainPatterns()).pure[IO]
  
  private def succeedingImplementation
    (fqdn: Fqdn)
    (patterns: List[Regex]): RuleResult = 
      Right(()).pure[IO]