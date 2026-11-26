package pe.edu.upn.agrovisionlurin.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile InspectionDao _inspectionDao;

  private volatile AccountDao _accountDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `inspections` (`id` TEXT NOT NULL, `plotId` TEXT NOT NULL, `subPlot` TEXT NOT NULL, `imagePath` TEXT NOT NULL, `diagnosisClass` INTEGER NOT NULL, `confidence` REAL NOT NULL, `latencyMs` INTEGER NOT NULL, `severityLevel` TEXT NOT NULL, `recommendationsJson` TEXT NOT NULL, `syncStatus` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `userId` TEXT NOT NULL, `state` TEXT NOT NULL, `classCode` TEXT NOT NULL, `classLabel` TEXT NOT NULL, `modelVersion` TEXT NOT NULL, `errorMessage` TEXT NOT NULL, `plotCodeSnapshot` TEXT NOT NULL, `userNameSnapshot` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`plotId`) REFERENCES `plots`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_inspections_syncStatus` ON `inspections` (`syncStatus`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_inspections_createdAt` ON `inspections` (`createdAt`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_inspections_userId` ON `inspections` (`userId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_inspections_plotId` ON `inspections` (`plotId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `username` TEXT NOT NULL, `displayName` TEXT NOT NULL, `passwordHash` TEXT NOT NULL, `passwordSalt` TEXT NOT NULL, `role` TEXT NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_username` ON `users` (`username`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `plots` (`id` TEXT NOT NULL, `code` TEXT NOT NULL, `name` TEXT NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_plots_code` ON `plots` (`code`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'fccea131b51940f27cd5e2ac0424251f')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `inspections`");
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `plots`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsInspections = new HashMap<String, TableInfo.Column>(19);
        _columnsInspections.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("plotId", new TableInfo.Column("plotId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("subPlot", new TableInfo.Column("subPlot", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("imagePath", new TableInfo.Column("imagePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("diagnosisClass", new TableInfo.Column("diagnosisClass", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("confidence", new TableInfo.Column("confidence", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("latencyMs", new TableInfo.Column("latencyMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("severityLevel", new TableInfo.Column("severityLevel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("recommendationsJson", new TableInfo.Column("recommendationsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("syncStatus", new TableInfo.Column("syncStatus", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("state", new TableInfo.Column("state", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("classCode", new TableInfo.Column("classCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("classLabel", new TableInfo.Column("classLabel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("modelVersion", new TableInfo.Column("modelVersion", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("errorMessage", new TableInfo.Column("errorMessage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("plotCodeSnapshot", new TableInfo.Column("plotCodeSnapshot", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInspections.put("userNameSnapshot", new TableInfo.Column("userNameSnapshot", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInspections = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysInspections.add(new TableInfo.ForeignKey("users", "RESTRICT", "NO ACTION", Arrays.asList("userId"), Arrays.asList("id")));
        _foreignKeysInspections.add(new TableInfo.ForeignKey("plots", "RESTRICT", "NO ACTION", Arrays.asList("plotId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesInspections = new HashSet<TableInfo.Index>(4);
        _indicesInspections.add(new TableInfo.Index("index_inspections_syncStatus", false, Arrays.asList("syncStatus"), Arrays.asList("ASC")));
        _indicesInspections.add(new TableInfo.Index("index_inspections_createdAt", false, Arrays.asList("createdAt"), Arrays.asList("ASC")));
        _indicesInspections.add(new TableInfo.Index("index_inspections_userId", false, Arrays.asList("userId"), Arrays.asList("ASC")));
        _indicesInspections.add(new TableInfo.Index("index_inspections_plotId", false, Arrays.asList("plotId"), Arrays.asList("ASC")));
        final TableInfo _infoInspections = new TableInfo("inspections", _columnsInspections, _foreignKeysInspections, _indicesInspections);
        final TableInfo _existingInspections = TableInfo.read(db, "inspections");
        if (!_infoInspections.equals(_existingInspections)) {
          return new RoomOpenHelper.ValidationResult(false, "inspections(pe.edu.upn.agrovisionlurin.data.local.InspectionEntity).\n"
                  + " Expected:\n" + _infoInspections + "\n"
                  + " Found:\n" + _existingInspections);
        }
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(7);
        _columnsUsers.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("username", new TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("passwordHash", new TableInfo.Column("passwordHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("passwordSalt", new TableInfo.Column("passwordSalt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("role", new TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("active", new TableInfo.Column("active", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(1);
        _indicesUsers.add(new TableInfo.Index("index_users_username", true, Arrays.asList("username"), Arrays.asList("ASC")));
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(db, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(pe.edu.upn.agrovisionlurin.data.local.LocalUser).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsPlots = new HashMap<String, TableInfo.Column>(4);
        _columnsPlots.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlots.put("code", new TableInfo.Column("code", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlots.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlots.put("active", new TableInfo.Column("active", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlots = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlots = new HashSet<TableInfo.Index>(1);
        _indicesPlots.add(new TableInfo.Index("index_plots_code", true, Arrays.asList("code"), Arrays.asList("ASC")));
        final TableInfo _infoPlots = new TableInfo("plots", _columnsPlots, _foreignKeysPlots, _indicesPlots);
        final TableInfo _existingPlots = TableInfo.read(db, "plots");
        if (!_infoPlots.equals(_existingPlots)) {
          return new RoomOpenHelper.ValidationResult(false, "plots(pe.edu.upn.agrovisionlurin.data.local.LocalPlot).\n"
                  + " Expected:\n" + _infoPlots + "\n"
                  + " Found:\n" + _existingPlots);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "fccea131b51940f27cd5e2ac0424251f", "79569eca10e6edc18f1672ce6efcdc7b");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "inspections","users","plots");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `inspections`");
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `plots`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(InspectionDao.class, InspectionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AccountDao.class, AccountDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public InspectionDao inspectionDao() {
    if (_inspectionDao != null) {
      return _inspectionDao;
    } else {
      synchronized(this) {
        if(_inspectionDao == null) {
          _inspectionDao = new InspectionDao_Impl(this);
        }
        return _inspectionDao;
      }
    }
  }

  @Override
  public AccountDao accountDao() {
    if (_accountDao != null) {
      return _accountDao;
    } else {
      synchronized(this) {
        if(_accountDao == null) {
          _accountDao = new AccountDao_Impl(this);
        }
        return _accountDao;
      }
    }
  }
}
