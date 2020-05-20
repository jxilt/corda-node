package jxilt

import jxilt.states.AssetState
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.TestIdentity
import net.corda.testing.core.singleIdentity
import net.corda.testing.driver.DriverDSL
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.NodeHandle
import net.corda.testing.driver.driver
import org.junit.Test
import java.util.concurrent.Future
import kotlin.test.assertEquals

class DriverBasedTest {
    private val bankA = TestIdentity(CordaX500Name("BankA", "", "GB"))
    private val bankB = TestIdentity(CordaX500Name("BankB", "", "US"))

    @Test
    fun `client can issue assets`() = withDriver {
        val (partyAHandle, _) = startNodes(bankA, bankB)
        val partyARpcOps = partyAHandle.rpc

        val quantity = 3
        IssueOp(partyARpcOps).execute(listOf(quantity.toString()))

        val assets = partyARpcOps.vaultQuery(AssetState::class.java).states
        assertEquals(1, assets.size)
        val asset = assets.single().state.data
        assertEquals(quantity, asset.quantity)
        assertEquals(partyAHandle.nodeInfo.singleIdentity(), asset.owner)
    }

    private fun withDriver(test: DriverDSL.() -> Unit) = driver(
            DriverParameters(isDebug = true, startNodesInProcess = true)
    ) { test() }

    private fun DriverDSL.startNodes(vararg identities: TestIdentity) = identities
            .map { startNode(providedName = it.name) }
            .map { it.getOrThrow() }
}