if [ -z $1 ] 
then
    echo "The name of the node's folder must be passed as the first argument."
    exit
fi

mkdir generated_nodes
cp resources/node.conf generated_nodes/$1_node.conf
curl -L https://software.r3.com/artifactory/corda-releases/net/corda/corda-tools-network-bootstrapper/4.4/corda-tools-network-bootstrapper-4.4.jar --output generated_nodes/bootstrapper.jar

# Bootstrapping the network.
cd generated_nodes
java -jar bootstrapper.jar

# Clean-up.
rm checkpoints_agent-*.log
rm diagnostic-*.log
rm node-*.log