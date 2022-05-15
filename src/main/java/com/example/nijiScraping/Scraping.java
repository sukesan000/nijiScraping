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

        //全ライバーのURLと名前の取得
        Elements hrefs = document.select("a[href*=YouTube]");
        Elements names = document.select("div.grow.order-2 > p.text-base.ease-out");

        int idNum = 1;
        int namesNum = 0;
        for (Element element: hrefs){
            Member member = new Member();
            String href = element.attr("href");
            member.setChannel_id(href.substring(32));
            member.setId(idNum);
            member.setChannel_link(href);
            member.setName(names.get(namesNum).text());
            memberList.add(member);
            idNum++;
            namesNum++;
        }

        //youtubeの情報を保存
        List<Channel> channelInfoList = njService.getChannelInfo(memberList);
        njService.saveChannelInfo(channelInfoList, memberList);
        logger.info("スクレイピングを終了します");
    }
}
