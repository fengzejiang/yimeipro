package com.yimeinew.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.support.v4.app.ActivityCompat;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;


public class DownloadAPK extends AsyncTask<String, Integer, String> {

	ProgressDialog progressDialog;
	File file;
	Context mContext;
	public static boolean b;
	public DownloadAPK(ProgressDialog progressDialog,Context context) {
		this.progressDialog = progressDialog;
		mContext = context;
	}

	@Override
	protected String doInBackground(String... params) {
		// 根据url获取网络数据生成apk文件
		URL url;
		HttpURLConnection conn;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;

		try {
			url = new URL(params[0]);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);

			int fileLength = conn.getContentLength();
			bis = new BufferedInputStream(conn.getInputStream());
			//文件路径
			String fileName = Environment.getExternalStorageDirectory()
					.getPath() + "/yimeiDown/"+ System.currentTimeMillis()+"shineon.apk";
			file = new File(fileName);
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			fos = new FileOutputStream(file);
			byte data[] = new byte[8 * 1024];
			long total = 0;
			int count;
			while ((count = bis.read(data)) != -1) {
				total += count;
				publishProgress((int) (total * 100 / fileLength));
				fos.write(data, 0, count);
				fos.flush();
			}
			fos.flush();

		} catch (IOException e) {
			
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (bis != null) {
					bis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
	}

	@Override
	protected void onPostExecute(String s) {
		// 到这里说明下载完成，判断文件是否存在，如果存在，执行安装apk的操作
		  super.onPostExecute(s);
          openFile(file);                 //打开安装apk文件操作
          progressDialog.dismiss();
	}

	private void openFile(File file) {
		//android7安装
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
			StrictMode.setVmPolicy(builder.build());
		}
		//android8安装
		if (Build.VERSION.SDK_INT >= 26) {
			boolean hasInstallPermission = mContext.getPackageManager().canRequestPackageInstalls();
			if (!hasInstallPermission) {
				//请求安装未知应用来源的权限
				ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, 6666);
			}
		}
		if (file != null) {
			if (!file.exists()) {
				CommonUtils.showError(mContext,"存储权限没有获取成功，请到设置-》应用-》易美工具1.3-》权限");
			}else {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file),
						"application/vnd.android.package-archive");
				mContext.startActivity(intent);
			}
		}
	}




	public static ProgressDialog getProgressDialog(Context context){
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setTitle("提示");
		progressDialog.setMessage("正在下载...");
		progressDialog.setIndeterminate(false);
		progressDialog.setMax(100);
		progressDialog.setCancelable(false); // 设置不可点击界面之外的区域让对话框消失
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // 进度条类型
		return progressDialog;
	}

	/**
	 *
	 * @param context
	 */
	public static void checkVersion(Context context){
		if(CommonUtils.isRepeat("login_down_apk_key","login_down_apk")){//防止重复点击
			return;
		}
		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if(msg.what==2019002){
					ProgressDialog progressDialog = DownloadAPK.getProgressDialog(context);
					progressDialog.show();
					new DownloadAPK(progressDialog, context).execute(CommCL.APK_URL);
				}
			}
		};
		Thread thread= new Thread(new Runnable() {
			@Override
			public void run() {
				String json=ToolUtils.getUrl(CommCL.URi+"version.json");//获取服务器版本号
				JSONObject obj = JSONObject.parseObject(json);
				String version=obj.getString("VERSION");
				int ci=CommCL.SHOW_VERSION.compareTo(version);
				if(ci<0){
					Message msg=Message.obtain();
					msg.what=2019002;
					msg.obj="open";
					handler.sendMessage(msg);
				}
			}
		});
		thread.start();

	}



}
