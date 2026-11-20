package at.fhooe.sail.vis.general;

public class EnvData {
    private String mSensorName;

    private long mTimestamp;

    private int[] mValues;

    // extend functionality of the class
// (constructor, toString)


    public EnvData(long mTimestamp, String mSensorName, int[] mValues) {
        this.mSensorName = mSensorName;
        this.mTimestamp = mTimestamp;
        this.mValues = mValues;
    }

    public String toString() {
        StringBuilder s = new StringBuilder("TIMESTAMP:" + mTimestamp + " | " + this.mSensorName + " | ");
        boolean first = true;
        for (int val : mValues) {
            if (first) {
                s.append(" Sensorvalues: ");
            } else {
                s.append(" | ");
            }
            s.append(val);
        }

        return s.toString();
    }


    public String getmSensorName() {
        return mSensorName;
    }

    public void setmSensorName(String mSensorName) {
        this.mSensorName = mSensorName;
    }

    public long getmTimestamp() {
        return mTimestamp;
    }

    public void setmTimestamp(long mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public int[] getmValues() {
        return mValues;
    }

    public void setmValues(int[] mValues) {
        this.mValues = mValues;
    }
}
