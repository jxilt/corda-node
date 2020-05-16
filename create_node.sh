if [ -z $1 ] 
then
    echo "The name of the node's folder must be passed as the first argument."
    exit
fi

mkdir $1
cp resources/node.conf $1/node.conf
curl -L https://r3.bintray.com/corda/net/corda/corda/4.4/corda-4.4.jar --output $1/corda.jar