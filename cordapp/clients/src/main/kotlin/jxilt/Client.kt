package jxilt

import jxilt.flows.IssueAsset
import jxilt.flows.TransferAsset
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.loggerFor
import java.util.*

/**
 * Connects to a Corda node via RPC and performs RPC operations on the node.
 */
fun main(args: Array<String>) = Client(args).start()

/**
 * A generic client for interacting with a node running the asset CorDapp.
 */
private class Client(args: Array<String>) {
    companion object {
        val logger = loggerFor<Client>()
    }

    private val rpcOps: CordaRPCOps

    init {
        require(args.size == 3) { "Usage: Client <node address> <rpc username> <rpc password>" }
        val nodeAddress = NetworkHostAndPort.parse(args[0])
        val rpcUsername = args[1]
        val rpcPassword = args[2]
        val connection = CordaRPCClient(nodeAddress).start(rpcUsername, rpcPassword)
        rpcOps = connection.proxy
    }

    fun start() {
        while (true) {
            selectAndExecuteOp()
        }
    }

    // TODO: Provide ability to exit loop.
    // Loops through the various possible operations and allows the user to select one.
    private fun selectAndExecuteOp() {
        println("Choose one of the following options:")
        clientOps.forEachIndexed { idx, op ->
            println("$idx) ${op.opName}")
        }

        val scanner = Scanner(System.`in`)

        val choice = scanner.nextInt()
        val chosenOp = clientOps[choice]

        val inputs = chosenOp.inputPrompts.map { inputPrompt ->
            println(inputPrompt)
            scanner.next()
        }

        chosenOp.execute(inputs)
    }

    // The list of registered operations.
    private val clientOps = listOf(IssueOp(rpcOps), TransferOp(rpcOps))
}

/**
 * An abstract RPC client operation.
 */
private interface ClientOp {
    val rpcOps: CordaRPCOps
    val opName: String
    val inputPrompts: List<String>
    fun execute(args: List<String>)
}

/**
 * An RPC client operation for issuing an asset.
 */
class IssueOp(override val rpcOps: CordaRPCOps): ClientOp {
    override val opName = "Issue asset."
    override val inputPrompts = listOf("Quantity to issue?")

    override fun execute(args: List<String>) {
        require(args.size == inputPrompts.size) { "Incorrect number of args passed." }
        val quantity = args[0].toInt()

        rpcOps.startFlowDynamic(IssueAsset::class.java, quantity).returnValue.get()
        println("$quantity of the asset successfully issued.")
    }
}

/**
 * An RPC client operation for transferring an asset.
 */
class TransferOp(override val rpcOps: CordaRPCOps) : ClientOp {
    override val opName = "Transfer asset."
    override val inputPrompts = listOf("Quantity to transfer?", "New owner?")

    override fun execute(args: List<String>) {
        require(args.size == inputPrompts.size) { "Incorrect number of args passed." }
        val quantity = args[0].toInt()
        val matchingOwners = rpcOps.partiesFromName(args[1], true)
        require(matchingOwners.size == 1) { "No single match found for new owner." }
        val newOwner = matchingOwners.single()

        rpcOps.startFlowDynamic(TransferAsset::class.java, quantity, newOwner).returnValue.get()
        println("$quantity of the asset transferred to ${newOwner.name}.")
    }
}