/** 
 *  @file    event.hpp
 *  @author  Gabriele Mencagli
 *  @date    14/08/2019
 *  
 *  @brief Structure of a input tuple
 *  
 *  This file defines the structure of the tuples generated by the source.
 *  The data type event_t must be default constructible, with a copy constructor
 *  and copy assignment operator, and it must provide and implement the setInfo() and
 *  getInfo() methods.
 */

#ifndef YSB_EVENT_HPP
#define YSB_EVENT_HPP

#include <windflow.hpp>

using namespace std;

// event_t struct
struct event_t
{
    unsigned long user_id; // user id
    unsigned long page_id; // page id
    unsigned long ad_id; // advertisement id
    unsigned int ad_type; // advertisement type (0, 1, 2, 3, 4) => ("banner", "modal", "sponsored-search", "mail", "mobile")
    unsigned int event_type; // event type (0, 1, 2) => ("view", "click", "purchase")
    unsigned int ip; // ip address
    uint64_t ts;
    //char padding[28]; // padding
};

#endif //YSB_EVENT_HPP
