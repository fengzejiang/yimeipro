package com.yimei.utils;

import android.content.SharedPreferences;
import com.yimei.activity.R;
import com.yimei.activity.deviceproduction.AddGluingActivity;
import com.yimei.activity.deviceproduction.CommonStationActivity;
import com.yimei.activity.deviceproduction.UnbindingBoxActivity;
import com.yimei.activity.qc.QCCommActivity;

import java.util.HashMap;

public class CommCL {

    public static final String URi="http://192.168.1.4:9999/jd/"; //服务器项目地址
    public static final String API="api";//接口名称

    public static final String DB_ID = "mes";//数据库连接id

    public static final String SHOW_VERSION = "v18-10-25 15:00";
    public static final String IP = "IP:"+URi.substring(7,URi.lastIndexOf(":"));

    public static final String INTENT_ACTION_SCAN_RESULT = "com.android.server.scannerservice.broadcast"; // 广播接收Action值
    public static final String SCN_CUST_EX_SCODE = "scannerdata";
    public static final String SCN_CUST_HONEY = "data";
    public static final int EQUIPMENT_STATE_OK = 0;//设备状态OK，如果是1设备处于维修中
    public static final int BOX_STATE_WORKING = 1;//料盒在使用中的状态值
    public static final int HOLD = 1;//批次被HOLD
    public static final int BOK = 1;//批次可开工
    public static final String COMM_OLD_STATE_FLD = "oldstate";//旧状态
    public static final String COMM_NEW_STATE_FLD = "newstate";//新状态
    public static final String COMM_RECORD_SID_FLD = "sid";//生产记录单号，或者
    public static final String COMM_OP_FLD = "op";//操作员字段
    public static final String COMM_SBID_FLD = "sbid" ;


    public static final String COMM_CHECK_FIRST = "01"; //首件检验
    public static final String COMM_CHECK_ROUNT = "02"; //巡检检验类别




    public static final String PARAM_VALUE_API_ID_CHECK_UP = "chkup";//数据审核提交ID
    public static final String PARAM_CHK_ID_FLD = "chkid";
    public static final String PARAM_CEA_FLD = "cea";
    public static final int COMM_CHK_LIST = 33;//获取审批列表信息
    public static final int COMM_CHK_DO = 34;//执行审批
    public static final String PARAMS_MES_SB_ID_FLD = "sbid";
    public static final String PARAMS_MES_PRT_NO_FLD = "prtno";


    public static HashMap<String,Integer> menuImgMap;
    public static HashMap<String,Class> menuActivitys;


    public static final String RTN_ID="id";//服务端返回成功失败字段，0（调用成功）,1（可能是失败，具体调用查看），-1（可能是服务器错误）
    public static final String RTN_MESSAGE = "message";//调用服务以后，服务端返回的信息
    public static final String RTN_CODE="code";//返回查询内容中的是否有值的字段,0（调用成功，但是没有字段）,1(有数据)
    public static final String RTN_DATA="data";//调用服务以后返回的数据对象字段
    public static final String RTN_VALUES="values";//调用服务以后，返回的数据对象字段（list）



    //访问参数常量
    public static final String PARAMS_APIID = "apiId";//服务器用于区分提供何种服务字段
    public static final String PARAMS_DBID = "dbid";//用于连接数据库id字段（约定）
    public static final String PARAMS_USRCODE = "usercode";//用户登录账号字段（约定）
    public static final String PARAMS_PASSWORD = "pwd";//用户登录密码字段（约定）
    public static final String PARAMS_DATA_TYPE = "datatype";//服务器用于获取保存数据类型的字段（约定）
    public static final String PARAMS_JSON_STR = "jsonstr";//保存数据的字符串字段
    public static final String PARAMS_JSON_DATA = "jsondata";//用于调用mesudp存放list数据字段（约定）
    public static final String PARAMS_P_CELL= "pcell";//服务器获取对象定义的字段（约定）
    public static final String PARAMS_MES_UPD_ID_FLD= "id";//服务器获取对象定义的字段（约定）
    public static final String PARAM_ASSIST_FLD="assistid";//调用辅助服务中用于获取辅助定义中的标志字段

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

    //系统ApiId
    public static final String COMM_MES_UDP_UNBIND_VALUE = "650";//用于料盒号绑定和解绑
    public static final String COMM_MES_UDP_CHANGE_STATE_RECORD_VALUE = "290";//用于批次修改生产记录状态ID，系统自动调用280
    public static final String COMM_MES_UDP_CHANGE_STATE_LOT_VALUE = "280";//用于批次修改创批状态ID
    public static final String COMM_MES_UDP_GLUING_VALUE = "300";//用于设备添加胶杯ID
    public static final String COMM_MES_UDP_XG_VALUE = "410";//用于设备添加锡膏ID


    public static final String COMM_BIND_ON_BIND = "1";//料盒号绑定标志值
    public static final String COMM_BIND_UN_BIND = "0";//料盒号解绑标志值



    public static final String AID_M_PROCESS_QUERY = "{M_PROCESSNEW}";
    public static final String AID_ZC_QCREASON = "{CAUSEDQ}";//检验发起原因
    public static final String AID_PROCESS_ID = "{PROCESSAQ}";//制程检验项目辅助ID
    public static final String AID_BOX_QUERY="{MBOXQUERY}";//料盒号辅助ID
    public static final String AID_QJ_BOX_QUERY="{QJBOXWEB}";//器件批次解绑辅助ID
    public static final String AID_QJ_EQUIPMENT_QUERY="{EQUIPMENT}";//设备辅助ID
    public static final String AID_QJ_PRO_RECORD_QUERY="{MSBMOLIST}";//生产记录查询辅助ID
    public static final String AID_QJ_BATCH_RECORD_QUERY="{QJBOXWEB}";//生产批次查询辅助ID
    public static final String AID_All_OP = "{MESOPWEB}";//业务员ID
    public static final String AID_P_RECORD_GLUING_ID = "{MSBMOLIST_JJ}";//(加胶查询)设备任务列表ID
    public static final String AID_MATERIAL_LIST = "{MSMLLIST}";//材料明细表
    public static final String AID_MATERIAL_RECORD_LIST = "{MSMRECORDA}";//上料明细表
    public static final String AID_MAIN_MATERIAL_SID = "{MAINMR}";//上料主记录辅助ID
    public static final String AID_QC_BAT_INFO_QD = "{QCBATCHINFO}";//QC获取批次信息，前段
    public static final String AID_QC_BAT_INFO_HD = "{QCLOTINFO}";//QC获取批次信息，后段
    public static final String AID_GLUING_INFO_ID = "{MESGLUEJOBN}";//获取加胶信息辅助



    public static final String SAVE_DATA_STATE = "sys_stated";//数据状态

    public static final String CELL_ID_D0040WEB = "D0040WEB(D0040AWEB)";//检验项目
    public static final String CELL_ID_Q00101 = "Q00101(Q00101A)";//QC检验保存CellID
    public static final String CELL_ID_D2010 = "D2010";//加胶对象ID
    public static final String CELL_ID_D0090WEB = "D0090WEB";//解绑对象ID
    public static final String CELL_ID_D0001WEB = "D0001WEB";//生产记录对象ID


    public static final String BATCH_STATUS_READY = "00";//批次生产状态 --准备中
    public static final String BATCH_STATUS_IN = "01";//批次生产状态 -- 入站状态
    public static final String BATCH_STATUS_CHARGING= "02";//批次生产状态-- 上料
    public static final String BATCH_STATUS_WORKING = "03";//批次生产状态-- 生产中
    public static final String BATCH_STATUS_DONE = "04";//批次生产状态 --完工
    public static final String BATCH_STATUS_CHECKING = "07";//批次生产状态 -- 待检
    public static final String BATCH_STATUS_ABNORMAL = "0A";//批次生产状态 --异常
    public static final String BATCH_STATUS_PAUSE = "0B";//批次生产状态 -- 暂停生产
    public static final String BATCH_STATUS_STOP= "0C";//批次生产状态 -- 终止生产
    public static final String BATCH_STATUS_CONTROLLED= "0D";//批次生产状态 -- 生产受控


    public static final String COMM_NAME_FLD = "name";//通用名称字段
    public static final String COMM_ZC_ATTR_FLD = "attr";//制成属性
    public static SharedPreferences sharedPreferences;//全局缓存对象

    public final static String OPCaCheDir="OPCache";//缓存文件名称

    public static HashMap<String,String> STATEMap =  new HashMap<>();
    public static HashMap<String,String> STATEColorMap =  new HashMap<>();


    //制成属性
    public static final int ZC_ATTR_CHARGING = 1;//上料
    public static final int ZC_ATTR_START = 2;//开工
    public static final int ZC_ATTR_DONE = 4;//完工，出站
    public static final int ZC_ATTR_GLUING = 8;//加胶


    //上料校验类型
    public static final int CHECK_TYPE_MATERIAL = 0;//料号校验
    public static final int CHECK_TYPE_MATERIAL_LOT = 1;//料号+批次号校验
    public static final int CHECK_TYPE_MATERIAL_BIN = 2;//料号+BINCode校验

    public static final HashMap<String,String> houDuan = new HashMap<>();

    static {
        initMenuImgMap();
//        {00:准备;01:已入站;02:已上料;03:生产中;04:已出站;07:待检;08:已检;0A:异常;0B:暂停;0C:中止;0D:受控}
        STATEMap.put("00","准备");
        STATEMap.put("01","已入站");
        STATEMap.put("02","已上料");
        STATEMap.put("03","生产中");
        STATEMap.put("04","已出站");
        STATEMap.put("07","待检");
        STATEMap.put("08","已检");
        STATEMap.put("0A","异常");
        STATEMap.put("0B","暂停");
        STATEMap.put("0C","中止");
        STATEMap.put("0D","受控");
        STATEColorMap.put("00","#f0bd0a");
        STATEColorMap.put("01","#f0bd0a");
        STATEColorMap.put("02","#9c24ce");
        STATEColorMap.put("03","#1aa1e7");
        STATEColorMap.put("04","#1ac16c");
        STATEColorMap.put("07","#cf1f27");
        STATEColorMap.put("08","#cf1f27");
        STATEColorMap.put("0A","#ee3030");
        STATEColorMap.put("0B","#ee3030");
        STATEColorMap.put("0C","#ee3030");
        STATEColorMap.put("0D","#ee3030");

        houDuan.put("61","61");
        houDuan.put("71","71");
        houDuan.put("81","81");
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
    private static void initMenuImgMap(){
        menuImgMap = new HashMap<>();
        menuActivitys = new HashMap<>();
        if(menuImgMap.size()>0)
            return ;
        menuImgMap.put("D0001", R.drawable.zhandian);//通用工站
        menuActivitys.put("D0001", CommonStationActivity.class);
//        menuImgMap.put("D0002",R.drawable.mozu);//模组
        menuImgMap.put("D0020", R.drawable.biandai);//边带管理
        menuImgMap.put("D0030", R.drawable.zhuanchu);//器件转出登记
        menuImgMap.put("D0031", R.drawable.jieshou);//器件接收登记
        menuImgMap.put("D0050", R.drawable.mbox);//料盒解绑
        menuActivitys.put("D0050", UnbindingBoxActivity.class);
        menuImgMap.put("D0097", R.drawable.order);//绑定料盒
        menuImgMap.put("D2009", R.drawable.hunjiao);//混胶作业
        menuImgMap.put("D2010", R.drawable.jiajiao);//加胶登记
        menuActivitys.put("D2010", AddGluingActivity.class);
        menuImgMap.put("D5001", R.drawable.mozu);//模组通用
        menuImgMap.put("D5030", R.drawable.jieshou);//接收
        menuImgMap.put("D6004", R.drawable.jiaxigao);//添加锡膏

        menuImgMap.put("E0001", R.drawable.scfl);//生产发料
        menuImgMap.put("E0004", R.drawable.shengcanruku);//生产入库登记
        menuImgMap.put("E5004", R.drawable.zhijuruku);//治具入库
        menuImgMap.put("E5005", R.drawable.zhijulingchu);//治具领出
        menuImgMap.put("E5006", R.drawable.zhijuqingxi);//治具清洗
        menuImgMap.put("E6001", R.drawable.fix);//设备维修申请


        menuImgMap.put("H0003", R.drawable.zhuangxiang);//装箱作业

        menuImgMap.put("K0", R.drawable.ort);//ORT抽样

        menuImgMap.put("Q0", R.drawable.pinzhiguanli);//品质管理
        menuActivitys.put("Q0", QCCommActivity.class);

    }

}
