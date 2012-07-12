/*
 EcmDroid - Android Diagnostic Tool for Buell Motorcycles
 Copyright (C) 2012 by Michel Marti

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.ecmdroid;

import android.content.Context;
import android.util.Log;

public abstract class BitSetProvider {

	protected static final String TAG = "BitSetProvider";
	private static BitSetProvider bitsetProvider;

	public abstract BitSet getBitSet(String ecm, String name);

	public static synchronized BitSetProvider getInstance(Context ctx) {
		if (bitsetProvider == null) {
			try {
				long now = System.currentTimeMillis();
				bitsetProvider = new DatabaseBitSetProvider(ctx);
				Log.d(TAG, "BitSetParser took " + (System.currentTimeMillis() - now + "ms."));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return bitsetProvider;
	}
}