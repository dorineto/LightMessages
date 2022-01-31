#!/bin/bash

cd ./bin

java Server.LightMessageSocketServer&

sleep 5

java Client.LightMessageUI&
java Client.LightMessageUI&
