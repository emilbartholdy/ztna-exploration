package models

enum AsgmtStatus:
  case Active
  case Tombstoned
  case Deleted

case class Fqdn(value: String)

case class AsgmtId(value: Int)

sealed trait Port
case class SinglePort(value: Int) extends Port
case class PortRange(start: SinglePort, end: SinglePort) extends Port

case class AsgmtDomain(
  id: AsgmtId,
  domain: Fqdn,
  status: AsgmtStatus
)

object AsgmtDomain:
  def mocks() =
    val ad1 = AsgmtDomain(AsgmtId(1), Fqdn("www.google.com"), AsgmtStatus.Active)
    val ad2 = AsgmtDomain(AsgmtId(2), Fqdn("www.facebook.com"), AsgmtStatus.Active)
    val ad3 = AsgmtDomain(AsgmtId(3), Fqdn("www.twitter.com"), AsgmtStatus.Active)
    val ad4 = AsgmtDomain(AsgmtId(4), Fqdn("www.reddit.com"), AsgmtStatus.Tombstoned)
    val ad5 = AsgmtDomain(AsgmtId(5), Fqdn("www.youtube.com"), AsgmtStatus.Deleted)

    List(ad1, ad2, ad3, ad4, ad5)

// Domain Errors

sealed trait DomainError
case class FqdnExistsInZscaler() extends DomainError
case class FqdnExistsInInfrastructure() extends DomainError
case class FqdnDoesNotMatchAnyValidDomainPatterns() extends DomainError // e.g. *.novo.com (Patrick gave a longer list)

// Infrastructure

/**
  * Parsed from json data from ServiceNow
  */
sealed trait Request
case class SisoRequest(fqdn: Fqdn, ports: List[Port]) extends Request
case class DefaultRequest(fqdn: Fqdn) extends Request

/**
  * Results from 
  */
sealed trait Action
case class AddFqdnToDb(fqdn: Fqdn) extends Action