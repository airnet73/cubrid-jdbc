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
import java.io.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Calendar;
import java.net.URL;

import cubrid.jdbc.driver.CUBRIDBlob;
import cubrid.jdbc.driver.CUBRIDClob;
import cubrid.jdbc.driver.CUBRIDException;
import cubrid.jdbc.driver.CUBRIDJDBCErrorCode;

import cubrid.jdbc.jci.*;
import cubrid.sql.CUBRIDOID;

public class CUBRIDCallableStatement extends CUBRIDPreparedStatement implements
    CallableStatement
{
  private boolean was_null;

  protected CUBRIDCallableStatement(CUBRIDConnection c, UStatement us)
  {
    super(c, us, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
        Statement.NO_GENERATED_KEYS);
  }

  /*
   * java.sql.CallableStatement interface
   */

  public boolean wasNull() throws SQLException
  {
    checkIsOpen();
    return was_null;
  }

  public int getInt(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    int value;
    synchronized (u_stmt)
    {
      value = u_stmt.getInt(index);
      error = u_stmt.getRecentError();
    }

    checkGetXXXError();
    return value;
  }

  public String getString(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    String value;
    synchronized (u_stmt)
    {
      value = u_stmt.getString(index);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();
    return value;
  }

  public boolean getBoolean(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    boolean value;
    synchronized (u_stmt)
    {
      value = u_stmt.getBoolean(index);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();
    return value;
  }

  public byte getByte(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    byte value;
    synchronized (u_stmt)
    {
      value = u_stmt.getByte(index);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();
    return value;
  }

  public short getShort(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    short value;
    synchronized (u_stmt)
    {
      value = u_stmt.getShort(index);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();
    return value;
  }

  public long getLong(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    long value;
    synchronized (u_stmt)
    {
      value = u_stmt.getLong(index);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();
    return value;
  }

  public float getFloat(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    float value;
    synchronized (u_stmt)
    {
      value = u_stmt.getFloat(index);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();
    return value;
  }

  public double getDouble(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    double value;
    synchronized (u_stmt)
    {
      value = u_stmt.getDouble(index);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();
    return value;
  }

  public byte[] getBytes(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    byte[] value;
    synchronized (u_stmt)
    {
      value = u_stmt.getBytes(index);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();
    return value;
  }

  public Date getDate(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    Date value;
    synchronized (u_stmt)
    {
      value = u_stmt.getDate(index);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();
    return value;
  }

  public Time getTime(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    Time value;
    synchronized (u_stmt)
    {
      value = u_stmt.getTime(index);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();
    return value;
  }

  public Timestamp getTimestamp(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    Timestamp value;
    synchronized (u_stmt)
    {
      value = u_stmt.getTimestamp(index);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();
    return value;
  }

  public Object getObject(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    Object value;
    synchronized (u_stmt)
    {
      value = u_stmt.getObject(index);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();
    return value;
  }

  public BigDecimal getBigDecimal(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    BigDecimal value;
    synchronized (u_stmt)
    {
      value = u_stmt.getBigDecimal(index);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();
    return value;
  }

  public BigDecimal getBigDecimal(int index, int scale) throws SQLException
  {
    throw new java.lang.UnsupportedOperationException();
  }

  public Object getObject(int i, Map map) throws SQLException
  {
    throw new java.lang.UnsupportedOperationException();
  }

  public Ref getRef(int i) throws SQLException
  {
    throw new java.lang.UnsupportedOperationException();
  }

  public Blob getBlob(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    CUBRIDOID oid;
    synchronized (u_stmt)
    {
      oid = u_stmt.getGloOID(index - 1);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();

    if (oid == null)
      return null;
    return (new CUBRIDBlob(oid));
  }

  public Clob getClob(int index) throws SQLException
  {
    checkIsOpen();
    beforeGetValue(index);

    CUBRIDOID oid;
    synchronized (u_stmt)
    {
      oid = u_stmt.getGloOID(index - 1);
      error = u_stmt.getRecentError();
    }
    checkGetXXXError();
    if (oid == null)
      return null;
    return (new CUBRIDClob(oid, con.getUConnection().getCharset()));
  }

  public Array getArray(int i) throws SQLException
  {
    throw new java.lang.UnsupportedOperationException();
  }

  public Date getDate(int index, Calendar cal) throws SQLException
  {
    return (getDate(index));
  }

  public Time getTime(int index, Calendar cal) throws SQLException
  {
    return (getTime(index));
  }

  public Timestamp getTimestamp(int index, Calendar cal) throws SQLException
  {
    return (getTimestamp(index));
  }

  public URL getURL(int index) throws SQLException
  {
    throw new java.lang.UnsupportedOperationException();
  }

  public void setURL(String pName, URL val) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setNull(String pName, int sqlType) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setBoolean(String pName, boolean x) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setByte(String pName, byte x) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setShort(String pName, short x) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setInt(String pName, int x) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setLong(String pName, long x) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setFloat(String pName, float x) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setDouble(String pName, double x) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setBigDecimal(String pName, BigDecimal x) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setString(String pName, String x) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setBytes(String pName, byte[] x) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setDate(String pName, Date x) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setTime(String pName, Time x) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setTimestamp(String pName, Timestamp x) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setAsciiStream(String pName, InputStream x, int length)
      throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setBinaryStream(String pName, InputStream x, int length)
      throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setObject(String pName, Object x, int targetSqlType, int scale)
      throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setObject(String pName, Object x, int targetSqlType)
      throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setObject(String pName, Object x) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setCharacterStream(String pName, Reader reader, int length)
      throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setDate(String pName, Date x, Calendar cal) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setTime(String pName, Time x, Calendar cal) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setTimestamp(String pName, Timestamp x, Calendar cal)
      throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setNull(String pName, int sqlType, String typeName)
      throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public String getString(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public boolean getBoolean(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public byte getByte(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public short getShort(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public int getInt(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public long getLong(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public float getFloat(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public double getDouble(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public byte[] getBytes(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Date getDate(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Time getTime(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Timestamp getTimestamp(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Object getObject(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public BigDecimal getBigDecimal(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Object getObject(String pName, Map map) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Ref getRef(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Blob getBlob(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Clob getClob(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Array getArray(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Date getDate(String pName, Calendar cal) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Time getTime(String pName, Calendar cal) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Timestamp getTimestamp(String pName, Calendar cal) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public URL getURL(String pName) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void registerOutParameter(int index, int sqlType) throws SQLException
  {
    registerOutParameter(index);
  }

  public void registerOutParameter(int index, int sqlType, int scale)
      throws SQLException
  {
    registerOutParameter(index);
  }

  public void registerOutParameter(int index, int sqlType, String typeName)
      throws SQLException
  {
    registerOutParameter(index);
  }

  public void registerOutParameter(String pName, int sqlType)
      throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void registerOutParameter(String pName, int sqlType, int scale)
      throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void registerOutParameter(String pName, int sqlType, String typeName)
      throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public int[] executeBatch() throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void addBatch() throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  /*
   * ======================================================================= |
   * PRIVATE METHODS
   * =======================================================================
   */

  private void registerOutParameter(int index) throws SQLException
  {
    checkIsOpen();
    synchronized (u_stmt)
    {
      u_stmt.registerOutParameter(index - 1);
      error = u_stmt.getRecentError();
    }
    checkBindError();
  }

  private void beforeGetValue(int index) throws SQLException
  {
    if (index < 0 || index > u_stmt.getParameterCount())
    {
      throw new CUBRIDException(CUBRIDJDBCErrorCode.invalid_index);
    }

    synchronized (u_stmt)
    {
      u_stmt.fetch();
      error = u_stmt.getRecentError();
    }

    switch (error.getErrorCode())
    {
    case UErrorCode.ER_NO_ERROR:
      break;
    default:
      throw new CUBRIDException(error);
    }
  }

  void checkGetXXXError() throws SQLException
  {
    switch (error.getErrorCode())
    {
    case UErrorCode.ER_NO_ERROR:
      was_null = false;
      break;
    case UErrorCode.ER_WAS_NULL:
      was_null = true;
      break;
    default:
      throw new CUBRIDException(error);
    }
  }

}
