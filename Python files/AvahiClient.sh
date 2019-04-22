brokerIP=($(avahi-browse -rtp _coap._udp | grep "Broker" | grep "=" | grep "IPv4" | grep "eth0" | cut -f 8 -d ';'))
brokerPort=($(avahi-browse -rtp _coap._udp | grep "Broker" | grep "=" | grep "IPv4" | grep "eth0" | cut -f 9 -d ';'))
count=${#brokerIP[@]}
for ((i=0; i<${#brokerIP[@]}; i++));
do
java -jar ParkingSpot.jar ${brokerIP[i]} ${brokerPort[i]} &  #remove echo and replace filename by actual file
done
