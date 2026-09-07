package com.example.myapplicationdemo.database;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.example.myapplicationdemo.model.Group;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class GroupDao_Impl implements GroupDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<Group> __insertAdapterOfGroup;

  public GroupDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfGroup = new EntityInsertAdapter<Group>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `groups` (`id`,`name`,`memberCount`,`myBalance`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement, final Group entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getName());
        }
        statement.bindLong(3, entity.getMemberCount());
        statement.bindDouble(4, entity.getMyBalance());
      }
    };
  }

  @Override
  public void insertAll(final List<Group> groups) {
    DBUtil.performBlocking(__db, false, true, (_connection) -> {
      __insertAdapterOfGroup.insert(_connection, groups);
      return null;
    });
  }

  @Override
  public void insert(final Group group) {
    DBUtil.performBlocking(__db, false, true, (_connection) -> {
      __insertAdapterOfGroup.insert(_connection, group);
      return null;
    });
  }

  @Override
  public List<Group> getAll() {
    final String _sql = "SELECT * FROM groups";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "name");
        final int _columnIndexOfMemberCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "memberCount");
        final int _columnIndexOfMyBalance = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "myBalance");
        final List<Group> _result = new ArrayList<Group>();
        while (_stmt.step()) {
          final Group _item;
          final int _tmpId;
          _tmpId = (int) (_stmt.getLong(_columnIndexOfId));
          final String _tmpName;
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null;
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName);
          }
          final int _tmpMemberCount;
          _tmpMemberCount = (int) (_stmt.getLong(_columnIndexOfMemberCount));
          final double _tmpMyBalance;
          _tmpMyBalance = _stmt.getDouble(_columnIndexOfMyBalance);
          _item = new Group(_tmpId,_tmpName,_tmpMemberCount,_tmpMyBalance);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public int getMaxId() {
    final String _sql = "SELECT COALESCE(MAX(id), 0) FROM groups";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _result;
        if (_stmt.step()) {
          _result = (int) (_stmt.getLong(0));
        } else {
          _result = 0;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public void deleteAll() {
    final String _sql = "DELETE FROM groups";
    DBUtil.performBlocking(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        _stmt.step();
        return null;
      } finally {
        _stmt.close();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
