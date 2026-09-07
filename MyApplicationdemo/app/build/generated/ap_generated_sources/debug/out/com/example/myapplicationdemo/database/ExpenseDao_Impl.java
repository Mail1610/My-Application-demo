package com.example.myapplicationdemo.database;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.example.myapplicationdemo.model.Expense;
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
public final class ExpenseDao_Impl implements ExpenseDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<Expense> __insertAdapterOfExpense;

  public ExpenseDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfExpense = new EntityInsertAdapter<Expense>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `expenses` (`id`,`groupId`,`description`,`payer`,`totalAmount`,`myShare`,`date`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement, final Expense entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getGroupId());
        if (entity.getDescription() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getDescription());
        }
        if (entity.getPayer() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getPayer());
        }
        statement.bindDouble(5, entity.getTotalAmount());
        statement.bindDouble(6, entity.getMyShare());
        if (entity.getDate() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getDate());
        }
      }
    };
  }

  @Override
  public void insertAll(final List<Expense> expenses) {
    DBUtil.performBlocking(__db, false, true, (_connection) -> {
      __insertAdapterOfExpense.insert(_connection, expenses);
      return null;
    });
  }

  @Override
  public void insert(final Expense expense) {
    DBUtil.performBlocking(__db, false, true, (_connection) -> {
      __insertAdapterOfExpense.insert(_connection, expense);
      return null;
    });
  }

  @Override
  public List<Expense> getByGroupId(final int groupId) {
    final String _sql = "SELECT * FROM expenses WHERE groupId = ?";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, groupId);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfGroupId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "groupId");
        final int _columnIndexOfDescription = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "description");
        final int _columnIndexOfPayer = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "payer");
        final int _columnIndexOfTotalAmount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "totalAmount");
        final int _columnIndexOfMyShare = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "myShare");
        final int _columnIndexOfDate = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "date");
        final List<Expense> _result = new ArrayList<Expense>();
        while (_stmt.step()) {
          final Expense _item;
          final int _tmpId;
          _tmpId = (int) (_stmt.getLong(_columnIndexOfId));
          final int _tmpGroupId;
          _tmpGroupId = (int) (_stmt.getLong(_columnIndexOfGroupId));
          final String _tmpDescription;
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null;
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription);
          }
          final String _tmpPayer;
          if (_stmt.isNull(_columnIndexOfPayer)) {
            _tmpPayer = null;
          } else {
            _tmpPayer = _stmt.getText(_columnIndexOfPayer);
          }
          final double _tmpTotalAmount;
          _tmpTotalAmount = _stmt.getDouble(_columnIndexOfTotalAmount);
          final double _tmpMyShare;
          _tmpMyShare = _stmt.getDouble(_columnIndexOfMyShare);
          final String _tmpDate;
          if (_stmt.isNull(_columnIndexOfDate)) {
            _tmpDate = null;
          } else {
            _tmpDate = _stmt.getText(_columnIndexOfDate);
          }
          _item = new Expense(_tmpId,_tmpGroupId,_tmpDescription,_tmpPayer,_tmpTotalAmount,_tmpMyShare,_tmpDate);
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
    final String _sql = "SELECT COALESCE(MAX(id), 0) FROM expenses";
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
    final String _sql = "DELETE FROM expenses";
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
