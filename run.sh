#!/usr/bin/env bash

mvn clean package -DskipTests

java -cp target/offheap-1.0-SNAPSHOT.jar com.graphaware.offheap.map.client.TransientSharedMapClient