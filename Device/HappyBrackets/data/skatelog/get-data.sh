#!/bin/bash

### Script to download audio from PI by automating the scp command
### The device is added as a parameter to the script

### move to the correct dir for running this script (one level above where this script is)

if [ $# -gt 0 ]; then
    DEVICE_NAME=$1
else
   echo "Enter the device Name"
   read DEVICE
   DEVICE_NAME=$DEVICE
fi


if [ "$DEVICE_NAME" != "" ]; then
# run scp 

    HOST_ADDRESS="pi@${DEVICE_NAME}"
    echo “Downloading skate data from ${HOST_ADDRESS}”
    scp -r $HOST_ADDRESS:~/HappyBrackets/data/skatelog .

else
    echo "You need to enter the device name as argument to call. eg ${0} 192.168.0.18"
fi


