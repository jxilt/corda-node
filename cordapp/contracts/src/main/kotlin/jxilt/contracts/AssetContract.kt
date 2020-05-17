package jxilt.contracts

import jxilt.states.AssetState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class AssetContract : Contract {
    companion object {
        const val ID = "jxilt.contracts.AssetContract"
    }

    interface AssetCommands : CommandData
    object IssueCommand : TypeOnlyCommandData(), AssetCommands
    object TransferCommand: TypeOnlyCommandData(), AssetCommands

    override fun verify(tx: LedgerTransaction) {
        val assetCommands = tx.commandsOfType<AssetCommands>()
        if (assetCommands.size != 1)
            throw IllegalArgumentException("Asset transaction must have exactly one asset command.")
        val assetCommand = assetCommands.single()

        when (assetCommand.value) {
            IssueCommand -> verifyIssue(tx, assetCommand.signers)
            TransferCommand -> verifyTransfer(tx, assetCommand.signers)
            else -> throw IllegalArgumentException("Asset command not recognised.")
        }
    }

    private fun verifyIssue(tx: LedgerTransaction, signers: List<PublicKey>) {
        if (tx.inputsOfType<AssetState>().isNotEmpty())
            throw IllegalArgumentException("Asset issuance should have no asset inputs.")
        val assets = tx.outputsOfType<AssetState>()
        if (assets.size != 1)
            throw IllegalArgumentException("Asset issuance should have one asset output.")

        val asset = assets.single()
        if (!signers.contains(asset.owner.owningKey))
            throw java.lang.IllegalArgumentException("Asset issuance must be signed by asset owner.")
    }

    private fun verifyTransfer(tx: LedgerTransaction, signers: List<PublicKey>) {
        val inputAssets = tx.inputsOfType<AssetState>()
        val outputAssets = tx.outputsOfType<AssetState>()
        if (inputAssets.size != 1)
            throw IllegalArgumentException("Asset transfer should have one asset input.")
        if (outputAssets.size != 1)
            throw IllegalArgumentException("Asset transfer should have one asset output.")

        val inputAsset = inputAssets.single()
        val outputAsset = outputAssets.single()

        if (inputAsset.quantity != outputAsset.quantity)
            throw IllegalArgumentException("Asset transfer should not change asset quantity.")
        if (!signers.contains(inputAsset.owner.owningKey))
            throw java.lang.IllegalArgumentException("Asset transfer must be signed by existing asset owner.")
    }
}