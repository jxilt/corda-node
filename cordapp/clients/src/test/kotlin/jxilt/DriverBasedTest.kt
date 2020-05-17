package jxilt

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.TestIdentity
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
    fun `node test`() = withDriver {
        val (partyAHandle, partyBHandle) = startNodes(bankA, bankB)

        val rpcAddress = partyAHandle.rpcAddress
        val rpcUsername = partyAHandle.rpcUsers[0].username
        val rpcPassword = partyAHandle.rpcUsers[0].password

        // TODO: Use the above to test the client.
    }

    private fun withDriver(test: DriverDSL.() -> Unit) = driver(
            DriverParameters(isDebug = true, startNodesInProcess = true)
    ) { test() }

    private fun DriverDSL.startNodes(vararg identities: TestIdentity) = identities
            .map { startNode(providedName = it.name) }
            .map { it.getOrThrow() }
}