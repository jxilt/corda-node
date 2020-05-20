package jxilt

import jxilt.flows.IssueAsset
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.RPCOps
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.loggerFor
import java.util.*

/**
 * Connects to a Corda node via RPC and performs RPC operations on the node.
 *
 * The RPC connection is configured using command line arguments.
 */
fun main(args: Array<String>) = Client(args).start()

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

    private val clientOps = listOf(IssueOp(rpcOps), TransferOp)
}

private interface ClientOp {
    val opName: String
    val inputPrompts: List<String>
    fun execute(args: List<String>)
}

class IssueOp(val rpcOps: CordaRPCOps): ClientOp {
    override val opName = "Issue asset."
    override val inputPrompts = listOf("Quantity to issue?")

    override fun execute(args: List<String>) {
        require(args.size == 1) { "Incorrect number of args passed." }
        val quantity = args[0].toInt()
        rpcOps.startFlowDynamic(IssueAsset::class.java, quantity).returnValue.get()
        println("$quantity of the asset successfully issued.")
    }
}

object TransferOp: ClientOp {
    override val opName = "Transfer asset."
    override val inputPrompts = listOf("Quantity to transfer?", "New owner?")

    override fun execute(args: List<String>) {
        // TODO: Update to perform logic.
        println(args)
    }
}