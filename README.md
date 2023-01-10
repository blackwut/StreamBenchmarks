[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2FParaGroup%2FStreamBenchmarks&count_bg=%2379C83D&title_bg=%23555555&icon=&icon_color=%232F84E1&title=hits&edge_flat=false)](https://hits.seeyoufarm.com)

# StreamBenchmarks

This repository contains a set of stream processing applications taken from the literature, and from existing repositories (e.g., [here](https://github.com/GMAP/DSPBench)), which have been cleaned up properly. The applications can be run in a homogeneous manner and their execution collects statistics of throughput and latency in different ways.

Below we list the applications with the availability in different Stream Processing Engines and Libraries. We consider Apache Storm, Apache Flink and WindFlow ([link](https://github.com/ParaGroup/WindFlow)):

|      Application     | Acronym | Apache Storm | Apache Flink | WindFlow |
|:--------------------:|:-------:|:------------:|:------------:|:--------:|
|    FraudDetection    |    FD   |      Yes     |      Yes     |    Yes   |
|    SpikeDetection    |    SD   |      Yes     |      Yes     |    Yes   |
|   TrafficMonitoring  |    TM   |      Yes     |      Yes     |    Yes   |
|       WordCount      |    WC   |      Yes     |      Yes     |    Yes   |
|      LinearRoad      |    LR   |      Yes     |      Yes     |    Yes   |
|      VoipStream      |    VS   |      Yes     |      Yes     |    Yes   |
|   SentimentAnalysis  |    SA   |      No      |      No      |    Yes   |
|     LogProcessing    |    LP   |      No      |      No      |    Yes   |
|    MachineOutlier    |    MO   |      No      |      No      |    Yes   |
| ReinforcementLearner |    RL   |      No      |      No      |    Yes   |

This repository also contains small datasets used to run the applications except for LinearRoad and VoipStream. For these two applications, datasets can be generated as described in [1](path%20with%20spaces/other_file.md) and [2](path%20with%20spaces/other_file.md). Once generated, please copy the dataset files in the <strong>Datasets/LR</strong> and <strong>Datasets/VS</strong> folders respectively. The datasets are used by all versions of the same application in all the supported frameworks.

This repository is not totally cleaned and there is a certain duplication of code. The reason is because each application, for each framework, is designed to be a separated standalone project. Refer to the README file within each subfolder (application/framework) for further information about how to run each application and for the required dependencies.

## How to Cite
This repository uses the applications that we have recently added to a larger benchmark suite of streaming applications called <tt>DSPBench</tt> available on GitHub at the following [link](https://github.com/GMAP/DSPBench). If our applications revealed useful for your research, we kindly ask you to give credit to our effort by citing the following paper:
```
@article{DSPBench,
 author={Bordin, Maycon Viana and Griebler, Dalvan and Mencagli, Gabriele and Geyer, Cláudio F. R. and Fernandes, Luiz Gustavo L.},
 journal={IEEE Access},
 title={DSPBench: A Suite of Benchmark Applications for Distributed Data Stream Processing Systems},
 year={2020},
 volume={8},
 number={},
 pages={222900-222917},
 doi={10.1109/ACCESS.2020.3043948}
}
```

## Contributors
The main developer and maintainer of this repository is [Gabriele Mencagli](mailto:gabriele.mencagli@unipi.it). Other authors of the source code are Alessandra Fais, Andrea Cardaci and Cosimo Agati.
