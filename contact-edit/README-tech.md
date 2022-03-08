## Android 連絡先一覧表示・一括削除プログラム<br/>ソースコードの概略説明<!-- omit in toc -->

[Home](https://oasis3855.github.io/webpage/) > [Software](https://oasis3855.github.io/webpage/software/index.html) > [Software Download](https://oasis3855.github.io/webpage/software/software-download.html) > [android-phonetools](../README.md) > [contacts-editor](README.md) > ***ソースコード説明*** (this page)

<br />
<br />

Last Updated : Dec. 2016

- [名前で検索](#名前で検索)
- [電話番号で検索](#電話番号で検索)
- [エントリーの削除](#エントリーの削除)

<br />
<br />

## 名前で検索

Android連絡先は、「ID,名前,誕生日,...」を格納しているContactsデータベースと「ID,ID_Contacts,電話番号」「ID,ID_Contacts,email」… など一人が複数の値を持つことの出来る属性を格納しているデータベースからできている。

Contactsデータベース内のみで完結する「名前で検索」であれば、単純に次のようなプログラムで可能。 

```Java
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
```

## 電話番号で検索

「電話番号で検索」する場合は、このリレーショナル・データベース構造を意識して、電話番号データベースのCONTACT_IDがContactsデータベースのIDに一致するということを用いて2段階で検索をかける必要がある。 

```Java
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
```

## エントリーの削除

検索は上述のようにquery関数で行うことができ、削除は次のようにdelete関数で簡単に行える。 

```Java
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
```
