//
// Created by Anna Romankiewicz on 12.11.26.
//

#ifndef VIS_2026_DUMMYSENSOR_H
#define VIS_2026_DUMMYSENSOR_H

#endif //VIS_2026_DUMMYSENSOR_H


#pragma once
#include <string>
#include <random>

class DummySensor {
public:
    DummySensor(const std::string& _name, int _valueCount, int _min, int _max)
        : mName(_name), mValueCount(_valueCount), mDist(_min, _max) {}

    const std::string& GetName() const { return mName; }

    std::string ReadValues() {
        std::string result;
        for (int i = 0; i < mValueCount; i++) {
            if (i > 0) result += ";";
            result += std::to_string(mDist(Generator()));
        }
        return result;
    }

private:
    std::string mName;
    int mValueCount;
    std::uniform_int_distribution<int> mDist;

    static std::mt19937& Generator() {
        static thread_local std::mt19937 gen(std::random_device{}());   // one per thread
        return gen;
    }
};