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

    public List<Channel> getChannelInfo(List<Member> chId_list) throws IOException {
        YouTube youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName("youtube-cmdline-search-sample").build();

        List<Channel> channelsList = new ArrayList<>();

        for (Member member : chId_list) {
            String chId = member.getChannel_id();
            YouTube.Channels.List channelInfo = youtube.channels().list(Arrays.asList("id,snippet,statistics"));
            channelInfo.setKey(key);
            channelInfo.setId(Arrays.asList(chId));

            channelsResponse = channelInfo.execute();
            Channel channel = channelsResponse.getItems().get(0);
            channelsList.add(channel);
        }
        return channelsList;
    }

    public List<Member> getAllChannelId(){
        return nijidbRepository.getALLChannelId();
    }

    public List<Member> findMember(String kewword){
        return nijidbRepository.findMember(kewword);
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

                nijidbRepository.updateOne(memberList.get(i).getSubscriber(),
                        memberList.get(i).getVideo_count(),
                        memberList.get(i).getThumbnail(),
                        memberList.get(i).getChannel_id(),
                        memberList.get(i).getPublished_at(),
                        memberList.get(i).getChannel_link());
            }
            i++;
        }
    }

    public List<Member >getAllMemberInfo(){
        return nijidbRepository.getALLChannelInfo();
    }

    public void saveData(String id,String name,String channelLink){
        nijidbRepository.insertOne(id,name,channelLink);
    }
}
