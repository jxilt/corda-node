import os
import pathlib
from pathlib import Path
import shutil
import subprocess
from string import Template

BOOTSTRAPPER_URL = "https://software.r3.com/artifactory/corda-releases/net/corda/corda-tools-network-bootstrapper/4.4/corda-tools-network-bootstrapper-4.4.jar"

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

    template_node_conf_file = open("../resources/template_node.conf", "r")
    template_node_conf = Template(template_node_conf_file.read())

    try:
        for node in nodes:
            substitutions = {"name": node.name, "ip_address": node.ip_address}
            node_conf = template_node_conf.safe_substitute(substitutions)

            output_node_conf = open(node.name + "_node.conf", "w")
            try:
                output_node_conf.write(node_conf)
            finally:
                output_node_conf.close()
    finally:
        template_node_conf_file.close()

def download_and_run_bootstrapper():
    bootstrapper_url = BOOTSTRAPPER_URL
    bootstrapper_filename = "bootstrapper.jar"

    subprocess.run(["curl", "-L", bootstrapper_url, "--output", bootstrapper_filename])
    subprocess.run(["java", "-jar", "bootstrapper.jar"])

def clean_up():
    shutil.rmtree(".cache")
    for file in Path(".").glob("*.log"):
        pathlib.remove(file)

os.mkdir("generated_network")
os.chdir("generated_network")
generate_node_confs()
download_and_run_bootstrapper()
clean_up()