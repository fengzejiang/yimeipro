package com.yimei.modelInterface;

import android.content.Context;
import com.yimei.data.CWorkInfo;
import com.yimei.data.CeaPars;

/**
 * 界面组件基础接口
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/7 12:17
 */
public interface BaseView {
    /***
     * 显示Loading界面
     */
    void showLoading();

    /***
     * 隐藏Loading界面
     */
    void hideLoading();

    /***
     * 调用远程失败
     * @param message 错误消息
     */
    void onRemoteFailed(String message);

    /***
     * * 调用远程成功
     * @param message 消息内容
     */
    void showMessage(String message);

    /***
     * 跳转页面
     */
    void jumpNextActivity( Class<?> descClass);
    void jumpNextActivity(Context srcContent, Class<?> descClass);

    /***
     * 添加数据到UI
     * @param unBindInfo 数据对象
     */
    void addRow(Object unBindInfo);

    /***执行或者调用审批信息返回
     *
     * @param bok 是否成功
     * @param key 调用的是33还是34,33：获取审批信息，34执行审批信息
     * @param ceaPars 交互过程中的状态
     * @param cWorkInfo 审批流信息
     * @param error 错误信息
     */
    void checkActionBack(boolean bok, int key, CeaPars ceaPars, CWorkInfo cWorkInfo, String error);
}
