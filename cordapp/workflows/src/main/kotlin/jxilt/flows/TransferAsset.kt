package jxilt.flows

import co.paralleluniverse.fibers.Suspendable
import jxilt.contracts.AssetContract
import jxilt.states.AssetState
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class TransferAsset(private val quantity: Int, private val newOwner: Party) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        // TODO: Implement and use schema to query for asset of correct quantity.
        val assets = serviceHub.vaultService.queryBy<AssetState>().states
        val inputAsset = assets.find { it.state.data.quantity == quantity }
                ?: throw IllegalArgumentException("No asset state with correct quantity found.")

        // TODO: Use confidential identities.
        val outputAsset = inputAsset.state.data.copy(owner = newOwner)
        val assetTransferCommand = AssetContract.TransferCommand

        val notary = inputAsset.state.notary
        val txBuilder = TransactionBuilder(notary)
                .addInputState(inputAsset)
                .addOutputState(outputAsset, AssetContract.ID)
                .addCommand(assetTransferCommand, ourIdentity.owningKey)

        txBuilder.verify(serviceHub)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        val newOwnerSession = initiateFlow(newOwner)
        subFlow(FinalityFlow(signedTx, listOf(newOwnerSession)))
    }
}

@InitiatedBy(TransferAsset::class)
class TransferAssetResponder(private val initiatorSession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        subFlow(ReceiveFinalityFlow(initiatorSession))
    }
}