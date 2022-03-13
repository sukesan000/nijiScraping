package com.example.nijiScraping;

import com.google.api.services.youtube.model.Channel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Component
public class Scraping{
    @Autowired
    private NijidbService njService;
    Logger logger = Logger.getLogger("Scraping");

    public void saveMemberInfo() throws IOException {
        logger.info("スクレイピングを開始します");
        Document document = Jsoup.connect("https://refined-itsukara-link.neet.love/livers").get();
        List<Member> memberList = new ArrayList<>();

        Elements hrefs = document.select("a[href*=YouTube]");
        int idNum = 0;
        for (Element element: hrefs){
            Member member = new Member();
            String href = element.attr("href");
            member.setChannel_id(href.substring(32));
            member.setId(idNum);
            member.setChannel_link(href);
            memberList.add(member);
            idNum++;
        }

        int num = 0;
        Elements names = document.select("div.flex-grow > p.ease-out");
        for (Element element: names){
            Member member = memberList.get(num);
            String name = element.text();
            member.setName(name);
            num++;
        }

        //名前とidとチャンネルURLをDBにセット
        for(Member member: memberList){
            String id = member.getChannel_id();
            String name = member.getName();
            String channelLink = member.getChannel_link();
            njService.saveData(id, name, channelLink);
        }

        List<Channel> channelInfoList = njService.getChannelInfo(memberList);
        njService.saveChannelInfo(channelInfoList, memberList);
        logger.info("スクレイピングを終了します");
    }
}
