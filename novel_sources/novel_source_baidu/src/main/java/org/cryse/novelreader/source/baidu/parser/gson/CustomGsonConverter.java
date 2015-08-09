package org.cryse.novelreader.source.baidu.parser.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Created by Cryse Hillmess(YINKUN TAO) on 14-2-2.
 * Email: tyk5555@hotmail.com
 */
public class CustomGsonConverter implements Converter {
    private final Gson gson;
    private String encoding;
    /**
     * Create an instance using the supplied {@link com.google.gson.Gson} object for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use UTF-8.
     */
    public CustomGsonConverter(Gson gson) {
        this(gson, "UTF-8");
    }

    /**
     * Create an instance using the supplied {@link com.google.gson.Gson} object for conversion. Encoding to JSON and
     * decoding from JSON (when no charset is specified by a header) will use the specified encoding.
     */
    public CustomGsonConverter(Gson gson, String encoding) {
        this.gson = gson;
        this.encoding = encoding;
    }

    @Override public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            String responseBodyString = MiniIOUtils.toString(body.in());
            if(responseBodyString.contains("<!DOCTYPE html>"))
                return responseBodyString;
            JsonParser parser = new JsonParser();
            JsonObject element = (JsonObject)parser.parse(responseBodyString);
            JsonObject novelDataObject = element.getAsJsonObject("data").getAsJsonObject("novel");
            Iterator<Map.Entry<String, JsonElement>> iter = novelDataObject.entrySet().iterator();
            JsonElement dataSetObject = null;
            if(iter.hasNext()) {
                Map.Entry<String, JsonElement> entry = iter.next();
                JsonObject object = (JsonObject) entry.getValue();
                dataSetObject = object.get("dataset");
            } else {
                dataSetObject = element;
            }
            // String filteredString = jsonPreprocessor.process(responseBodyString);
            return gson.fromJson(dataSetObject, type);
        } catch (JsonParseException | IOException e) {
            throw new ConversionException(e);
        } finally {
        }
    }

    @Override public TypedOutput toBody(Object object) {
        try {
            return new JsonTypedOutput(gson.toJson(object).getBytes(encoding), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    private static class JsonTypedOutput implements TypedOutput {
        private final byte[] jsonBytes;
        private final String mimeType;

        JsonTypedOutput(byte[] jsonBytes, String encode) {
            this.jsonBytes = jsonBytes;
            this.mimeType = "application/json; charset=" + encode;
        }

        @Override public String fileName() {
            return null;
        }

        @Override public String mimeType() {
            return mimeType;
        }

        @Override public long length() {
            return jsonBytes.length;
        }

        @Override public void writeTo(OutputStream out) throws IOException {
            out.write(jsonBytes);
        }
    }

    private static class MiniIOUtils {
        private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
        public static String toString(InputStream input) throws IOException {
            StringWriter sw = new StringWriter();
            copy(input, sw);
            return sw.toString();
        }

        public static void copy(InputStream input, Writer output)
                throws IOException {
            InputStreamReader in = new InputStreamReader(input);
            copy(in, output);
        }

        public static int copy(Reader input, Writer output) throws IOException {
            long count = copyLarge(input, output);
            if (count > Integer.MAX_VALUE) {
                return -1;
            }
            return (int) count;
        }

        public static long copyLarge(Reader input, Writer output) throws IOException {
            char[] buffer = new char[DEFAULT_BUFFER_SIZE];
            long count = 0;
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;
            }
            return count;
        }
    }

}