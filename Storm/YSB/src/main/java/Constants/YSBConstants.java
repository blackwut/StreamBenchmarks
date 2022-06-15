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


package Constants;

// class YSBConstants
public interface YSBConstants extends BaseConstants {
    String DEFAULT_PROPERTIES = "/YSB/YSB.properties";
    String DEFAULT_TOPO_NAME = "YSB";

    interface Conf {
        String RUNTIME = "yb.runtime_sec";
        String BUFFER_SIZE = "yb.buffer_size";
        String POLLING_TIME = "yb.polling_time_ms";
        String NUM_KEYS = "yb.numKeys";
    }

    interface Component extends BaseComponent {
        String FILTER = "filter";
        String JOINER = "joiner";
        String WINAGG = "winAggregate";
    }

    interface Field extends BaseField {
        String UUID = "uuid";
        String UUID2 = "uuid2";
        String AD_ID = "ad_id";
        String AD_TYPE = "ad_type";
        String EVENT_TYPE = "event_type";
        String TIMESTAMP = "timestamp";
        String IP = "ip";
        String CMP_ID = "cmp_id";
        String COUNTER = "counter";
    }
}
