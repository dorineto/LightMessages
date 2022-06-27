#!/bin/bash

cd ./bin

debugServer=false
debugCliente=false

if [[ "$1" = "-h" || "$1" = "--help" ]]
then
    echo "Usage: runTest.sh [--debug (cliente | server)]"
    exit 0
fi

if [ "$1" = "--debug" ] 
then

    if [[ "$2" = "" && "$3" = "" ]] 
    then
        echo "Invalid option"
        echo "Usage: runTest.sh [--debug (cliente | server)]"
        exit 1
    fi

    if [[ "$2" = "cliente" || "$3" = "cliente" ]] 
    then
        debugCliente=true
    fi

    if [[ "$2" = "server" || "$3" = "server" ]] 
    then
        debugServer=true
    fi

fi

if ! $debugServer
then 
    java Server.LightMessageSocketServer&
fi

sleep 5

if ! $debugCliente
then
    java Client.LightMessageUI&
fi

java Client.LightMessageUI& 

