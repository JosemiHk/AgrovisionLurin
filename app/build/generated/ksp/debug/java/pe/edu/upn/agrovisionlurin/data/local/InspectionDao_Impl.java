package pe.edu.upn.agrovisionlurin.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class InspectionDao_Impl implements InspectionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<InspectionEntity> __insertionAdapterOfInspectionEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsSynced;

  public InspectionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfInspectionEntity = new EntityInsertionAdapter<InspectionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `inspections` (`id`,`plotId`,`subPlot`,`imagePath`,`diagnosisClass`,`confidence`,`latencyMs`,`severityLevel`,`recommendationsJson`,`syncStatus`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InspectionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getPlotId());
        statement.bindString(3, entity.getSubPlot());
        statement.bindString(4, entity.getImagePath());
        statement.bindLong(5, entity.getDiagnosisClass());
        statement.bindDouble(6, entity.getConfidence());
        statement.bindLong(7, entity.getLatencyMs());
        statement.bindString(8, entity.getSeverityLevel());
        statement.bindString(9, entity.getRecommendationsJson());
        statement.bindLong(10, entity.getSyncStatus());
        statement.bindLong(11, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfMarkAsSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE inspections SET syncStatus = 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertInspection(final InspectionEntity inspection,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfInspectionEntity.insert(inspection);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markAsSynced(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsSynced.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkAsSynced.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<InspectionEntity>> getAllInspections() {
    final String _sql = "SELECT * FROM inspections ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"inspections"}, new Callable<List<InspectionEntity>>() {
      @Override
      @NonNull
      public List<InspectionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlotId = CursorUtil.getColumnIndexOrThrow(_cursor, "plotId");
          final int _cursorIndexOfSubPlot = CursorUtil.getColumnIndexOrThrow(_cursor, "subPlot");
          final int _cursorIndexOfImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "imagePath");
          final int _cursorIndexOfDiagnosisClass = CursorUtil.getColumnIndexOrThrow(_cursor, "diagnosisClass");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfLatencyMs = CursorUtil.getColumnIndexOrThrow(_cursor, "latencyMs");
          final int _cursorIndexOfSeverityLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "severityLevel");
          final int _cursorIndexOfRecommendationsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "recommendationsJson");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<InspectionEntity> _result = new ArrayList<InspectionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InspectionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPlotId;
            _tmpPlotId = _cursor.getString(_cursorIndexOfPlotId);
            final String _tmpSubPlot;
            _tmpSubPlot = _cursor.getString(_cursorIndexOfSubPlot);
            final String _tmpImagePath;
            _tmpImagePath = _cursor.getString(_cursorIndexOfImagePath);
            final int _tmpDiagnosisClass;
            _tmpDiagnosisClass = _cursor.getInt(_cursorIndexOfDiagnosisClass);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final long _tmpLatencyMs;
            _tmpLatencyMs = _cursor.getLong(_cursorIndexOfLatencyMs);
            final String _tmpSeverityLevel;
            _tmpSeverityLevel = _cursor.getString(_cursorIndexOfSeverityLevel);
            final String _tmpRecommendationsJson;
            _tmpRecommendationsJson = _cursor.getString(_cursorIndexOfRecommendationsJson);
            final int _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getInt(_cursorIndexOfSyncStatus);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new InspectionEntity(_tmpId,_tmpPlotId,_tmpSubPlot,_tmpImagePath,_tmpDiagnosisClass,_tmpConfidence,_tmpLatencyMs,_tmpSeverityLevel,_tmpRecommendationsJson,_tmpSyncStatus,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getPendingSyncInspections(
      final Continuation<? super List<InspectionEntity>> $completion) {
    final String _sql = "SELECT * FROM inspections WHERE syncStatus = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<InspectionEntity>>() {
      @Override
      @NonNull
      public List<InspectionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlotId = CursorUtil.getColumnIndexOrThrow(_cursor, "plotId");
          final int _cursorIndexOfSubPlot = CursorUtil.getColumnIndexOrThrow(_cursor, "subPlot");
          final int _cursorIndexOfImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "imagePath");
          final int _cursorIndexOfDiagnosisClass = CursorUtil.getColumnIndexOrThrow(_cursor, "diagnosisClass");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfLatencyMs = CursorUtil.getColumnIndexOrThrow(_cursor, "latencyMs");
          final int _cursorIndexOfSeverityLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "severityLevel");
          final int _cursorIndexOfRecommendationsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "recommendationsJson");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<InspectionEntity> _result = new ArrayList<InspectionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InspectionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPlotId;
            _tmpPlotId = _cursor.getString(_cursorIndexOfPlotId);
            final String _tmpSubPlot;
            _tmpSubPlot = _cursor.getString(_cursorIndexOfSubPlot);
            final String _tmpImagePath;
            _tmpImagePath = _cursor.getString(_cursorIndexOfImagePath);
            final int _tmpDiagnosisClass;
            _tmpDiagnosisClass = _cursor.getInt(_cursorIndexOfDiagnosisClass);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final long _tmpLatencyMs;
            _tmpLatencyMs = _cursor.getLong(_cursorIndexOfLatencyMs);
            final String _tmpSeverityLevel;
            _tmpSeverityLevel = _cursor.getString(_cursorIndexOfSeverityLevel);
            final String _tmpRecommendationsJson;
            _tmpRecommendationsJson = _cursor.getString(_cursorIndexOfRecommendationsJson);
            final int _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getInt(_cursorIndexOfSyncStatus);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new InspectionEntity(_tmpId,_tmpPlotId,_tmpSubPlot,_tmpImagePath,_tmpDiagnosisClass,_tmpConfidence,_tmpLatencyMs,_tmpSeverityLevel,_tmpRecommendationsJson,_tmpSyncStatus,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Integer> getPendingSyncCount() {
    final String _sql = "SELECT COUNT(*) FROM inspections WHERE syncStatus = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"inspections"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
