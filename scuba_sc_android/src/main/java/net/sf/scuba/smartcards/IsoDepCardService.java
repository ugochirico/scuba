/*
 * This file is part of the SCUBA smart card framework.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * Inspired by the work of Max Guenther (max.math.guenther@googlemail.com) for 
 * aJMRTD (an Android client for JMRTD, released under the LGPL license).
 *
 * Copyright (C) 2009-2013 The SCUBA team.
 *
 * $Id: $
 */

package net.sf.scuba.smartcards;

import java.io.IOException;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;
import net.sf.scuba.smartcards.CommandAPDU;
import net.sf.scuba.smartcards.ResponseAPDU;
import android.annotation.TargetApi;
import android.nfc.tech.IsoDep;
import android.os.Build;

/**
 * Card service implementation for sending APDUs to a terminal using the
 * Android NFC (<code>android.nfc.tech</code>) classes available in Android
 * SDK 2.3.3 (API 10) and higher.
 * 
 * @author Pim Vullers (pim@cs.ru.nl)
 * 
 * @version $Revision: 214 $
 */
public class IsoDepCardService extends CardService {

  private static final long serialVersionUID = -8123218195642784731L;

  private IsoDep nfc;
  private int apduCount;

  /**
   * Constructs a new card service.
   * 
   * @param terminal the card terminal to connect to
   */
  public IsoDepCardService(IsoDep nfc) {
    this.nfc = nfc;
    apduCount = 0;
  }

  /**
   * Opens a session with the card.
   */
  public void open() throws CardServiceException {
    if (isOpen()) { return; }
    try {
      nfc.connect();
      if (!nfc.isConnected()) { 
        throw new CardServiceException("failed to connect");
      }
      state = SESSION_STARTED_STATE;
    } catch (IOException e) {
      e.printStackTrace();
      throw new CardServiceException(e.toString());
    }
  }

  /**
   * Whether there is an open session with the card.
   */
  public boolean isOpen() {
    if (nfc.isConnected()) {
      state = SESSION_STARTED_STATE;
      return true;
    } else {
      state = SESSION_STOPPED_STATE;
      return false;
    }
  }

  /**
   * Sends an APDU to the card.
   * 
   * @param ourCommandAPDU the command apdu to send
   * @return the response from the card, including the status word
   * @throws CardServiceException - if the card operation failed 
   */
  public ResponseAPDU transmit(CommandAPDU ourCommandAPDU) throws CardServiceException {
    try {
      if (!nfc.isConnected()) {
        throw new CardServiceException("Not connected");
      }
      byte[] responseBytes = nfc.transceive(ourCommandAPDU.getBytes());
      if (responseBytes == null || responseBytes.length < 2) {
        throw new CardServiceException("Failed response");
      }
      ResponseAPDU ourResponseAPDU = new ResponseAPDU(responseBytes);
      notifyExchangedAPDU(++apduCount, ourCommandAPDU, ourResponseAPDU);
      return ourResponseAPDU;
    } catch (IOException e) {
      throw new CardServiceException(e.getMessage());
    } catch (Exception e) {
      throw new CardServiceException(e.getMessage());
    }
  }

  public byte[] getATR() {
    return null; // FIXME
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public boolean isExtendedAPDULengthSupported() {
    if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
      return nfc.isExtendedLengthApduSupported();
    }
    int maxTranceiveLength = nfc.getMaxTransceiveLength();
    if (maxTranceiveLength > 261) {
      return true;
    }
    return false;
  }

  /**
   * Closes the session with the card.
   */
  public void close() {
    try {
      nfc.close();
      state = SESSION_STOPPED_STATE;
    } catch (IOException e) {
      /* Disconnect failed? Fine... */
    }
  }
}
