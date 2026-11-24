/**
 * @file DummySensor.h
 * @brief Declaration and implementation of the DummySensor class.
 */
//
// Created by Anna Romankiewicz on 12.11.26.
//

#pragma once
#include <string>
#include <random>

/**
 * @brief Simulated sensor that delivers random values.
 *
 * Each reading returns a fixed number of random integers within [min, max].
 * Used by the Environment_SocketServer instead of real hardware.
 */
class DummySensor {
public:
    /**
     * @brief Creates a sensor with a name and a value range.
     * @param _name name of the sensor, used by the clients to request it
     * @param _valueCount number of values delivered per reading
     * @param _min smallest possible value
     * @param _max largest possible value
     */
    DummySensor(const std::string& _name, int _valueCount, int _min, int _max)
        : mName(_name), mValueCount(_valueCount), mDist(_min, _max) {}

    /**
     * @brief Returns the name of the sensor.
     * @return name given in the constructor
     */
    const std::string& GetName() const { return mName; }

    /**
     * @brief Generates a new reading with random values.
     * @return values separated by ';', e.g. "12;400;7" (no trailing separator)
     */
    std::string ReadValues() {
        std::string result;
        for (int i = 0; i < mValueCount; i++) {
            if (i > 0) result += ";";
            result += std::to_string(mDist(Generator()));
        }
        return result;
    }

private:
    std::string mName;  ///< name of the sensor
    int mValueCount;  ///< number of values per reading
    std::uniform_int_distribution<int> mDist;  ///< uniform distribution in the range [min, max]

    /**
     * @brief Returns the random number generator of the calling thread.
     *
     * thread_local: every thread has its own generator, so no locking is needed.
     * @return reference to the thread local Mersenne Twister generator
     */
    static std::mt19937& Generator() {
        static thread_local std::mt19937 gen(std::random_device{}());   // one per thread
        return gen;
    }
};