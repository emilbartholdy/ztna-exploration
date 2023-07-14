package clients.zscaler:

  import cats.effect.{IO};
  import models._

  class ZScalerApiClient(
    val readAllFqdn: IO[List[Fqdn]],
    val addFqdn: Fqdn => IO[Unit]
  )

  object ZScalerApiClient:  
    def mock: ZScalerApiClient =
      ZScalerApiClient(
        IO.pure(AsgmtDomain.mocks().map(_.domain)),
        _ => IO.unit
      )
    
    def failing: ZScalerApiClient =
      ZScalerApiClient(
        IO.raiseError(Exception("ZScaler API is down")),
        _ => IO.raiseError(Exception("ZScaler API is down"))
      )

package clients.infrastructure:

  import cats.effect.{IO}
  import models._
  
  class InfrastructureClient(
    val readAllAsgmtDomains: IO[List[AsgmtDomain]]
  )

  object InfrastructureClient:  
    def mock: InfrastructureClient =
      InfrastructureClient(
        IO.pure(AsgmtDomain.mocks())
      )
    
    def failing: InfrastructureClient =
      InfrastructureClient(
        IO.raiseError(Exception("Cannot connect to database"))
      )