/**************************************************************************************
 *  Copyright (c) 2025- Gabriele Mencagli and Alberto Ottimo
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

package ClusterMonitoring;

import Util.Log;
import Util.MetricGroup;
import org.slf4j.Logger;
import java.io.IOException;
import java.time.Duration;

import Util.ThroughputCounter;
import org.slf4j.LoggerFactory;
import Constants.BaseConstants;
import java.util.concurrent.TimeUnit;

import org.apache.flink.api.java.functions.KeySelector;
import javax.xml.crypto.dsig.spec.XPathType.Filter;

import Constants.ClusterMonitoringConstants;
import Constants.ClusterMonitoringConstants.Conf;
import ClusterMonitoring.CMRecord;
import ClusterMonitoring.FileParserSource;
import Constants.ClusterMonitoringConstants.Component;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.io.File;
import java.io.FileWriter;
import Util.Metric;

/**
 *  @author  Alberto Ottimo
 *  @version June 2025
 *
 *  The topology entry class.
 */
public class ClusterMonitoring {
    private static final Logger LOG = Log.get(ClusterMonitoring.class);

    // main method
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals(BaseConstants.HELP)) {
            String alert = "Parameters: --rate <value> --sampling <value> --parallelism <nSource nFilter nWindow nSink> [--chaining]\n";
            LOG.error(alert);
        }
        else {
            // load configuration
            ParameterTool params;
            Configuration conf;
            try {
                params = ParameterTool.fromPropertiesFile(ClusterMonitoring.class.getResourceAsStream(ClusterMonitoringConstants.DEFAULT_PROPERTIES));
                conf = params.getConfiguration();
                LOG.debug("Loaded configuration file");
            }
            catch (IOException e) {
                LOG.error("Unable to load configuration file", e);
                throw new RuntimeException("Unable to load configuration file", e);
            }
            // parse command line arguments
            boolean isCorrect = true;
            int gen_rate = -1;
            int sampling = 1;
            int source_par_deg = 1;
            int filter_par_deg = 1;
            int window_par_deg = 1;
            int sink_par_deg = 1;
            boolean isChaining = false;
            if (args.length == 9 || args.length == 10) {
                if (!args[0].equals("--rate")) {
                    isCorrect = false;
                }
                else {
                    try {
                        gen_rate = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException e) {
                        isCorrect = false;
                    }
                }
                if (!args[2].equals("--sampling"))
                    isCorrect = false;
                else {
                    try {
                        sampling = Integer.parseInt(args[3]);
                    }
                    catch (NumberFormatException e) {
                        isCorrect = false;
                    }
                }
                if (!args[4].equals("--parallelism"))
                    isCorrect = false;
                else {
                    try {
                        source_par_deg = Integer.parseInt(args[5]);
                        filter_par_deg = Integer.parseInt(args[6]);
                        window_par_deg = Integer.parseInt(args[7]);
                        sink_par_deg = Integer.parseInt(args[8]);
                    }
                    catch (NumberFormatException e) {
                        isCorrect = false;
                    }
                }
                if (args.length == 10) {
                    if (!args[9].equals("--chaining")) {
                        isCorrect = false;
                    }
                    else {
                        isChaining = true;
                    }
                }
            }
            else {
                LOG.error("Error in parsing the input arguments");
                System.exit(1);
            }
            if (!isCorrect) {
               LOG.error("Error in parsing the input arguments");
               System.exit(1);
            }
            String file_path = conf.getString(Conf.SPOUT_PATH, "undefined");
            String topology_name = ClusterMonitoringConstants.DEFAULT_TOPO_NAME;
            long runTimeSec = conf.getLong(Conf.RUNTIME, 0L);

            // create the execution environment
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

            // flush as soon as possible in throttled mode (minimize the latency)
            if (gen_rate != 0) {
                env.setBufferTimeout(0);
            }

            // create the topology
            DataStream<CMRecord> source_stream = env.addSource(new FileParserSource(file_path, gen_rate, source_par_deg, conf, runTimeSec)).setParallelism(source_par_deg);

            DataStream<CMRecord> filter_stream = source_stream.filter(new FilterFunction<CMRecord>() {
                    @Override
                    public boolean filter(CMRecord cmRecord) throws Exception {
                        return cmRecord.eventType == 3;
                    }
            }).setParallelism(filter_par_deg);

            DataStream<CMResult> window_stream = filter_stream.keyBy(
                new KeySelector<CMRecord, Integer>() {
                    @Override
                    public Integer getKey(CMRecord cmRecord) throws Exception {
                        return cmRecord.jobId;
                    }
                }
            ).window(SlidingProcessingTimeWindows.of(Time.seconds(60), Time.seconds(1)))
                .aggregate(new AggregateFunction<CMRecord, CMResult, CMResult>() {
                    @Override
                    public CMResult createAccumulator() {
                        return new CMResult(0, 0);
                    }

                    @Override
                    public CMResult add(CMRecord input, CMResult partial) {
                        partial.cpu += input.cpu;

                        return partial;
                    }

                    @Override
                    public CMResult getResult(CMResult result) {
                        return result;
                    }

                    @Override
                    public CMResult merge(CMResult left, CMResult right) {
                        left.cpu += right.cpu;
                        if (left.ts > right.ts) {
                            left.ts = right.ts;
                        }
                        return left;
                    }
            }).setParallelism(window_par_deg);

            window_stream.addSink(new ConsoleSink(sink_par_deg, gen_rate, sampling)).setParallelism(sink_par_deg);

            // print app info
            LOG.info("Executing ClusterMonitoring with parameters:\n" +
                     "  * rate: " + ((gen_rate == 0) ? "full_speed" : gen_rate) + " tuples/second\n" +
                     "  * sampling: " + sampling + "\n" +
                     "  * source: " + source_par_deg + "\n" +
                     "  * moving-average: " + filter_par_deg + "\n" +
                     "  * spike-detector: " + window_par_deg + "\n" +
                     "  * sink: " + sink_par_deg + "\n" +
                     "  * topology: source -> moving-average -> spike-detector -> sink");

            // run the topology
            try {
                // configure the environment
                if (!isChaining) {
                    env.disableOperatorChaining();
                    LOG.info("Chaining is disabled");
                }
                else {
                    LOG.info("Chaining is enabled");
                }
                // run the topology
                LOG.info("Submitting topology");
                JobExecutionResult result = env.execute();
                LOG.info("Exiting");
                // measure throughput
                double throughput = (double) (ThroughputCounter.getValue() / result.getNetRuntime(TimeUnit.SECONDS));
                LOG.info("Measured throughput: " + throughput + " tuples/second");
                // dump the metrics
                // LOG.info("Dumping metrics");
                // MetricGroup.dumpAll();

                boolean print_header = false;
                String csvFile = "results.csv";
                File file = new File(csvFile);
                if (!file.exists()) {
                    file.createNewFile();
                    print_header = true;
                }

                FileWriter writer = new FileWriter(file, true);
                if (print_header) {
                    writer.write("Application" + "," +
                                 "Source" + "," +
                                 "Average" + "," +
                                 "Detector" + "," +
                                 "Sink" + "," +
                                 "BatchSize" + "," +
                                 "Sampling" + "," +
                                 "Runtime (s)" + "," +
                                 "Throughput (t/s)" + "," +
                                 "Time (s)" + "," +
                                 "Samples" + "," +
                                 "Total" + "," +
                                 "Mean" + "," +
                                 "0" + "," +
                                 "5" + "," +
                                 "25" + "," +
                                 "50" + "," +
                                 "75" + "," +
                                 "95" + "," +
                                 "100"
                                 + "\n");
                }

                Metric latencyMetric = MetricGroup.getMetric("latency");

                // write results into the file
                writer.write(topology_name + "," +
                             source_par_deg + "," +
                             filter_par_deg + "," +
                             window_par_deg + "," +
                             sink_par_deg + "," +
                             0 + "," +
                             sampling + "," +
                             60 + "," +
                             throughput + "," +
                             result.getNetRuntime(TimeUnit.SECONDS) + "," +
                             latencyMetric.getN() + "," +
                             latencyMetric.getTotal() + "," +
                             latencyMetric.getMean() + "," +
                             latencyMetric.getMin() + "," +
                             latencyMetric.getPercentile(5) + "," +
                             latencyMetric.getPercentile(25) + "," +
                             latencyMetric.getPercentile(50) + "," +
                             latencyMetric.getPercentile(75) + "," +
                             latencyMetric.getPercentile(95) + "," +
                             latencyMetric.getMax()
                             + "\n");
                writer.flush();
                writer.close();
            }
            catch (Exception e) {
                LOG.error(e.toString());
            }
        }
    }
}
