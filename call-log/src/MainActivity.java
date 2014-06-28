/**********
    Android 通話履歴の表示・追加・削除のための基本機能サンプルプログラム
    
    (C) 2014 INOUE Hirokazu

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *********/

package com.example.android_calllog_tool02;

import android.R.integer;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.appcompat.R.string;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.text.InputType;
import android.widget.DatePicker;

import java.util.Calendar;
import java.text.*;
import java.util.Date;

import android.widget.ScrollView;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /*************
     * メニューの項目が選択された場合
     * 
     ************/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        // 「このアプリについて」メニュー
        if (id == R.id.menu_about) {
            Toast.makeText(MainActivity.this, "通話履歴 簡易ツール\n(C)INOUE Hirokazu\nGNU GPL software", Toast.LENGTH_LONG).show();
            return true;
        }
        // 「通話履歴の表示」メニュー
        else if (id == R.id.menu_query_log) {
            // DateTime_Test_func();
            QueryLog_InputDlg();
            return true;
        }
        // 「通話履歴の追加」メニュー
        else if (id == R.id.menu_add_dummy_log) {
            AddLog_InputDlg();
            return true;
        }
        // 「通話履歴の選択削除」メニュー
        else if (id == R.id.menu_del_log) {
            DeleteLog_InputDlg();
            return true;
        }
        // 「プログラム終了」メニューが押された場合
        else if (id == R.id.menu_quit) {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
        return super.onOptionsItemSelected(item);
    }

    /*************
     * 通話履歴表示のための期間を入力するAlertDialog
     * 
     ************/
    private void QueryLog_InputDlg(){
        // 表示開始日、表示終了日をセットする変数
        final Calendar cal[] = new Calendar[2];
        cal[0] = Calendar.getInstance();
        cal[1] = Calendar.getInstance();
        
        // ActionDialogにセットするレイアウト（このレイアウト内に、各コンポーネントを配置）
        final LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        
        // 文字列「表示開始日」を表示
        TextView text1 = new TextView(this);
        text1.setText("表示開始日");
        layout.addView(text1);
        // 表示開始日を選択するDatePicker
        final DatePicker date1 = new DatePicker(MainActivity.this);
        date1.setCalendarViewShown(false);
        Calendar calNow = Calendar.getInstance();
        calNow.add(Calendar.MONTH, -1);
        date1.updateDate(calNow.get(Calendar.YEAR), calNow.get(Calendar.MONTH), calNow.get(Calendar.DAY_OF_MONTH));
        layout.addView(date1);
        
        // 文字列「表示終了日」を表示
        TextView text2 = new TextView(this);
        text2.setText("集計終了日");
        layout.addView(text2);
        // 表示終了日を選択するDatePicker
        final DatePicker date2 = new DatePicker(MainActivity.this);
        date2.setCalendarViewShown(false);
        layout.addView(date2);
        
        // AlertDialogを構築する
        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle("通話履歴 表示期間の指定");
        dlg.setView(layout);
        // Yes/No ボタンの定義
        dlg.setPositiveButton("履歴表示", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                //Yesボタンが押された時の処理
                // 集計開始日時 (Monthは0〜11)
                cal[0].set(date1.getYear(), date1.getMonth(), date1.getDayOfMonth(), 0, 0, 0);
                // 集計終了日時
                cal[1].set(date2.getYear(), date2.getMonth(), date2.getDayOfMonth(), 23, 59, 59);
                // 画面に通話履歴を表示する
                QueryLog_Main(cal);
            }});
        dlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                //Noボタンが押された時の処理
            }});
        // AlertDialogを表示する
        dlg.show();
        return;
    }
    
    /*************
     * 指定された期間の通話履歴を画面表示する
     * @param Calendar cal[] : 期間を指定する日付
     ************/
    private void QueryLog_Main(Calendar cal[]){
        // 多数行のテキストをスクロール表示するレイアウト
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(scrollView);
        scrollView.addView(layout);
        
        // 画面上のテキストエリアを定義
        TextView text = new TextView(this);
        text.setText("しばらくお待ちください");
        text.setTextSize((int)(text.getTextSize()*0.8));
        layout.addView(text);
        
        // クエリ文字列のプレースホルダに代入する値の配列 ＝ [0]:集計開始日時, [1]:集計終了日時
        String[] mSelectionArgs = { String.valueOf(cal[0].getTimeInMillis()), String.valueOf(cal[1].getTimeInMillis()-1000) };
        
        // 通話履歴の取得（SQLクエリ風）
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
            CallLog.Calls.CONTENT_URI,  // データの種類
            null,   // 項目(null 全項目)
            CallLog.Calls.DATE + " >= ? AND " + CallLog.Calls.DATE + " <= ?",   // クエリ文字列 (nullの場合すべてのデータ対象)
            mSelectionArgs,             // クエリ文字列内のプレースホルダーに代入する値を格納した配列
            CallLog.Calls.DEFAULT_SORT_ORDER    // ソート順序
        );
        // 画面表示する文字列を作成するための一時変数
        String result = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        // クエリ結果を1つずつ、一時変数の文字列に格納していく
        if (cursor.moveToFirst()) {
            do{
                // 発信・着信・未応答
                String callType = cursor.getString(cursor.getColumnIndex("TYPE"));
                if(callType.equals(String.valueOf(CallLog.Calls.INCOMING_TYPE))){
                    result += "[着] ";
                }
                else if(callType.equals(String.valueOf(CallLog.Calls.OUTGOING_TYPE))){
                    result += "[発] ";
                }
                else if(callType.equals(String.valueOf(CallLog.Calls.MISSED_TYPE))){
                    result += "[×] ";
                }
                // 電話番号
                result += cursor.getString(cursor.getColumnIndex("NUMBER")) + "\n";
                // 日時
                Date date = new Date(cursor.getLong(cursor.getColumnIndex("DATE")));
                result += " " + dateFormat.format(date) + " 〜";
                // 通話時間
                result += cursor.getString(cursor.getColumnIndex("DURATION")) + "秒\n";
                // 改行
                result += "------\n";
            }  while (cursor.moveToNext());
        }
        text.setText(result);
    }
    
    /*************
     * 通話履歴の追加条件を入力するAlertDialog
     * 
     ************/
    // 画面上のラジオボタンのID
    final int ID_RADIO_OUTGOING = 0x2001;
    final int ID_RADIO_INCOMMING = 0x2002;
    final int ID_RADIO_MISSED = 0x2003;
    
    private void AddLog_InputDlg() {
        // ActionDialogにセットするレイアウト（このレイアウト内に、各コンポーネントを配置）
        final LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        
        // 文字列「日時」を表示
        TextView text1 = new TextView(this);
        text1.setText("日時");
        layout.addView(text1);
        // 日時を入力するテキストボックス
        final EditText editViewDate = new EditText(MainActivity.this);
        editViewDate.setLines(1);   // 1行
        editViewDate.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_NORMAL);
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar calNow = Calendar.getInstance();
        editViewDate.setText(dateFmt.format(calNow.getTime()));
        layout.addView(editViewDate);

        // 通話秒数の文字列とテキストボックスを横に並べるためのレイアウト
        final LinearLayout layout_duration = new LinearLayout(MainActivity.this);
        layout_duration.setOrientation(LinearLayout.HORIZONTAL);
        // 文字列「通話秒数」を表示
        TextView text2 = new TextView(this);
        text2.setText("通話秒数");
        layout_duration.addView(text2);
        // 通話秒数を入力するテキストボックス
        final EditText editViewDuration = new EditText(MainActivity.this);
        editViewDuration.setLines(1);   // 1行
        editViewDuration.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editViewDuration.setText("0");
        layout_duration.addView(editViewDuration);
        // 文字列＋テキストボックスを並べたレイアウトを、全体のレイアウトに配置
        layout.addView(layout_duration);
        
        // 通話種別のラジオボタン（グループ化する）
        final RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(RadioGroup.HORIZONTAL);
        // 「発信」ラジオボタン
        RadioButton radioOutgoingButton = new RadioButton(this);
        radioOutgoingButton.setText("発信");
        radioOutgoingButton.setId(ID_RADIO_OUTGOING);
        rg.addView(radioOutgoingButton);
        // 「着信」ラジオボタン
        RadioButton radioIncommingButton = new RadioButton(this);
        radioIncommingButton.setText("着信");
        radioIncommingButton.setId(ID_RADIO_INCOMMING);
        rg.addView(radioIncommingButton);
        // 「不在」ラジオボタン
        RadioButton radioMissedButton = new RadioButton(this);
        radioMissedButton.setText("不在");
        radioMissedButton.setId(ID_RADIO_MISSED);
        rg.addView(radioMissedButton);
        // 「発信」をデフォルト選択とし、ラジオボタングループを全体レイアウトに配置
        rg.check(ID_RADIO_OUTGOING);
        layout.addView(rg);
        
        // 文字列「電話番号」を表示
        TextView text3 = new TextView(this);
        text3.setText("電話番号");
        layout.addView(text3);
        // 電話番号を入力するテキストボックス
        final EditText editViewNum = new EditText(MainActivity.this);
        editViewNum.setLines(1);   // 1行
        editViewNum.setInputType(InputType.TYPE_CLASS_PHONE);
        editViewNum.setText("09012345678");
        layout.addView(editViewNum);
        
        // AlertDialogを構築する
        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle("通話履歴の追加");
        dlg.setView(layout);
        // Yes/No ボタンの定義
        dlg.setPositiveButton("履歴追加", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                //Yesボタンが押された時の処理
                Calendar cal = Calendar.getInstance();
                cal = Str2Date(editViewDate.getText().toString(), "");
                SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                // 通話履歴を1件追加し、結果をToastで表示する
                if(AddLog_Main(editViewNum.getText().toString(), cal,
                        Integer.valueOf(editViewDuration.getText().toString()), rg.getCheckedRadioButtonId())){
                    Toast.makeText(MainActivity.this, "通話履歴を追加しました", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "ERROR : 入力内容にエラーがありました", Toast.LENGTH_SHORT).show();
                }
            }});
        dlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                //Noボタンが押された時の処理
            }});
        // AlertDialogを表示する
        dlg.show();
    }
    
    /*************
     * 通話履歴を1件追加する
     * @param String num : 電話番号, Calendar cal : 日時, int duration : 通話秒数, int type : 発・着・不在
     ************/
    private Boolean AddLog_Main(String num, Calendar cal, int duration, int type){
        // 引数が範囲内かどうかのチェック
        if(cal.getTimeInMillis() <= 0) return(false);
        if(num.length() <= 0) return(false);
        if(duration < 0) return(false);
        if(type != ID_RADIO_OUTGOING && type != ID_RADIO_INCOMMING && type != ID_RADIO_MISSED) return(false);
        
        // 通話履歴の項目をContentValuesに格納
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, num);
        values.put(CallLog.Calls.DATE, String.valueOf(cal.getTimeInMillis()));
        values.put(CallLog.Calls.DURATION, duration);
        if(type == ID_RADIO_INCOMMING) values.put(CallLog.Calls.TYPE, CallLog.Calls.INCOMING_TYPE);
        else if(type == ID_RADIO_OUTGOING) values.put(CallLog.Calls.TYPE, CallLog.Calls.OUTGOING_TYPE);
        else values.put(CallLog.Calls.TYPE, CallLog.Calls.MISSED_TYPE);
        values.put(CallLog.Calls.NEW, 1);
        values.put(CallLog.Calls.CACHED_NAME, "");
        values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
        values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "");
        
        // 通話履歴データベースに追加 (INSERT)
        ContentResolver resolver = getContentResolver();
        resolver.insert(CallLog.Calls.CONTENT_URI, values);
        return(true);
    }
    
    /*************
     * 通話履歴の削除条件を入力するAlertDialog
     * 
     ************/
    private void DeleteLog_InputDlg() {
        // ActionDialogにセットするレイアウト（このレイアウト内に、各コンポーネントを配置）
        final LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        
        // 文字列「消去開始日時」を表示
        TextView text1 = new TextView(this);
        text1.setText("消去開始日時");
        layout.addView(text1);
        // 日時を入力するテキストボックス
        final EditText editViewDate1 = new EditText(MainActivity.this);
        editViewDate1.setLines(1);   // 1行
        editViewDate1.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_NORMAL);
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar calNow = Calendar.getInstance();
        editViewDate1.setText(dateFmt.format(calNow.getTime()));
        layout.addView(editViewDate1);
        
        // 文字列「消去終了日時」を表示
        TextView text2 = new TextView(this);
        text2.setText("消去終了日時");
        layout.addView(text2);
        // 日時を入力するテキストボックス
        final EditText editViewDate2 = new EditText(MainActivity.this);
        editViewDate2.setLines(1);   // 1行
        editViewDate2.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_NORMAL);
        editViewDate2.setText(dateFmt.format(calNow.getTime()));
        layout.addView(editViewDate2);
        
        // 文字列「電話番号」を表示
        TextView text3 = new TextView(this);
        text3.setText("電話番号 (空欄の時は全ての番号が対象)");
        layout.addView(text3);
        // 電話番号を入力するテキストボックス
        final EditText editViewNum = new EditText(MainActivity.this);
        editViewNum.setLines(1);   // 1行
        editViewNum.setInputType(InputType.TYPE_CLASS_PHONE);
        editViewNum.setText("09012345678");
        layout.addView(editViewNum);
        
        // AlertDialogを構築する
        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle("通話履歴の削除");
        dlg.setView(layout);
        // Yes/No ボタンの定義
        dlg.setPositiveButton("履歴削除", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                //Yesボタンが押された時の処理
                Calendar calStart = Calendar.getInstance();
                calStart = Str2Date(editViewDate1.getText().toString(), "");
                SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Calendar calEnd = Calendar.getInstance();
                calEnd = Str2Date(editViewDate2.getText().toString(), "");
                // 通話履歴を削除する
                int result = DeleteLog_Main(editViewNum.getText().toString(), calStart, calEnd);
                // 結果をToastで表示する
                if(result >= 0){
                    Toast.makeText(MainActivity.this, "通話履歴を " + String.valueOf(result)+ " 件削除しました", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "ERROR : 入力内容にエラーがありました", Toast.LENGTH_SHORT).show();
                }
            }});
        dlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                //Noボタンが押された時の処理
            }});
        // AlertDialogを表示する
        dlg.show();
    }
    
    /*************
     * 指定された条件で通話履歴を一括削除する
     * @param String num : 電話番号, Calendar calStart, calEnd : 開始・終了日時
     ************/
    private int DeleteLog_Main(String num, Calendar calStart, Calendar calEnd){
        // 引数が範囲内かどうかのチェック
        if(calStart.getTimeInMillis() <= 0 || calEnd.getTimeInMillis() <= 0) return(-1);
        if(calStart.getTimeInMillis() > calEnd.getTimeInMillis()) return(-1);
        
        // クエリ文字列のプレースホルダに代入する値の配列 ＝ [0]:開始日時, [1]:終了日時
        String[] mSelectionArgs = { String.valueOf(calStart.getTimeInMillis()), String.valueOf(calEnd.getTimeInMillis())};
        if(num.length() > 0) {
            // 電話番号が指定された場合は、配列の3つ目に電話番号を格納
            String[] tempArgs = { String.valueOf(calStart.getTimeInMillis()), String.valueOf(calEnd.getTimeInMillis()), num};
            // 要素数を1個増やした一時配列を定義し、それを上書きすることで、要素数を1個増やす
            mSelectionArgs = tempArgs;
        }
        // SQLクエリ文字列
        String strWhere = "";
        if(num.length() <= 0) strWhere = CallLog.Calls.DATE + " >= ? AND " + CallLog.Calls.DATE + " <= ?";
        else strWhere = CallLog.Calls.DATE + " >= ? AND " + CallLog.Calls.DATE + " <= ? AND " + CallLog.Calls.NUMBER + " = ?";
        // 通話履歴の削除（SQLクエリ風）
        ContentResolver resolver = getContentResolver();
        int result = resolver.delete(
            CallLog.Calls.CONTENT_URI,  // データの種類
            strWhere,   // フィルタ条件(null フィルタなし)
            mSelectionArgs   // フィルタ用パラメータ
        );
        // 削除したレコードの件数を返す
        return(result);
    }
    
    /*************
     * 年月日の文字列を与えると、Calendarクラスの値で返す
     * @param String StrDate + StrTime : 日時文字列
     ************/
    private Calendar Str2Date(String StrDate, String StrTime){
        Calendar calendarResult = Calendar.getInstance();
        calendarResult.clear();
        // 日時表現のフォーマットを指定する
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        // 日時チェックを厳密に行う（「13月」などと外れた値の場合はExceptionが発生）
        format.setLenient(false);
        ParsePosition pos = new ParsePosition(0);
        try{
            calendarResult.setTime(format.parse(StrDate + " " + StrTime, pos));
        } catch(Exception e){
            // エラーが発生した場合は、1970/1/1のミリ秒が設定される。
            calendarResult.setTimeInMillis(0);
        };
        return(calendarResult);
    }
}
