package ru.ponyhawks.android.utils;

import com.cab404.moonlight.framework.AccessProfile;
import com.cab404.moonlight.framework.ShortRequest;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BasicHttpEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Simple imgur uploader
 * Created at 23:42 on 22/09/15
 *
 * @author cab404
 */
public final class Imgur {

    private Imgur() {
    }

    public static class Gateway extends AccessProfile {
        private String token;

        public Gateway(String token) {
            host = new HttpHost("api.imgur.com", 443, "https");
            this.token = token;
        }

        @Override
        public HttpResponse exec(HttpRequestBase request, boolean follow, int timeout) {
            request.addHeader("Authorization", "Client-ID " + token);
            return super.exec(request, follow, timeout);
        }
    }

    public static class Upload extends ShortRequest {

        private JSONObject response;
        private InputStream stream;
        int length = -1;

        public Upload(InputStream stream) {
            this.stream = stream;
        }

        public Upload(InputStream stream, int length) {
            this.stream = stream;
            this.length = length;
        }

        public Upload(File file) throws FileNotFoundException {
            this.stream = new FileInputStream(file);
            this.length = (int) file.length();
        }

        byte[] readStream(InputStream stream) throws IOException {
            int blockSize = 1024 * 1024;
            int read_count = 0;
            byte[] block = new byte[blockSize];

            while ((read_count += stream.read(block, read_count, block.length - read_count)) >= 0)
                if (read_count == block.length)
                    block = Arrays.copyOf(block, block.length + blockSize);

            return block;
        }


        @Override
        protected HttpRequestBase getRequest(AccessProfile accessProfile) {
            HttpPost post = new HttpPost("/3/image");
            BasicHttpEntity entity = new BasicHttpEntity();
            byte[] bytes;
            try {
                bytes = readStream(stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            entity.setContent(new ByteArrayInputStream(bytes));
            entity.setContentLength(bytes.length);
            post.setEntity(entity);

            return post;
        }

        @Override
        protected void handleResponse(String response) {
            try {
                this.response = new JSONObject(response);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        public JSONObject getResponse() {
            return response;
        }
    }

}

