package jxilt

import jxilt.contracts.AssetContract
import jxilt.flows.IssueAsset
import jxilt.flows.TransferAsset
import jxilt.flows.TransferAssetResponder
import jxilt.states.AssetState
import net.corda.core.node.services.queryBy
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class FlowTests {
    private val network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
        TestCordapp.findCordapp("jxilt.contracts"),
        TestCordapp.findCordapp("jxilt.flows")
    )))
    private val a = network.createNode()
    private val b = network.createNode()

    init {
        listOf(a, b).forEach {
             it.registerInitiatedFlow(TransferAssetResponder::class.java)
        }
    }

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `asset is issued`() {
        val quantity = 99
        val flow = a.startFlow(IssueAsset(quantity))
        network.runNetwork()
        flow.get()

        a.transaction {
            val assets = a.services.vaultService.queryBy<AssetState>().states
            assertEquals(1, assets.size)

            val asset = assets.single().state
            assertEquals(asset.contract, AssetContract.ID)
            assertEquals(a.info.singleIdentity(), asset.data.owner)
            assertEquals(quantity, asset.data.quantity)
        }
    }

    @Test
    fun `asset is transferred`() {
        val quantity = 99
        val issueFlow = a.startFlow(IssueAsset(quantity))
        network.runNetwork()
        issueFlow.get()

        val transferFlow = a.startFlow(TransferAsset(quantity, b.info.singleIdentity()))
        network.runNetwork()
        transferFlow.get()

        a.transaction {
            val assets = a.services.vaultService.queryBy<AssetState>().states
            assertEquals(0, assets.size)
        }

        b.transaction {
            val assets = b.services.vaultService.queryBy<AssetState>().states
            assertEquals(1, assets.size)

            val asset = assets.single().state
            assertEquals(asset.contract, AssetContract.ID)
            assertEquals(b.info.singleIdentity(), asset.data.owner)
            assertEquals(quantity, asset.data.quantity)
        }
    }

    @Test
    fun `asset to be transferred does not exist`() {
        val quantity = 99
        val issueFlow = a.startFlow(IssueAsset(quantity))
        network.runNetwork()
        issueFlow.get()

        val transferFlow = a.startFlow(TransferAsset(quantity + 1, b.info.singleIdentity()))
        network.runNetwork()
        assertFails { transferFlow.get() }
    }
}