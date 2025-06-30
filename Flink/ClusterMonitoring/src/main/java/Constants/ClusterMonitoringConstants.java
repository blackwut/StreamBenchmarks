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

package Constants;

/**
 *  @author  Alberto Ottimo
 *  @version June 2025
 *
 *  Constants peculiar of the ClusterMonitoring application.
 */
public interface ClusterMonitoringConstants extends BaseConstants {
    String DEFAULT_PROPERTIES = "/clustermonitoring/cm.properties";
    String DEFAULT_TOPO_NAME = "ClusterMonitoring";
    Short DEFAULT_TARGET_FIELD = 3;

    interface Conf {
        String RUNTIME = "cm.runtime_sec";
        String SPOUT_PATH = "cm.spout.path";
        String FILTER_TARGET_FIELD = "cm.filter.targetField";
        String WINDOW_SIZE = "cm.window.size";
        String WINDOW_SLIDE = "cm.window.slide";
    }

    interface Component extends BaseComponent {
        String FILTER = "filter";
        String WINDOW = "window";
    }

    interface Field extends BaseField {
        String DEVICE_ID = "deviceID";
        String VALUE = "value";
        String MOVING_AVG = "movingAverage";
    }

    interface DatasetParsing {
        int CREATION_TS = 0;
        int JOB_ID = 1;
        int TASK_ID = 2;
        int MACHINE_ID = 3;
        int EVENT_TYPE = 4;
        int CATEGORY = 5;
        int PRIORITY = 6;
        int CPU = 7;
        int RAM = 8;
        int DISK = 9;
        int CONSTRAINTS = 10;
    }
}
