package com.mc.onmusic_relase.utils;

import android.util.Log;

import com.mc.onmusic_relase.AppInterface;
import com.naveed.ytextractor.utils.RegexUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class SoundCloud {
    private String link;
    private String viewCount;
    public class SoundCloudModel {
        private String Title, AuthorName, ImageUrl, StreamUrl, normalUrl;
        public SoundCloudModel(String title, String authorName, String imageUrl, String streamUrl,String normalUrl) {
            Title = title;
            AuthorName = authorName;
            ImageUrl = imageUrl;
            StreamUrl = streamUrl;
            this.normalUrl = normalUrl;
        }

        public String getNormalUrl() {
            return normalUrl;
        }

        public String getTitle() {
            return Title;
        }

        public String getAuthorName() {
            return AuthorName;
        }

        public String getImageUrl() {
            return ImageUrl;
        }

        public String getStreamUrl() {
            return StreamUrl;
        }
    }
    private SoundCloudModel model;
    private static final String TAG = "SoundCloud";
    public SoundCloud(String url) {
        this.link = url;
        url = "https://soundcloud.com/oembed?format=json&url="+url;
        HttpHandler handler = new HttpHandler();
        String response = handler.makeServiceCall(url);
        Log.e(TAG, "SoundCloud: Link"+url );
        try {
            if (response != null) {
                JSONObject obj = new JSONObject(response);
                String title = obj.getString("title");
                title = YTutils.getVideoTitle(title);
                String author = obj.getString("author_name");
                String imageUri = obj.getString("thumbnail_url");

                String code = obj.getString("html");
                String html = code.replace("%3A", ":")
                        .replace("%2F", "/");

                List<String> strings = RegexUtils.getAllMatches("https://api.soundcloud.com/(.*?)&", html);
                String streamUrl = strings.get(0).replace("&", "");

                String trackId = streamUrl.substring(streamUrl.lastIndexOf('/') + 1);

                String link = "https://api.soundcloud.com/tracks/" + trackId + "/stream?client_id=" + AppInterface.SOUNDCLOUD_API;

                String manualUrl=null;
                Log.e(TAG, "getLinkStreamFromSoundCloud: Url: " + link);
                String redirectUrl = YTutils.getRedirectAppUrl(link);
                if(!StringUtils.isEmpty(redirectUrl)){
                    manualUrl = redirectUrl;
                }else {
                    String dataServer = DownloadUtils.downloadString(link);
                    if (!StringUtils.isEmpty(dataServer)) {
                        try {
                            JSONObject mJsonObject = new JSONObject(dataServer);
                            manualUrl = mJsonObject.getString("http_mp3_128_url");
                        } catch (Exception e) {
                            e.printStackTrace();
                            manualUrl = link;
                        }
                    }
                }
                Log.e("FINAL_STREAM_URL", manualUrl + "");

                model = new SoundCloudModel(
                        title, author, imageUri, manualUrl,url
                );
            }
        }catch (Exception e){
            Log.e(TAG, "SoundCloud: Exception: "+e.getMessage());
        }
    }

    public SoundCloud(String url,boolean parseOnlyDetails) {
        this.link = url;
        url = "https://soundcloud.com/oembed?format=json&url="+url;
        HttpHandler handler = new HttpHandler();
        String response = handler.makeServiceCall(url);
        Log.e(TAG, "SoundCloud: Link"+url );
        try {
            if (response != null) {
                JSONObject obj = new JSONObject(response);
                String title = obj.getString("title");
                title = YTutils.getVideoTitle(title);
                String author = obj.getString("author_name");
                String imageUri = obj.getString("thumbnail_url");

                String code = obj.getString("html");
                String html = code.replace("%3A", ":")
                        .replace("%2F", "/");

                String manualUrl=null;

                if (!parseOnlyDetails) {
                    List<String> strings = RegexUtils.getAllMatches("https://api.soundcloud.com/(.*?)&", html);
                    String streamUrl = strings.get(0).replace("&", "");

                    String trackId = streamUrl.substring(streamUrl.lastIndexOf('/') + 1);

                    String link = "https://api.soundcloud.com/tracks/" + trackId + "/stream?client_id=" + AppInterface.SOUNDCLOUD_API;

                    Log.e(TAG, "getLinkStreamFromSoundCloud: Url: " + link);
                    String redirectUrl = YTutils.getRedirectAppUrl(link);
                    if(!StringUtils.isEmpty(redirectUrl)){
                        manualUrl = redirectUrl;
                    }else {
                        String dataServer = DownloadUtils.downloadString(link);
                        if (!StringUtils.isEmpty(dataServer)) {
                            try {
                                JSONObject mJsonObject = new JSONObject(dataServer);
                                manualUrl = mJsonObject.getString("http_mp3_128_url");
                            } catch (Exception e) {
                                e.printStackTrace();
                                manualUrl = link;
                            }
                        }
                    }
                    Log.e("FINAL_STREAM_URL", manualUrl + "");
                }
                model = new SoundCloudModel(
                        title, author, imageUri, manualUrl,url
                );
            }
        }catch (Exception e){
            Log.e(TAG, "SoundCloud: Exception: "+e.getMessage());
        }
    }

    public String getViewCount() {
        return viewCount;
    }

    public void captureViews() {
        try {
            URLConnection connection = (new URL(link)).openConnection();
            connection.connect();
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            StringBuilder builder = new StringBuilder();
            while((line = reader.readLine()) != null)
            {
                builder.append(line).append("\n");
                if (line.contains("play_count\" content=\"")) {
                    Log.e(TAG, "captureViews: In Playback count..."+line );
                    List<String> strings = RegexUtils.getAllMatches("play_count\" content=\"(.*?)\"",
                            line);
                    if (strings.size()>0) { //"soundcloud:play_count" content="17579994"
                        Log.e(TAG, "captureViews: In Playback Found..." );
                        String match = strings.get(0);
                        viewCount = match.replace("\"","").split("=")[1].trim();
                        Log.e(TAG, "captureViews: ViewCount: "+viewCount );
                        return;
                    }
                }
               /* if (line.contains("content=\"UserLikes")) {

                }*/
            }
            in.close();
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
      /*  HttpHandler handler = new HttpHandler();
        handler.makeServiceCall("view-source:"+link);*/
        //
    }

    public SoundCloudModel getModel() {
        return model;
    }
}
