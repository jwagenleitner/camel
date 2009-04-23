/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.IOHelper;

public class GzipDataFormat implements DataFormat {

    public void marshal(Exchange exchange, Object graph, OutputStream stream)
        throws Exception {
        InputStream is = exchange.getContext().getTypeConverter().convertTo(InputStream.class, graph);
        if (is == null) {
            throw new IllegalArgumentException("Cannot get the inputstream for GzipDataFormat mashalling");
        }

        GZIPOutputStream zipOutput = new GZIPOutputStream(stream);
        try {
            IOHelper.copy(is, zipOutput);
        } finally {
            zipOutput.close();
        }
        
    }

    public Object unmarshal(Exchange exchange, InputStream stream)
        throws Exception {
        InputStream is = ExchangeHelper.getMandatoryInBody(exchange, InputStream.class);
        GZIPInputStream unzipInput = new GZIPInputStream(is);
        
        // Create an expandable byte array to hold the inflated data
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOHelper.copy(unzipInput, bos);
        return bos.toByteArray();
    }

}
