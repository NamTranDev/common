package core.data.serializer;

import android.util.Pair;

import com.activeandroid.serializer.TypeSerializer;

import org.json.JSONArray;

final public class StringPairSerializer extends TypeSerializer {
    @Override
    public Class<?> getDeserializedType() {
        return Pair[].class;
    }

    @Override
    public Class<?> getSerializedType() {
        return String.class;
    }

    @Override
    public String serialize(Object data) {
        if (data == null) {
            return null;
        }
        return toString((Pair<String, String>[]) data);
    }

    @Override
    public Pair<String, String>[] deserialize(Object data) {
        if (data == null) {
            return null;
        }
        return toPair((String) data);
    }

    private Pair<String, String>[] toPair(String value) {
        String[] values = value.split("|");
        Pair<String, String>[] result = new Pair[values.length];
        for (int i = 0; i < values.length; i++) {
            String pair = values[i];
            result[i] = new Pair<>(pair.split("|")[0], pair.split("|")[1]);
        }
        return result;
    }

    private String toString(Pair<String, String>[] values) {
        JSONArray result = new JSONArray();
        for (int i = 0; i < values.length; i++) {
            String pair = values[i].first + "|" + values[i].second;
            result.put(pair);
        }
        return result.toString();
    }
}
