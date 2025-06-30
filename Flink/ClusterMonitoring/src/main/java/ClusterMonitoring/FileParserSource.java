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
import Util.Sampler;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import Util.MetricGroup;
import org.slf4j.Logger;
import java.util.Scanner;
import java.io.IOException;
import java.util.ArrayList;
import Util.ThroughputCounter;
import org.slf4j.LoggerFactory;
import java.io.FileNotFoundException;
import static Constants.BaseConstants.*;
import com.google.common.collect.ImmutableMap;
import Constants.ClusterMonitoringConstants.Conf;
import Constants.ClusterMonitoringConstants.Field;
import ClusterMonitoring.CMRecord;

import org.apache.flink.configuration.Configuration;
import Constants.ClusterMonitoringConstants.DatasetParsing;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

/**
 *  @author  Alberto Ottimo
 *  @version June 2025
 *
 *  The spout is in charge of reading the input data file containing
 *  measurements from a set of sensor devices, parsing it
 *  and generating the stream of records toward the MovingAverageCalculator.
 *
 *  Format of the input file:
 *  <date:yyyy-mm-dd, time:hh:mm:ss.xxx, epoch:int, deviceID:int, temperature:real, humidity:real, light:real, voltage:real>
 *
 *  Data example can be found here: http://db.csail.mit.edu/labdata/labdata.html
 */
public class FileParserSource extends RichParallelSourceFunction<CMRecord> {
    private static final Logger LOG = Log.get(FileParserSource.class);
    private String file_path;
    private Integer rate;
    private long generated;
    private long nt_execution;
    private int par_deg;
    private int index;
    private long runTimeSec;
    private Sampler throughput;
    private boolean isRunning;
    // state of the spout (contains parsed data)
    private Map<Long, Integer> mapJobId;
    private Integer  nextJobId;

    private ArrayList<Long> jobId;
    private ArrayList<Integer> eventType;
    private ArrayList<Float> cpu;
    private Configuration config;

    // Constructor
    public FileParserSource(String file, int gen_rate, int p_deg, Configuration _config, long _runTimeSec) {
        file_path = file;
        rate = gen_rate;        // number of tuples per second
        par_deg = p_deg;        // spout parallelism degree
        generated = 0;          // total number of generated tuples
        nt_execution = 0;       // number of executions of nextTuple() method
        index = 0;
        runTimeSec = _runTimeSec;
        isRunning = true;
        mapJobId = new HashMap<>(1 << 20);
        nextJobId = 0;
        jobId = new ArrayList<>();
        eventType = new ArrayList<>();
        cpu = new ArrayList<>();
        config = _config;
    }

    // open method
    @Override
    public void open(Configuration parameters) throws IOException {
        throughput = new Sampler();
        parseDataset();
    }

    // run method
    @Override
    public void run(SourceContext<CMRecord> ctx) throws Exception {
        long epoch = System.nanoTime();
        // generation loop
        while ((System.nanoTime() - epoch < runTimeSec * 1e9) && isRunning) {
            // send tuple
            long timestamp = System.nanoTime();
            long _jobId = jobId.get(index);

            Integer _newJobId = mapJobId.get(_jobId);
            if (_newJobId == null) {
                _newJobId = nextJobId++;
                mapJobId.put(_jobId, _newJobId);
            }

            ctx.collect(new CMRecord(
                _newJobId,
                eventType.get(index),
                cpu.get(index),
                timestamp));
            generated++;
            index++;
            if (rate != 0) { // not full speed
                long delay_nsec = (long) ((1.0d / rate) * 1e9);
                active_delay(delay_nsec);
            }
            // check the dataset boundaries
            if (index >= jobId.size()) {
                index = 0;
                nt_execution++;
            }
        }
        // terminate the generation
        isRunning = false;
        // dump metric
        double rate = generated / ((System.nanoTime() - epoch) / 1e9); // per second
        long t_elapsed = (long) ((System.nanoTime() - epoch) / 1e6);  // elapsed time in milliseconds
        /*LOG.info("[Source] execution time: " + t_elapsed +
                 " ms, generations: " + nt_execution +
                 ", generated: " + generated +
                 ", bandwidth: " + rate +  // tuples per second
                 " tuples/s");*/
        //throughput.add(rate);
        //MetricGroup.add("throughput", throughput);
        ThroughputCounter.add(generated);
    }

    // parseDataset method
    private void parseDataset() {
        try {
            Scanner scan = new Scanner(new File(file_path));
            while (scan.hasNextLine()) {
                String[] fields = scan.nextLine().split("\\s+"); // regex quantifier (matches one or many whitespaces)
                if (fields.length >= 8) {
                    jobId.add(Long.valueOf(fields[DatasetParsing.JOB_ID]));
                    eventType.add(Integer.valueOf(fields[DatasetParsing.EVENT_TYPE]));
                    cpu.add(Float.valueOf(fields[DatasetParsing.CPU]));
                }
                else
                    LOG.debug("[Source] incomplete record");
            }
            scan.close();
        }
        catch (FileNotFoundException | NullPointerException e) {
            LOG.error("The file {} does not exists", file_path);
            throw new RuntimeException("The file '" + file_path + "' does not exists");
        }
    }

    /**
     * Add some active delay (busy-waiting function).
     * @param nsecs wait time in nanoseconds
     */
    private void active_delay(double nsecs) {
        long t_start = System.nanoTime();
        long t_now;
        boolean end = false;
        while (!end) {
            t_now = System.nanoTime();
            end = (t_now - t_start) >= nsecs;
        }
    }

    @Override
    public void cancel() {
        //LOG.info("cancel");
        isRunning = false;
    }

    @Override
    public void close() {
        //LOG.info("close");
    }
}
