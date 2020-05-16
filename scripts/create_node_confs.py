class NodeInfo:
    def __init__(self, name, ip_address):
        self.name = name
        self.ip_address = ip_address

nodes = []

while True:
    name = input("New node name ('exit' to stop): ")
    if name == "exit":
        break

    ip_address = input("New node IP address ('exit' to stop): ")
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

    f = open("generated_nodes/" + node.name + "_node.conf", "w")
    try:
        f.write(node_conf)
    finally:
        f.close()