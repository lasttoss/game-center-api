package io.dy.gamecenter.api.utils;

import java.io.*;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {

    public static String compress(String data) throws IOException {
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(data.getBytes("UTF-8"));
        gzip.close();
        String base64Encoded = Base64.getEncoder().encodeToString(obj.toByteArray());
        return base64Encoded;
    }

    public static String decompress(String compressedData) throws IOException {
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(compressedData)));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        String outStr = "";
        String line;
        while ((line = bf.readLine()) != null) {
            outStr += line;
        }
        return outStr;
    }
}
