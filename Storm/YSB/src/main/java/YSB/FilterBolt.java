/**************************************************************************************
 *  Copyright (c) 2019- Gabriele Mencagli
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


package YSB;

import Util.Log;
import java.util.*;
import org.slf4j.Logger;
import Constants.YSBConstants;
import org.slf4j.LoggerFactory;
import Constants.YSBConstants.*;
import Util.config.Configuration;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;

// class FilterBolt
public class FilterBolt extends BaseRichBolt {
    private static final Logger LOG = Log.get(FilterBolt.class);
    protected OutputCollector collector;
    protected Configuration config;
    protected TopologyContext context;
    private long t_start;
    private long t_end;
    private long processed;
    private int par_deg;

    // constructor
    FilterBolt(int p_deg) {
        par_deg = p_deg;     // bolt parallelism degree
    }

    // prepare method
    @Override
    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        t_start = System.nanoTime(); // bolt start time in nanoseconds
        processed = 0;               // total number of processed tuples
        config = Configuration.fromMap(stormConf);
        context = topologyContext;
        collector = outputCollector;
    }

    // execute method
    @Override
    public void execute(Tuple tuple) {
        String uuid = tuple.getStringByField(Field.UUID);
        String uuid2 = tuple.getStringByField(Field.UUID2);
        String ad_id = tuple.getStringByField(Field.AD_ID);
        String ad_type = tuple.getStringByField(Field.AD_TYPE);
        String event_type = tuple.getStringByField(Field.EVENT_TYPE);
        long ts = tuple.getLongByField(Field.TIMESTAMP);
        String ip = tuple.getStringByField(Field.IP);
        if (event_type.equals("view")) {
            collector.emit(new Values(uuid, uuid2, ad_id, ad_type, event_type, ts, ip));
        }
        //collector.ack(tuple);
        processed++;
        t_end = System.nanoTime();
    }

    // cleanup method
    @Override
    public void cleanup() {
        long t_elapsed = (t_end - t_start) / 1000000; // elapsed time in milliseconds
        /*LOG.info("[Filter] execution time: " + t_elapsed +
                " ms, processed: " + processed +
                ", bandwidth: " + processed / (t_elapsed / 1000) +  // tuples per second
                " tuples/s");*/
    }

    // declareOutputFields
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.UUID, Field.UUID2, Field.AD_ID, Field.AD_TYPE, Field.EVENT_TYPE, Field.TIMESTAMP, Field.IP));
    }
}
