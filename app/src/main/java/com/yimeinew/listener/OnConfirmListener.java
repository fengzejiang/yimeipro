package com.yimeinew.listener;

import android.content.DialogInterface;

public abstract class OnConfirmListener {
    /**
     * 确定按钮
     */
    public abstract void OnConfirm(DialogInterface dialog);
    /**
     * 确定按钮
     */
    public abstract void OnCancel(DialogInterface dialog);
}
