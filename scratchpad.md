# bad ux

* multiple node start-ups required to detect all missing config options. Starting with a blank file only indicates myLegalName and p2pAddress are missing. Adding them goes on to indicate that rpcSettings.address are missing. Adding that goes on to indicate that rpcSettings.adminAddress is missing

# bugs

* this page refers to a webserver JAR that doesn't exist (https://docs.corda.net/docs/corda-os/4.4/generating-a-node.html)
* this page appears to have bad link text ("see docs:") (https://docs.corda.net/docs/corda-os/4.4/generating-a-node.html)

# unsolved

* error message when starting node: "[ERROR] 12:05:01+0100 [main] internal.NodeStartupLogging. - Exception during node startup: Couldn't find network parameters file and compatibility zone wasn't configured/isn't reachable. [errorCode=1917kd6, moreInformationAt=https://errors.corda.net/OS/4.4/1917kd6]"