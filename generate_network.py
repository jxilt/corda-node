import os
import pathlib
from pathlib import Path
import shutil
import subprocess
from string import Template

BOOTSTRAPPER_URL = "https://software.r3.com/artifactory/corda-releases/net/corda/corda-tools-network-bootstrapper/4.4/corda-tools-network-bootstrapper-4.4.jar"

class NodeInfo:
    def __init__(self, name, rpc_port):
        self.name = name
        self.rpc_port = rpc_port

def gen_node_confs_and_docker_compose():
    nodes = []

    while True:
        name = input("New node name (type 'exit' when done): ")
        if name == "exit":
            break

        rpc_port = input("New node RPC port (type 'exit' when done): ")
        if rpc_port == "exit":
            break

        nodes.append(NodeInfo(name, rpc_port))

    with open("../resources/template_node_conf", "r") as input_node_conf, \
        open("../resources/template_docker_compose", "r") as input_docker_compose, \
        open("docker-compose.yml", "w") as output_docker_compose:

        template_node_conf = Template(input_node_conf.read())
        template_docker_compose = Template(input_docker_compose.read())

        output_docker_compose.write("version: '3.0'\r\n\r\nservices:\r\n")

        for node in nodes:
            node_conf = template_node_conf.safe_substitute(
                {"name": node.name})
            with open(node.name + "_node.conf", "w") as output_node_conf:
                output_node_conf.write(node_conf)

            docker_compose_segment = template_docker_compose.safe_substitute(
                {"name": node.name, "rpc_port": node.rpc_port})
            output_docker_compose.write(docker_compose_segment)

def download_and_run_bootstrapper():
    bootstrapper_url = BOOTSTRAPPER_URL
    bootstrapper_filename = "bootstrapper.jar"

    subprocess.run(["curl", "-L", bootstrapper_url, "--output", bootstrapper_filename])
    subprocess.run(["java", "-jar", "bootstrapper.jar"])

def clean_up():
    shutil.rmtree(".cache")
    for file in Path(".").glob("*.log"):
        file.unlink()

os.mkdir("generated_network")
os.chdir("generated_network")
gen_node_confs_and_docker_compose()
download_and_run_bootstrapper()
clean_up()