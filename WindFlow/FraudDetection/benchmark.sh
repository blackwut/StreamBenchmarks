#!/bin/sh

make clean
make all
RUNTIME=60
SAMPLING=128
OP_PAR=24

# for SOURCE_PAR in 2; do
#     MAX_OP_PAR=`expr 20 - $SOURCE_PAR - 1`
#     for OP_PAR in `seq $SOURCE_PAR 2 $MAX_OP_PAR`; do
#         for B in 0 1 2 4 8 16 32 64 128; do
#             echo "./bin/fd --rate 0 --keys 0 --sampling $SAMPLING --batch $B --time $RUNTIME --parallelism \"$SOURCE_PAR,$OP_PAR,1\""
#             ./bin/fd --rate 0 --keys 0 --sampling $SAMPLING --batch $B --time $RUNTIME --parallelism "$SOURCE_PAR,$OP_PAR,1"
#         done
#     done
# done

for SOURCE_PAR in 2 3; do
    MAX_OP_PAR=`expr 40 - $SOURCE_PAR - 1`
    for OP_PAR in `seq 2 2 $MAX_OP_PAR`; do
        for B in 0 1 2 4 8 16 32 64 128; do
            echo "./bin/fd --rate 0 --keys 0 --sampling $SAMPLING --batch $B --time $RUNTIME --parallelism \"$SOURCE_PAR,$OP_PAR,1\""
            ./bin/fd --rate 0 --keys 0 --sampling $SAMPLING --batch $B --time $RUNTIME --parallelism "$SOURCE_PAR,$OP_PAR,1"
            sleep 10
        done
    done
done