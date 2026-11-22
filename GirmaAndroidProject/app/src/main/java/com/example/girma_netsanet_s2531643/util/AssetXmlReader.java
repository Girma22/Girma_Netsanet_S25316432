
package com.example.girma_netsanet_s2531643.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class AssetXmlReader {

    public static String readXml(
            Context context,
            String fileName) throws IOException {

        StringBuilder xml = new StringBuilder();

        try (
                InputStream input =
                        context.getAssets().open(fileName);

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        input,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while ((line = reader.readLine()) != null) {
                xml.append(line).append('\n');
            }
        }

        return xml.toString();
    }
}
