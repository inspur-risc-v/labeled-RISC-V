// See LICENSE.SiFive for license details.

package rocketchip

import Chisel._
import config._
import diplomacy._
import uncore.tilelink2._
import uncore.devices._
import util._
import coreplex._

trait RocketPlexMaster extends HasTopLevelNetworks {
  val module: RocketPlexMasterModule

  val coreplex = LazyModule(new DefaultCoreplex)

  coreplex.l2in :=* fsb.node
  bsb.node :*= coreplex.l2out
  socBus.node := coreplex.mmio
  coreplex.mmioInt := intBus.intnode

  require (mem.size == coreplex.mem.size)
  (mem zip coreplex.mem) foreach { case (xbar, channel) => xbar.node :=* channel }
}

trait RocketPlexMasterBundle extends HasTopLevelNetworksBundle {
  val outer: RocketPlexMaster
  val tcrs = Vec(p(RocketTilesKey).size, new Bundle {
    val clock = Clock(INPUT)
    val reset = Bool(INPUT)
    })
}

trait RocketPlexMasterModule extends HasTopLevelNetworksModule {
  val outer: RocketPlexMaster
  val io: RocketPlexMasterBundle
  val clock: Clock
  val reset: Bool

  outer.coreplex.module.io.tcrs.zipWithIndex.map { case (tcr, i) =>
    tcr.clock := io.tcrs(i).clock
    tcr.reset := io.tcrs(i).reset
  }
}
