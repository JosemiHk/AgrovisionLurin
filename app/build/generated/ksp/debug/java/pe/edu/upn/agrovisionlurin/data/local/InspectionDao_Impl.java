package pe.edu.upn.agrovisionlurin.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
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

  private final EntityDeletionOrUpdateAdapter<InspectionEntity> __updateAdapterOfInspectionEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteInspection;

  public InspectionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfInspectionEntity = new EntityInsertionAdapter<InspectionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `inspections` (`id`,`plotId`,`subPlot`,`imagePath`,`diagnosisClass`,`confidence`,`latencyMs`,`severityLevel`,`recommendationsJson`,`syncStatus`,`createdAt`,`userId`,`state`,`classCode`,`classLabel`,`modelVersion`,`errorMessage`,`plotCodeSnapshot`,`userNameSnapshot`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
        statement.bindString(12, entity.getUserId());
        statement.bindString(13, entity.getState());
        statement.bindString(14, entity.getClassCode());
        statement.bindString(15, entity.getClassLabel());
        statement.bindString(16, entity.getModelVersion());
        statement.bindString(17, entity.getErrorMessage());
        statement.bindString(18, entity.getPlotCodeSnapshot());
        statement.bindString(19, entity.getUserNameSnapshot());
      }
    };
    this.__updateAdapterOfInspectionEntity = new EntityDeletionOrUpdateAdapter<InspectionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `inspections` SET `id` = ?,`plotId` = ?,`subPlot` = ?,`imagePath` = ?,`diagnosisClass` = ?,`confidence` = ?,`latencyMs` = ?,`severityLevel` = ?,`recommendationsJson` = ?,`syncStatus` = ?,`createdAt` = ?,`userId` = ?,`state` = ?,`classCode` = ?,`classLabel` = ?,`modelVersion` = ?,`errorMessage` = ?,`plotCodeSnapshot` = ?,`userNameSnapshot` = ? WHERE `id` = ?";
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
        statement.bindString(12, entity.getUserId());
        statement.bindString(13, entity.getState());
        statement.bindString(14, entity.getClassCode());
        statement.bindString(15, entity.getClassLabel());
        statement.bindString(16, entity.getModelVersion());
        statement.bindString(17, entity.getErrorMessage());
        statement.bindString(18, entity.getPlotCodeSnapshot());
        statement.bindString(19, entity.getUserNameSnapshot());
        statement.bindString(20, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteInspection = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM inspections WHERE id = ?";
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
  public Object insertInspections(final List<InspectionEntity> inspections,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfInspectionEntity.insert(inspections);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateInspection(final InspectionEntity inspection,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfInspectionEntity.handle(inspection);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteInspection(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteInspection.acquire();
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
          __preparedStmtOfDeleteInspection.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getInspectionById(final String id,
      final Continuation<? super InspectionEntity> $completion) {
    final String _sql = "SELECT * FROM inspections WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<InspectionEntity>() {
      @Override
      @Nullable
      public InspectionEntity call() throws Exception {
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
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfClassCode = CursorUtil.getColumnIndexOrThrow(_cursor, "classCode");
          final int _cursorIndexOfClassLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "classLabel");
          final int _cursorIndexOfModelVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "modelVersion");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfPlotCodeSnapshot = CursorUtil.getColumnIndexOrThrow(_cursor, "plotCodeSnapshot");
          final int _cursorIndexOfUserNameSnapshot = CursorUtil.getColumnIndexOrThrow(_cursor, "userNameSnapshot");
          final InspectionEntity _result;
          if (_cursor.moveToFirst()) {
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
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final String _tmpClassCode;
            _tmpClassCode = _cursor.getString(_cursorIndexOfClassCode);
            final String _tmpClassLabel;
            _tmpClassLabel = _cursor.getString(_cursorIndexOfClassLabel);
            final String _tmpModelVersion;
            _tmpModelVersion = _cursor.getString(_cursorIndexOfModelVersion);
            final String _tmpErrorMessage;
            _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            final String _tmpPlotCodeSnapshot;
            _tmpPlotCodeSnapshot = _cursor.getString(_cursorIndexOfPlotCodeSnapshot);
            final String _tmpUserNameSnapshot;
            _tmpUserNameSnapshot = _cursor.getString(_cursorIndexOfUserNameSnapshot);
            _result = new InspectionEntity(_tmpId,_tmpPlotId,_tmpSubPlot,_tmpImagePath,_tmpDiagnosisClass,_tmpConfidence,_tmpLatencyMs,_tmpSeverityLevel,_tmpRecommendationsJson,_tmpSyncStatus,_tmpCreatedAt,_tmpUserId,_tmpState,_tmpClassCode,_tmpClassLabel,_tmpModelVersion,_tmpErrorMessage,_tmpPlotCodeSnapshot,_tmpUserNameSnapshot);
          } else {
            _result = null;
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
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfClassCode = CursorUtil.getColumnIndexOrThrow(_cursor, "classCode");
          final int _cursorIndexOfClassLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "classLabel");
          final int _cursorIndexOfModelVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "modelVersion");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfPlotCodeSnapshot = CursorUtil.getColumnIndexOrThrow(_cursor, "plotCodeSnapshot");
          final int _cursorIndexOfUserNameSnapshot = CursorUtil.getColumnIndexOrThrow(_cursor, "userNameSnapshot");
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
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final String _tmpClassCode;
            _tmpClassCode = _cursor.getString(_cursorIndexOfClassCode);
            final String _tmpClassLabel;
            _tmpClassLabel = _cursor.getString(_cursorIndexOfClassLabel);
            final String _tmpModelVersion;
            _tmpModelVersion = _cursor.getString(_cursorIndexOfModelVersion);
            final String _tmpErrorMessage;
            _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            final String _tmpPlotCodeSnapshot;
            _tmpPlotCodeSnapshot = _cursor.getString(_cursorIndexOfPlotCodeSnapshot);
            final String _tmpUserNameSnapshot;
            _tmpUserNameSnapshot = _cursor.getString(_cursorIndexOfUserNameSnapshot);
            _item = new InspectionEntity(_tmpId,_tmpPlotId,_tmpSubPlot,_tmpImagePath,_tmpDiagnosisClass,_tmpConfidence,_tmpLatencyMs,_tmpSeverityLevel,_tmpRecommendationsJson,_tmpSyncStatus,_tmpCreatedAt,_tmpUserId,_tmpState,_tmpClassCode,_tmpClassLabel,_tmpModelVersion,_tmpErrorMessage,_tmpPlotCodeSnapshot,_tmpUserNameSnapshot);
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
    final String _sql = "SELECT * FROM inspections WHERE syncStatus = 0 ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, true, _cancellationSignal, new Callable<List<InspectionEntity>>() {
      @Override
      @NonNull
      public List<InspectionEntity> call() throws Exception {
        __db.beginTransaction();
        try {
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
            final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
            final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
            final int _cursorIndexOfClassCode = CursorUtil.getColumnIndexOrThrow(_cursor, "classCode");
            final int _cursorIndexOfClassLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "classLabel");
            final int _cursorIndexOfModelVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "modelVersion");
            final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
            final int _cursorIndexOfPlotCodeSnapshot = CursorUtil.getColumnIndexOrThrow(_cursor, "plotCodeSnapshot");
            final int _cursorIndexOfUserNameSnapshot = CursorUtil.getColumnIndexOrThrow(_cursor, "userNameSnapshot");
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
              final String _tmpUserId;
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
              final String _tmpState;
              _tmpState = _cursor.getString(_cursorIndexOfState);
              final String _tmpClassCode;
              _tmpClassCode = _cursor.getString(_cursorIndexOfClassCode);
              final String _tmpClassLabel;
              _tmpClassLabel = _cursor.getString(_cursorIndexOfClassLabel);
              final String _tmpModelVersion;
              _tmpModelVersion = _cursor.getString(_cursorIndexOfModelVersion);
              final String _tmpErrorMessage;
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
              final String _tmpPlotCodeSnapshot;
              _tmpPlotCodeSnapshot = _cursor.getString(_cursorIndexOfPlotCodeSnapshot);
              final String _tmpUserNameSnapshot;
              _tmpUserNameSnapshot = _cursor.getString(_cursorIndexOfUserNameSnapshot);
              _item = new InspectionEntity(_tmpId,_tmpPlotId,_tmpSubPlot,_tmpImagePath,_tmpDiagnosisClass,_tmpConfidence,_tmpLatencyMs,_tmpSeverityLevel,_tmpRecommendationsJson,_tmpSyncStatus,_tmpCreatedAt,_tmpUserId,_tmpState,_tmpClassCode,_tmpClassLabel,_tmpModelVersion,_tmpErrorMessage,_tmpPlotCodeSnapshot,_tmpUserNameSnapshot);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
            _statement.release();
          }
        } finally {
          __db.endTransaction();
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
