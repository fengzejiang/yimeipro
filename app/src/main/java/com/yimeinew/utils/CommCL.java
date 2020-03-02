package com.yimeinew.utils;

import android.content.SharedPreferences;
import com.yimeinew.activity.R;
import com.yimeinew.activity.ck.ScrkActivity;
import com.yimeinew.activity.deviceproduction.*;
import com.yimeinew.activity.deviceproduction.commsub.OutReceiveActivity;
import com.yimeinew.activity.pack.PackStationActivity;
import com.yimeinew.activity.qc.QCCommActivity;
import com.yimeinew.activity.qc.QCCommStationActivity;
import com.yimeinew.activity.sb.WxQcqrActivity;
import com.yimeinew.activity.sb.WxScqrActivity;
import com.yimeinew.activity.sb.WxqrActivity;
import com.yimeinew.activity.sb.WxsqActivity;
import com.yimeinew.entity.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class CommCL {

    //    public static final String URi = "http://192.168.0.179:9999/jd/"; //服务器项目地址
    //    public static final String DB_ID = "mes";//数据库连接id
    public static  String URi = "http://59.53.182.251:8088/mes/"; //服务器项目地址
    public static  String NURi = "http://192.168.7.15:8088/mes/";
    public static  String WURi = "http://59.53.182.251:8088/mes/";
    public static final String DB_ID = "01";//数据库连接id
    public static final String URi_KEY="URiKeyToIp";
    public static final long ALLOW_REPEAT_TIME=1000*3;//应许重复的时间阈值
    public static final long ALLOW_CANDO_TIME=1000*8;//应许再次执行的时间阈值
    public static final boolean isDev = true;//初始化登录账户密码.(发布是为false)
    public static final boolean isTest = false;//true的时候跳过时间控制。false是有时间控制的(发布是为false)
    public static final String API = "api";//接口名称
    public static String WIFIMAC="PDA";//当获取不到PDA设备号的时候使用这个字段

//    public static final String URi="http://192.168.7.15:8088/mes/"; //服务器项目地址
//    public static final String DB_ID = "01";//数据库连接id

    public static final String SHOW_VERSION = "V20-02-25 09:50";
    public static  String IP = "IP:" + URi.substring(7, URi.lastIndexOf(":"));
    public static final String APK_URL=URi+"shineon.apk";
    public static final String UP_APK_KEY="adminadmin";
    public static final String INTENT_ACTION_SCAN_RESULT = "com.android.server.scannerservice.broadcast"; // 广播接收Action值
    public static final String INTENT_ACTION_UROVO_SCAN_RESULT = "android.intent.ACTION_DECODE_DATA";
    public static final String SCN_CUST_EX_SCODE = "scannerdata";
    public static final String SCN_CUST_UROVO = "barcode_string";
    public static final String SCN_CUST_HONEY = "data";
    public static final String API_SAVE_STATUS="3";
    public static final String API_UPDATE_STATUS="2";
    public static final int EQUIPMENT_STATE_OK = 0;//设备状态OK，如果是1设备处于维修中
    public static final int BOX_STATE_WORKING = 1;//料盒在使用中的状态值
    public static final int HOLD = 1;//批次被HOLD
    public static final int BOK = 1;//批次可开工
    public static final String COMM_OLD_STATE_FLD = "oldstate";//旧状态
    public static final String COMM_NEW_STATE_FLD = "newstate";//新状态
    public static final String COMM_RECORD_SID_FLD = "sid";//生产记录单号，或者
    public static final String COMM_OP_FLD = "op";//操作员字段
    public static final String COMM_SBID_FLD = "sbid";
    public static final String COMM_SLKID_FLD = "slkid";
    public static final String COMM_LOTNO_FLD = "lotno";

    public static final String COMM_CHECK_FIRST = "01"; //首件检验
    public static final String COMM_CHECK_ROUNT = "02"; //巡检检验类别


    public static final String PARAM_VALUE_API_ID_CHECK_UP = "chkup";//数据审核提交ID
    public static final String PARAM_CHK_ID_FLD = "chkid";
    public static final String PARAM_CEA_FLD = "cea";
    public static final int COMM_CHK_LIST = 33;//获取审批列表信息
    public static final int COMM_CHK_DO = 34;//执行审批
    public static final String PARAMS_MES_SB_ID_FLD = "sbid";
    public static final String PARAMS_MES_PRT_NO_FLD = "prtno";


    public static HashMap<String, Integer> menuImgMap;
    public static HashMap<String, Class> menuActivitys;


    public static final String RTN_ID = "id";//服务端返回成功失败字段，0（调用成功）,1（可能是失败，具体调用查看），-1（可能是服务器错误）
    public static final String RTN_MESSAGE = "message";//调用服务以后，服务端返回的信息
    public static final String RTN_CODE = "code";//返回查询内容中的是否有值的字段,0（调用成功，但是没有字段）,1(有数据)
    public static final String RTN_DATA = "data";//调用服务以后返回的数据对象字段
    public static final String RTN_VALUES = "values";//调用服务以后，返回的数据对象字段（list）


    //访问参数常量
    public static final String PARAMS_APIID = "apiId";//服务器用于区分提供何种服务字段
    public static final String PARAMS_DBID = "dbid";//用于连接数据库id字段（约定）
    public static final String PARAMS_USRCODE = "usercode";//用户登录账号字段（约定）
    public static final String PARAMS_PASSWORD = "pwd";//用户登录密码字段（约定）
    public static final String PARAMS_DATA_TYPE = "datatype";//服务器用于获取保存数据类型的字段（约定）
    public static final String PARAMS_JSON_STR = "jsonstr";//保存数据的字符串字段
    public static final String PARAMS_JSON_DATA = "jsondata";//用于调用mesudp存放list数据字段（约定）
    public static final String PARAMS_P_CELL = "pcell";//服务器获取对象定义的字段（约定）
    public static final String PARAMS_MES_UPD_ID_FLD = "id";//服务器获取对象定义的字段（约定）
    public static final String PARAM_ASSIST_FLD = "assistid";//调用辅助服务中用于获取辅助定义中的标志字段

    public static final String PARAM_VALUE_API_ID_LOGIN = "login";//调用登录服务
    public static final String PARAM_VALUE_API_ID_ASSIST = "assist1";//调用辅助服务
    public static final String PARAM_VALUE_API_ID_SAVE = "savedata";//调用保存数据服务
    public static final String PARAM_VALUE_API_ID_MES_UDP = "mesudp";//调用保存数据服务
    public static final String PARAM_VALUE_API_ID_SAVE_DATA_TYPE_JSON = "1";//JSON格式提交数据
    public static final String PARAM_CONT_FLD = "cont";//辅助查询设置条件字段
    public static final String PARAM_SIZE_FLD = "size";//辅助查询设置每页返回条数字段
    public static final String COMM_ZC_NO_FLD = "zcno";//通用制成号字段
    public static final String COMM_ZC_INFO_FLD = "ZC_INFO";//页面跳转传入的制成信息
    public static final String COMM_RECORD_FLD = "RECORD";//页面跳转传入的生产记录信息
    public static final String COMM_ZC_NO_FLD1 = "zcno1";//通用下一制成号字段
    public static final String COMM_M_BOX_FLD = "mbox";//通用料盒号字段
    public static final String COMM_BIND_FLD = "bind";//通用料盒号解绑和绑定字段
    public static final String COMM_SID1_FLD = "sid1";//通用批次号字段（用于前段批次）
    public static final String COMM_QTY_FLD = "qty";//通用数量字段
    public static final String COMM_ABNORMAL_FLD="abnormal";//通用生产记录表的异常字段
    public static final String COMM_ABNORMALOP_FLD="abnormalop";//通用生产记录表的异常字段
    public static final String COMM_DCID_FLD="dcid";//通用生产记录表的手持设备MAC
    //系统ApiId
    public static final String COMM_MES_UDP_UNBIND_VALUE = "650";//用于料盒号绑定和解绑
    public static final String COMM_MES_UDP_MBOXZJ_VALUE = "655";//用于料盒号绑定支架的绑定
    public static final String COMM_MES_UDP_CHANGE_STATE_RECORD_VALUE = "290";//用于批次修改生产记录状态ID，系统自动调用280
    public static final String COMM_MES_UDP_CHANGE_STATE_TESTLOT_VALUE = "280";//用于修改创批状态ID
    public static final String COMM_MES_UDP_CHANGE_STATE_QUICK_VALUE = "200";//用于修改快速过站ID
    public static final String COMM_MES_UDP_GLUING_VALUE = "300";//用于设备添加胶杯ID
    public static final String COMM_MES_UDP_XG_VALUE = "410";//用于设备添加锡膏ID


    public static final String COMM_BIND_ON_BIND = "1";//料盒号绑定标志值
    public static final String COMM_BIND_UN_BIND = "0";//料盒号解绑标志值


    public static final String AID_M_PROCESS_QUERY = "{M_PROCESSNEW}";
    public static final String AID_ZC_QCREASON = "{CAUSEDQ}";//检验发起原因
    //设备维修确认
    public static final String AID_SBWXQR="{SBWXQR}";
    public static final String AID_SBWXQR2="{SBWXQR2}";
    public static final String AID_SBWXQR3="{SBWXQR3}";
    public static final String AID_SBWXYY="{SBWXYY}";//设备维修原因
    public static final String AID_WXGHPJ="{WXGHPJ}";
    public static final String AID_PROCESS_ID = "{PROCESSAQ}";//制程检验项目辅助ID
    public static final String AID_BOX_QUERY = "{MBOXQUERY}";//料盒号辅助ID
    public static final String AID_QJ_BOX_QUERY = "{QJBOXWEB2}";//器件批次解绑辅助ID  原来是QJBOXWEB
    public static final String AID_QJ_EQUIPMENT_QUERY = "{EQUIPMENT}";//设备辅助ID
    public static final String AID_QJ_EQUIPMENT= "{EQUIPMENT}";//设备辅助ID
    public static final String AID_QJ_PRO_RECORD_QUERY = "{MSBMOLIST}";//生产记录查询辅助ID
    public static final String AID_QJ_PRO_RECORD_QUERY_BD = "{BDMSBMOLIST}";//编带生产记录查询辅助ID
    public static final String AID_QJ_BATCH_RECORD_QUERY = "{QJBOXWEB2}";//生产批次查询辅助ID
    public static final String AID_QJ_LOT_PRECORD_QUERY = "{TESTLOTQUERY}";//生产批次查询辅助ID
    public static final String AID_QJ_JIAOSHUI_XIAOQI_QUERY = "{Q_GRECORD}";//器件胶水效期查询辅助ID
    public static final String AID_All_OP = "{MESOPWEB}";//业务员ID
    public static final String AID_P_RECORD_GLUING_ID = "{MSBMOLIST_JJ}";//(加胶查询)设备任务列表ID
    public static final String AID_MATERIAL_LIST = "{MSMLLIST}";//材料明细表
    public static final String AID_MATERIAL_RECORD_LIST = "{MSMRECORDA}";//上料明细表
    public static final String AID_MATERIAL_RECORD_MAX = "{QJSLMAX}";//上料明细表

    public static final String AID_MAIN_MATERIAL_SID = "{MAINMR}";//上料主记录辅助ID
    public static final String AID_QC_BAT_INFO_QD = "{QCBATCHINFO}";//QC获取批次信息，前段
    public static final String AID_QC_BAT_INFO_HD = "{QCLOTINFO}";//QC获取批次信息，后段
    public static final String AID_OQC_BAT_INFO_HD="{OQCLOTINFO}";//OQC获取批次信息,仓库
    public static final String AID_OQC_INFO_HD="{OQCINFO}";//出货检验已扫入信息获取
    public static final String AID_GLUING_INFO_ID = "{MESGLUEJOBN}";//获取加胶信息辅助
    public static final String AID_ERP_PRDT="{PRDNO}";//获取ERP货品信息
    public static final String AID_QUICK_LOT="{MOZCLISTWEB}";//快速过站获取LOT表信息
    public static final String AID_QUICK_LOT2="{MOZCLISTWEB2}";//二次快速过站获取LOT表信息
    public static final String AID_CK_MM="MESTMM0";//生产入库检验查询
    public static final String SAVE_DATA_STATE = "sys_stated";//数据状态2表示更新、3表示新增
    public static final String AID_ENSB_GLUE_INFO_ID = "{Q_GRECORD2}";//获取加胶机台的胶水信息
    public static final String AID_GJ_WAFER_QTY="{GJWAFERQTY}";//获取固晶上晶片是否上够
    public static final String AID_MATERIAL_USE="{MATERIALUSE}";//获取材料批次使用次数
    public static final String AID_PACK_TRAY_MO="{PACK_TRAY_MO}";//按Tray获取Tray和工单信息
    public static final String AID_PACK_MARKING="{PACK_MARKING}";//按喷码获取信息
    public static final String AID_PACK_Get_INFO="{PACK_GETINFO}";//按主键获取包装信息
    public static final String AID_PACK_ALREADY="{PACK_ALREADY}";//按主键获取包装信息

	public static final String AID_MZ_KB="{MZKBGZ}";//快速过站获取LOT表信息
    public static final String AID_WXQR="{WXQR}";//维修确认
    public static final String AID_BLYY="{BLYY}";//不良原因
    public static final String AID_BLPSX="{BLPSX}";//不良送修
    public static final String AID_BLPSX2="{BLPSX2}";//不良送修
    public static final String AID_MZWX_ZCNO="{MZWX_ZCNO}";//不良送修制程
    public static final String AID_WEB_INSSYSCL="{WEB_INSSYSCL}";//取常量定义数据

    public static final String AID_XDK="{XDK1}";//下单颗
    public static final String AID_MES_BOX_PRD_SORT="{BOX_PRD_SORT}";//器件转出接收查询
	public static final String AID_MES_OUT_IN="{MESOUTINWEB}";//器件转出接收查询
	public static final String AID_TEST_LOT_WEB="{TESTLOTWEB}";//器件看带查询
    public static final String AID_BAD_RECODE="{BAD_RECODE}";//看带不良登记查询
    public static final String AID_TMM0_WEB="{TMM0_WEB}";//正常入库缴库单查询
    public static final String AID_BXSL="{BXSL}";//报修数量
    public static final String AID_OKSL="{OKSL}";//OK数量
    public static final String AID_NGSL="{NGSL}";//NG数量

    public static final String AID_BXSL2="{BXSL2}";//品质报修数量
    public static final String AID_OKSL2="{OKSL2}";//品质OK数量
    public static final String AID_NGSL2="{NGSL2}";//品质NG数量



    public static final String CELL_ID_D0040WEB = "D0040WEB(D0040AWEB)";//检验项目
    public static final String CELL_ID_Q00101 = "Q00101(Q00101A)";//QC检验保存CellID
    public static final String CELL_ID_D2010 = "D2010";//加胶对象ID
    public static final String CELL_ID_D0090WEB = "D0090WEB";//解绑对象ID
    public static final String CELL_ID_D0001WEB = "D0001WEB";//生产记录对象ID
    public static final String CELL_ID_D0071 = "D0071";//生产记录对象ID快速过站
    public static final String CELL_ID_D0073W = "D0073W";//生产记录对象ID二次快速过站
    public static final String CELL_ID_D0092A="D0092A";//料盒绑支架主表
    public static final String CELL_ID_D0092B="D0092C";//料盒绑支架子表
    public static final String LOT_QJ_BIANDAI_LOTQTY_XIUGAI="D0070PDA";//TestLot数量修改语句
    public static final String LOT_QJ_BIANDAI_QTY_XIUGAI="D0020PDA";//编带数量修改mes_precord对象定义
    public static final String LOT_QJ_BIANDAI_QTY_XIUGAI_JILU="D0071PDA";//编带数量修改记录表
    public static final String CELL_ID_D300101WEBAB="D300101WEBAB";//生产记录表更新异常标识的对象定义
    public static final String CELL_ID_D0074W="D0074W";//抛出异常登记表
    public static final String CELL_ID_Q00113A="Q00113AWEB";//出货检验主表
    public static final String CELL_ID_Q00113B="Q00113BWEB";//出货检验子表
    public static final String CELL_ID_F0028="F0028";//模组内装扫喷码表头
    public static final String CELL_ID_F0028WEB="F0028WEB";//模组内装扫喷码表头更新已包装数
    public static final String CELL_ID_F0028AWEB="F0028AWEB";//模组内装扫喷码表身
    public static final String CELL_ID_D5010AWEB="D5010AWEB";//内装更新喷码状态和时间
	public static final String CELL_ID_D50309 = "D50309";//模组卡板过站
    public static final String CELL_ID_D50309WEB = "D50309WEB";
    public static final String CELL_ID_D5080= "D5080";//维修确认
    public static final String CELL_ID_D5080WEB= "D5080WEB";//生产维修确认
    public static final String CELL_ID_D5084WEB= "D5084WEB";//品质维修确认
    public static final String CELL_ID_D5064= "D5064";//不良品送修
    public static final String CELL_ID_D5064WEEB= "D5064WEB";//不良品送修
    public static final String CELL_ID_D5064A= "D5064AWEBC";//不良品送修
    public static final String CELL_ID_D5010WEBC= "D5010WEBC";
	public static final String CELL_ID_D0030WEB ="D0030WEB";//转出对象定义
    public static final String CELL_ID_D0031WEB ="D0031WEB";//接收对象定义
    public static final String CELL_ID_B0055WEB1 ="B0055WEB1";//料号绑支架批次更新料号基础表
    public static final String CELL_ID_B0055WEB2 ="B0055WEB2";//料号绑设备程式更新料号基础表
    public static final String CELL_ID_D009BA ="D009BA";//料号绑设备程式保存
    public static final String CELL_ID_D009BBWEB ="D009BBWEB";//料号绑设备程式保存
    public static final String CELL_ID_D0071A ="D0071A";//看带不良登记
    public static final String CELL_ID_E6001="E6001";//设备维修申请
    public static final String CELL_ID_E6002A="E6002A";//设备维修确认-开始维修
    public static final String CELL_ID_E6002B="E6002B";//设备维修确认-结束维修
    public static final String CELL_ID_E6002C="E6002C";//设备维修确认-更换配件
    public static final String CELL_ID_E6003="E6003";//设备维修生产确认
    public static final String CELL_ID_B0003B="B0003B";//设备维修申请
    public static final String CELL_ID_E0004AWEB="E0004AWEB";//入库校验
    public static final String CELL_ID_E0004WEB="E0004WEB";//入库更新收料人



    public static final String BATCH_STATUS_READY = "00";//批次生产状态 --准备中
    public static final String BATCH_STATUS_IN = "01";//批次生产状态 -- 入站状态
    public static final String BATCH_STATUS_CHARGING = "02";//批次生产状态-- 上料
    public static final String BATCH_STATUS_WORKING = "03";//批次生产状态-- 生产中
    public static final String BATCH_STATUS_DONE = "04";//批次生产状态 --完工
    public static final String BATCH_STATUS_CHECKING = "07";//批次生产状态 -- 待检
    public static final String BATCH_STATUS_ABNORMAL = "0A";//批次生产状态 --异常
    public static final String BATCH_STATUS_PAUSE = "0B";//批次生产状态 -- 暂停生产
    public static final String BATCH_STATUS_STOP = "0C";//批次生产状态 -- 终止生产
    public static final String BATCH_STATUS_CONTROLLED = "0D";//批次生产状态 -- 生产受控
    public static final String LOT_STATUS_TEST = "0";//测试号生产状态 -- 已测试
    public static final String LOT_STATUS_BIANDAI = "1";//测试号生产状态 -- 已编带
    public static final String LOT_STATUS_KANDAI = "2";//测试号生产状态 -- 已看带


    public static final String COMM_NAME_FLD = "name";//通用名称字段
    public static final String COMM_ZC_ATTR_FLD = "attr";//制成属性
    public static SharedPreferences sharedPreferences;//全局缓存对象
    public final static String OPCaCheDir = "OPCache";//缓存文件名称
    /**talbe state 辅助和color*/
    public static HashMap<String, String> STATEMap = new HashMap<>();
    public static HashMap<String, String> STATEColorMap = new HashMap<>();
    /**talbe 校验 辅助和color*/
    public static HashMap<String, String> CheckMap = new HashMap<>();
    public static HashMap<String, String> CheckColorMap = new HashMap<>();
	//设别维修状态
    public static HashMap<String, String> STATESbwx = new HashMap<>();


    //制成属性
    public static final int ZC_ATTR_CHARGING = 1;//上料
    public static final int ZC_ATTR_START = 2;//开工
    public static final int ZC_ATTR_DONE = 4;//完工，出站
    public static final int ZC_ATTR_GLUING = 8;//加胶

    //工序时间卡控设置（单位都是秒）
    public static final int HANXIAN_QINGXI_MAX_WAIT_TIME=6*60*60;//焊线清洗应许最大等待的时间6小时。
    public static final int DIANJIAO_YURE_MAX_WAIT_TIME=10*60*60;//点胶预热应许最大等待的时间10小时。
    public static final int DIANJIAO_HONGKAO_MIN_WAIT_TIME=1*60*60;//点胶烘烤应许最小等待的时间1小时。
    public static final int DIANJIAO_HONGKAO_MAX_WAIT_TIME=2*60*60;//点胶烘烤应许最大等待的时间2小时。
    public static final int DIANJIAO_VALIDITY_MAX_WAIT_TIME=80;//点胶胶水在机台过期时间80分钟。
    //上料校验类型
    public static final int CHECK_TYPE_MATERIAL = 0;//料号校验
    public static final int CHECK_TYPE_MATERIAL_LOT = 1;//料号+批次号校验
    public static final int CHECK_TYPE_MATERIAL_BIN = 2;//料号+BINCode校验

    public static final HashMap<String, String> houDuan = new HashMap<>();
    /*静态常量*/
    public static final String JUMP_KEY_MESPRecord="JUMP_KEY_MESPRecord";//跳转页面key 传递生产记录
    /*系统控制常量*/
    public static final int ADDGLUING_P_REPEAT_NUM=5;//固晶上料晶片可以重复的次数
    public static final int ADDGLUING_EXCEED_NUM=3;//固晶每个批次可以超过应发量三次。

    //点胶异常说明
    public static final ArrayList<Pair> DJYCSM=new ArrayList<>();
    static {
        DJYCSM.add(new Pair("---请选择异常类型---",""));
        DJYCSM.add(new Pair("排胶加粉","1"));
        DJYCSM.add(new Pair("配比异常","2"));
        initMenuImgMap();
//        {00:准备;01:已入站;02:已上料;03:生产中;04:已出站;07:待检;08:已检;0A:异常;0B:暂停;0C:中止;0D:受控}
        STATEMap.put("00", "准备");
        STATEMap.put("01", "已入站");
        STATEMap.put("02", "已上料");
        STATEMap.put("03", "生产中");
        STATEMap.put("04", "已出站");
        STATEMap.put("07", "待检");
        STATEMap.put("08", "已检");
        STATEMap.put("0A", "异常");
        STATEMap.put("0B", "暂停");
        STATEMap.put("0C", "中止");
        STATEMap.put("0D", "受控");

        STATEColorMap.put("00", "#f0bd0a");
        STATEColorMap.put("01", "#f0bd0a");
        STATEColorMap.put("02", "#9c24ce");
        STATEColorMap.put("03", "#1aa1e7");
        STATEColorMap.put("04", "#1ac16c");
        STATEColorMap.put("07", "#cf1f27");
        STATEColorMap.put("08", "#cf1f27");
        STATEColorMap.put("0A", "#ee3030");
        STATEColorMap.put("0B", "#ee3030");
        STATEColorMap.put("0C", "#ee3030");
        STATEColorMap.put("0D", "#ee3030");

        houDuan.put("61", "61");
        houDuan.put("71", "71");
        houDuan.put("81", "81");

        CheckMap.put("0", "");
        CheckMap.put("1", "V");
        CheckColorMap.put("0", "#ffffff");
        CheckColorMap.put("1", "#37e700");
		STATESbwx.put("0","待维修");
        STATESbwx.put("1","维修中");
        STATESbwx.put("2","待确认");
        STATESbwx.put("3","已确认");
    }

    /**
     * 初始化菜单对应的图片ID
     * D0001通用工站
     * D0020编带管理
     * D0030器件转出登记
     * D0031器件接收登记
     * D0050外观检验--->解绑料盒
     * D0097工单绑定料盒*
     * D009A清洗料盒
     * D2009混胶登记
     * D2010加胶登记
     * D5001模组通用工站*
     * D5030快速过站
     * D6004加锡膏登记
     * E0001生产领料
     * E0004生产入库
     * E5004治具入库
     * E5005治具领出
     * E5006治具清洗
     * E6001设备报修申请*
     * H0003装箱作业
     * K0 ORT
     * Q0品质管理
     */
    private static void initMenuImgMap() {
        menuImgMap = new HashMap<>();
        menuActivitys = new HashMap<>();
        if (menuImgMap.size() > 0)
            return;
        menuImgMap.put("D0001", R.drawable.zhandian);//通用工站
        menuActivitys.put("D0001", CommonStationActivity.class);
//        menuImgMap.put("D0002",R.drawable.mozu);//模组
        menuImgMap.put("D0020", R.drawable.biandai);//编带管理
        menuActivitys.put("D0020", BianDaiActivity.class);

        menuImgMap.put("D0030", R.drawable.zhuanchu);//器件转出登记
        menuActivitys.put("D0030", OutReceiveActivity.class);
        menuImgMap.put("D0031", R.drawable.jieshou);//器件接收登记
        menuActivitys.put("D0031", OutReceiveActivity.class);

        menuImgMap.put("D0050", R.drawable.mbox);//料盒解绑
        menuActivitys.put("D0050", UnbindingBoxActivity.class);
        menuImgMap.put("D0092", R.drawable.order);//支架绑定料盒在菜单定义里头有个外部系统填1时才能显示在手机端
        menuActivitys.put("D0092", MboxBindzjActivity.class);
        menuImgMap.put("D009B", R.drawable.order);//支架除湿
        menuActivitys.put("D009B", PreheatZjActivity.class);
        menuImgMap.put("D0097", R.drawable.order);
        menuImgMap.put("D2009", R.drawable.hunjiao);//混胶作业
        menuImgMap.put("D2010", R.drawable.jiajiao);//加胶登记
        menuActivitys.put("D2010", AddGluingActivity.class);
        menuImgMap.put("D5001", R.drawable.mozu);//模组通用

        menuImgMap.put("D5030", R.drawable.jieshou);//快速过站
        menuActivitys.put("D5030", FastStationActivity.class);//快速过站

        menuImgMap.put("D5040", R.drawable.jieshou);//2次快速过站
        menuActivitys.put("D5040", Fast2StationActivity.class);//快速过站

        menuImgMap.put("D6004", R.drawable.jiaxigao);//添加锡膏

        menuImgMap.put("E0001", R.drawable.scfl);//生产发料
        menuImgMap.put("E0004", R.drawable.shengcanruku);//生产入库登记
        menuActivitys.put("E0004", ScrkActivity.class);//生产入库登记

        menuImgMap.put("E5004", R.drawable.zhijuruku);//治具入库
        menuImgMap.put("E5005", R.drawable.zhijulingchu);//治具领出
        menuImgMap.put("E5006", R.drawable.zhijuqingxi);//治具清洗
        menuImgMap.put("E6001", R.drawable.fix);//设备维修申请
        menuActivitys.put("E6001", WxsqActivity.class);//设备维修申请
        menuImgMap.put("E6002", R.drawable.mozu);//设备维确认
        menuActivitys.put("E6002", WxqrActivity.class);//设备维修确认
        menuImgMap.put("E6003", R.drawable.zhuanchu);//设备维确认
        menuActivitys.put("E6003", WxScqrActivity.class);//设备维修s生产确认
        menuImgMap.put("E6005", R.drawable.mozu);//设备维确认
        menuActivitys.put("E6005", WxQcqrActivity.class);//设备维修s生产确认



        menuImgMap.put("D5060", R.drawable.scwxgl);//维修
        menuActivitys.put("D5060", RepairStationActivity.class);//维修

        menuImgMap.put("F0", R.drawable.scbz);//包装管理
        menuActivitys.put("F0", PackStationActivity.class);

        menuImgMap.put("H0003", R.drawable.zhuangxiang);//装箱作业

        menuImgMap.put("K0", R.drawable.ort);//ORT抽样

        menuImgMap.put("Q0", R.drawable.pinzhiguanli);//品质管理
        menuActivitys.put("Q0", QCCommStationActivity.class);


    }

}
