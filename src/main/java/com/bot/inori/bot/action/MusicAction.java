package com.bot.inori.bot.action;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bot.inori.bot.handler.MessageHandler;
import com.bot.inori.bot.model.data.Music_Cache_Data;
import com.bot.inori.bot.model.req.MediaMessage;
import com.bot.inori.bot.model.req.MusicMessage;
import com.bot.inori.bot.model.req.TextMessage;
import com.bot.inori.bot.model.res.MetadataChain;
import com.bot.inori.bot.utils.HttpUtils;
import com.bot.inori.bot.utils.StringUtil;
import com.bot.inori.bot.utils.WrapHtmlUtils;
import com.bot.inori.bot.utils.annotation.BotCommand;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class MusicAction {

    @BotCommand(cmd = "#听", alias = "＃听", description = "选择听的歌曲", permit = false)
    public void listen(MetadataChain chain) {
        String cmd = chain.getBasicCommand().substring(2).trim();
        if (StringUtil.isBlank(cmd) || !StringUtil.isNumeric(cmd)) return;
        String url = Music_Cache_Data.Cache_Music.get(chain.getSender().getUser_id());
        if (url == null) chain.sendReplyMsg(new TextMessage("请先点歌"));
        else dealMusic(url, chain, Integer.parseInt(cmd), 1);
    }

    @BotCommand(cmd = "p", alias = "P", description = "翻页", permit = false)
    public void page(MetadataChain chain) {
        String cmd = chain.getBasicCommand().substring(1).trim();
        if (StringUtil.isBlank(cmd) || !StringUtil.isNumeric(cmd)) return;
        String url = Music_Cache_Data.Cache_Music.get(chain.getSender().getUser_id());
        if (url == null) chain.sendReplyMsg(new TextMessage("请先点歌"));
        else dealMusic(url, chain, null, Integer.parseInt(cmd));
    }

    @BotCommand(cmd = "网易云点歌", description = "网易云点歌", permit = false)
    public void netease(MetadataChain chain) {
        String cmd = chain.getBasicCommand().substring(5).trim();
        if (StringUtil.isBlank(cmd)) {
            chain.sendReplyMsg(new TextMessage("歌名呢？"));
            return;
        }
        dealMusic(String.format("https://api.mrgnb.cn/API/netease.php?msg=%s&count=20", URLEncoder.encode(cmd, StandardCharsets.UTF_8)),
                chain, null, 1);
    }

    @BotCommand(cmd = "QQ点歌", description = "QQ点歌", permit = false)
    public void qqmusic(MetadataChain chain) {
        String cmd = chain.getBasicCommand().substring(4).trim();
        if (StringUtil.isBlank(cmd)) {
            chain.sendReplyMsg(new TextMessage("歌名呢？"));
            return;
        }
        dealMusic(String.format("https://api.mrgnb.cn/API/qqmusic.php?msg=%s&count=20", URLEncoder.encode(cmd, StandardCharsets.UTF_8)),
                chain, null, 1);
    }

    @BotCommand(cmd = "酷狗点歌", description = "酷狗点歌", permit = false)
    public void kgmusic(MetadataChain chain) {
        String cmd = chain.getBasicCommand().substring(4).trim();
        if (StringUtil.isBlank(cmd)) {
            chain.sendReplyMsg(new TextMessage("歌名呢？"));
            return;
        }
        dealMusic(String.format("https://api.mrgnb.cn/API/kgmusic.php?msg=%s&count=20", URLEncoder.encode(cmd, StandardCharsets.UTF_8)),
                chain, null, 1);
    }

    public static void main(String[] args) {
        JSONObject res = HttpUtils.sendGet("https://api.mrgnb.cn/API/netease.php?msg=起风了&count=20&page=1", false);
        JSONArray array = res.getJSONArray("data");
        WrapHtmlUtils.generateMusic(array);
    }

    private void dealMusic(String url, MetadataChain chain, Integer index, Integer page) {
        try {
            if (index != null) url += "&n=" + index;
            if (page == null) page = 1;
            JSONObject res = HttpUtils.sendGet(url +  "&page=" + page, false);
            if (res.isEmpty() || res.getInteger("code") != 200 || res.getJSONArray("data").isEmpty()) {
                chain.sendReplyMsg(new TextMessage("没有找到歌曲"));
                return;
            }
            JSONArray array = res.getJSONArray("data");
            if (index == null) {
                if (array.size() == 1) dealMusic(url, chain, 0, page);
                chain.sendReplyMsg(MediaMessage.imageMedia(WrapHtmlUtils.generateMusic(array)));
                Music_Cache_Data.Cache_Music.put(chain.getSender().getUser_id(), url);
            } else {
                JSONObject data = array.getJSONObject(0);
                List<Object> list = new ArrayList<>();
                if (url.contains("netease.php")) {
                    list.add(MusicMessage.music("163", data.getString("id")));
                } else if (url.contains("qqmusic.php")) {
                    if (data.get("song_url") != null)
                        list.add(MusicMessage.musicCustom("https://y.qq.com/n/ryqq/songDetail/" + data.getString("mid"), data.getString("song_url"),
                                data.getString("name"), data.getString("singername"), data.getString("album_img")));
                    else list.add(new TextMessage("付费歌曲，获取源文件失败"));
                } else {
                    StringBuilder builder = new StringBuilder();
                    builder.append("歌手：").append(data.getString("singername")).append("\n")
                            .append("歌名：").append(data.getString("name"));
                    if (data.containsKey("play_count") && data.get("play_count") != null) builder.append("\n").append("播放数：").append(data.getString("play_count"));
                    if (data.containsKey("like_count") && data.get("like_count") != null) builder.append("\n").append("喜欢数：").append(data.getString("like_count"));
                    if (data.containsKey("comment_count") && data.get("comment_count") != null) builder.append("\n").append("评论数：").append(data.getString("comment_count"));
                    if (data.containsKey("collect_count") && data.get("collect_count") != null) builder.append("\n").append("收藏数：").append(data.getString("collect_count"));
                    if (data.containsKey("duration") && data.get("duration") != null) builder.append("\n").append("时长：").append(data.getString("duration"));
                    if (data.containsKey("file_size") && data.get("file_size") != null) builder.append("\n").append("大小：").append(data.getString("file_size"));
                    if (data.containsKey("publish_date") && data.get("publish_date") != null) builder.append("\n").append("发布日：").append(data.getString("publish_date"));
                    list.add(new TextMessage(builder.toString()));
                    if (data.containsKey("image_url")) list.add(MediaMessage.imageMedia(data.getString("image_url")));
                    if (data.containsKey("album_img")) list.add(MediaMessage.imageMedia(data.getString("album_img")));
                }
                chain.sendMsg(list);
                String song_url = data.getString("song_url");
                if (song_url.startsWith("http")) chain.sendMsg(MediaMessage.audioMedia(song_url));
                String mv_url = data.getString("mv_url");
                if (mv_url != null && mv_url.startsWith("http")) chain.sendMsg(MediaMessage.videoMedia(mv_url));
            }
        } catch (Exception e) {
            MessageHandler.getLogger().error("点歌报错 {}", e.getMessage());
        }
    }
}
