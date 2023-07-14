package givens

import clients.zscaler.ZScalerApiClient
import clients.infrastructure.InfrastructureClient
import validation._
import actionprocessing._

given mockZScalerApiClient: ZScalerApiClient = ZScalerApiClient.mock
given mockInfrastructureClient: InfrastructureClient = InfrastructureClient.mock
given validationFlowContainer: Validator = Validator.live
given actionProcessor: ActionProcessor = new ActionProcessor()