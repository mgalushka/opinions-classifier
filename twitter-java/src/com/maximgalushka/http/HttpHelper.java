package com.maximgalushka.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>Utility which executes HTTP requests
 * and delegate retrieved content processing to some callback handler</p>
 *
 * @author Maxim Galushka
 * @since 07/09/2011
 */
public class HttpHelper <T>{

    private static final Logger log = Logger.getLogger(HttpHelper.class);

    private HttpClient httpClient;

    public HttpHelper(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void download(String url, File to)
            throws Exception {

        log.trace(String.format("Prepare to download file: [%s]", url));
        HttpGet get = new HttpGet(url);
        HttpResponse response = httpClient.execute(get);
        HttpEntity entity = response.getEntity();

        log.trace(
          String.format(
            "Downloading file: [%s] to: [%s]",
            url,
            to.getPath()
          ));

        //Create file
        OutputStream os = new FileOutputStream(to);
        InputStream is = entity.getContent();
        byte[] buf = new byte[4096];
        int read;
        while ((read = is.read(buf)) != -1) {
            os.write(buf, 0, read);
        }
        os.close();
    }
}
