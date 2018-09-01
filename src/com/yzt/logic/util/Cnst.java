package com.yzt.logic.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



/**
 * 常量
 */
public class Cnst {
	
	public final static String PING_MESSAGE = "ping";
	
	// 获取项目版本信息
    public static final String version = ProjectInfoPropertyUtil.getProperty("project_version", "1.5");
    public static Boolean isTest = true;//是否是测试环境


    public static final String p_name = ProjectInfoPropertyUtil.getProperty("p_name", "wsw_X14");
    public static final String o_name = ProjectInfoPropertyUtil.getProperty("o_name", "u_consume");
    public static final String gm_url = ProjectInfoPropertyUtil.getProperty("gm_url", "");
    
    //保存可以清的缓存
    public static final String REDIS_PREFIX = ProjectInfoPropertyUtil.getProperty("redis.prefix","");
    //保存不能清的缓存
    public static final String REDIS_RECORD_PREFIX = ProjectInfoPropertyUtil.getProperty("redis.record_prefix","");

    //回放配置
    public static final String BACK_FILE_PATH = ProjectInfoPropertyUtil.getProperty("backFilePath", "1.5");
    public static final String FILE_ROOT_PATH = ProjectInfoPropertyUtil.getProperty("fileRootPath", "1.5");
    public static final String INTERFACED_FILE_PATH = ProjectInfoPropertyUtil.getProperty("interfacedFilePath", "./");
    
    
    public static String SERVER_IP = getLocalAddress();
    public static String HTTP_URL = "http://".concat(Cnst.SERVER_IP).concat(":").concat(ProjectInfoPropertyUtil.getProperty("httpUrlPort", "8086")).concat("/");
    public static String getLocalAddress(){
		String ip = "";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ip;
	}
    
    
//    public static final String cid = ProjectInfoPropertyUtil.getProperty("cid", "33");;
    //redis配置
    public static final String REDIS_HOST = ProjectInfoPropertyUtil.getProperty("redis.host", "");
    public static final String REDIS_PORT = ProjectInfoPropertyUtil.getProperty("redis.port", "");
    public static final String REDIS_PASSWORD = ProjectInfoPropertyUtil.getProperty("redis.password", "");

    //mina的端口
    public static final String MINA_PORT = ProjectInfoPropertyUtil.getProperty("port", "");
    
    //用户服务地址
    public static final String GETUSER_URL = ProjectInfoPropertyUtil.getProperty("getUser_url","");
    //mina
    public static final int Session_Read_BufferSize = 2048 * 10;
    public static final int Session_life = 60;
    public static final int WriteTimeOut = 500;
    
    public static final String rootPath = ProjectInfoPropertyUtil.getProperty("rootPath", "");

    public static final long HEART_TIME = 9000;//心跳时间，前端定义为8s，避免网络问题延时，后端计算是以9s计算
    public static final int MONEY_INIT = 10;//初始赠送给用户的房卡数
    //开房选项中的是否
    public static final int YES = 1;
    public static final int NO = 0;
    

    public static final long ROOM_OVER_TIME = 5*60*60*1000;//房间定时5小时解散
    public static final long ROOM_CREATE_DIS_TIME = 40*60*1000;//创建房间之后，40分钟解散
    public static final long ROOM_DIS_TIME = 5*60*1000;//玩家发起解散房间之后，5分钟自动解散
    public static final String CLEAN_3 = "0 0 3 * * ?";
    public static final String CLEAN_EVERY_HOUR = "0 0 0/1 * * ?";
    public static final String COUNT_EVERY_TEN_MINUTE = "0 0/1 * * * ?";
    public static final long BACKFILE_STORE_TIME = 3*24*60*60*1000;//回放文件保存时间
   
    
    //玩家数据更新间隔时间
    public static long updateDiffTime =3*24*3600*1000;
    //测试时间
//    public static final long ROOM_OVER_TIME = 60*1000;//
//    public static final long ROOM_CREATE_DIS_TIME = 6*1000;
//    public static final long ROOM_DIS_TIME = 10*1000;
//	public static final String CLEAN_3 = "0/5 * * * * ?";
//	public static final String CLEAN_EVERY_HOUR = "0/5 * * * * ?";
//    public static final String COUNT_EVERY_TEN_MINUTE = "0/1 * * * * ?";
//    public static final long BACKFILE_STORE_TIME = 60*1000;//回放文件保存时间
    
    

    public static final int ROOM_LIFE_TIME_CREAT = (int) ((ROOM_OVER_TIME/1000)+200);//创建时，5小时，redis用
    public static final int ROOM_LIFE_TIME_DIS = (int) ((ROOM_DIS_TIME/1000)+200);//解散房间时，300s，redis用
    public static final int ROOM_LIFE_TIME_COMMON = (int) ((ROOM_CREATE_DIS_TIME/1000)+200);//正常开局存活时间，redis用
    public static final int OVERINFO_LIFE_TIME_COMMON = (int) (10*60);//大结算 overInfo 存活时间
    public static final int PLAYOVER_LIFE_TIME =3*24*60*60;//战绩保存时间
    public static final int HUIFANG_LIFE_TIME = 30*60;//30分钟 回放写入文件后删除
    
    public static final int DIS_ROOM_RESULT = 1;

    public static final int DIS_ROOM_TYPE_1 = 1;//创建房间40分钟解散类型
    public static final int DIS_ROOM_TYPE_2 = 2;//玩家点击解散房间类型

    public static final int PAGE_SIZE = 10;
    



    public static final String USER_SESSION_USER_ID = "user_id";
    public static final String USER_SESSION_IP = "ip";
    
    

    //房间信息中的state
    // 1等待玩家入坐；2游戏中；3小结算  4大结算或已解散
    public static final int ROOM_STATE_CREATED = 1;
    public static final int ROOM_STATE_GAMIING = 2;
    public static final int ROOM_STATE_XJS = 3;
    public static final int ROOM_STATE_YJS = 4;


    //房间类型
    public static final int ROOM_TYPE_1 = 1;//房主模式
    public static final int ROOM_TYPE_2 = 2;//自由模式
    public static final int ROOM_TYPE_3 = 3;//AA


    //开房的局数对应消耗的房卡数
    public static final Map<Integer,Integer> moneyMap = new HashMap<>();
    static {
        moneyMap.put(10,3);
        moneyMap.put(15,4);
        moneyMap.put(30,8);
    }
    //玩家在线状态 state 
    public static final int PLAYER_LINE_STATE_INLINE = 1;//"inline"
    public static final int PLAYER_LINE_STATE_OUT = 2;//"out"
    
    //玩家进入或退出代开房间
    public static final int PLAYER_EXTRATYPE_ADDROOM = 1;//进入
    public static final int PLAYER_EXTRATYPE_EXITROOM = 2;//退出
    public static final int PLAYER_EXTRATYPE_JIESANROOM = 3;//解散
    public static final int PLAYER_EXTRATYPE_LIXIAN = 4;//离线
    public static final int PLAYER_EXTRATYPE_SHANGXIAN = 5;//上线
    public static final int PLAYER_EXTRATYPE_KAIJU = 6;//房间开局
    //玩家状态
    public static final int PLAYER_STATE_DATING = 1;//"dating"
    public static final int PLAYER_STATE_IN = 2;//"in"
    public static final int PLAYER_STATE_PREPARED = 3;//"prepared"
    public static final int PLAYER_STATE_GAME = 4;//"game"
    public static final int PLAYER_STATE_JIAOPAI = 5;//"game"
    

    //请求状态
    public static final int REQ_STATE_FUYI = -1;//敬请期待
    public static final int REQ_STATE_0 = 0;//非法请求
    public static final int REQ_STATE_1 = 1;//正常
    public static final int REQ_STATE_2 = 2;//余额不足
    public static final int REQ_STATE_3 = 3;//已经在其他房间中
    public static final int REQ_STATE_4 = 4;//房间不存在
    public static final int REQ_STATE_5 = 5;//房间人员已满
    public static final int REQ_STATE_6 = 6;//游戏中，不能退出房间
    public static final int REQ_STATE_7 = 7;//有玩家拒绝解散房间
    public static final int REQ_STATE_8 = 8;//玩家不存在（代开模式中，房主踢人用的）
    public static final int REQ_STATE_9 = 9;//接口id不符合，需请求大接口
    public static final int REQ_STATE_10 = 10;//代开房间创建成功
    public static final int REQ_STATE_11 = 11;//已经代开过10个了，不能再代开了
    public static final int REQ_STATE_12 = 12;//房间存在超过24小时解散的提示
    public static final int REQ_STATE_13 = 13;//房间40分钟未开局解散提示
    public static final int REQ_STATE_14 = 14;//ip不一致
    
    
    //以下是俱乐部的
    public static final int REQ_STATE_15 = 15;//已经在俱乐部里了
    public static final int REQ_STATE_16 = 16;//玩家加入俱乐部的数量已满
    public static final int REQ_STATE_17 = 17;//申请操作成功
    public static final int REQ_STATE_18 = 18;//已经操作过了
    public static final int REQ_STATE_19 = 19;//玩家不在当前俱乐部
    public static final int REQ_STATE_20 = 20;//当前创建的房间数量已满
    public static final int REQ_STATE_21 = 21;//俱乐部剩余房卡不足，不能创建房间
    public static final int REQ_STATE_22 = 22;//今日玩家房卡消耗已达上限
    public static final int REQ_STATE_23 = 23;//该俱乐部不存在

    

    //动作类型
    public static final int ACTION_TYPE_FAPAI = 1;
    public static final int ACTION_TYPE_CHUPAI = 2;
    public static final int ACTION_TYPE_BUCHU = 3;
    public static final int ACTION_TYPE_JIAOPAI = 4;
    //这个是都没动作的时候给的推送
    
    
    //退出类型
    public static final int EXIST_TYPE_EXIST = 1;//"exist"
    public static final int EXIST_TYPE_DISSOLVE = 2;//"dissolve";

    // 项目根路径
    public static String ROOTPATH = "";
    
    //redis存储的key的不同类型的前缀
    public static final String REDIS_PREFIX_ROOMMAP = REDIS_PREFIX.concat("_ROOM_MAP_");//房间信息--可以清理
    public static final String REDIS_PREFIX_OPENIDUSERMAP = REDIS_PREFIX.concat("_OPENID_USERID_MAP_");//openId-user数据--可以清理
    public static final String REDIS_PREFIX_USER_ID_USER_MAP = REDIS_PREFIX.concat("_USER_ID_USER_MAP_");//通过userId获取用户 --可以清理
    
    //代开房间列表
    public static final String REDIS_PREFIX_DAI_ROOM_LIST = REDIS_PREFIX.concat("_DAIKAI_ROOM_LIST_");//通过代开房间列表
    //redis中通知的key
    public static final String NOTICE_KEY = REDIS_PREFIX.concat("_NOTICE_KEY");
    
    public static final String PROJECT_PREFIX = REDIS_PREFIX.concat("_*");
    
    public static final String REDIS_ONLINE_NUM_COUNT = REDIS_PREFIX.concat("_ONLINE_NUM_");
    
    public static final String REDIS_HEART_PREFIX = REDIS_PREFIX.concat("_HEART_USERS_MAP");
    
    public static final String REDIS_HUIFANG_MAP = REDIS_PREFIX.concat("_HUIFANG_MAP_");//房间号+时间戳+小局
    
    //俱乐部
    //清理的
    public static final String REDIS_CLUB_CLUBMAP = REDIS_PREFIX.concat("_CLUB_MAP_");//俱乐部信息--清理,防止停服的时候代理后端从房卡
    public static final String REDIS_CLUB_ROOM_LIST = REDIS_PREFIX.concat("_CLUB_MAP_LIST_");//存放俱乐部未开局房间信息--这个可以清
    //不清理的
    public static final String REDIS_CLUB_PLAY_RECORD_PREFIX = REDIS_RECORD_PREFIX.concat("_CLUB_PLAY_RECORD_");//房间战绩
    public static final String REDIS_CLUB_PLAY_RECORD_PREFIX_ROE_USER =  REDIS_RECORD_PREFIX.concat("_CLUB_PLAY_RECORD_FOR_USER_");//玩家字段
    public static final String REDIS_CLUB_TODAYSCORE_ROE_USER = REDIS_RECORD_PREFIX.concat("_CLUB_TODAYSCORE_FOR_USER_");//统计玩家今日分数
    public static final String REDIS_CLUB_TODAYJUNUM_ROE_USER = REDIS_RECORD_PREFIX.concat("_CLUB_TODAYJUNUM_FOR_USER_");//统计玩家今日局数
    public static final String REDIS_CLUB_ACTIVE_NUM = REDIS_RECORD_PREFIX.concat("_CLUB_ACTIVE_NUM_");//今天活跃人数
    public static final String REDIS_CLUB_TODAYKAI_NUM = REDIS_RECORD_PREFIX.concat("_CLUB_TODAYKAI_NUM_");//今天开局数
    public static final int REDIS_CLUB_DIE_TIME = 3*24*60*60;//玩家战绩和俱乐部每天活跃保存时间
    public static final int REDIS_CLUB_PLAYERJUNUM_TIME = 2*24*60*60;//玩家今日局数保存时间

    
    //这个字段不清理，存放玩家战绩，定时任务定期清理内容
    public static final String REDIS_PLAY_RECORD_PREFIX = REDIS_RECORD_PREFIX.concat("_PLAY_RECORD_");//房间战绩
    public static final String REDIS_PLAY_RECORD_PREFIX_ROE_USER = REDIS_RECORD_PREFIX.concat("_PLAY_RECORD_FOR_USER_");//玩家字段
    public static final String REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI = REDIS_RECORD_PREFIX.concat("_PLAY_RECORD_FOR_DAIKAI_");//代开房间
    public static final String REDIS_PLAY_RECORD_PREFIX_OVERINFO = REDIS_PREFIX.concat("_PLAY_RECORD_OVERINFO_");//大结算
    
    public static Map<String,String> ROUTE_MAP = new ConcurrentHashMap<String, String>();
    public static Map<String,String> ROUTE_MAP_SEND = new ConcurrentHashMap<String, String>();
    //FIX ME 别多打空格 生成文件会有问题
    static{
    	ROUTE_MAP_SEND.put("a","interfaceId");
    	ROUTE_MAP_SEND.put("b","state");
    	ROUTE_MAP_SEND.put("c","message");
    	ROUTE_MAP_SEND.put("d","info");
    	ROUTE_MAP_SEND.put("e","others");
    	ROUTE_MAP_SEND.put("f","page");
    	ROUTE_MAP_SEND.put("g","infos");
    	ROUTE_MAP_SEND.put("h","pages");
    	ROUTE_MAP_SEND.put("i","connectionInfo");
    	ROUTE_MAP_SEND.put("j","help");
    	ROUTE_MAP_SEND.put("k","userId");
    	ROUTE_MAP_SEND.put("l","content");
    	ROUTE_MAP_SEND.put("m","tel");
    	ROUTE_MAP_SEND.put("n","roomType");
    	ROUTE_MAP_SEND.put("o","type");
    	ROUTE_MAP_SEND.put("p","clubId");
    	ROUTE_MAP_SEND.put("q","clubName");
    	ROUTE_MAP_SEND.put("r","clubUserName");
    	ROUTE_MAP_SEND.put("s","allNums");
    	ROUTE_MAP_SEND.put("t","maxNums");
    	ROUTE_MAP_SEND.put("u","freeStart");
    	ROUTE_MAP_SEND.put("v","freeEnd");
    	ROUTE_MAP_SEND.put("w","clubMoney");
    	ROUTE_MAP_SEND.put("x","cardQuota");
    	ROUTE_MAP_SEND.put("y","juNum");
    	ROUTE_MAP_SEND.put("z","used");
    	ROUTE_MAP_SEND.put("A","roomId");
    	ROUTE_MAP_SEND.put("B","reqState");
    	ROUTE_MAP_SEND.put("C","playerNum");
    	ROUTE_MAP_SEND.put("D","money");
    	ROUTE_MAP_SEND.put("E","playStatus");
    	ROUTE_MAP_SEND.put("F","position");
    	ROUTE_MAP_SEND.put("G","userInfo");
    	ROUTE_MAP_SEND.put("H","playType");
    	ROUTE_MAP_SEND.put("I","wsw_sole_action_id");
    	ROUTE_MAP_SEND.put("J","roomInfo");
    	ROUTE_MAP_SEND.put("K","lastNum");
    	ROUTE_MAP_SEND.put("L","roomIp");
    	ROUTE_MAP_SEND.put("M","ip");
    	ROUTE_MAP_SEND.put("N","xjst");
    	ROUTE_MAP_SEND.put("O","score");
    	ROUTE_MAP_SEND.put("P","userName");
    	ROUTE_MAP_SEND.put("Q","userImg");
    	ROUTE_MAP_SEND.put("R","joinIndex");
    	ROUTE_MAP_SEND.put("S","gender");
    	ROUTE_MAP_SEND.put("T","createTime");
    	ROUTE_MAP_SEND.put("U","circleNum");
    	ROUTE_MAP_SEND.put("V","openId");
    	ROUTE_MAP_SEND.put("W","cId");
    	ROUTE_MAP_SEND.put("X","currentUser");
    	ROUTE_MAP_SEND.put("Y","anotherUsers");
    	ROUTE_MAP_SEND.put("Z","version");
    	ROUTE_MAP_SEND.put("aa","userAgree");
    	ROUTE_MAP_SEND.put("ab","notice");
    	ROUTE_MAP_SEND.put("ac","actNum");
    	ROUTE_MAP_SEND.put("ad","exState");
    	ROUTE_MAP_SEND.put("ae","pais");
    	ROUTE_MAP_SEND.put("af","xiaoJuNum");
    	ROUTE_MAP_SEND.put("ag","zhuangPlayer");
    	ROUTE_MAP_SEND.put("ah","dissolveTime");
    	ROUTE_MAP_SEND.put("ai","othersAgree");
    	ROUTE_MAP_SEND.put("aj","dissolveRoom");
    	ROUTE_MAP_SEND.put("ak","continue");
    	ROUTE_MAP_SEND.put("al","nextAction");
    	ROUTE_MAP_SEND.put("am","nextActionUserId");
    	ROUTE_MAP_SEND.put("an","currAction");
    	ROUTE_MAP_SEND.put("ao","currActionUserId");
    	ROUTE_MAP_SEND.put("ap","lastAction");
    	ROUTE_MAP_SEND.put("aq","lastActionUserId");
    	ROUTE_MAP_SEND.put("ar", "agree");
    	ROUTE_MAP_SEND.put("as", "idx");
    	ROUTE_MAP_SEND.put("at", "date");
    	ROUTE_MAP_SEND.put("au", "extra");
    	ROUTE_MAP_SEND.put("av", "extraType");  	
    	ROUTE_MAP_SEND.put("aw", "startPosition");
    	ROUTE_MAP_SEND.put("ax", "x_index");
    	ROUTE_MAP_SEND.put("ay", "y_index");
    	ROUTE_MAP_SEND.put("az", "distance");
    	ROUTE_MAP_SEND.put("ba", "xiaoJuInfo");
    	ROUTE_MAP_SEND.put("bb", "backUrl");   	
    	//麻将特有
    	ROUTE_MAP_SEND.put("ca", "lastChuPai");
    	ROUTE_MAP_SEND.put("cb", "lastChuPaiUserId");
    	ROUTE_MAP_SEND.put("cc", "currPaiNum");
    	ROUTE_MAP_SEND.put("cd", "isWin");
    	//营口特有
    	ROUTE_MAP_SEND.put("ea", "lianDui");
    	ROUTE_MAP_SEND.put("eb", "shuangWang");
    	ROUTE_MAP_SEND.put("ec", "siLu");
    	ROUTE_MAP_SEND.put("ed", "wuLu");
    	ROUTE_MAP_SEND.put("ee", "liuLu");
    	ROUTE_MAP_SEND.put("ef", "beiShu");
    	ROUTE_MAP_SEND.put("eg", "maxPeople");
    	ROUTE_MAP_SEND.put("eh", "zhuangRule");
    	ROUTE_MAP_SEND.put("ei", "winInfo");
    	ROUTE_MAP_SEND.put("ej", "zhaNum");
    	ROUTE_MAP_SEND.put("ek", "maxNum");
    	ROUTE_MAP_SEND.put("el", "winNum");
    	ROUTE_MAP_SEND.put("em", "loseNum");
    	ROUTE_MAP_SEND.put("en", "currBeiShu");
    	ROUTE_MAP_SEND.put("eo", "users");
    	ROUTE_MAP_SEND.put("ep", "hasChu");
    	ROUTE_MAP_SEND.put("eq", "action");
    	ROUTE_MAP_SEND.put("er", "nowBeiShu");
    	ROUTE_MAP_SEND.put("es", "sameLv");
    	//反向一次
    	Iterator<String> i = ROUTE_MAP_SEND.keySet().iterator();
    	while(i.hasNext()){
    		String key = i.next();
    		ROUTE_MAP.put(ROUTE_MAP_SEND.get(key), key);
    	}
    }



    //胡分
    public static final int PINGHU = 1;
    public static final int QINGYISE = 2;
    public static final int PIAOHU = 3;
    public static final int QIDUI = 4;
    public static final int TUIDAOHU = 5;
    public static final int QIONGHU = 6;
//    public static final int SHOUBAYI = 7;
    public static final int ZHUANGJIA = 8;
    public static final int ZIMO = 9;
    public static final int DIANPAO = 10;    
    public static final int JUETOUHUN = 11;
    public static final int QIANGTOUJUE = 12;
    public static final int PUFEN = 13;

    //房间状态--出牌
    public static final Integer ROOM_ACTION_CHU = 1;
    //房间状态--摸牌
    public static final Integer ROOM_ACTION_MO = 2;
    
	//制造假数据的房间id  从10000开始
	public static  Integer TEST_ROOMID = 100000;
	public static  Integer TEST_CLUB_ROOMID = 1000000;
	public static  Integer TEST_DAIKAI_ROOMID = 400000;


    public static final int TEST_PLAYOVER_LIFE_TIME =3*60*60;//测试战绩保存3个小时时间
    public static final String splitStr = "_";


    //数据存储
    public static String get_REDIS_PLAY_RECORD_PREFIX_OVERINFO(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PLAY_RECORD_PREFIX_OVERINFO.concat(cId).concat(splitStr);
    }
    
    /**
     * 存放俱乐部的信息  key:cid+clubid ---  value:club实体
     */
    public static String get_REDIS_PREFIX_CLUBMAP(String cid) {
    	if(cid == null || "".equals(cid))
    		return null;
    	return REDIS_CLUB_CLUBMAP.concat(cid).concat(splitStr);
	}
    /*
     * 俱乐部未开房间信息 key:cid+clubid ---  value: roomid
     */
    public static String get_REDIS_CLUB_ROOM_LIST(String cid) {
    	if(cid == null || "".equals(cid))
    		return null;
    	return REDIS_CLUB_ROOM_LIST.concat(cid).concat(splitStr);
    }
    /*
     * 玩家信息 key:cid userId,clubId,时间 ---  value:roomid实体
     */
    public static String get_REDIS_CLUB_PLAY_RECORD_PREFIX_ROE_USER(String cid) {
    	if(cid == null || "".equals(cid))
    		return null;
    	return REDIS_CLUB_PLAY_RECORD_PREFIX_ROE_USER.concat(cid).concat(splitStr);
    }
    /*
     * 玩家房间信息 key:
     */
    public static String get_REDIS_CLUB_PLAY_RECORD_PREFIX(String cid) {
    	if(cid == null || "".equals(cid))
    		return null;
    	return REDIS_CLUB_PLAY_RECORD_PREFIX.concat(cid).concat(splitStr);
    }
    /*
     * 俱乐部活跃人数   
     */
    public static String get_REDIS_CLUB_ACTIVE_NUM(String cid) {
    	if(cid == null || "".equals(cid))
    		return null;
    	return REDIS_CLUB_ACTIVE_NUM.concat(cid).concat(splitStr);
    }
    
    /*
     * 俱乐部今天开局数
     */
    public static String get_REDIS_CLUB_TODAYKAI_NUM(String cid) {
    	if(cid == null || "".equals(cid))
    		return null;
    	return REDIS_CLUB_TODAYKAI_NUM.concat(cid).concat(splitStr);
    }
    
    
    /*
     * 俱乐部玩家今日局数
     */
    public static String getREDIS_CLUB_TODAYJUNUM_ROE_USER(String cid) {
    	if(cid == null || "".equals(cid))
    		return null;
    	return REDIS_CLUB_TODAYJUNUM_ROE_USER.concat(cid).concat(splitStr);
    }
    public static String get_REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI.concat(cId).concat(splitStr);
    }
    public static String get_REDIS_PLAY_RECORD_PREFIX_ROE_USER(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PLAY_RECORD_PREFIX_ROE_USER.concat(cId).concat(splitStr);
    }
    
    public static String get_REDIS_PLAY_RECORD_PREFIX(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PLAY_RECORD_PREFIX.concat(cId).concat(splitStr);
    }
    
    public static String get_ROOM_DAIKAI_KEY(String cId){
		if(cId == null || "".equals(cId))
    		return null;
		return REDIS_PREFIX_DAI_ROOM_LIST.concat(cId).concat(splitStr);
	}
    public static String get_REDIS_HEART_PREFIX(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_HEART_PREFIX.concat(cId);
    }
    public static String get_REDIS_ONLINE_NUM_COUNT(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_ONLINE_NUM_COUNT.concat(cId).concat(splitStr);
    }
    public static String get_NOTICE_KEY(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return NOTICE_KEY.concat(splitStr).concat(cId);
    }
    public static String get_REDIS_PREFIX_USER_ID_USER_MAP(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PREFIX_USER_ID_USER_MAP.concat(cId).concat(splitStr);
    }
    public static String get_REDIS_PREFIX_OPENIDUSERMAP(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	
    	return REDIS_PREFIX_OPENIDUSERMAP.concat(cId).concat(splitStr);
    }
    public static String get_REDIS_PREFIX_ROOMMAP(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PREFIX_ROOMMAP.concat(cId).concat(splitStr);
    }

	public static String getREDIS_CLUB_TODAYSCORE_ROE_USER(String cId) {
		if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_CLUB_TODAYSCORE_ROE_USER.concat(cId).concat(splitStr);
	}

	public static String getREDIS_PREFIX_DAI_ROOM_LIST(String cId) {
		if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PREFIX_DAI_ROOM_LIST.concat(cId).concat(splitStr);
	}
 
    


}
