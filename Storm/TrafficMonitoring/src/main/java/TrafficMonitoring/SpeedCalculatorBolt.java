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

package TrafficMonitoring;

import Util.Log;
import Constants.TrafficMonitoringConstants.*;
import RoadModel.Road;
import Util.config.Configuration;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *  @author  Alessandra Fais
 *  @version June 2019
 *
 *  This operator uses the roadID result generated by Map-Match operator
 *  to update the average speed record of the corresponding road.
 */
public class SpeedCalculatorBolt extends BaseRichBolt {
    private static final Logger LOG = Log.get(SpeedCalculatorBolt.class);

    protected OutputCollector collector;
    protected Configuration config;
    protected TopologyContext context;

    private Map<Integer, Road> roads;

    private long t_start;
    private long t_end;
    private long processed;
    private int par_deg;

    SpeedCalculatorBolt(int p_deg) {
        par_deg = p_deg;     // bolt parallelism degree
    }

    @Override
    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {

        t_start = System.nanoTime(); // bolt start time in nanoseconds
        processed = 0;               // total number of processed tuples

        config = Configuration.fromMap(stormConf);
        context = topologyContext;
        collector = outputCollector;

        roads = new HashMap<>();
    }

    @Override
    public void execute(Tuple tuple) {
        int roadID = tuple.getIntegerByField(Field.ROAD_ID);
        int speed  = tuple.getIntegerByField(Field.SPEED);
        long timestamp = tuple.getLongByField(Field.TIMESTAMP);

        LOG.debug("[Calculator] tuple: roadID " + roadID +
                  ", speed " + speed +
                  ", ts " + timestamp);

        int averageSpeed = 0;
        int count = 0;

        if (!roads.containsKey(roadID)) {
            Road road = new Road(roadID);
            road.addRoadSpeed(speed);
            road.setCount(1);
            road.setAverageSpeed(speed);

            roads.put(roadID, road);
            averageSpeed = speed;
            count = 1;
        } else {
            Road road = roads.get(roadID);

            int sum = 0;

            if (road.getRoadSpeedSize() < 2) {
                road.incrementCount();
                road.addRoadSpeed(speed);

                for (int it : road.getRoadSpeed()) {
                    sum += it;
                }

                averageSpeed = (int)((double)sum/(double)road.getRoadSpeedSize());
                road.setAverageSpeed(averageSpeed);
                count = road.getRoadSpeedSize();
            } else {
                double avgLast = roads.get(roadID).getAverageSpeed();
                double temp = 0;

                for (int it : road.getRoadSpeed()) {
                    sum += it;
                    temp += Math.pow((it-avgLast), 2);
                }

                int avgCurrent = (int) ((sum + speed)/((double)road.getRoadSpeedSize() + 1));
                temp = (temp + Math.pow((speed - avgLast), 2)) / (road.getRoadSpeedSize());
                double stdDev = Math.sqrt(temp);

                if (Math.abs(speed - avgCurrent) <= (2 * stdDev)) {
                    road.incrementCount();
                    road.addRoadSpeed(speed);
                    road.setAverageSpeed(avgCurrent);

                    averageSpeed = avgCurrent;
                    count = road.getRoadSpeedSize();
                }
            }
        }

        collector.emit(tuple, new Values(roadID, averageSpeed, count, timestamp));
        //collector.ack(tuple);

        processed++;
        t_end = System.nanoTime();
    }

    @Override
    public void cleanup() {
        long t_elapsed = (t_end - t_start) / 1000000; // elapsed time in milliseconds

        /*LOG.info("[Calculator] execution time: " + t_elapsed +
                " ms, processed: " + processed +
                ", bandwidth: " + processed / (t_elapsed / 1000) +  // tuples per second
                " tuples/s");*/
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Field.ROAD_ID, Field.AVG_SPEED, Field.COUNT, Field.TIMESTAMP));
    }

}
