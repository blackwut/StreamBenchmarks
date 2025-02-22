/**************************************************************************************
 *  Copyright (c) 2019- Gabriele Mencagli and Andrea Cardaci
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

#pragma once

#include<linear_road/dispatcher.hpp>
#include<linear_road/notification_tuple.hpp>
#include<linear_road/segment_identifier.hpp>
#include<unordered_map>
#include<unordered_set>
#include<windflow.hpp>

namespace linear_road {

class TollNotificationPos
{
public:

    void operator ()(const Dispatcher::Tuple &tuple, wf::Shipper<NotificationTuple> &shipper, wf::RuntimeContext &rc);

private:

    void check_minute(short minute);

private:

    std::unordered_map<int, short> all_cars_;
    std::unordered_map<int, TollNotification> last_toll_notification_;
    SegmentIdentifier segment_to_check_;
    std::unordered_set<SegmentIdentifier> current_minute_accidents_;
    std::unordered_set<SegmentIdentifier> previous_minute_accidents_;
    std::unordered_map<SegmentIdentifier, int> current_minute_counts_;
    std::unordered_map<SegmentIdentifier, int> previous_minute_counts_;
    std::unordered_map<SegmentIdentifier, int> current_minute_lavs_;
    std::unordered_map<SegmentIdentifier, int> previous_minute_lavs_;
    int current_minute_ = -1;
};

}
