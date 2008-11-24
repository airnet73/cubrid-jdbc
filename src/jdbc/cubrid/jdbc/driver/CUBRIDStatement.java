/*
 * Copyright (C) 2008 Search Solution Corporation. All rights reserved by Search Solution. 
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met: 
 *
 * - Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer. 
 *
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution. 
 *
 * - Neither the name of the <ORGANIZATION> nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software without 
 *   specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE. 
 *
 */

package cubrid.jdbc.driver;

import java.sql.*;
import java.util.*;

import cubrid.jdbc.driver.CUBRIDException;
import cubrid.jdbc.driver.CUBRIDJDBCErrorCode;
import cubrid.jdbc.driver.CUBRIDResultSet; //import cubrid.sql.CUBRIDOID;
import cubrid.jdbc.jci.*;
import cubrid.sql.*;

/**
 * Title: CUBRID JDBC Driver Description:
 * 
 * @version 2.0
 */

public class CUBRIDStatement implements Statement
{
  protected CUBRIDConnection con;
  protected UConnection u_con;
  protected UStatement u_stmt;
  protected UStatement auto_generatedkeys_stmt;
  protected UError error;
  protected boolean completed;
  protected UResultInfo[] result_info;
  protected CUBRIDResultSet current_result_set;
  protected CUBRIDResultSet auto_generatedkeys_result_set;
  protected boolean is_closed;
  protected int update_count;
  protected boolean query_info_flag;
  protected boolean only_query_plan;

  private int max_field_size;
  private int max_rows;
  private int query_timeout;
  private int type;
  private int concurrency;
  private boolean is_scrollable;
  private boolean is_updatable;
  private boolean is_sensitive;
  private int fetch_direction;
  private int fetch_size;
  private ArrayList batchs;
  private int result_index;

  protected CUBRIDStatement(CUBRIDConnection c, int t, int concur)
  {
    con = c;
    u_con = con.u_con;
    u_stmt = null;
    is_closed = false;
    max_field_size = 0;
    max_rows = 0;
    update_count = -1;
    current_result_set = null;
    auto_generatedkeys_result_set = null;
    auto_generatedkeys_stmt = null;
    query_timeout = 0;
    error = null;
    type = t;
    concurrency = concur;
    is_scrollable = t != ResultSet.TYPE_FORWARD_ONLY;
    is_updatable = concur == ResultSet.CONCUR_UPDATABLE;
    is_sensitive = t == ResultSet.TYPE_SCROLL_SENSITIVE;
    fetch_direction = ResultSet.FETCH_FORWARD;
    fetch_size = 0;
    batchs = new ArrayList();
    completed = true;
    result_info = null;
    query_info_flag = false;
    only_query_plan = false;
  }

  /*
   * java.sql.Statement interface
   */

  public ResultSet executeQuery(String sql) throws SQLException
  {
    try
    {
      synchronized (con)
      {
        synchronized (this)
        {
          checkIsOpen();
          if (!completed)
          {
            complete();
          }
          prepare(sql);

          if (!u_stmt.getSqlType())
          {
            u_stmt.close();
            u_stmt = null;
            throw new CUBRIDException(
                CUBRIDJDBCErrorCode.invalid_query_type_for_executeQuery);
          }

          executeCore(false);
          getMoreResults();
          current_result_set.complete_on_close = true;
          return current_result_set;
        }
      }
    }
    catch (NullPointerException e)
    {
      throw new CUBRIDException(CUBRIDJDBCErrorCode.statement_closed);
    }
  }

  public int executeUpdate(String sql) throws SQLException
  {
    int returnvalue;

    returnvalue = executeUpdate(sql, Statement.NO_GENERATED_KEYS);

    return returnvalue;
  }

  public void close() throws SQLException
  {
    try
    {
      synchronized (con)
      {
        synchronized (this)
        {
          if (is_closed)
          {
            return;
          }
          is_closed = true;
          complete();
          con.removeStatement(this);

          con = null;
          u_con = null;
          error = null;
        }
      }
    }
    catch (NullPointerException e)
    {
    }
  }

  public synchronized int getMaxFieldSize() throws SQLException
  {
    checkIsOpen();
    return max_field_size;
  }

  public synchronized void setMaxFieldSize(int max) throws SQLException
  {
    checkIsOpen();
    if (max < 0)
    {
      throw new IllegalArgumentException();
    }
    max_field_size = max;
  }

  public synchronized int getMaxRows() throws SQLException
  {
    checkIsOpen();
    return max_rows;
  }

  public synchronized void setMaxRows(int max) throws SQLException
  {
    checkIsOpen();
    if (max < 0)
    {
      throw new IllegalArgumentException();
    }
    max_rows = max;
  }

  public synchronized void setEscapeProcessing(boolean enable)
      throws SQLException
  {
    checkIsOpen();
  }

  public synchronized int getQueryTimeout() throws SQLException
  {
    checkIsOpen();
    return query_timeout;
  }

  public synchronized void setQueryTimeout(int seconds) throws SQLException
  {
    checkIsOpen();
    if (seconds < 0)
    {
      throw new IllegalArgumentException();
    }
    query_timeout = seconds;
  }

  public void cancel() throws SQLException
  {
    try
    {
      UError err = u_stmt.cancel();

      switch (err.getErrorCode())
      {
      case UErrorCode.ER_NO_ERROR:
      case UErrorCode.ER_IS_CLOSED:
        break;
      default:
        throw new CUBRIDException(err);
      }
    }
    catch (NullPointerException e)
    {
    }
  }

  public synchronized SQLWarning getWarnings() throws SQLException
  {
    checkIsOpen();
    return null;
  }

  public synchronized void clearWarnings() throws SQLException
  {
    checkIsOpen();
  }

  public synchronized void setCursorName(String name) throws SQLException
  {
    checkIsOpen();
  }

  public boolean execute(String sql) throws SQLException
  {
    boolean returnvalue;

    returnvalue = execute(sql, Statement.NO_GENERATED_KEYS);

    return returnvalue;
  }

  public synchronized ResultSet getResultSet() throws SQLException
  {
    checkIsOpen();
    return current_result_set;
  }

  public synchronized int getUpdateCount() throws SQLException
  {
    return update_count;
  }

  public boolean getMoreResults() throws SQLException
  {
    try
    {
      synchronized (con)
      {
        synchronized (this)
        {
          checkIsOpen();

          if (current_result_set != null)
          {
            current_result_set.close();
            current_result_set = null;
          }

          if (completed)
          {
            update_count = -1;
            return false;
          }

          if (result_index == result_info.length)
          {
            if (u_stmt.getCommandType() != CUBRIDCommandType.CUBRID_STMT_CALL_SP)
              complete();
            update_count = -1;
            return false;
          }

          if (result_index != 0)
          {
            u_stmt.nextResult();
            error = u_stmt.getRecentError();
            switch (error.getErrorCode())
            {
            case UErrorCode.ER_NO_ERROR:
              break;
            default:
              throw new CUBRIDException(error);
            }
          }

          boolean result_type = result_info[result_index].isResultSet();

          if (result_type)
          {
            int rs_type = type;
            int rs_concurrency = concurrency;
            if (type == ResultSet.TYPE_SCROLL_SENSITIVE
                && u_stmt.isOIDIncluded() == false)
              rs_type = ResultSet.TYPE_SCROLL_INSENSITIVE;
            if (concurrency == ResultSet.CONCUR_UPDATABLE
                && u_stmt.isOIDIncluded() == false)
              rs_concurrency = ResultSet.CONCUR_READ_ONLY;
            current_result_set = new CUBRIDResultSet(con, this, rs_type,
                rs_concurrency);
          }
          else
          {
            update_count = result_info[result_index].getResultCount();
          }

          result_index++;
          return result_type;
        }
      }
    }
    catch (NullPointerException e)
    {
      throw new CUBRIDException(CUBRIDJDBCErrorCode.statement_closed);
    }
  }

  public synchronized void setFetchDirection(int direction) throws SQLException
  {
    checkIsOpen();

    if (!is_scrollable)
      throw new CUBRIDException(CUBRIDJDBCErrorCode.non_scrollable_statement);

    switch (direction)
    {
    case ResultSet.FETCH_FORWARD:
    case ResultSet.FETCH_REVERSE:
    case ResultSet.FETCH_UNKNOWN:
      fetch_direction = direction;
      break;
    default:
      throw new IllegalArgumentException();
    }
  }

  public synchronized int getFetchDirection() throws SQLException
  {
    checkIsOpen();
    return fetch_direction;
  }

  public synchronized void setFetchSize(int rows) throws SQLException
  {
    checkIsOpen();
    if (rows <= 0)
    {
      throw new IllegalArgumentException();
    }
    fetch_size = rows;
  }

  public synchronized int getFetchSize() throws SQLException
  {
    checkIsOpen();
    return fetch_size;
  }

  public synchronized int getResultSetConcurrency() throws SQLException
  {
    checkIsOpen();
    return concurrency;
  }

  public synchronized int getResultSetType() throws SQLException
  {
    checkIsOpen();
    return type;
  }

  public synchronized void addBatch(String sql) throws SQLException
  {
    checkIsOpen();
    batchs.add(sql);
  }

  public synchronized void clearBatch() throws SQLException
  {
    checkIsOpen();
    batchs.clear();
  }

  public int[] executeBatch() throws SQLException
  {
    try
    {
      synchronized (con)
      {
        synchronized (this)
        {
          checkIsOpen();
          if (!completed)
          {
            complete();
          }
          UBatchResult batch_results;
          String[] batch_querys = new String[batchs.size()];
          for (int i = 0; i < batchs.size(); i++)
          {
            batch_querys[i] = (String) batchs.get(i);
          }
          batch_results = u_con.batchExecute(batch_querys);
          error = u_con.getRecentError();

          switch (error.getErrorCode())
          {
          case UErrorCode.ER_NO_ERROR:
            break;
          default:
            throw new CUBRIDException(error);
          }

          con.autoCommit();
          return (checkBatchResult(batch_results));
        }
      }
    }
    catch (NullPointerException e)
    {
      throw new CUBRIDException(CUBRIDJDBCErrorCode.statement_closed);
    }
  }

  public synchronized Connection getConnection() throws SQLException
  {
    checkIsOpen();
    return con;
  }

  // 3.0
  public synchronized boolean execute(String sql, int autoGeneratedKeys)
      throws SQLException
  {
    try
    {
      synchronized (con)
      {
        synchronized (this)
        {
          checkIsOpen();
          if (!completed)
          {
            complete();
          }
          prepare(sql);
          executeCore(true);
          if (autoGeneratedKeys == Statement.RETURN_GENERATED_KEYS
              && u_stmt.getCommandType() == CUBRIDCommandType.CUBRID_STMT_INSERT)
            MakeAutoGeneratedKeysResultSet();
          else
            resetGeneratedKeysResultSet();
          return getMoreResults();
        }
      }
    }
    catch (NullPointerException e)
    {
      throw new CUBRIDException(CUBRIDJDBCErrorCode.statement_closed);
    }
  }

  public synchronized boolean execute(String sql, int[] columnIndexes)
      throws SQLException
  {
    boolean returnvalue;
    // auto = Statement.RETURN_GENERATED_KEYS;

    returnvalue = execute(sql, Statement.NO_GENERATED_KEYS);
    // insertAutogenerated();

    return returnvalue;
  }

  public synchronized boolean execute(String sql, String[] columnNames)
      throws SQLException
  {
    boolean returnvalue;
    // auto = Statement.RETURN_GENERATED_KEYS;

    returnvalue = execute(sql, Statement.NO_GENERATED_KEYS);
    // insertAutogenerated();

    return returnvalue;
  }

  public synchronized int executeUpdate(String sql, int autoGeneratedKeys)
      throws SQLException
  {
    try
    {
      synchronized (con)
      {
        synchronized (this)
        {
          checkIsOpen();
          if (!completed)
          {
            complete();
          }
          prepare(sql);

          if (u_stmt.getSqlType())
          {
            u_stmt.close();
            u_stmt = null;
            throw new CUBRIDException(
                CUBRIDJDBCErrorCode.invalid_query_type_for_executeUpdate);
          }

          executeCore(false);
          if (autoGeneratedKeys == Statement.RETURN_GENERATED_KEYS
              && u_stmt.getCommandType() == CUBRIDCommandType.CUBRID_STMT_INSERT)
            MakeAutoGeneratedKeysResultSet();
          else
            resetGeneratedKeysResultSet();

          getMoreResults();
          complete();
          return update_count;
        }
      }
    }
    catch (NullPointerException e)
    {
      throw new CUBRIDException(CUBRIDJDBCErrorCode.statement_closed);
    }
  }

  public synchronized int executeUpdate(String sql, int[] columnIndexes)
      throws SQLException
  {
    int returnvalue;
    // auto = Statement.RETURN_GENERATED_KEYS;

    returnvalue = executeUpdate(sql, Statement.NO_GENERATED_KEYS);
    // insertAutogenerated();

    return returnvalue;
  }

  public synchronized int executeUpdate(String sql, String[] columnNames)
      throws SQLException
  {
    int returnvalue;
    // auto = Statement.RETURN_GENERATED_KEYS;

    returnvalue = executeUpdate(sql, Statement.NO_GENERATED_KEYS);
    // insertAutogenerated();

    return returnvalue;
  }

  public synchronized ResultSet getGeneratedKeys() throws SQLException
  {
    if (auto_generatedkeys_result_set == null)
    {
      auto_generatedkeys_result_set = new CUBRIDResultSet(null);
    }
    return auto_generatedkeys_result_set;
  }

  public synchronized boolean getMoreResults(int current) throws SQLException
  {
    /*
     * try { synchronized (con) { synchronized (this) { checkIsOpen();
     * 
     * if (current_result_set != null) { current_result_set.close();
     * current_result_set = null; }
     * 
     * if (completed) { update_count = -1; return false; }
     * 
     * if (result_index == result_info.length) { complete(); update_count = -1;
     * return false; }
     * 
     * if (result_index != 0) { u_stmt.nextResult(); error =
     * u_stmt.getRecentError(); switch (error.getErrorCode()) { case
     * UErrorCode.ER_NO_ERROR : break; default : throw new
     * CUBRIDException(error); } }
     * 
     * boolean result_type = result_info[result_index].isResultSet();
     * 
     * if (result_type) { int rs_type = type; int rs_concurrency = concurrency;
     * if (type == ResultSet.TYPE_SCROLL_SENSITIVE && u_stmt.isOIDIncluded() ==
     * false) rs_type = ResultSet.TYPE_SCROLL_INSENSITIVE; if (concurrency ==
     * ResultSet.CONCUR_UPDATABLE && u_stmt.isOIDIncluded() == false)
     * rs_concurrency = ResultSet.CONCUR_READ_ONLY; current_result_set = new
     * CUBRIDResultSet(con, this, rs_type, rs_concurrency); } else {
     * update_count = result_info[result_index].getResultCount(); }
     * 
     * result_index++; return result_type; } } } catch (NullPointerException e)
     * { throw new CUBRIDException(CUBRIDJDBCErrorCode.statement_closed); }
     */
    return false;
  }

  public synchronized int getResultSetHoldability() throws SQLException
  {
    return ResultSet.CLOSE_CURSORS_AT_COMMIT;
  }

  // 3.0

  /**
   * Executes an SQL <code>INSERT</code> statement and returns a
   * <code>CUBRIDOID</code> object that represents the OID of the object
   * inserted by the given query.
   * 
   * @param sql
   *          an SQL <code>INSERT</code> statement
   * @return a <code>CUBRIDOID</code> object that represents the OID of the
   *         object inserted by the given query.
   * @exception SQLException
   *              if <code>this</code> object is closed.
   * @exception IllegalArgumentException
   *              if <code>sql</code> is <code>null</code>.
   * @exception SQLException
   *              if <code>sql</code> is not an SQL <code>INSERT</code>
   *              statement.
   * @exception SQLException
   *              if a database access error occurs
   */

  public CUBRIDOID executeInsert(String sql) throws SQLException
  {
    try
    {
      synchronized (con)
      {
        synchronized (this)
        {
          checkIsOpen();
          if (!completed)
          {
            complete();
          }
          prepare(sql);
          CUBRIDOID oid = executeInsertCore();
          complete();
          return oid;
        }
      }
    }
    catch (NullPointerException e)
    {
      throw new CUBRIDException(CUBRIDJDBCErrorCode.statement_closed);
    }
  }

  public byte getStatementType()
  {
    if (u_stmt != null)
      return (u_stmt.getCommandType());
    else
      return CUBRIDCommandType.CUBRID_STMT_UNKNOWN;
  }

  public String getQueryplan(String sql) throws SQLException
  {
    checkIsOpen();
    String plan;

    if (sql == null)
      return "";

    plan = u_con.getQueryplanOnly(sql);

    error = u_con.getRecentError();
    switch (error.getErrorCode())
    {
    case UErrorCode.ER_NO_ERROR:
      break;
    default:
      throw new CUBRIDException(error);
    }

    if (plan == null)
      return "";

    return plan;
  }

  public String getQueryplan() throws SQLException
  {
    checkIsOpen();

    if (u_stmt == null)
      return "";

    String plan = u_stmt.getQueryplan();
    error = u_stmt.getRecentError();
    switch (error.getErrorCode())
    {
    case UErrorCode.ER_NO_ERROR:
      break;
    default:
      throw new CUBRIDException(error);
    }

    if (plan == null)
      return "";

    return plan;
  }

  public void setQueryInfo(boolean value)
  {
    query_info_flag = value;
  }

  public void setOnlyQueryPlan(boolean value)
  {
    only_query_plan = value;
  }

  protected CUBRIDOID executeInsertCore() throws SQLException
  {
    CUBRIDCancelQueryThread t = null;

    if (query_timeout > 0)
    {
      t = new CUBRIDCancelQueryThread(this, query_timeout);
      t.start();
    }

    CUBRIDOID oid = u_stmt.executeInsert(false);

    if (query_timeout > 0)
    {
      t.queryended();
    }

    error = u_stmt.getRecentError();

    switch (error.getErrorCode())
    {
    case UErrorCode.ER_NO_ERROR:
      break;
    case UErrorCode.ER_CMD_IS_NOT_INSERT:
      con.autoRollback();
      throw new CUBRIDException(
          CUBRIDJDBCErrorCode.invalid_query_type_for_executeInsert);
    default:
      UError cpErr = new UError(error);
      con.autoRollback();
      throw new CUBRIDException(cpErr);
    }

    completed = false;
    return oid;
  }

  protected int[] checkBatchResult(UBatchResult batch_results)
      throws SQLException
  {
    int[] result = batch_results.getResult();
    if (batch_results.getErrorFlag() == false)
      return result;
    int num_result = batch_results.getResultNumber();
    String[] ErrorMsg = batch_results.getErrorMessage();
    int[] errCode = batch_results.getErrorCode();
    BatchUpdateException bex = null;
    for (int i = 0; i < num_result; i++)
    {
      if (result[i] < 0)
      {
        if (bex == null)
          bex = new BatchUpdateException(ErrorMsg[i], null, errCode[i], result);
        else
          bex.setNextException(new SQLException(ErrorMsg[i], null, errCode[i]));
      }
    }
    throw bex;
  }

  protected void executeCore(boolean all) throws SQLException
  {
    if (u_stmt.is_result_cacheable())
    {
      jdbc_cache_make(all);
    }
    else
    {
      executeCoreInternal(all, null);
    }

    result_info = u_stmt.getResultInfo();
    result_index = 0;

    if (con.getAutoCommit())
    {
      if (result_info.length > 1
          || u_stmt.getCommandType() == CUBRIDCommandType.CUBRID_STMT_CALL_SP)
      {
        u_con.turnOnAutoCommitBySelf();
      }
    }

    completed = false;
  }

  protected void executeCoreInternal(boolean all, UStatementCacheData cache_data)
      throws SQLException
  {
    CUBRIDCancelQueryThread t = null;

    if (query_timeout > 0)
    {
      t = new CUBRIDCancelQueryThread(this, query_timeout);
      t.start();
    }

    u_stmt.execute(false, max_rows, max_field_size, all, is_sensitive,
        is_scrollable, query_info_flag, only_query_plan, cache_data);

    if (query_timeout > 0)
    {
      t.queryended();
    }

    error = u_stmt.getRecentError();
    checkExecuteError();
  }

  protected void jdbc_cache_make(boolean all) throws SQLException
  {
    UStatementCacheData cache_data = null;
    Thread this_thread = Thread.currentThread();

    UResCache res_cache = u_stmt.getResCache();

    cache_data = res_cache.getCacheData();

    executeCoreInternal(all, cache_data);
    res_cache.saveCacheData(cache_data);
  }

  void complete() throws SQLException
  {
    if (completed)
    {
      return;
    }
    completed = true;

    if (current_result_set != null)
    {
      current_result_set.close();
      current_result_set = null;
    }

    if (u_stmt != null)
    {
      u_stmt.close();
      if (!u_stmt.isReturnable())
      {
        u_stmt = null;
      }
    }

    result_info = null;
    con.autoCommit();
  }

  synchronized void resetGeneratedKeysResultSet()
  {
    try
    {
      if (auto_generatedkeys_result_set != null)
      {
        auto_generatedkeys_result_set.close();
        auto_generatedkeys_result_set = null;
      }
      if (auto_generatedkeys_stmt != null)
      {
        auto_generatedkeys_stmt.close(false);
        auto_generatedkeys_stmt = null;
      }
    }
    catch (Exception e)
    {
    }
    finally
    {
      auto_generatedkeys_result_set = null;
      auto_generatedkeys_stmt = null;
    }
  }

  synchronized boolean MakeAutoGeneratedKeysResultSet() throws SQLException
  {
    checkIsOpen();

    if (auto_generatedkeys_result_set != null)
    {
      auto_generatedkeys_result_set.close();
      auto_generatedkeys_result_set = null;
    }

    auto_generatedkeys_stmt = new UStatement(u_stmt);
    if (!auto_generatedkeys_stmt.getGeneratedKeys())
      return false;
    error = auto_generatedkeys_stmt.getRecentError();
    switch (error.getErrorCode())
    {
    case UErrorCode.ER_NO_ERROR:
      break;
    default:
      throw new CUBRIDException(error);

    }

    auto_generatedkeys_result_set = new CUBRIDResultSet(auto_generatedkeys_stmt);

    return true;
  }

  private void checkIsOpen() throws SQLException
  {
    if (is_closed)
    {
      throw new CUBRIDException(CUBRIDJDBCErrorCode.statement_closed);
    }
  }

  private void prepare(String sql) throws SQLException
  {
    byte prepareFlag;
    prepareFlag = (is_updatable || is_sensitive) ? UConnection.PREPARE_UPDATABLE
        : (byte) 0;
    u_stmt = con.prepare(sql, prepareFlag);
  }

  private void checkExecuteError() throws SQLException
  {
    switch (error.getErrorCode())
    {
    case UErrorCode.ER_NO_ERROR:
      break;
    default:
      UError cpErr = new UError(error);
      con.autoRollback();
      throw new CUBRIDException(cpErr);
    }
  }
}
