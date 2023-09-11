/**************************************************************************************
 *  Copyright (c) 2019- Gabriele Mencagli and Alessandra Fais
 *
 *  This file is part of StreamBenchmarks.
 *
 *  StreamBenchmarks is free software dual licensed under the GNU LGPL or MIT License.
 *  You can redistribute it and/or modify it under the terms of the
 *    * GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version
 *    OR
 *    * MIT License: https://github.com/ParaGroup/StreamBenchmarks/blob/master/LICENSE.MIT
 *
 *  StreamBenchmarks is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  You should have received a copy of the GNU Lesser General Public License and
 *  the MIT License along with WindFlow. If not, see <http://www.gnu.org/licenses/>
 *  and <http://opensource.org/licenses/MIT/>.
 **************************************************************************************
 */

#include<string>
#include<vector>
#include<sstream>
#include<iostream>
#include<ff/ff.hpp>
#include<windflow.hpp>
#include "../includes/nodes/sink.hpp"
#include "../includes/util/tuple.hpp"
#include "../includes/util/cli_util.hpp"
#include "../includes/nodes/predictor.hpp"
#include "../includes/nodes/light_source.hpp"

using namespace std;
using namespace ff;
using namespace wf;

using count_key_t = pair<KEY_T, uint64_t>;
using key_map_t = unordered_map<string, count_key_t>;

// global variables
key_map_t entity_key_map; // contains a mapping between string keys and integer keys for each entity_id
size_t entity_unique_key = 0; // unique integer key
vector<pair<string, string>> parsed_file; // contains strings extracted from the input file
vector<tuple_t> dataset; // contains all the tuples in memory
atomic<long> sent_tuples; // total number of tuples sent by all the sources

// map_and_parse_dataset function
void map_and_parse_dataset(const string &file_path, const string &split_regex)
{
    ifstream file(file_path);
    if (file.is_open()) {
        string line;
        while (getline(file, line)) {
            string entity_id = line.substr(0, line.find(split_regex));
            string record = line.substr(line.find(split_regex) + 1, line.size());
            // map keys
            if (entity_key_map.find(entity_id) == entity_key_map.end()) { // the key is not present in the hash map
                entity_key_map.insert(make_pair(entity_id, make_pair(entity_unique_key, 0)));
                entity_unique_key++;
            }
            // save parsed file
            parsed_file.insert(parsed_file.end(), make_pair(entity_id, record));
        }
        file.close();
    }
}

// create_tuples function
void create_tuples(int num_keys)
{
    std::uniform_int_distribution<std::mt19937::result_type> dist(0, num_keys-1);
    mt19937 rng;
    rng.seed(0);
    for (int next_tuple_idx = 0; next_tuple_idx < parsed_file.size()/4; next_tuple_idx++) {
        // create tuple
        auto tuple_content = parsed_file.at(next_tuple_idx);
        tuple_t t;
        t.record = tuple_content.second;
        if (num_keys == 0) {
            t.key = (entity_key_map.find(tuple_content.first)->second).first;
        }
        else {
            t.key = dist(rng);
        }
        // t.ts = 0L;
        t.score = 0;
        dataset.insert(dataset.end(), t);
    }
}

// Main
int main(int argc, char* argv[])
{
    /// parse arguments from command line
    int option = 0;
    int index = 0;
    string file_path;
    size_t source_par_deg = 0;
    size_t predictor_par_deg = 0;
    size_t sink_par_deg = 0;
    int rate = 0;
    sent_tuples = 0;
    long sampling = 0;
    bool chaining = false;
    size_t batch_size = 0;
    size_t num_keys = 0;
    if (argc == 11 || argc == 12 || argc == 13 || argc == 14) {
        while ((option = getopt_long(argc, argv, "r:k:s:p:b:c:t:", long_opts, &index)) != -1) {
            file_path = _input_file;
            switch (option) {
                case 'r': {
                    rate = atoi(optarg);
                    break;
                }
                case 's': {
                    sampling = atoi(optarg);
                    break;
                }
                case 'b': {
                    batch_size = atoi(optarg);
                    break;
                }
                case 'k': {
                    num_keys = atoi(optarg);
                    break;
                }
                case 'p': {
                    vector<size_t> par_degs;
                    string pars(optarg);
                    stringstream ss(pars);
                    for (size_t i; ss >> i;) {
                        par_degs.push_back(i);
                        if (ss.peek() == ',')
                            ss.ignore();
                    }
                    if (par_degs.size() != 3) {
                        printf("Error in parsing the input arguments\n");
                        exit(EXIT_FAILURE);
                    }
                    else {
                        source_par_deg = par_degs[0];
                        predictor_par_deg = par_degs[1];
                        sink_par_deg = par_degs[2];
                    }
                    break;
                }
                case 'c': {
                    chaining = true;
                    break;
                }
                case 't': {
                    app_run_time = atoi(optarg) * 1000000000L;
                    break;
                }
                default: {
                    printf("Error in parsing the input arguments\n");
                    exit(EXIT_FAILURE);
                }
            }
        }
    }
    else if (argc == 2) {
        while ((option = getopt_long(argc, argv, "h", long_opts, &index)) != -1) {
            switch (option) {
                case 'h': {
                    printf("Parameters: --rate <value> --keys <value> --sampling <value> --batch <size> --parallelism <nSource,nPredictor,nSink> [--chaining]\n");
                    exit(EXIT_SUCCESS);
                }
            }
        }
    }
    else {
        printf("Error in parsing the input arguments\n");
        exit(EXIT_FAILURE);
    }
    /// data pre-processing
    map_and_parse_dataset(file_path, ",");
    create_tuples(num_keys);
    /// application starting time
    unsigned long app_start_time = current_time_nsecs();
    cout << "Executing FraudDetection with parameters:" << endl;
    if (rate != 0) {
        cout << "  * rate: " << rate << " tuples/second" << endl;
    }
    else {
        cout << "  * rate: full_speed tupes/second" << endl;
    }
    cout << "  * batch size: " << batch_size << endl;
    cout << "  * sampling: " << sampling << endl;
    cout << "  * source: " << source_par_deg << endl;
    cout << "  * predictor: " << predictor_par_deg << endl;
    cout << "  * sink: " << sink_par_deg << endl;
    cout << "  * topology: source -> predictor -> sink" << endl;
    PipeGraph topology(topology_name, Execution_Mode_t::DEFAULT, Time_Policy_t::EVENT_TIME);
    if (!chaining) { // no chaining
        /// create the operators
        Source_Functor source_functor(dataset, rate, app_start_time, batch_size);
        Source source = Source_Builder(source_functor)
                            .withParallelism(source_par_deg)
                            .withName(light_source_name)
                            .withOutputBatchSize(batch_size)
                            .build();
        Predictor_Functor predictor_functor(app_start_time);
        Filter predictor = Filter_Builder(predictor_functor)
                                .withParallelism(predictor_par_deg)
                                .withName(predictor_name)
                                .withKeyBy([](const tuple_t &t) -> KEY_T { return t.key; })
                                .withOutputBatchSize(batch_size)
                                .build();
        Sink_Functor sink_functor(sampling, app_start_time);
        Sink sink = Sink_Builder(sink_functor)
                        .withParallelism(sink_par_deg)
                        .withName(sink_name)
                        .build();
        /// create the application
        MultiPipe &mp = topology.add_source(source);
        cout << "Chaining is disabled" << endl;
        mp.add(predictor);
        mp.add_sink(sink);
    }
    else { // chaining
        /// create the operators
        Source_Functor source_functor(dataset, rate, app_start_time, batch_size);
        Source source = Source_Builder(source_functor)
                            .withParallelism(source_par_deg)
                            .withName(light_source_name)
                            .withOutputBatchSize(batch_size)
                            .build();
        Predictor_Functor predictor_functor(app_start_time);
        Filter predictor = Filter_Builder(predictor_functor)
                                .withParallelism(predictor_par_deg)
                                .withName(predictor_name)
                                .withKeyBy([](const tuple_t &t) -> KEY_T { return t.key; })
                                .build();
        Sink_Functor sink_functor(sampling, app_start_time);
        Sink sink = Sink_Builder(sink_functor)
                        .withParallelism(sink_par_deg)
                        .withName(sink_name)
                        .build();
        /// create the application
        MultiPipe &mp = topology.add_source(source);
        cout << "Chaining is enabled" << endl;
        mp.chain(predictor);
        mp.chain_sink(sink);
    }
    cout << "Executing topology" << endl;
    /// evaluate topology execution time
    volatile unsigned long start_time_main_usecs = current_time_usecs();
    topology.run();
    volatile unsigned long end_time_main_usecs = current_time_usecs();
    cout << "Exiting" << endl;
    double elapsed_time_seconds = (end_time_main_usecs - start_time_main_usecs) / (1000000.0);
    double throughput = sent_tuples / elapsed_time_seconds;
    cout << "Measured throughput: " << (int) throughput << " tuples/second" << endl;
    // cout << "Dumping metrics" << endl;
    // util::metric_group.dump_all();

    bool print_header = false;
    ifstream in_file("results.csv");
    if (in_file.peek() == std::ifstream::traits_type::eof()) {
        print_header = true;
    }
    in_file.close();

    ofstream out_file;
    out_file.open("results.csv", ios_base::app);
    if (print_header) {
        out_file << "Application" << ","
                 << "Source" << ","
                 << "Predictor" << ","
                 << "Sink" << ","
                 << "BatchSize" << ","
                 << "Sampling" << ","
                 << "Runtime (s)" << ","
                 << "Throughput (t/s)" << ","
                 << "Time (s)" << ","
                 << "Samples" << ","
                 << "Total" << ","
                 << "Mean" << ","
                 << "0" << ","
                 << "5" << ","
                 << "25" << ","
                 << "50" << ","
                 << "75" << ","
                 << "95" << ","
                 << "100"
                 << endl;
    }

    auto latency_metric = util::metric_group.get_metric("latency");

    out_file << fixed
             << "FraudDetection" << ","
             << source_par_deg << ","
             << predictor_par_deg << ","
             << sink_par_deg << ","
             << batch_size << ","
             << sampling << ","
             << app_run_time / 1000000000L << ","
             << (size_t)throughput << ","
             << elapsed_time_seconds << ","
             << latency_metric.getN() << ","
             << latency_metric.total() << ","
             << latency_metric.mean() << ","
             << latency_metric.min() << ","
             << latency_metric.percentile(0.05) << ","
             << latency_metric.percentile(0.25) << ","
             << latency_metric.percentile(0.5) << ","
             << latency_metric.percentile(0.75) << ","
             << latency_metric.percentile(0.95) << ","
             << latency_metric.max()
             << endl;

    out_file.close();

    return 0;
}
