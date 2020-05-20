import os
import shutil
import subprocess

class NodeInfo:
    def __init__(self, name, ip_address):
        self.name = name
        self.ip_address = ip_address

def generate_node_confs():
    nodes = []

    while True:
        name = input("New node name (type 'exit' when done): ")
        if name == "exit":
            break

        ip_address = input("New node IP address (type 'exit' when done): ")
        if ip_address == "exit":
            break

        nodes.append(NodeInfo(name, ip_address))

    for node in nodes:
        node_conf = """myLegalName = "O={name},L=New York,C=US"
    p2pAddress = "{ip_address}:10000"
    rpcSettings {{
        address = "{ip_address}:10003"
        adminAddress = "{ip_address}:10004"
    }}""".format(name = node.name, ip_address = node.ip_address)

        f = open(node.name + "_node.conf", "w")
        try:
            f.write(node_conf)
        finally:
            f.close()

def download_and_run_bootstrapper():
    bootstrapper_url = "https://software.r3.com/artifactory/corda-releases/net/corda/corda-tools-network-bootstrapper/4.4/corda-tools-network-bootstrapper-4.4.jar"
    bootstrapper_filename = "bootstrapper.jar"

    subprocess.run(["curl", "-L", bootstrapper_url, "--output", bootstrapper_filename])
    subprocess.run(["java", "-jar", "bootstrapper.jar"])

def clean_up():
    shutil.rmtree(".cache")
    os.remove("checkpoints_agent-*.log")
    os.remove("diagnostic-*.log")
    os.remove("node-*.log")

os.mkdir("generated_network")
os.chdir("generated_network")
generate_node_confs()
download_and_run_bootstrapper()
clean_up()
