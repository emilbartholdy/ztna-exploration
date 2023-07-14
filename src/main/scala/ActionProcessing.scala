package actionprocessing

import models._
import validation._
import cats.effect.IO

class ActionProcessor():
  def process(actions: List[Action]): List[IO[Unit]] =
    def aux(action: Action): IO[Unit] =
      action match
        case AddFqdnToDb(fqdn) => IO.println(s"Adding $fqdn to db...")
        case _ => IO.println("Unknown action")

    actions.map(aux)