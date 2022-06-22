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

#include "count_tuple.hpp"
#include <ostream>

namespace linear_road {

CountTuple::CountTuple()
    : minute_number(-1)
    , xway(-1)
    , segment(-1)
    , direction(-1)
    , count(-1)
{}

bool CountTuple::is_progress_tuple() const
{
    return xway == -1;
}

std::ostream &operator <<(std::ostream &os, const CountTuple &tuple)
{
    return os << " minute_number=" << tuple.minute_number
              << " xway=" << tuple.xway
              << " segment=" << tuple.segment
              << " direction=" << tuple.direction
              << " count=" << tuple.count
              << " ...";
}

}
