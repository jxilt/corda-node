node_name = input("First node name: ")
ip_address = input("First node IP address: ")

node_conf = """myLegalName = "O={node_name},L=New York,C=US"
p2pAddress = "{ip_address}:10000"
rpcSettings {{
    address = "{ip_address}:10003"
    adminAddress = "{ip_address}:10004"
}}""".format(node_name = node_name, ip_address = ip_address)

f = open("generated_nodes/" + node_name + "_node.conf", "w")
try:
    f.write(node_conf)
finally:
    f.close()