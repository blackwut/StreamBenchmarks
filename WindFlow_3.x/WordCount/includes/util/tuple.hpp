/**
 *  @file    tuple.hpp
 *  @author  Alessandra Fais
 *  @date    07/06/2019
 *
 *  @brief Structure of a input tuple
 *
 *  This file defines the structure of the tuples generated by the source.
 *  The data type tuple_t must be default constructible, with a copy constructor
 *  and copy assignment operator, and it must provide and implement the setInfo() and
 *  getInfo() methods.
 */

#ifndef WORDCOUNT_TUPLE_HPP
#define WORDCOUNT_TUPLE_HPP

#include <windflow.hpp>

using namespace std;

struct tuple_t {
    string text_line;       // line of the parsed dataset (text, book, ...)
    size_t key;             // line number
    uint64_t ts;

    // default constructor
    tuple_t() : text_line(""), key(0) {}

    // constructor
    tuple_t(string &_line, size_t _key) :
        text_line(_line), key(_key) {}
};

#endif //WORDCOUNT_TUPLE_HPP
