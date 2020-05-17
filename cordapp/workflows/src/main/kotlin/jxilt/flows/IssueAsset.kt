package jxilt.flows

import co.paralleluniverse.fibers.Suspendable
import jxilt.contracts.AssetContract
import jxilt.states.AssetState
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class IssueAsset(private val quantity: Int) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        // TODO: Use confidential identity.
        val asset = AssetState(quantity, ourIdentity)
        val assetIssueCommand = AssetContract.IssueCommand

        // TODO: Select notary properly.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(asset, AssetContract.ID)
                .addCommand(assetIssueCommand, ourIdentity.owningKey)

        txBuilder.verify(serviceHub)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(FinalityFlow(signedTx, listOf()))
    }
}