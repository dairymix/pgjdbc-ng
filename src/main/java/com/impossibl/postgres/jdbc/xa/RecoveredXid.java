/**
 * Copyright (c) 2014, impossibl.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of impossibl.com nor the names of its contributors may
 *    be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
/*-------------------------------------------------------------------------
 *
 * Copyright (c) 2004-2011, PostgreSQL Global Development Group
 *
 *
 *-------------------------------------------------------------------------
 */
package com.impossibl.postgres.jdbc.xa;

import java.util.Arrays;

import javax.transaction.xa.Xid;

class RecoveredXid implements Xid {
  private int formatId;
  private byte[] globalTransactionId;
  private byte[] branchQualifier;

  @Override
  public int getFormatId() {
    return formatId;
  }

  @Override
  public byte[] getGlobalTransactionId() {
    return globalTransactionId;
  }

  @Override
  public byte[] getBranchQualifier() {
    return branchQualifier;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) // optimization for the common case.
      return true;

    if (!(o instanceof Xid))
      return false;

    Xid other = (Xid) o;
    if (other.getFormatId() != formatId)
      return false;
    if (!Arrays.equals(globalTransactionId, other.getGlobalTransactionId()))
      return false;
    if (!Arrays.equals(branchQualifier, other.getBranchQualifier()))
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 37;

    hashCode += 7 * formatId;
    hashCode += 7 * Arrays.hashCode(globalTransactionId);
    hashCode += 7 * Arrays.hashCode(branchQualifier);

    return hashCode;
  }

  /**
   * This is for debugging purposes only
   */
  @Override
  public String toString() {
    return xidToString(this);
  }

  //--- Routines for converting xid to string and back.

  static String xidToString(Xid xid) {
    return xid.getFormatId() + "_"
      + Base64.encodeBytes(xid.getGlobalTransactionId(), Base64.DONT_BREAK_LINES) + "_"
      + Base64.encodeBytes(xid.getBranchQualifier(), Base64.DONT_BREAK_LINES);
  }

  /**
   * @param s
   * @return recovered xid, or null if s does not represent a
   *   valid xid encoded by the driver.
   */
  static Xid stringToXid(String s) {
    RecoveredXid xid = new RecoveredXid();

    int a = s.indexOf('_');
    int b = s.lastIndexOf('_');

    if (a == b) // this also catches the case a == b == -1.
      return null;

    try {
      xid.formatId = Integer.parseInt(s.substring(0, a));
      xid.globalTransactionId = Base64.decode(s.substring(a + 1, b));
      xid.branchQualifier = Base64.decode(s.substring(b + 1));

      if (xid.globalTransactionId == null || xid.branchQualifier == null)
        return null;
    }
    catch (Exception ex) {
      return null; // Doesn't seem to be an xid generated by this driver.
    }

    return xid;
  }
}