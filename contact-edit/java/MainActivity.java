package com.example.vm.contactseditor;

/**
 * ContactEditor : 連絡先 一覧表示・一括削除プログラム
 * (C) 2016 INOUE Hirokazu
 * version 1.0   (2016/December/23)
 * <p>
 * http://oasis.halfmoon.jp/
 * This Program is GNU GPL version 3 free software
 * https://ja.osdn.net/projects/opensource/wiki/licenses%252FGNU_General_Public_License_version_3.0
 */

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

  // Android 6.0以降での連絡先読み出し・書き込み権限取得用
  private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
  private static final int PERMISSIONS_REQUEST_WRITE_CONTACTS = 101;

  // 検索・削除対象の名前・電話番号 （複数指定はコンマ区切り）
  String configName = "";
  String configPhoneNumbers = "";

  /**
   * onCreate : Activity構築時 最初に1回実行される
   * 連絡先アクセス権の取得、preferenceからの初期値読み出しを行う
   *
   * @param savedInstanceState
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    String strTemp = "";

    TextView textView = (TextView) findViewById(R.id.textView);
    if(textView != null) textView.setMovementMethod(new ScrollingMovementMethod());    // スクロールバー表示

    // 連絡先 読み出し・書き込みアクセス権の取得（Android 6.0以上の場合）
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission
            .READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
              PERMISSIONS_REQUEST_READ_CONTACTS);
      strTemp += "READ_CONTACTS権限取得（>Android 6.0)";
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission
            .WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS},
              PERMISSIONS_REQUEST_WRITE_CONTACTS);
      strTemp += "WRITE_CONTACTS権限取得（>Android 6.0)";
    }

    // preferenceより初期設定値を読み込む
    SharedPreferences pref = getSharedPreferences("Contacts_Editor", Context.MODE_PRIVATE);
    configName = pref.getString("configName", "%山田%,%佐藤%");
    configPhoneNumbers = pref.getString("configPhoneNumbers", "03-1234-5678");
    strTemp += ("検索初期値読込 name=" + configName + "\n");
    strTemp += ("検索初期値読込 phone=" + configPhoneNumbers + "\n");
    strTemp += getContactCount();

    strTemp += "メニューボタンから操作可能です...\n\n\n\n\n\n\n" + "連絡先 一覧表示・一括削除プログラム\nversion1.0\n(C)2016 " +
            "INOUE Hirokazu";
    if(textView != null) textView.setText(strTemp);
  }

  /**
   * メニューを構築する
   *
   * @param menu
   * @return
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  /**
   * メニューの項目が選択された時に呼び出される
   * ここで、連絡先項目の一覧表示、一括削除、初期値設定に処理を分岐
   *
   * @param item 選択された項目のid
   * @return
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_list_contacts) {
      // 連絡先 一覧表示
      ListContacts("list");
      return true;
    } else if (id == R.id.action_delete_contacts) {
      // 連絡先 条件削除
      ListContacts("delete");
      return true;
    } else if (id == R.id.action_settings) {
      SettingsDialog();
      return true;
    } else if (id == R.id.action_quit) {
      // プログラムの終了
      android.os.Process.killProcess(android.os.Process.myPid());
      System.exit(1);
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * 初期値設定のためのAlertDialogを表示、結果をpreferenceに保存する
   */
  private void SettingsDialog() {
    // AlertDialog上に作成するレイアウトコンテナ
    final LinearLayout layout = new LinearLayout(MainActivity.this);
    layout.setOrientation(LinearLayout.VERTICAL);

    // ラベル
    TextView textName = new TextView(this);
    textName.setText("名前");
    layout.addView(textName);
    // 名前入力テキストボックス
    final EditText editName = new EditText(MainActivity.this);
    editName.setInputType(InputType.TYPE_CLASS_TEXT);
    editName.setText(configName);
    layout.addView(editName);

    // ラベル
    TextView textPhone = new TextView(this);
    textPhone.setText("電話番号");
    layout.addView(textPhone);
    // 電話番号入力テキストボックス
    final EditText editPhone = new EditText(MainActivity.this);
    editPhone.setInputType(InputType.TYPE_CLASS_TEXT);
    editPhone.setText(configPhoneNumbers);
    layout.addView(editPhone);

    // ラベル
    TextView textHelp = new TextView(this);
    textHelp.setText("複数指定は「,」で区切る。任意文字一致は「%」など、一般のSQL構文が使えます");
    layout.addView(textHelp);

    // AlertDialogを構築する
    AlertDialog.Builder dlg = new AlertDialog.Builder(this);
    dlg.setTitle("検索・削除対象の設定");
    dlg.setView(layout);

    dlg.setPositiveButton("決定", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        //Yesボタンが押された時の処理（ここではログに出力している）
        SharedPreferences.Editor prefEditor = getSharedPreferences("Contacts_Editor", Context
                .MODE_PRIVATE).edit();
        configPhoneNumbers = editPhone.getText().toString();
        prefEditor.putString("configPhoneNumbers", configPhoneNumbers);
        configName = editName.getText().toString();
        prefEditor.putString("configName", configName);
        prefEditor.apply();

      }
    });
    // AlertDialogを表示する
    dlg.show();
  }

  /**
   * 一覧表示、一括削除のための条件設定（名前か電話番号か）のAlertDialogを表示し、
   * 入力・選択内容に基づき表示・削除の各関数へ処理を引き継ぐ
   *
   * @param list_or_delete "list" または "delete" を指定
   */
  private void ListContacts(final String list_or_delete) {
    // AlertDialog上に作成するレイアウトコンテナ
    final LinearLayout layout = new LinearLayout(MainActivity.this);
    layout.setOrientation(LinearLayout.VERTICAL);

    final RadioGroup radioGroup = new RadioGroup(MainActivity.this);
    final RadioButton radioPhone = new RadioButton(MainActivity.this);
    radioPhone.setText("電話番号で検索");
    radioGroup.addView(radioPhone);

    final RadioButton radioName = new RadioButton(MainActivity.this);
    radioName.setText("名前で検索");
    radioGroup.addView(radioName);
    layout.addView(radioGroup);

    // テキストボックス
    final EditText editText = new EditText(MainActivity.this);
    editText.setInputType(InputType.TYPE_CLASS_TEXT);
    editText.setText("");
    layout.addView(editText);

    // AlertDialogを構築する
    AlertDialog.Builder dlg = new AlertDialog.Builder(this);
    if (list_or_delete.equalsIgnoreCase("list")) {
      dlg.setTitle("一覧表示");
    } else {
      dlg.setTitle("削除");
    }
    dlg.setView(layout);

    // 初期値入力
    radioName.setChecked(true);
    editText.setText(configName);

    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (radioPhone.isChecked()) {
          editText.setText(configPhoneNumbers);
        } else if (radioName.isChecked()) {
          editText.setText(configName);
        }
      }
    });

    dlg.setPositiveButton("決定", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        //Yesボタンが押された時の処理
        String strTemp = "";
        if (radioName.isChecked()) {
          strTemp += "名前で検索\n";
        } else if (radioPhone.isChecked()) {
          strTemp += "名前で検索\n";
        } else {
          strTemp += "ラジオボタンが未選択";
        }
        strTemp += ("指定文字列" + editText.getText().toString() + "\n");
        TextView textView = (TextView) findViewById(R.id.textView);
        if(textView != null) textView.setText(strTemp);

        if (list_or_delete.equalsIgnoreCase("list")) {
          if (editText.getText().toString().isEmpty()) {
            strTemp += getAllContacts();
          } else if (radioName.isChecked()) {
            strTemp += getContactsByName(editText.getText().toString());
          } else if (radioPhone.isChecked()) {
            strTemp += getContactsByPhone(editText.getText().toString());
          }
        } else if (list_or_delete.equalsIgnoreCase("delete")) {
          if (editText.getText().toString().isEmpty()) {
            strTemp += "条件を空白にして全削除の機能は実装されていません\n";
          } else if (radioName.isChecked()) {
            strTemp += deleteContactsByName(editText.getText().toString());
          } else if (radioPhone.isChecked()) {
            strTemp += deleteContactsByPhone(editText.getText().toString());
          }
        }
        if(textView != null) textView.setText(strTemp);
      }
    });
    // AlertDialogを表示する
    dlg.show();
  }

  /**
   * 連絡先に格納されている件数を返す
   * @return 件数を格納した文字列を返す
   */
  private String getContactCount() {
    int count = 0;
    String strTemp = "";
    Cursor cr = null;
    try {
      cr = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
              null);
      if (cr != null)  count = cr.getCount();
    } catch (Exception e) {
      strTemp += (e.getMessage() + "\n");
    } finally {
      if (cr != null && !cr.isClosed()) cr.close();
    }
    strTemp += ( "連絡先 データ件数=" + count + "\n" );
    return (strTemp);
  }

  /**
   * 連絡先の全てのアイテムを画面表示用文字列に格納する。
   *
   * @return 名前・email・電話番号を格納した文字列が返される
   */
  private String getAllContacts() {
    String strTemp = "";
    Cursor cr = null;
    try {
      cr = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
              null);
      if (cr != null && cr.moveToFirst()) {
        do {
          // idの表示
          String id = cr.getString(cr.getColumnIndexOrThrow(ContactsContract.CommonDataKinds
                  .Identity._ID));
          strTemp += ("id=" + id + "\n");
          // 表示名・電話番号・emailの表示
          strTemp += getContactData(id);
          // =====
          strTemp += "=====\n";
        } while (cr.moveToNext());
      }
    } catch (Exception e) {
      strTemp += (e.getMessage() + "\n");
    } finally {
      if (cr != null && !cr.isClosed()) cr.close();
    }
    return (strTemp);
  }

  /**
   * 指定された名前文字列に一致する連絡先アイテムを画面表示用文字列に格納する。
   *
   * @param names 検索条件となる名前文字列（複数はコンマ区切り）
   * @return 名前・email・電話番号を格納した文字列が返される
   */
  private String getContactsByName(String names) {
    String strTemp = "";
    Cursor cr = null;
    try {
      String sql = ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
      String[] arrayName = names.split(",", 0);
      for (String _name : arrayName) {
        String[] args = {_name};
        strTemp += ("検索キーワード=" + _name + "\n");
        cr = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, sql, args,
                null);
        if (cr != null) {
          if (cr.moveToFirst()) {
            do {
              // データIDの表示
              String id = cr.getString(cr.getColumnIndexOrThrow(ContactsContract.CommonDataKinds
                      .Identity._ID));
              strTemp += ("id=" + id + "\n");
              // 表示名・電話番号・emailの表示
              strTemp += getContactData(id);
              // =====
              strTemp += "=====\n";
            } while (cr.moveToNext());
          }
        }
      }
    } catch (Exception e) {
      strTemp += (e.getMessage() + "\n");
    } finally {
      if (cr != null && !cr.isClosed()) cr.close();
    }

    return (strTemp);
  }

  /**
   * 指定された電話番号文字列に一致する連絡先アイテムを画面表示用文字列に格納する。
   *
   * @param phones 検索条件となる電話番号文字列（複数はコンマ区切り）
   * @return 名前・email・電話番号を格納した文字列が返される
   */
  private String getContactsByPhone(String phones) {
    String strTemp = "";
    Cursor cr = null;
    try {
      String sql = ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?";
      String[] arrayPhone = phones.split(",", 0);
      for (String _phone : arrayPhone) {
        String[] args = {_phone};
        strTemp += ("検索キーワード=" + _phone + "\n");
        cr = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                sql, args, null);
        if (cr != null) {
          if (cr.moveToFirst()) {
            do {
              // データIDの表示
              String id = cr.getString(cr.getColumnIndexOrThrow(ContactsContract.CommonDataKinds
                      .Phone.CONTACT_ID));
              strTemp += ("id=" + id + "\n");
              // 表示名・電話番号・emailの表示
              strTemp += getContactData(id);
              // =====
              strTemp += "=====\n";
            } while (cr.moveToNext());
          }
        }
      }
    } catch (Exception e) {
      strTemp += (e.getMessage() + "\n");
    } finally {
      if (cr != null && !cr.isClosed()) cr.close();
    }
    return (strTemp);
  }

  /**
   * 指定された名前文字列に一致する連絡先アイテムを一括削除する。
   *
   * @param names 検索条件となる名前文字列（複数はコンマ区切り）
   * @return 削除したアイテムのid及び削除総数を格納した文字列が返される
   */
  private String deleteContactsByName(String names) {
    String strTemp = "";
    Cursor cr = null;
    int count = 0;
    try {
      String sql = ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
      String[] arrayName = names.split(",", 0);
      for (String _name : arrayName) {
        String[] args = {_name};
        strTemp += ("検索キーワード=" + _name + "\n");
        cr = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, sql, args,
                null);
        if (cr != null) {
          if (cr.moveToFirst()) {
            do {
              // データIDの表示
              String id = cr.getString(cr.getColumnIndexOrThrow(ContactsContract.CommonDataKinds
                      .Identity._ID));
              strTemp += ("id=" + id + " deleted\n");
              // 削除
              Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, cr
                      .getString(cr.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));
              count += getContentResolver().delete(uri, null, null);
            } while (cr.moveToNext());
          }
        }
      }
      strTemp += (count + "件削除\n");
    } catch (Exception e) {
      strTemp += (e.getMessage() + "\n");
    } finally {
      if (cr != null && !cr.isClosed()) cr.close();
    }

    return (strTemp);
  }

  /**
   * 指定された電話番号文字列に一致する連絡先アイテムを一括削除する。
   *
   * @param phones 検索条件となる電話番号文字列（複数はコンマ区切り）
   * @return 削除したアイテムのid及び削除総数を格納した文字列が返される
   */
  private String deleteContactsByPhone(String phones) {
    String strTemp = "";
    Cursor cr = null;
    int count = 0;
    try {
      String sql = ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?";
      String[] arrayPhone = phones.split(",", 0);
      for (String _phone : arrayPhone) {
        String[] args = {_phone};
        strTemp += ("検索キーワード=" + _phone + "\n");
        cr = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                sql, args, null);
        if (cr != null) {
          if (cr.moveToFirst()) {
            do {
              // データIDの表示
              String id = cr.getString(cr.getColumnIndexOrThrow(ContactsContract.CommonDataKinds
                      .Identity._ID));
              strTemp += ("id=" + id + " deleted\n");
              // 削除
              Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, cr
                      .getString(cr.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));
              count += getContentResolver().delete(uri, null, null);
            } while (cr.moveToNext());
          }
        }
      }
      strTemp += (count + "件削除\n");
    } catch (Exception e) {
      strTemp += (e.getMessage() + "\n");
    } finally {
      if (cr != null && !cr.isClosed()) cr.close();
    }

    return (strTemp);
  }

  /**
   * 指定された連絡先idの表示名・電話番号・emailを格納した文字列を返す
   *
   * @param id : idを一件を指定する
   * @return 表示名・電話番号・emailを格納した文字列を返す
   */
  private String getContactData(String id) {
    String strTemp = "";
    Cursor cr = null;
    Cursor eml = null;
    Cursor tel = null;

    try {
      Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id);
      cr = getContentResolver().query(uri, null, null, null, null);
      if (cr != null && cr.moveToFirst()) {
        strTemp += (cr.getString(cr.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) + "\n");
      }
      if (cr != null) cr.close();

      // 電話番号
      tel = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
              ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
      if (tel != null && tel.moveToFirst()) {
        do {
          strTemp += (tel.getString(tel.getColumnIndex(ContactsContract.CommonDataKinds.Email
                  .DATA)) + "\n");
        } while (tel.moveToNext());
      } else {
        strTemp += "電話番号なし\n";
      }
      if(tel != null) tel.close();

      // email
      eml = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
              ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + id, null, null);
      if (eml != null && eml.moveToFirst()) {
        do {
          strTemp += (eml.getString(eml.getColumnIndex(ContactsContract.CommonDataKinds.Email
                  .DATA)) + "\n");
        } while (eml.moveToNext());
      } else {
        strTemp += "emailなし\n";
      }
      if(eml != null) eml.close();

    } catch (Exception e) {
      strTemp += (e.getMessage() + "\n");
    } finally {
      if (cr != null && !cr.isClosed()) cr.close();
      if (eml != null && !eml.isClosed()) eml.close();
      if (tel != null && !tel.isClosed()) tel.close();
    }
    return (strTemp);
  }

}
