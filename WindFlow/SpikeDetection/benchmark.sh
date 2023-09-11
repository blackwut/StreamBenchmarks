#!/bin/sh

make clean
make all
RUNTIME=60
SAMPLING=128

FILTER_PAR=1
SINK_PAR=1


for SOURCE_PAR in 3 2; do
    MAX_OP_PAR=`expr 20 - $SOURCE_PAR - 1 - 1`
    echo "MAX_OP_PAR: $MAX_OP_PAR"
    for OP_PAR in `seq 6 1 $MAX_OP_PAR`; do
        for B in 0 2 4 8 16 32 64 128 256 512 1024 2048 4096 8192 16384 32768; do
            echo "./bin/sd --rate 0 --keys 0 --sampling $SAMPLING --batch $B --time $RUNTIME --parallelism \"$SOURCE_PAR,$OP_PAR,$FILTER_PAR,$SINK_PAR\""
            ./bin/sd --rate 0 --keys 0 --sampling $SAMPLING --batch $B --time $RUNTIME --parallelism "$SOURCE_PAR,$OP_PAR,$FILTER_PAR,$SINK_PAR"
        done
    done
done