package com.mc.onmusic_relase.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.naveed.ytextractor.utils.RegexUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class LyricsApi extends AsyncTask<Void,Void,Void> {
    private static final String TAG = "LyricsApi";
    String videoTitle, authorName; Context activity;
    public LyricsApi(Context activity, String videoTitle, String authorName) {
        this.activity = activity;
        this.videoTitle = videoTitle;
        this.authorName = authorName;
    }

    String data;

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (data!=null)
        onLyricFound(Html.fromHtml(data));
        else onLyricFound(null);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        HttpHandler handler = new HttpHandler();
        if (authorName.contains("("))
            authorName = authorName.split("\\(")[0];
        String link = "https://www.azlyrics.com/lyrics/"+
                authorName.trim().toLowerCase().replace(" ","")
                +"/"+
                videoTitle.toLowerCase().trim().replace(" ","")+".html";
        Log.e(TAG, "doInBackground: Url: "+link );

        try {
            Document document = Jsoup.connect(link).get();
            Element element = document.body();
            String text = element.toString();

            YTutils.writeContent(activity,YTutils.getFile("new.html").getPath(),text);

            if (RegexUtils.hasMatch("<b>\"(.*?)\"</b>",text)) {
                Log.e(TAG, "doInBackground: Lyrics found!" );
                StringBuilder builder = new StringBuilder();
                String[] lines = text.split("\n|\r");
                for (int i=0;i<lines.length;i++) {
                    String line = lines[i];
                    if (line.trim().startsWith("<div>")) {
                        int j=i+1;
                        do {
                            if (lines[j].trim().startsWith("<!--")) {
                                String l = lines[j];
                                l = l.substring(l.indexOf('>')+1);
                                builder.append(l).append("\n");
                            }else
                            builder.append(lines[j]).append("\n");
                            j++;
                        }while (!lines[j].trim().startsWith("</div>"));

                       data = builder.toString();
                        break;
                    }
                }
            }else { onError("Lyrics not found");}
        } catch (Exception e) {
            e.printStackTrace();
            onError("Failed to connect API");
        }
        return null;

    }
    public void onLyricFound(Spanned data) {

    }
    public void onError(String error) {

    }
}
