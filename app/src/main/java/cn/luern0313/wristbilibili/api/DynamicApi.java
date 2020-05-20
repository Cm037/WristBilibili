package cn.luern0313.wristbilibili.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.DynamicModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;
import cn.luern0313.wristbilibili.util.SharedPreferencesUtil;

/**
 * 被 luern0313 创建于 2018/10/30.
 * 动态的api
 * 写完这个文件我才知道为什么程序员被叫成代码民工。。
 * 这绝对就只是个力气活啊。。
 * 不过好在不用动脑子（滑稽）
 * 辛苦b站程序们，动态有至少十几种
 * 我只做了五种23333
 */

public class DynamicApi
{
    private final String DYNAMICTYPE = "268435455";
    private String csrf;
    private String selfMid;
    private String mid;
    private JSONArray dynamicJsonArray;
    private boolean isSelf;

    private DynamicModel dynamicModel;

    private String lastDynamicId;
    private ArrayList<String> webHeaders;

    public DynamicApi(String mid, boolean isSelf)
    {
        this.csrf = SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "");
        this.selfMid = SharedPreferencesUtil.getString(SharedPreferencesUtil.mid, "");
        this.mid = mid;
        this.isSelf = isSelf;
        this.dynamicModel = new DynamicModel();
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
            add("Referer"); add("https://www.bilibili.com/anime");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public void getDynamic() throws IOException
    {
        try
        {
            if(isSelf)
            {
                String url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new";
                String arg = "uid=" + mid + "&type=" + DYNAMICTYPE;
                dynamicJsonArray = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()).getJSONObject("data").getJSONArray("cards");
            }
            else
            {
                String url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history";
                String arg = "visitor_uid=" + selfMid + "&host_uid=" + mid + "&offset_dynamic_id=0";
                dynamicJsonArray = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()).getJSONObject("data").getJSONArray("cards");
            }
            if(dynamicJsonArray.length() == 0) dynamicJsonArray = null;
        }
        catch (JSONException e)
        {
            dynamicJsonArray = null;
            e.printStackTrace();
        }
    }

    public void getHistoryDynamic() throws IOException
    {
        try
        {
            if(isSelf)
            {
                String url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_history";
                String arg = "uid=" + mid + "&offset_dynamic_id=" + lastDynamicId + "&type=" + DYNAMICTYPE;
                dynamicJsonArray = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()).getJSONObject("data").getJSONArray("cards");
            }
            else
            {
                String url = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history";
                String arg = "visitor_uid=" + selfMid + "&host_uid=" + mid + "&offset_dynamic_id=" + lastDynamicId;
                dynamicJsonArray = new JSONObject(NetWorkUtil.get(url + "?" + arg, webHeaders).body().string()).getJSONObject("data").getJSONArray("cards");
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            dynamicJsonArray = new JSONArray();
        }
    }

    public ArrayList<DynamicModel> getDynamicList()
    {
        ArrayList<DynamicModel> dynamicList = new ArrayList<>();
        for (int i = 0; i < dynamicJsonArray.length(); i++)
        {
            JSONObject dy = dynamicJsonArray.optJSONObject(i);
            DynamicModel dynamic = getDynamicClass(dy.optString("card"), dy.optJSONObject("desc"));
            dynamicList.add(dynamic);
            if(i == dynamicJsonArray.length() - 1)
                lastDynamicId = dy.optJSONObject("desc").optString("dynamic_id_str");
        }
        return dynamicList;
    }

    private DynamicModel getDynamicClass(String cardStr, JSONObject desc)
    {
        try
        {
            JSONObject card = new JSONObject(cardStr);
            switch(desc.optInt("type"))
            {
                case 1:
                    return dynamicModel.new DynamicShareModel(card, desc, false);
                case 2:
                    return dynamicModel.new DynamicAlbumModel(card, desc, false);
                case 4:
                    return dynamicModel.new DynamicTextModel(card, desc, false);
                case 8:
                    return dynamicModel.new DynamicVideoModel(card, desc, false);
                case 64:
                    return dynamicModel.new DynamicArticleModel(card, desc, false);
                case 512:
                case 4098:
                case 4099:
                case 4101:
                    return dynamicModel.new DynamicBangumiModel(card, desc, false);
                case 2048:
                    return dynamicModel.new DynamicUrlModel(card, desc, false);
                case 4200:
                    return dynamicModel.new DynamicLiveModel(card, desc, false);
                case 4300:
                    return dynamicModel.new DynamicFavorModel(card, desc, false);
            }
            return dynamicModel.new DynamicUnknownModel(card, desc, false);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String likeDynamic(String dynamicId, String action) throws IOException
    {
        try
        {
            String url = "https://api.vc.bilibili.com/dynamic_like/v1/dynamic_like/thumb";
            String arg = "uid=" + mid + "&dynamic_id=" + dynamicId + "&up=" + action + "&csrf_token=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, arg, webHeaders).body().string());
            if(result.getInt("code") == 0)
                return "";
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return "未知错误，点赞失败。。";
    }

    public String sendReply(String oid, String type, String text) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/v2/reply/add";
            String arg = "oid=" + oid + "&type=" + type + "&message=" + text + "&plat=1&jsonp=jsonp&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, arg, webHeaders).body().string());
            if(result.getInt("code") == 0)
                return "";
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return "未知错误，评论失败。。";
    }
}
