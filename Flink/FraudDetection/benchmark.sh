#!/bin/sh

mvn clean install
# RUNTIME=60
SAMPLING=128

for S in 4 5 6 7 8; do
    LAST=`expr 40 - 1 - $S`
    for OP_PAR in `seq 2 2 $LAST`; do
        echo "java -cp target/FraudDetection-1.0.jar FraudDetection.FraudDetection --rate 0 --sampling $SAMPLING --parallelism $S $OP_PAR 1"
        java -cp target/FraudDetection-1.0.jar FraudDetection.FraudDetection --rate 0 --sampling $SAMPLING --parallelism $S $OP_PAR 1
    done
done
