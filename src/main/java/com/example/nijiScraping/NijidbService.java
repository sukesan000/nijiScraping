package com.example.nijiScraping;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class NijidbService {
    @Autowired
    private NijidbRepository nijidbRepository;
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final com.google.api.client.json.JsonFactory JSON_FACTORY = new JacksonFactory();
    //APIキー
    String key = "AIzaSyCfLSCfatZoJJ7asA404PkrZG5lf4Servc";
    //検索実行
    ChannelListResponse channelsResponse;

    public List<Channel> getChannelInfo(List<Member> memberList) throws IOException {
        YouTube youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName("youtube-cmdline-search-sample").build();

        List<Channel> channelsList = new ArrayList<>();
        try{
            for (Member member : memberList) {
                String chId = member.getChannel_id();
                YouTube.Channels.List channelInfo = youtube.channels().list(Arrays.asList("id,snippet,statistics"));
                channelInfo.setKey(key);
                channelInfo.setId(Arrays.asList(chId));

                //API実行
                channelsResponse = channelInfo.execute();
                Channel channel = channelsResponse.getItems().get(0);
                channelsList.add(channel);
            }
        }catch (Exception e){
            System.out.println("youtube上の情報取得に失敗しました。: " + e);
        }

        return channelsList;
    }

    public void saveChannelInfo(List<Channel> channelInfoList, List<Member> memberList){
        int i = 0;
        for(Channel channel : channelInfoList){
            String channelId = channel.getId();
            String thumbnail = channel.getSnippet().getThumbnails().getHigh().getUrl();
            Date publishedAt_ = null;
            DateTime publishedAt =  channel.getSnippet().getPublishedAt();
            String datetimeStr = publishedAt.toString();
            int n = datetimeStr.indexOf("T");
            String dateStr = datetimeStr.substring(0, n);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try{
                publishedAt_ = format.parse(dateStr);
            }catch (ParseException e) {
                e.printStackTrace();
            }
            int subscriver = channel.getStatistics().getSubscriberCount().intValue();
            int videoCount = channel.getStatistics().getVideoCount().intValue();
            String subscriver_ = String.format("%,d", subscriver);
            String videoCount_ = String.format("%,d", videoCount);

            //entityに記録されているid
            String id = memberList.get(i).getChannel_id();
            String channelLink = memberList.get(i).getChannel_link();

            //entityにセット
            if(channelId.equals(id)){
                memberList.get(i).setSubscriber(subscriver_);
                memberList.get(i).setVideo_count(videoCount_);
                memberList.get(i).setThumbnail(thumbnail);
                memberList.get(i).setPublished_at(publishedAt_);
                memberList.get(i).setChannel_link(channelLink);
                i++;
            }
        }
        saveData(memberList);
    }

    public void saveData(List<Member> Members){
        nijidbRepository.saveAll(Members);
    }
}
