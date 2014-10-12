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
 * Copyright (C) 2009-2013 The SCUBA team.
 *
 * $Id$
 */

package net.sf.scuba.smartcards;

import java.io.PrintStream;

import net.sf.scuba.util.Hex;

/**
 * A dummy card service to produce APDU traces instead of the actual communication
 * with CAD. So far can only serve APDUs that expect 9000 status words.
 * 
 * @author Wojciech Mostowski <woj@cs.ru.nl>
 *
 */
public class DummyAcceptingCardService extends CardService {

	private static final long serialVersionUID = 959248891375637853L;

	private transient PrintStream out = null;
    
    private boolean closed = false;
    
    public DummyAcceptingCardService(PrintStream out) {
        this.out = out;
    }
    
    public void close() {
        out.flush();
        out.close();
        closed = true;
    }

    public boolean isOpen() {
        return !closed;
    }

    public void open() throws CardServiceException {
    }

    public ResponseAPDU transmit(CommandAPDU apdu) throws CardServiceException {
    	
    	String c = Hex.bytesToHexString( apdu.getBytes());
        String r = "9000";
        out.println("==> "+c);
        out.println("<== "+r);
        ResponseAPDU response = new ResponseAPDU(Hex.hexStringToBytes(r));
        return response;
    }

    public byte[] getATR() {
    	return null; // FIXME
    }
}