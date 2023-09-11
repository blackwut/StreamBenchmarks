# Author: Gabriele Mencagli
# Date: 11/01/2020

UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S), Darwin)
FF_ROOT         = $(HOME)/projects/fastflow
WF_INCLUDES     = $(HOME)/projects/WindFlow/wf
RAPIDJSON_INCS  = -I /opt/homebrew/Cellar/rapidjson/1.1.0/include
else
FF_ROOT         = $(HOME)/fastflow
WF_INCLUDES     = $(HOME)/WindFlow/wf
RAPIDJSON_INCS  =
endif

INCLUDE_DIR     = ../includes
OUT_DIR         = ../bin

CXX             = g++
CXXFLAGS        = -std=c++17
INCLUDES        = -I $(FF_ROOT) -I $(WF_INCLUDES) -I $(INCLUDE_DIR) $(RAPIDJSON_INCS)
MACRO           = -DFF_BOUNDED_BUFFER -DDEFAULT_BUFFER_CAPACITY=32786 -DNDEBUG
OPTFLAGS        = -g -O3 -finline-functions
LDFLAGS         = -pthread

all: fd

metric.o: ../includes/util/metric.cpp
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(MACRO) $(OPTFLAGS) $< -c

metric_group.o: ../includes/util/metric_group.cpp
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(MACRO) $(OPTFLAGS) $< -c

sampler.o: ../includes/util/sampler.cpp
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(MACRO) $(OPTFLAGS) $< -c

fd.o: fd.cpp
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(MACRO) $(OPTFLAGS) $< -c

fd: metric.o metric_group.o sampler.o fd.o
	$(CXX) metric.o metric_group.o sampler.o fd.o -o $(OUT_DIR)/fd $(LDFLAGS)

clean:
	rm -f $(OUT_DIR)/fd
	rm -f *.o

.DEFAULT_GOAL := all
.PHONY: all clean
