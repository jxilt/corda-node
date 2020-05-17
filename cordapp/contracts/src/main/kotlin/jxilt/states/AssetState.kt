package jxilt.states

import jxilt.contracts.AssetContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

@BelongsToContract(AssetContract::class)
data class AssetState(val quantity: Int, val owner: AbstractParty) : ContractState {
    override val participants = listOf(owner)

    init {
        if (quantity <= 0)
            throw IllegalArgumentException("Asset quantity must be positive.")
    }
}
