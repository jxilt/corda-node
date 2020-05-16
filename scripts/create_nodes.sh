# Set-up the node creation
mkdir generated_nodes
python3 scripts/create_node_confs.py

# Bootstrapping the network.
cd generated_nodes
curl -L https://software.r3.com/artifactory/corda-releases/net/corda/corda-tools-network-bootstrapper/4.4/corda-tools-network-bootstrapper-4.4.jar \
    --output bootstrapper.jar
java -jar bootstrapper.jar

# Clean-up.
rm -r .cache
rm checkpoints_agent-*.log
rm diagnostic-*.log
rm node-*.log